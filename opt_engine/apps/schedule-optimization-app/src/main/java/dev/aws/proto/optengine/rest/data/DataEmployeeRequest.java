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
public class DataEmployeeRequest {
    String date;
    String empNo;
    String type;

    @Override
    public String toString() { return String.format("Date : %s\t EmployeeNo : %s\tType : %s", date, empNo, type); }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.date + this.empNo + this.type);
    }
}
