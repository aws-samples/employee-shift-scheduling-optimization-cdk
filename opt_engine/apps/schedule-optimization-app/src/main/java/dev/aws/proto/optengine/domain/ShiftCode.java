// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package dev.aws.proto.optengine.domain;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftCode {
    private String code;

    private String type;

    private LocalTime startTime;

    private LocalTime endTime;

    private Duration duration;

    public ShiftCode(String code) {
        if(StringUtil.isNullOrEmpty(code)) throw new IllegalArgumentException("Invalid shift code : [%s]" + code==null?"null":code);
        this.code = code;
        this.type = code.substring(0,1);
        setShiftTimeByShiftType(type);
    }

    private void setShiftTimeByShiftType(String type) {
        switch (type) {
            case "A":
                startTime = LocalTime.of(6, 30);
                endTime = LocalTime.of(15, 30);
                duration = Duration.ofMinutes(480);
                break;
            case "P":
                startTime = LocalTime.of(13, 30);
                endTime = LocalTime.of(22, 0);
                duration = Duration.ofMinutes(450);
                break;
            case "L":
                startTime = LocalTime.of(8, 30);
                endTime = LocalTime.of(18, 0);
                duration = Duration.ofMinutes(600);
                break;
            default:
                throw new IllegalArgumentException("Invalid shift type : "+ type==null?"null":type );
        }

    }
}
