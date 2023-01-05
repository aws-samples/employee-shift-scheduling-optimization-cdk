// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Getter
@NoArgsConstructor
public class Shift {
    private LocalDate shiftDate;
    private ShiftCode shiftCode;
    private Skill skill;
    private int requiredEmpCnt;
    private int weekIndex;

    // Week calculation basement(Monday)
    private static final LocalDate WEEK_BASEMENT = LocalDate.of(2022, 1, 3);

    public Shift(LocalDate shiftDate, String shiftCode, Skill skill, int requiredEmpCnt) {
        this.shiftDate = shiftDate;
        this.shiftCode = new ShiftCode(shiftCode);
        this.skill = skill;
        this.requiredEmpCnt = requiredEmpCnt;
        this.weekIndex = (int)ChronoUnit.WEEKS.between(WEEK_BASEMENT, shiftDate);
    }

    public Duration getDuration(){
        return shiftCode.getDuration();
    }
}
