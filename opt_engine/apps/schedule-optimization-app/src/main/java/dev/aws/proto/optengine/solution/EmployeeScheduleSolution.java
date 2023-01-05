// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solution;

import dev.aws.proto.optengine.domain.Employee;
import dev.aws.proto.optengine.domain.ShiftAssignment;
import dev.aws.proto.optengine.domain.rq.DayOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOnRequest;
import dev.aws.proto.optengine.util.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import dev.aws.proto.appcore.solution.SolutionBase;

import java.util.List;

@Data
@NoArgsConstructor
@PlanningSolution
public class EmployeeScheduleSolution extends SolutionBase {

    @PlanningEntityCollectionProperty
    private List<ShiftAssignment> shiftAssignmentList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = Constants.PlanningEmployeeRange)
    private List<Employee> employeeList;

    @ProblemFactCollectionProperty
    private List<DayOffRequest> dayOffRqList;

    @ProblemFactCollectionProperty
    private List<ShiftOffRequest> shiftOffRqList;

    @ProblemFactCollectionProperty
    private List<ShiftOnRequest> shiftOnRqList;

    @PlanningScore
    private HardMediumSoftScore score;

    @Builder
    public EmployeeScheduleSolution(List<ShiftAssignment> shiftAssignmentList, List<Employee> employeeList, List<DayOffRequest> dayOffList, List<ShiftOffRequest> shiftOffList, List<ShiftOnRequest> shiftOnList, long createdAt) {
        this.shiftAssignmentList = shiftAssignmentList;
        this.employeeList = employeeList;
        this.dayOffRqList = dayOffList;
        this.shiftOffRqList = shiftOffList;
        this.shiftOnRqList = shiftOnList;
        super.createdAt = createdAt;
    }
}
