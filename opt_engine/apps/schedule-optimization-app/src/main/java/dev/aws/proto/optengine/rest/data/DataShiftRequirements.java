// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataShiftRequirements {
    String targetDate;
    String skill;
    String atndCode;
    int requiredCnt;

    @Override
    public String toString() { return String.format("Date : %s\tSkill : %s\tAtndCode : %s\tReqCnt : %d", targetDate, skill, atndCode, requiredCnt); }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.targetDate + this.skill + this.atndCode);
    }
}
