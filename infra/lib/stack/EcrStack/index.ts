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
    aws_ecr as ecr,
    RemovalPolicy,
    CfnOutput,
} from 'aws-cdk-lib'
import * as CommonUtil from '../../util/common'

export interface EcrStackProps extends StackProps {
    readonly env: Environment
    readonly namespace: string
    readonly parameterStoreKeys: Record<string, string>
}
   
export class EcrStack extends Stack {

    private readonly ecrRepo: ecr.Repository;

    constructor(scope: Construct, id: string, props: EcrStackProps) {
        super(scope, id, props)

        const { env, namespace, parameterStoreKeys } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;

        //-------------------------------------------------------
        // Optimization Engine
        //-------------------------------------------------------
        // ECR repositiry
        this.ecrRepo = new ecr.Repository(this, `${id}EcrRepository`, {
            repositoryName: namespaced('opt-engine-repo').toLowerCase(),

            // for dev mode only
            removalPolicy: RemovalPolicy.DESTROY,
        });
        CommonUtil.putParameter(this, parameterStoreKeys.ecrOptEngineRepo, this.ecrRepo.repositoryName);
        new CfnOutput(this, 'EcrRepository', { exportName: 'EcrRepository', value: this.ecrRepo.repositoryName });
    }
}