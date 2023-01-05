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
public class DataEmployee {
    String empNo;
    String name;

    String mainSkill;

    String[] skillList;

    @Override
    public String toString() {
        return String.format("EmployeeNo : %s\tName : %s\tSkill : %s\tSkillList : %s"
                , empNo, name, mainSkill, String.join(",", skillList));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.empNo);
    }
}
