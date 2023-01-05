package dev.aws.proto.optengine.solution.inspect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportItem {
    private String causedBy;
    private String score;
    private List<ReportItemDetail> detail;
}
