// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

public interface ShiftType {
    String A = "A"; // From Morning
    String P = "P"; // From Afternoon
    String L = "L"; // LongTime (Full day)
    String DAY_OFF = "V"; // DayOff
    String SHIFT_OFF = "H"; // ShiftOff
}
