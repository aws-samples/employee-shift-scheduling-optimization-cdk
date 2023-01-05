// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @PlanningId
    private String empNo;

    private String name;

    private Skill mainSkill;

    private List<Skill> availSkillList;

    private int dayOffCount;

    private int shiftOffCount;

    @Override
    public String toString() {
        var skillList = availSkillList.stream().map(d -> d.name()).collect(Collectors.toList());
        return String.format("%s[%s] - Skill : %s(%s)", name, empNo, mainSkill.name(), String.join(",", skillList));
    }

    @Override
    public int hashCode() {
        return empNo.hashCode();
    }
}
