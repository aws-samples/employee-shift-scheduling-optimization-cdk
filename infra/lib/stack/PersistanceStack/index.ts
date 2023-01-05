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
    RemovalPolicy,
    Environment,
    aws_dynamodb as ddb,
    aws_s3 as s3,
    aws_ec2 as ec2,
} from 'aws-cdk-lib'
import * as CommonUtil from '../../util/common'
import { PolicyStatements } from '../../util/policy-util'
  
export interface PersistanceStackProps extends StackProps {
    readonly env: Environment
    readonly namespace: string
    readonly parameterStoreKeys: Record<string, string>
    readonly optEngine: Record<string, string>
}
  
export class PersistanceStack extends Stack {
    private readonly solverJobs: ddb.ITable
    private readonly solutionBucket: s3.IBucket

    constructor(scope: Construct, id: string, props: PersistanceStackProps) {
        super(scope, id, props)
        
        const { env, namespace, parameterStoreKeys } = props
        const namespaced = (name: string) => `${namespace}-${env.region}-${name}`;
        
        //-------------------------------------------------------
        // Storage
        //-------------------------------------------------------
        this.solverJobs = new ddb.Table(this, 'SolverJobsTable', {
            tableName: namespaced('schedule-solver-jobs'),
            partitionKey: {
                name: 'Id',
                type: ddb.AttributeType.STRING,
            },
            billingMode: ddb.BillingMode.PAY_PER_REQUEST,
            encryption: ddb.TableEncryption.AWS_MANAGED,

            // for dev mode only
            removalPolicy: RemovalPolicy.DESTROY
        })
        CommonUtil.putParameter(this, parameterStoreKeys.ddbSolutionState, this.solverJobs.tableName)

        this.solutionBucket = new s3.Bucket(this, 'SolutionBucket', {
            bucketName: namespaced(`${this.account}-schedule-solution`),
            encryption: s3.BucketEncryption.S3_MANAGED,
            blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
            enforceSSL: true,
            serverAccessLogsPrefix: 'logs/',

            // for dev mode only
            removalPolicy: RemovalPolicy.DESTROY,
            autoDeleteObjects: true,
        })
        CommonUtil.putParameter(this, parameterStoreKeys.s3SolutionBucket, this.solutionBucket.bucketName)       
    }
}
