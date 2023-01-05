// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app.ssmparams.ddb")
public interface DdbProperties {

    @WithName("table.solver-jobs")
    String solverJobTableNameParam();
}
