package dev.aws.proto.optengine.solution.inspect;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportItemDetail {
    private String reason;
    private String score;
    private String description;
}
