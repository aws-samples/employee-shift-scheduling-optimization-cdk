/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import { Construct } from 'constructs'
import { 
    StackProps,
    Environment,
    Duration,
    aws_iam as iam,
    aws_ecs as ecs,
    aws_ecr as ecr,    
    aws_codecommit as codecommit,
    aws_codebuild as codebuild,
    aws_codepipeline as codepipeline,
    aws_codepipeline_actions as actions,
    aws_cloudformation as cf
} from 'aws-cdk-lib'

export interface CiCdPipelineConstructProps extends StackProps {
    // values from config
    readonly env: Environment
    readonly namespace: string
    readonly codeCommit: Record<string, string>
    readonly enableKeyRotation?: boolean
    readonly parameterStoreKeys: Record<string, string>
    
    // values from other stack
    readonly ecrRepo: ecr.IRepository
    readonly ecsService: ecs.BaseService
    readonly appPath: string
    readonly platform: string,
    readonly preDockerBuildCmd: string[]
    readonly postDockerBuildCmd: string[]
}

interface DockerBuildProps{
    readonly ecrRepo: ecr.IRepository
    readonly appPath: string
    readonly containerName: string
    readonly platform: string,
    readonly preDockerBuildCmd: string[]
    readonly postDockerBuildCmd: string[]
}
   
export class CiCdPipelineConstruct extends Construct {

    constructor(scope: Construct, id: string, props: CiCdPipelineConstructProps) {
        super(scope, id)

        const { 
            env, namespace, codeCommit, parameterStoreKeys, 
            ecrRepo, ecsService, appPath, platform, preDockerBuildCmd, postDockerBuildCmd
        } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;

        // 1. Code Commit
        const codeRepo = codecommit.Repository.fromRepositoryName(this, `${id}CodeRepo`, codeCommit.repositoryName)
        const sourceOutput = new codepipeline.Artifact();
        const sourceAction = new actions.CodeCommitSourceAction({
            actionName: 'CodeCommit_SourceMerge',
            repository: codeRepo,
            output: sourceOutput,
            branch: codeCommit.deployBranchName
        })

        // 2. Code Build
        const buildProps: DockerBuildProps = {
            ecrRepo,
            appPath,
            containerName: parameterStoreKeys.optEngineContainerName,
            platform,
            preDockerBuildCmd,
            postDockerBuildCmd
        };
        const buildOutput = new codepipeline.Artifact();
        const buildAction = new actions.CodeBuildAction({
            actionName: 'CodeBuild_DockerBuild',
            project: this.createBuildProject(buildProps),
            input: sourceOutput,
            outputs: [buildOutput],
        });

        // 3. Manual Approval
        const approvalAction = new actions.ManualApprovalAction({
            actionName: 'Manual_Approve',
        });

        // 4. Code Deploy
        const deployAction = new actions.EcsDeployAction({
            actionName: 'ECS_ContainerDeploy',
            service: ecsService,
            imageFile: new codepipeline.ArtifactPath(buildOutput, ( buildProps.appPath ? `${buildProps.appPath}/imagedefinitions.json` : 'imagedefinitions.json')),
            deploymentTimeout: Duration.minutes(60)
        });

        // #. Build Pipeline
        new codepipeline.Pipeline(this, 'ECSServicePipeline', {
            pipelineName: `${props.stackName}-Pipeline`,
            enableKeyRotation: props.enableKeyRotation ? props.enableKeyRotation : true,
            stages: [
                {
                    stageName: 'Source',
                    actions: [sourceAction],
                },
                {
                    stageName: 'Build',
                    actions: [buildAction],
                },
                {
                    stageName: 'Approve',
                    actions: [approvalAction],
                },
                {
                    stageName: 'Deploy',
                    actions: [deployAction],
                }
            ]
        });
    }

    private createBuildProject(buildProps: DockerBuildProps): codebuild.Project {
        const buildCommandsInit = [
            'echo "In Build Phase"',
            'cd $APP_PATH',
            'ls -l',
        ];
        const buildCommandsDocker = [
            '$(aws ecr get-login --no-include-email)',
            `docker build${buildProps.platform?(' --platform '+buildProps.platform):''} -t $ECR_REPO_URI:$TAG .`,
            'docker push $ECR_REPO_URI:$TAG',
        ];

        const appPath = buildProps.appPath ? `${buildProps.appPath}` : '.';
        const project = new codebuild.Project(this, 'DockerBuild', {
            projectName: `OptEngineDockerBuild`,
            environment: {
                buildImage: codebuild.LinuxBuildImage.AMAZON_LINUX_2_3,
                computeType: codebuild.ComputeType.SMALL,
                privileged: true
            },
            environmentVariables: {
                'ECR_REPO_URI': {
                    value: `${buildProps.ecrRepo.repositoryUri}`
                },
                'CONTAINER_NAME': {
                    value: `${buildProps.containerName}`
                },
                'APP_PATH': {
                    value: appPath
                }
            },
            buildSpec: codebuild.BuildSpec.fromObject({
                version: "0.2",
                phases: {
                    pre_build: {
                        commands: [
                            'echo "In Pre-Build Phase"',
                            'export TAG=latest',
                            'echo $TAG'
                        ]
                    },
                    build: {
                        commands: [
                            ...buildCommandsInit,
                            ...(buildProps.preDockerBuildCmd ? buildProps.preDockerBuildCmd : []),
                            ...buildCommandsDocker,
                            ...(buildProps.postDockerBuildCmd ? buildProps.postDockerBuildCmd : []),
                        ]
                    },
                    post_build: {
                        commands: [
                            'echo "In Post-Build Phase"',
                            'pwd',
                            "printf '[{\"name\":\"%s\",\"imageUri\":\"%s\"}]' $CONTAINER_NAME $ECR_REPO_URI:$TAG > imagedefinitions.json",
                            "pwd; ls -al",
                            "cat imagedefinitions.json"
                        ]
                    }
                },
                artifacts: {
                    files: [
                        `${appPath}/imagedefinitions.json`
                    ]
                }
            }),
        });

        buildProps.ecrRepo.grantPullPush(project.role!);
        this.appendEcrReadPolicy('build-policy', project.role!);

        return project;
    }

    private appendEcrReadPolicy(baseName: string, role: iam.IRole) {
        const statement = new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            resources: ['*'],
            actions: [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage"
            ]
        });

        const policy = new iam.Policy(this, baseName);
        policy.addStatements(statement);

        role.attachInlinePolicy(policy);
    }
}