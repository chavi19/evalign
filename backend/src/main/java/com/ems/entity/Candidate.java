package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CANDIDATES")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    @ManyToOne
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    private String candidateName;
    private LocalDateTime createdAt = LocalDateTime.now();
}
