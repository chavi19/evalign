package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "EVALUATORS")
public class Evaluator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evaluatorId;

    @Column(unique = true)
    private String empId;

    private String name;
    private String vertical;
    private String domain;
    
    private String availabilityStatus; // AVAILABLE, UNAVAILABLE
    private LocalDate unavailableFrom;
    private LocalDate unavailableTo;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
