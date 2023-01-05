// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain.rq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ShiftOffRequest extends EmployeeRequest {
    public ShiftOffRequest(String empNo, LocalDate shiftDate) {
        super(empNo, shiftDate);
    }
}
