// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.util;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String PlanningEmployeeRange = "empRange";

    public static final DateTimeFormatter DefaultDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DefaultDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private Constants() {
        throw new AssertionError("Utility class");
    }
}
