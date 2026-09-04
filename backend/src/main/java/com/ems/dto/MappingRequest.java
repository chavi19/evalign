package com.ems.dto;
import lombok.Data;
@Data
public class MappingRequest {
    private Long cohortId;
    private Long candidateId;
    private Long evaluatorId;
    private String round; // INTERIM or FINAL
    private Integer attempt;
}
