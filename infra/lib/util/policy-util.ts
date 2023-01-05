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

  import {  aws_iam as iam } from 'aws-cdk-lib'
 import { S3, DynamoDB } from 'cdk-iam-actions/lib/actions'
 
 // ssm params
 const ssmPolicyStatement = (actions: string[], resources: string[]): iam.PolicyStatement => {
     const policyStatement = new iam.PolicyStatement({
       effect: iam.Effect.ALLOW,
       actions,
       resources,
     })
   
     return policyStatement
 }
   
 const readSSMParams = (region: string, account: string): iam.PolicyStatement => {
     return ssmPolicyStatement(
       ['ssm:Describe*', 'ssm:Get*', 'ssm:List*'],
       [`arn:aws:ssm:${region}:${account}:parameter/*`],
     )
 }
 
 // s3
 const bucketPolicyStatement = (actions: string[], bucketArn: string): iam.PolicyStatement => {
     const policyStatement = new iam.PolicyStatement({
         effect: iam.Effect.ALLOW,
         actions,
         resources: [bucketArn, `${bucketArn}/*`],
     })
 
     return policyStatement
 }
 
 const readBucket = (bucketArn: string): iam.PolicyStatement => {
     return bucketPolicyStatement([S3.GET_OBJECT, S3.HEAD_BUCKET, S3.LIST_BUCKET], bucketArn)
 }
 
 const writeBucket = (bucketArn: string): iam.PolicyStatement => {
     return bucketPolicyStatement([S3.PUT_OBJECT, S3.PUT_OBJECT_ACL], bucketArn)
 }
 
 const readWriteBucket = (bucketArn: string): iam.PolicyStatement => {
     return bucketPolicyStatement(
         [S3.GET_OBJECT, S3.HEAD_BUCKET, S3.LIST_BUCKET, S3.PUT_OBJECT, S3.PUT_OBJECT_ACL],
         bucketArn,
     )
 }
 const deleteObjectBucket = (bucketArn: string): iam.PolicyStatement => {
     return bucketPolicyStatement([S3.DELETE_OBJECT], bucketArn)
 }
 
 // dynamodb
 const tablePolicyStatement = (actions: string[], tableArn: string): iam.PolicyStatement => {
     const policyStatement = new iam.PolicyStatement({
       effect: iam.Effect.ALLOW,
       actions,
       resources: [tableArn],
     })
   
     return policyStatement
 }
   
 const readDDBTable = (tableArn: string): iam.PolicyStatement => {
     return tablePolicyStatement(
       [DynamoDB.GET_ITEM, DynamoDB.BATCH_GET_ITEM, DynamoDB.GET_RECORDS, DynamoDB.SCAN, DynamoDB.QUERY],
       tableArn,
     )
 }
   
 const updateDDBTable = (tableArn: string): iam.PolicyStatement => {
     return tablePolicyStatement([DynamoDB.UPDATE_ITEM, DynamoDB.PUT_ITEM], tableArn)
 }
   
 const batchWriteDDBTable = (tableArn: string): iam.PolicyStatement => {
     return tablePolicyStatement([DynamoDB.UPDATE_ITEM, DynamoDB.PUT_ITEM, DynamoDB.BATCH_WRITE_ITEM], tableArn)
 }
   
 const deleteFromDDBTable = (tableArn: string): iam.PolicyStatement => {
     return tablePolicyStatement([DynamoDB.DELETE_ITEM], tableArn)
 }
 
 // iam
 const iamPassRole = (account: string): iam.PolicyStatement => {
     return ssmPolicyStatement(['iam:PassRole'], [`arn:aws:iam::${account}:role/*`])
 }
 
 // ecr
 const readEcrRepo = (resource?: string) => {
     return new iam.PolicyStatement({
         effect: iam.Effect.ALLOW,
         resources: [resource?resource:'*'],
         actions: [
             "ecr:GetAuthorizationToken",
             "ecr:BatchCheckLayerAvailability",
             "ecr:GetDownloadUrlForLayer",
             "ecr:BatchGetImage",
         ]
     });
 }
 
 // log
 const createLog = (resource?: string) => {
     return new iam.PolicyStatement({
         effect: iam.Effect.ALLOW,
         resources: [resource?resource:'*'],
         actions: [
             "logs:CreateLogStream",
             "logs:PutLogEvents"
         ]
     });
 }
 
 // export
 export const PolicyStatements = {
     ssm : { readSSMParams },
     s3 : { readBucket, writeBucket, readWriteBucket, deleteObjectBucket },
     ddb: { readDDBTable, updateDDBTable, batchWriteDDBTable, deleteFromDDBTable },
     iam: { iamPassRole },
     ecr: { readEcrRepo },
     log: { createLog }
 };