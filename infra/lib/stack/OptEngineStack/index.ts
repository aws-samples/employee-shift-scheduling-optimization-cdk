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
    Stack, 
    StackProps,
    Environment,
    aws_iam as iam,
    aws_dynamodb as ddb,
    aws_s3 as s3,
    aws_ec2 as ec2,
    aws_elasticloadbalancingv2 as loadBalancer,
    aws_ecs as ecs,
    aws_ecr as ecr,
    aws_autoscaling as asc,
    aws_ecs_patterns as ecsPatterns,
    aws_logs as logs,
} from 'aws-cdk-lib'
import * as CommonUtil from '../../util/common'
import { PolicyStatements } from '../../util/policy-util'
import { CiCdPipelineConstruct } from '../../construct/cicd-pipeline'
  
export interface OptEngineStackProps extends StackProps {
    readonly env: Environment
    readonly namespace: string
    readonly parameterStoreKeys: Record<string, string>
    readonly optEngine: Record<string, string>
    readonly codeCommit: Record<string, string>    
}
  
export class OptEngineStack extends Stack {
    private readonly commonVpc: ec2.IVpc
    private readonly solverJobs: ddb.ITable
    private readonly solutionBucket: s3.IBucket
    
    public readonly ecrRepo: ecr.IRepository;
    public readonly ecsService: ecs.BaseService;
    public readonly alb: loadBalancer.ApplicationLoadBalancer;

    constructor(scope: Construct, id: string, props: OptEngineStackProps) {
        super(scope, id, props)
        
        const { env, namespace, parameterStoreKeys } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;
        
        //-------------------------------------------------------
        // Vpc
        //-------------------------------------------------------
        const commonVpcId = CommonUtil.getParameterFromLookup(this, parameterStoreKeys.commonVpcId)
        this.commonVpc = ec2.Vpc.fromLookup(this, 'CommonVpc', { vpcId: commonVpcId })

        //-------------------------------------------------------
        // Storage
        //-------------------------------------------------------
        const solverJobTableName = CommonUtil.getParameter(this, parameterStoreKeys.ddbSolutionState);
        this.solverJobs = ddb.Table.fromTableName(this, `${id}SolverJobTable`, solverJobTableName);

        const solutionBuckerName = CommonUtil.getParameter(this, parameterStoreKeys.s3SolutionBucket);
        this.solutionBucket = s3.Bucket.fromBucketName(this, `${id}SolutionBucket`, solutionBuckerName);
        
        //-------------------------------------------------------
        // Optimization Engine
        //-------------------------------------------------------

        // ECR repositiry
        const repositoryName = CommonUtil.getParameter(this, parameterStoreKeys.ecrOptEngineRepo);
        this.ecrRepo = ecr.Repository.fromRepositoryName(this, `${id}EcrOptEngine`, repositoryName);
        
        // ECS cluster
        const cluster = new ecs.Cluster(this, `${id}Cluster`, { 
            clusterName: namespaced(`${id}Cluster`),
            vpc: this.commonVpc,
            containerInsights: true,
            capacity: {
                instanceType: new ec2.InstanceType('c6i.2xlarge'),
                machineImage: ecs.EcsOptimizedImage.amazonLinux2(),
                desiredCapacity: 1,
                minCapacity: 0,
                maxCapacity: 3,
                blockDevices: [{
                    deviceName: '/dev/xvda',
                    volume: asc.BlockDeviceVolume.ebs(50, { encrypted: true }),
                }]
            }
        });

        // Task Role
        const optEngineTaskRole = new iam.Role(this, `${id}TaskRole`, {
            roleName: namespaced(`${id}TaskRole`),
            assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
            description: `Role for ${id} ECS Task`,
            inlinePolicies: {
                parameterStoreAccess: new iam.PolicyDocument({
                    statements: [
                        PolicyStatements.ssm.readSSMParams(this.region, this.account)
                    ],
                }),            
                solveResult: new iam.PolicyDocument({
                    statements: [
                        PolicyStatements.ddb.readDDBTable(this.solverJobs.tableArn),
                        PolicyStatements.ddb.updateDDBTable(this.solverJobs.tableArn),
                        PolicyStatements.ddb.batchWriteDDBTable(this.solverJobs.tableArn),
                        PolicyStatements.s3.readWriteBucket(this.solutionBucket.bucketArn)
                    ],
                }), 
            }
        });

        // ECS config
        const albEc2Service = new ecsPatterns.ApplicationLoadBalancedEc2Service(this, `${id}AlbEc2Service`, {
            cluster : cluster,
            publicLoadBalancer: false,
            memoryReservationMiB: 7000,
            taskImageOptions: {
                containerName: parameterStoreKeys.optEngineContainerName,
                image: ecs.ContainerImage.fromEcrRepository(this.ecrRepo),                
                taskRole: optEngineTaskRole,
                enableLogging: true,
                logDriver: ecs.LogDriver.awsLogs({
                    streamPrefix: id,
                    logRetention: logs.RetentionDays.ONE_DAY,
                }),
            }
        });
        
        this.ecsService = albEc2Service.service;
        this.alb = albEc2Service.loadBalancer;

        CommonUtil.putParameter(this, parameterStoreKeys.optEngineAlbDnsName, this.alb.loadBalancerDnsName);
        CommonUtil.putParameter(this, parameterStoreKeys.optEngineServiceArn, this.ecsService .serviceArn);

        //-------------------------------------------------------
        // CI/CD Pipeline
        //-------------------------------------------------------
        new CiCdPipelineConstruct(this, 'OptEngineCiCdPipeline', {
            ecrRepo: this.ecrRepo,
            ecsService: this.ecsService,
            appPath: 'opt_engine',
            platform: 'linux/amd64',
            preDockerBuildCmd: [
                './build_schedule_optimization_app.sh',
                'cd dist/schedule-optimization-app/'
            ],
            postDockerBuildCmd: [
                'cd ../../'
            ],
            ...props
        });
    }
}
