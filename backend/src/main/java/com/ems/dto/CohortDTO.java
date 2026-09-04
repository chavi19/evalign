package com.ems.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class CohortDTO {
    private Long cohortId;
    private String cohortName;
    private String batchCode;
    private Integer candidateCount;
    private Integer evaluatorsMapped;
    private LocalDate startDate;
    private String status;
}
