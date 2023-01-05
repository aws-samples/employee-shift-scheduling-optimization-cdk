// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.persistance;

import dev.aws.proto.core.util.aws.S3Utility;
import dev.aws.proto.core.util.aws.SsmUtility;
import dev.aws.proto.optengine.config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class S3SolutionBucketService {

    private static final Logger logger = LoggerFactory.getLogger(S3SolutionBucketService.class);

    @Inject
    S3Properties bucketProperties;

    private String bucketName;

    public S3SolutionBucketService(S3Properties bucketProperties) {
        this.bucketProperties = bucketProperties;
        this.bucketName = SsmUtility.getParameterValue(bucketProperties.solutionBucketNameParam());
    }

    public void uploadFile(String keyPath, String localFilePath){
        S3Utility.uploadFile(this.bucketName, keyPath, Paths.get(localFilePath));
    }

    public void downloadFile(String keyPath, String localFilePath){
        S3Utility.downloadFile(this.bucketName, keyPath, Paths.get(localFilePath));
    }
}
