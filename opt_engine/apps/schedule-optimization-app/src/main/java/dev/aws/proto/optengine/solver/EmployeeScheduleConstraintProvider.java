// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solver;

import dev.aws.proto.optengine.domain.ShiftAssignment;
import dev.aws.proto.optengine.domain.rq.DayOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOffRequest;
import dev.aws.proto.optengine.domain.rq.ShiftOnRequest;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeScheduleConstraintProvider implements ConstraintProvider {

    private static final Logger logger = LoggerFactory.getLogger( EmployeeScheduleConstraintProvider.class );

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // << General >>-----------------------------------------------------------------
                oneShiftPerDay(constraintFactory),

                // << Legal >>-----------------------------------------------------------------
                workingTimePerWeek(constraintFactory),

                // << Skill >>-----------------------------------------------------------------
                skillRequirements(constraintFactory),

                // << Employee Requests >>-----------------------------------------------------------------
                dayOffRequest(constraintFactory),
                shiftOffRequest(constraintFactory),
                shiftOnRequest(constraintFactory),
        };
    }


    // [HARD] One employee can attend one shift a day
    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee),
                        Joiners.equal(ShiftAssignment::getShiftDate))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("One employee can one shift a day");
    }

    // [HARD] The shift must be assigned to shift based on employee's skills.
    Constraint skillRequirements(ConstraintFactory constraintFactory) {
        return  constraintFactory
                .forEach(ShiftAssignment.class)
                .filter(shiftAssignment -> !shiftAssignment.getEmployee().getAvailSkillList().contains(shiftAssignment.getSkill()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Employee does not have skill");
    }

    // [HARD] A employee can attend less than 48 hours for a week
    Constraint workingTimePerWeek(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(ShiftAssignment.class)
                .groupBy(ShiftAssignment::getEmployee, ShiftAssignment::getWeekIndex, ConstraintCollectors.sumDuration(ShiftAssignment::getDuration))
                .filter((employee, week, duration) -> duration.toMinutes() > 48 * 60)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Work time less than 48 hours for a week");
    }

    // [MEDIUM] Employees don't like working on day-off
    Constraint dayOffRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DayOffRequest.class)
                .join(ShiftAssignment.class,
                        Joiners.equal(DayOffRequest::getEmpNo, ShiftAssignment::getEmpNo),
                        Joiners.equal(DayOffRequest::getShiftDate, ShiftAssignment::getShiftDate))
                .penalize(HardMediumSoftScore.ONE_MEDIUM)
                .asConstraint("Working on day-off request");
    }

    // [SOFT] Employees don't like working on shift-off
    Constraint shiftOffRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ShiftOffRequest.class)
                .join(ShiftAssignment.class,
                        Joiners.equal(ShiftOffRequest::getEmpNo, ShiftAssignment::getEmpNo),
                        Joiners.equal(ShiftOffRequest::getShiftDate, ShiftAssignment::getShiftDate))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint("Working on shift-off request");
    }

    // [SOFT] Employees don't like resting on shift-on request
    Constraint shiftOnRequest(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ShiftOnRequest.class)
                .ifNotExists(ShiftAssignment.class, Joiners.equal(ShiftOnRequest::getEmpNo, ShiftAssignment::getEmpNo),
                        Joiners.equal(ShiftOnRequest::getShiftDate, ShiftAssignment::getShiftDate),
                        Joiners.equal(ShiftOnRequest::getType, ShiftAssignment::getShiftType))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint("Shift is not assigned on shift-on request");
    }
}
