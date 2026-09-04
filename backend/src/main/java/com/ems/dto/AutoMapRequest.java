package com.ems.dto;
import lombok.Data;
@Data
public class AutoMapRequest {
    private Long cohortId;
    private String round; // INTERIM or FINAL
}
