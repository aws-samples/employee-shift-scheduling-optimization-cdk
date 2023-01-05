// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

public enum Skill {
    FR("FR"), PR("PR"), MC_EY("MC_EY");

    private final String skillCode;

    Skill(String skill){
        this.skillCode = skill;
    }

    public boolean equalsName(String otherSkillCode) {
        return this.skillCode.equals(otherSkillCode);
    }

    @Override
    public String toString() {
        return this.skillCode;
    }
}
