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
    Tags, 
    aws_ec2 as ec2, 
    RemovalPolicy,
    Environment
} from 'aws-cdk-lib'
import * as CommonUtil from '../../util/common'
 
 export interface VpcStackProps extends StackProps {
    readonly env: Environment
    readonly namespace: string
    readonly vpcConfig: Record<string, string>
    readonly parameterStoreKeys: Record<string, string>
 }
 
 export class VpcStack extends Stack {
    readonly vpc: ec2.IVpc

    constructor(scope: Construct, id: string, props: VpcStackProps) {
        super(scope, id, props)
        
        const { env, vpcConfig: { vpcCidr, vpcName, hasNatGateway }, namespace, parameterStoreKeys } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;

        let natProps = {}
        if(hasNatGateway) {
            natProps = {
                natGateways: 1,
                natGatewayProvider: ec2.NatProvider.gateway()
            }
        } else {
            natProps = {
                natGateways: 0
            }
        }

        // create a VPC
        const vpc = new ec2.Vpc(this, 'Vpc', {
            ipAddresses: ec2.IpAddresses.cidr(vpcCidr),
            maxAzs: 3,
            subnetConfiguration: [
                {
                    subnetType: ec2.SubnetType.PUBLIC,
                    name: namespaced('public'),
                    cidrMask: 24,
                },
                {
                    subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
                    cidrMask: 24,
                    name: namespaced('private'),
                },
                {
                    subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
                    cidrMask: 24,
                    name: namespaced('isolated'),
                }
            ],
            /*
            // enable flowlog for PROD
            flowLogs: {
                "VpcTrafficLog": {
                    trafficType: ec2.FlowLogTrafficType.REJECT,
                    maxAggregationInterval: ec2.FlowLogMaxAggregationInterval.ONE_MINUTE
                }
            }
            */
            ...natProps
        })

        // removal policy : destroy if dev mode only
        vpc.applyRemovalPolicy(RemovalPolicy.DESTROY);

        // vpc endpoint for internal traffic
        vpc.addGatewayEndpoint('VpcEndPointS3', { service: ec2.GatewayVpcEndpointAwsService.S3 })
        vpc.addGatewayEndpoint('VpcEndPointDDB', { service: ec2.GatewayVpcEndpointAwsService.DYNAMODB })
        vpc.addInterfaceEndpoint('SsmEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.SSM })
        
        // ECS VPC Endpoint 
        // see-also : https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/vpc-endpoints.html
        vpc.addInterfaceEndpoint('EcrApiEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.ECR })
        vpc.addInterfaceEndpoint('EcrDockerEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.ECR_DOCKER })
        vpc.addInterfaceEndpoint('EcsApiEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.ECS })
        vpc.addInterfaceEndpoint('EcsAgentEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.ECS_AGENT })
        vpc.addInterfaceEndpoint('EcsTelemetryEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.ECS_TELEMETRY })
        vpc.addInterfaceEndpoint('CodeDeployEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.CODEDEPLOY })
        vpc.addInterfaceEndpoint('CodeDeployCommandEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.CODEDEPLOY_COMMANDS_SECURE })
        vpc.addInterfaceEndpoint('CloudWatchLogsEndpoint', { service: ec2.InterfaceVpcEndpointAwsService.CLOUDWATCH_LOGS })

        Tags.of(vpc).add('Name', namespaced(vpcName))
        CommonUtil.putParameter(this, parameterStoreKeys.commonVpcId, vpc.vpcId)

        this.vpc = vpc
    }
 }
 