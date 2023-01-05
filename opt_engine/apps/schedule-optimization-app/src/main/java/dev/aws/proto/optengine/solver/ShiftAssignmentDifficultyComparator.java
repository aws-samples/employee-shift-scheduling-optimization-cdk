// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.solver;

import dev.aws.proto.optengine.domain.ShiftAssignment;
import java.util.Comparator;

public class ShiftAssignmentDifficultyComparator implements Comparator<ShiftAssignment> {

    @Override
    public int compare(ShiftAssignment a, ShiftAssignment b) {

        // late shiftDate is more difficult
        int difficulty = a.getShiftDate().compareTo(b.getShiftDate());

        return difficulty;
    }
}
