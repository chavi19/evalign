package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "COHORTS")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cohortId;

    private String cohortName;
    private String batchCode;

    @ManyToOne
    @JoinColumn(name = "poc_id")
    private User poc;

    private Integer candidateCount;
    private LocalDate startDate;
    private String status; // ACTIVE, IN_PROGRESS, COMPLETED

    private LocalDateTime createdAt = LocalDateTime.now();
}
