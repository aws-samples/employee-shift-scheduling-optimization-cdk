// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.rest.response;

import dev.aws.proto.optengine.persistance.data.SolverJobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private String problemId = "-";
    private String state = "-";
    private long createdAt = -1L;

    public StatusResponse(SolverJobStatus status){
        this.problemId = status.getProblemId();
        this.state = status.getState();
        this.createdAt = status.getCreatedAt();
    }
}
