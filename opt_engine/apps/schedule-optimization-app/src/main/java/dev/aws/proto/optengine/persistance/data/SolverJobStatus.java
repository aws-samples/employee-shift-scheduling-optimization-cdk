// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.persistance.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SolverJobStatus {
    private String problemId = "-";
    private String state;
    private String score;
    private long createdAt = -1L;
    private long solverDurationInMs = -1L;
    private String apiRequestFile;
    private String solutionFile;
    private String inspectionReportFile;
}
