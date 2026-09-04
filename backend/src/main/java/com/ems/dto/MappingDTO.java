package com.ems.dto;

import lombok.Data;

@Data
public class MappingDTO {
    private Long mappingId;
    private Long cohortId;
    private Long candidateId;
    private String candidateName;
    private Long evaluatorId;
    private String evaluatorName;
    private String round; // INTERIM, FINAL
    private Integer attempt;
    private String status; // SUGGESTED, CONFIRMED
    private Long interimEvaluatorId;
    private String interimEvaluatorName;
    private String evaluatorAvailability;
    private String cohortName;
    private String mappedByName;
    private java.time.LocalDateTime mappedAt;
    private String ruleWarning;
}

