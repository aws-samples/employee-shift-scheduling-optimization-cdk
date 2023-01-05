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
    aws_ssm as ssm 
} from 'aws-cdk-lib'

// ParameterStore --------------------------------------------------------------------------------------
export const putParameter = (ctx: Construct, key: string, value: string, description?: string) => {
    new ssm.StringParameter(ctx, 'SsmParams'+key, {
        parameterName: key,
        stringValue: value,
        description: description,
    });
}

export const getParameter = (ctx: Construct, key: string) =>  ssm.StringParameter.valueForStringParameter(ctx, key);

export function getParameterFromLookup (ctx: Construct, key: string): string {
    return  ssm.StringParameter.valueFromLookup(ctx, key);
}

// Miscellaneous
export const toInt = (input: string, defaultValue?: number) => {
    let num = parseInt(input);

    if(isNaN(num) && defaultValue !== undefined) return defaultValue;
    else return num;
}