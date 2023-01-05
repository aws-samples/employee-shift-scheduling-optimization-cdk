// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

import dev.aws.proto.optengine.solver.EmployeeStrengthComparator;
import dev.aws.proto.optengine.solver.ShiftAssignmentDifficultyComparator;

import dev.aws.proto.optengine.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.LocalDate;
import java.util.Objects;


@Data
@NoArgsConstructor
@PlanningEntity(difficultyComparatorClass = ShiftAssignmentDifficultyComparator.class)
public class ShiftAssignment extends Shift {

    @PlanningId
    private String id;

    @PlanningVariable(valueRangeProviderRefs = { Constants.PlanningEmployeeRange }, strengthComparatorClass = EmployeeStrengthComparator.class)
    private Employee employee;

    public ShiftAssignment(String id, LocalDate shiftDate, String shiftCode, Skill skill, int requiredEmpCnt) {
        super(shiftDate, shiftCode, skill, requiredEmpCnt);
        this.id = id;
    }

    public String getEmpNo(){
        return employee.getEmpNo();
    }

    public String getShiftType(){
        return getShiftCode().getType();
    }

    @Override
    public String toString() {
        return String.format("ShiftAssignment - ID : %s , Date : %s , Code : %s , Skill : %s , Employee : %s[%s] "
                , getId()
                , getShiftDate().format(Constants.DefaultDateFormatter)
                , getShiftCode(), getSkill().name()
                , employee!=null?employee.getName():"null"
                , employee!=null?employee.getEmpNo():"null");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
