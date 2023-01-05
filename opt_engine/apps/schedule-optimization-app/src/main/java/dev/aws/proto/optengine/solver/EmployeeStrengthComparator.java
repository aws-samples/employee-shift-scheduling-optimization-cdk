// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solver;

import dev.aws.proto.optengine.domain.Employee;

import java.util.Comparator;

public class EmployeeStrengthComparator implements Comparator<Employee>  {
    private static final Comparator<Employee> COMPARATOR =
            // 1. More difficult to assign work for employees with a lot of day-off
            Comparator.comparingInt( (Employee employee) -> employee.getDayOffCount() )
            // 2. If same day-off, it is more difficult with a lot of shift-off
            .thenComparingLong(Employee::getShiftOffCount);

    @Override
    public int compare(Employee a, Employee b) {
        return COMPARATOR.compare(a, b);
    }
}
