// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.rest.request;

import dev.aws.proto.optengine.rest.data.DataEmployee;
import dev.aws.proto.optengine.rest.data.DataEmployeeRequest;
import dev.aws.proto.optengine.rest.data.DataShiftRequirements;
import lombok.Data;

@Data
public class ScheduleSolveRequest {

    private String executionId;

    // Employee requirements for each shift
    private DataShiftRequirements[] shiftRequirements;

    // Employee list
    private DataEmployee[] employees;

    // Employee Requirements (ShiftOff/DayOff/...)
    private DataEmployeeRequest[] employeeRequests;

    public String getSummary(){
        return String.format("ShiftRequirements : %d\tEmployee : %d\tEmployeeRequest : %d", len(shiftRequirements), len(employees), len(employeeRequests));
    }

    private int len(Object[] t) { return t==null?0:t.length; }
}
