// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solution.builder;

import dev.aws.proto.optengine.domain.Skill;
import dev.aws.proto.optengine.domain.Employee;
import dev.aws.proto.optengine.domain.ShiftAssignment;
import dev.aws.proto.optengine.domain.ShiftType;
import dev.aws.proto.optengine.domain.rq.DayOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOnRequest;
import dev.aws.proto.optengine.rest.request.ScheduleSolveRequest;
import dev.aws.proto.optengine.solution.EmployeeScheduleSolution;
import dev.aws.proto.optengine.util.Constants;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class EmployeeScheduleSolutionBuilder<Request_T extends ScheduleSolveRequest> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeScheduleSolutionBuilder.class);

    private Request_T solveRequest;

    public EmployeeScheduleSolution build() {
        if(solveRequest == null) return null;

        // Employee Request
        List<DayOffRequest> dayoff = new ArrayList<>();
        List<ShiftOffRequest> shiftOff = new ArrayList<>();
        List<ShiftOnRequest> shiftOn = new ArrayList<>();
        for(var rq : solveRequest.getEmployeeRequests()) {
            LocalDate shiftDate = LocalDate.parse(rq.getDate(), Constants.DefaultDateFormatter);
            switch (rq.getType()) {
                case ShiftType.DAY_OFF:
                    dayoff.add(new DayOffRequest(rq.getEmpNo(), shiftDate));
                    break;
                case ShiftType.SHIFT_OFF:
                    shiftOff.add(new ShiftOffRequest(rq.getEmpNo(), shiftDate));
                    break;
                case ShiftType.A:
                case ShiftType.P:
                case ShiftType.L:
                    shiftOn.add(new ShiftOnRequest(rq.getEmpNo(), shiftDate, rq.getType()));
                    break;
                default:
                    logger.warn("Not supported RQ type - "+rq.toString());
            }
        }

        // Employee
        List<Employee> employeeList = new ArrayList<>();
        for(var emp : solveRequest.getEmployees()) {
            // Main Skill
            Skill mainSkill = Skill.valueOf(emp.getMainSkill());

            // All Duties
            List<Skill> skillList = new ArrayList<>();
            for(var skill : emp.getSkillList()) skillList.add(Skill.valueOf(skill.replace('/','_')));

            // number of off requests
            int numOfDayOff = (int)dayoff.stream().filter( rq -> rq.getEmpNo().equals(emp.getEmpNo())).count();
            int numOfShiftOff = (int)shiftOff.stream().filter( rq -> rq.getEmpNo().equals(emp.getEmpNo())).count();

            // Add Employee
            employeeList.add(new Employee(emp.getEmpNo(), emp.getName(), mainSkill, skillList, numOfDayOff, numOfShiftOff));
        }

        // Shift assignment
        List<ShiftAssignment> shiftAssignmentList = new ArrayList<>();
        for(var shift : solveRequest.getShiftRequirements()) {
            for(int i = 0 ; i < shift.getRequiredCnt() ; i++) {
                LocalDate shiftDate = LocalDate.parse(shift.getTargetDate(), Constants.DefaultDateFormatter);
                shiftAssignmentList.add(new ShiftAssignment(
                        String.format("SA-%s-%s-%s-%d", shift.getTargetDate(), shift.getSkill(), shift.getAtndCode(), i),
                        shiftDate,
                        shift.getAtndCode(),
                        Skill.valueOf(shift.getSkill().replace('/','_')),
                        shift.getRequiredCnt()
                ));
            }
        }

        // Create solution
        return EmployeeScheduleSolution.builder()
                .shiftAssignmentList(shiftAssignmentList)
                .employeeList(employeeList)
                .dayOffList(dayoff)
                .shiftOffList(shiftOff)
                .shiftOnList(shiftOn)
                .build();
    }
}
