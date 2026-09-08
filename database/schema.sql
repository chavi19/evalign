-- ================================================================
-- Evaluator Mapping System - Database Schema
-- Compatible with MySQL 8.x and H2 (for development)
-- ================================================================

-- USERS table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'POC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- COHORTS table
CREATE TABLE IF NOT EXISTS cohorts (
    cohort_id INT AUTO_INCREMENT PRIMARY KEY,
    cohort_name VARCHAR(150) NOT NULL,
    batch_code VARCHAR(50) NOT NULL,
    poc_id INT,
    candidate_count INT DEFAULT 0,
    start_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (poc_id) REFERENCES users(user_id)
);

-- CANDIDATES table
CREATE TABLE IF NOT EXISTS candidates (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    cohort_id INT NOT NULL,
    candidate_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cohort_id) REFERENCES cohorts(cohort_id)
);

-- EVALUATORS table
CREATE TABLE IF NOT EXISTS evaluators (
    evaluator_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    vertical VARCHAR(100),
    domain VARCHAR(100),
    availability_status VARCHAR(20) DEFAULT 'AVAILABLE',
    unavailable_from DATE,
    unavailable_to DATE,
    status_reason VARCHAR(255),
    is_permanent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- EVALUATOR_MAPPING table
CREATE TABLE IF NOT EXISTS evaluator_mapping (
    mapping_id INT AUTO_INCREMENT PRIMARY KEY,
    cohort_id INT NOT NULL,
    candidate_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    round VARCHAR(20) NOT NULL,
    attempt INT DEFAULT 1,
    mapped_by INT,
    mapped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SUGGESTED',
    FOREIGN KEY (cohort_id) REFERENCES cohorts(cohort_id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id),
    FOREIGN KEY (evaluator_id) REFERENCES evaluators(evaluator_id),
    FOREIGN KEY (mapped_by) REFERENCES users(user_id)
);

-- EVALUATOR_SHORTLIST table
CREATE TABLE IF NOT EXISTS evaluator_shortlist (
    shortlist_id INT AUTO_INCREMENT PRIMARY KEY,
    cohort_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    added_by INT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cohort_id) REFERENCES cohorts(cohort_id),
    FOREIGN KEY (evaluator_id) REFERENCES evaluators(evaluator_id),
    FOREIGN KEY (added_by) REFERENCES users(user_id),
    UNIQUE (cohort_id, evaluator_id)
);
