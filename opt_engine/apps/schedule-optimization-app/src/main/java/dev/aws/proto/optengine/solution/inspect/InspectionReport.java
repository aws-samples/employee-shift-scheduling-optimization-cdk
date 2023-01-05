package dev.aws.proto.optengine.solution.inspect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionReport {
    private String problemId;
    private String totalScore;
    private List<ReportItem> constraintReport;
    private List<ReportItem> employeeReport;
}
