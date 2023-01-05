// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app.ssmparams.s3")
public interface S3Properties {

    @WithName("bucket.schedule-solution")
    String solutionBucketNameParam();
}
