package com.ems.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EvaluatorDTO {
    private Long evaluatorId;
    private String empId;
    private String name;
    private String vertical;
    private String domain;
    private String availabilityStatus;
    private LocalDate unavailableFrom;
    private LocalDate unavailableTo;
    private String statusReason;
    private Boolean isPermanent;
}
