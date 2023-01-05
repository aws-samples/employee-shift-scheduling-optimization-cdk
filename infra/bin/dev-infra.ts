#!/usr/bin/env node

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

import 'source-map-support/register'
import * as core from 'aws-cdk-lib'
import config from '../config'

import { VpcStack } from '../lib/stack/VpcStack'
import { EcrStack } from '../lib/stack/EcrStack'
import { PersistanceStack } from '../lib/stack/PersistanceStack'
import { OptEngineStack } from '../lib/stack/OptEngineStack'
import { BastionHostStack } from '../lib/stack/BastionHostStack'

const app = new core.App()

const vpcStack = new VpcStack(app, 'CommonVpcStack', {
  stackName: `${config.namespace}-VpcStack`,
  ...config,
})

const persistanceStack = new PersistanceStack(app, 'PersistanceStack', {
  stackName: `${config.namespace}-PersistanceStack`,
  ...config
})

const ecrStack = new EcrStack(app, 'EcrStack', {
  stackName: `${config.namespace}-EcrStack`,
  ...config
})

const optEngine = new OptEngineStack(app, 'OptEngineStack', {
  stackName: `${config.namespace}-OptEngineStack`,
  ...config
})
optEngine.addDependency(persistanceStack)
optEngine.addDependency(vpcStack)
optEngine.addDependency(ecrStack)

const bastionHost = new BastionHostStack(app, 'BastionHostStack', {
  stackName: `${config.namespace}-BastionHostStack`,
  ...config
})
bastionHost.addDependency(vpcStack)
