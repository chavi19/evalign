package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "EVALUATOR_SHORTLIST")
public class EvaluatorShortlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shortlistId;

    @ManyToOne
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @ManyToOne
    @JoinColumn(name = "evaluator_id")
    private Evaluator evaluator;

    @ManyToOne
    @JoinColumn(name = "added_by")
    private User addedBy;

    private LocalDateTime addedAt = LocalDateTime.now();
}
