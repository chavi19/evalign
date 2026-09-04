package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private String role; // POC, BATCH_OWNER

    private LocalDateTime createdAt = LocalDateTime.now();
}
