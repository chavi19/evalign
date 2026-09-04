# Evaluator Mapping System (EMS)

Automating Interview Evaluator Availability & Mapping for Training Cohorts (Mid-term Interim & End-of-training Final rounds).

---

## 1. Project Overview

The **Evaluator Mapping System** is a full-stack web application designed for Batch Owners (POCs) to streamline evaluator management and student interview assignments across training cohorts. It enforces strict business constraints such as preventing evaluator repetitions between Interim and Final rounds or repeat Final attempts.

---

## 2. Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, MySQL Connector, Lombok, Apache POI (Excel upload), Maven.
- **Frontend:** Angular 19/17, TypeScript, Angular Router, HttpClient, Reactive Forms, CSS3.
- **Database:** MySQL 8.x (`evaluator_mapping_db`).

---

## 3. Database Setup

1. Make sure MySQL service is running.
2. Create the database:
   ```sql
   CREATE DATABASE evaluator_mapping_db;
   ```
3. Update database credentials in `backend/src/main/resources/application.properties` if your MySQL username/password differs from `root`/`root`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
   *Note: Hibernate will automatically create and update the database schema and seed demo data on startup.*

---

## 4. How to Run

### Run Backend:
```bash
cd backend
mvn spring-boot:run
```
*(Backend server runs at `http://localhost:8080`)*

### Run Frontend:
```bash
cd frontend
npm start
```
*(Frontend runs at `http://localhost:4200`)*

---

## 5. Demo Credentials

- **Email:** `admin@example.com`
- **Password:** `admin123`
- **Role:** Batch Owner (POC)

---

## 6. Main Features

1. **Batch Owner Login:**
   - Pre-seeded admin credentials for immediate testing.
   - Clean split screen layout.

2. **Cohorts Management (`/cohorts`):**
   - List, search, and filter cohorts.
   - Create new cohorts with batch code, candidate count, start date, and status.
   - Direct link to open mapping workspace per cohort.

3. **Evaluator Pool (`/evaluators`):**
   - Search by name or Emp ID.
   - Filter by vertical (Finance, IT, Insurance, Health), domain, and availability.
   - Toggle availability or set unavailability date ranges.
   - Excel upload via Apache POI (`EMP_ID`, `NAME`, `VERTICAL`, `DOMAIN`, `AVAILABILITY_STATUS`, `UNAVAILABLE_FROM`, `UNAVAILABLE_TO`).
   - Add evaluator to a cohort's shortlist.

4. **Evaluator Mapping Workspace (`/mapping/:cohortId`):**
   - Dedicated views for **Interim Interview** and **Final Interview**.
   - **Auto-Map Evaluators:** Automatically assigns available shortlisted evaluators with workload balancing.
   - **Manual Mapping & Reassign:** Choose specific evaluators with instant constraint validation.
   - **Confirm Mapping:** Confirms assignments and locks audit trail.

5. **Audit & Reports (`/reports`):**
   - View all mapping history across candidates, evaluators, cohorts, stages, and attempts.
   - Filter by cohort, stage, and confirmation status.
   - Export report to CSV.

---

## 7. Important Business Rules Enforced

- **RULE 1:** Only evaluators assigned/shortlisted to the selected cohort can be mapped.
- **RULE 2:** Only available/eligible evaluators can be mapped (`"Evaluator is not available"`).
- **RULE 3:** The evaluator used for a candidate's Interim interview **MUST NOT** be used for that candidate's Final interview (`"Blocked: same evaluator as Interim"`).
- **RULE 4:** If a candidate has a repeat Final attempt, every evaluator previously used for that candidate's Final attempts **MUST** be excluded (`"New evaluator required"`).
- **RULE 5:** Backend rejects invalid mappings with appropriate HTTP error status and messages even if submitted manually.

---

## 8. Sample Seed Data Included

- **1 Demo User:** `admin@example.com` / `admin123` (POC)
- **3 Cohorts:** `Batch-1 (DS Track)`, `Batch-2 (Full Stack)`, `Batch-3 (Cloud)`
- **7 Candidates:** `Priya Sharma`, `Rahul Verma`, `Aisha Khan`, `Meera Iqbal`, `John Doe`, `Jane Smith`, `Alice Johnson`
- **8 Evaluators:** Covering Finance, Insurance, Health, IT with Available/Unavailable statuses
- **Pre-configured Mappings:**
  - Candidates mapped in Interim
  - `Aisha Khan` with Final Attempt 1 confirmed (demonstrating Repeat Final / Attempt 2 constraint validation).
