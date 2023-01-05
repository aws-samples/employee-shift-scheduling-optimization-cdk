// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.core.exception;

public class OptimizeException extends RuntimeException {
    public OptimizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OptimizeException(String message) {
        super(message);
    }
}
