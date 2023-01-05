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
    aws_ec2 as ec2,
    Environment
} from 'aws-cdk-lib'
import * as CommonUtil from '../../util/common'

export interface BastionHostStackProps extends StackProps {
    readonly env: Environment
    readonly namespace: string
    readonly parameterStoreKeys: Record<string, string>
}
   
export class BastionHostStack extends Stack {

    constructor(scope: Construct, id: string, props: BastionHostStackProps) {
        super(scope, id, props)

        const { env, namespace, parameterStoreKeys } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;

        //-------------------------------------------------------
        // Vpc
        //-------------------------------------------------------
        const commonVpcId = CommonUtil.getParameterFromLookup(this, parameterStoreKeys.commonVpcId)
        const commonVpc = ec2.Vpc.fromLookup(this, 'CommonVpc', { vpcId: commonVpcId })

        //-------------------------------------------------------
        // Bastion Host
        //-------------------------------------------------------
        const host = new ec2.BastionHostLinux(this, 'BastionHost', {
            vpc: commonVpc,
            subnetSelection: { 
                subnetType: ec2.SubnetType.PUBLIC 
            },
            blockDevices: [{
              deviceName: '/dev/xvda',
              volume: ec2.BlockDeviceVolume.ebs(10, {
                encrypted: true,
              }),
            }],
        });
    }
}