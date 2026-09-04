package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "EVALUATOR_MAPPING")
public class EvaluatorMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @ManyToOne
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "evaluator_id")
    private Evaluator evaluator;

    private String round; // INTERIM, FINAL
    private Integer attempt;

    @ManyToOne
    @JoinColumn(name = "mapped_by")
    private User mappedBy;

    private LocalDateTime mappedAt = LocalDateTime.now();
    private String status; // SUGGESTED, CONFIRMED
}
