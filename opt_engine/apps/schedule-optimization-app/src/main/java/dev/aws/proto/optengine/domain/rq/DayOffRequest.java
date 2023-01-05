// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain.rq;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DayOffRequest extends EmployeeRequest {
    public DayOffRequest(String empNo, LocalDate shiftDate) {
        super(empNo, shiftDate);
    }
}
