// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain.rq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftOnRequest extends EmployeeRequest {
    String type;
    public ShiftOnRequest(String empNo, LocalDate shiftDate, String type) {
        super(empNo, shiftDate);
        this.type = type;
    }
}
