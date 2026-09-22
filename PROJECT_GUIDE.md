# Evaluator Mapping System (EMS) — Complete Developer & Project Guide

> **Target Audience:** Developers, Technical Leads, and Interview Candidates familiar with Java, Spring Boot, MySQL, Angular, and Apache POI who want a complete end-to-end architectural reference, business rule specification, and interview preparation guide.

---

## Table of Contents
1. [Section A: Project Overview](#section-a-project-overview)
2. [Section B: Technology Stack & Technical Rationale](#section-b-technology-stack--technical-rationale)
3. [Section C: End-to-End Application Flow](#section-c-end-to-end-application-flow)
4. [Section D: Database Architecture & Relational Schema](#section-d-database-architecture--relational-schema)
5. [Section E: Backend Architecture & Layered Design](#section-e-backend-architecture--layered-design)
6. [Section F: Frontend Architecture & Component Flow](#section-f-frontend-architecture--component-flow)
7. [Section G: Excel Processing Workflows](#section-g-excel-processing-workflows)
8. [Section H: Evaluator Availability & Date-Overlap Logic](#section-h-evaluator-availability--date-overlap-logic)
9. [Section I: REST API Catalog & HTTP Conventions](#section-i-rest-api-catalog--http-conventions)
10. [Section J: Business Logic & Rule Engine](#section-j-business-logic--rule-engine)
11. [Section K: Testing Suite & Verification](#section-k-testing-suite--verification)
12. [Section L: Team Member Responsibilities & Interview Q&A Guide](#section-l-team-member-responsibilities--interview-qa-guide)
13. [Section M: Step-by-Step Manual Recreation Plan (16 Stages)](#section-m-step-by-step-manual-recreation-plan-16-stages)

---

## Section A: Project Overview

### What the Evaluator Mapping System Does
The **Evaluator Mapping System (EMS)** is an enterprise talent operations web application designed for Batch Owners and Points of Contact (POCs). It automates and enforces strict governance over how Subject Matter Expert (SME) evaluators are shortlisted, verified for availability, and assigned to student candidates across **Interim** and **Final** interview rounds.

### What Problem It Solves
1. **Human Scheduling Conflicts & Biases**: In large corporate training academies with hundreds of trainees and evaluators, manual spreadsheet-based assignments frequently cause bias (e.g., the same evaluator assessing a trainee in both Interim and Final rounds).
2. **Dynamic Leave Windows vs Permanent Departures**: Evaluators take temporary PTO or project assignments. A static "unavailable" flag either locks out available evaluators or schedules absent ones. EMS dynamically calculates availability against the specific interview window while permanently excluding resigned personnel.
3. **Spreadsheet De-synchronization**: Multiple POCs editing disconnected Excel files results in lost roster notes. EMS acts as a single source of truth, synchronizing database changes back into master Excel files via Apache POI without breaking spreadsheet formatting.
4. **Workload Imbalance**: Manual distribution creates bottlenecks where some SMEs are assigned 10 candidates while others receive none. EMS provides a one-click automated workload balancing algorithm.

### Target Users
- **Batch Owners / POCs**: Create cohorts, upload candidate lists, shortlist SMEs, schedule interview windows, and execute automated mapping.
- **Talent Operations / Leadership**: View audit logs, review workload distributions, and download updated master rosters.

---

## Section B: Technology Stack & Technical Rationale

| Layer / Technology | Version | Purpose & Technical Rationale |
| :--- | :--- | :--- |
| **Java** | 17 LTS | Modern LTS Java runtime providing strong type safety, records, enhanced switch expressions, and long-term enterprise support. |
| **Spring Boot** | 3.2.5 | Core backend framework offering rapid production configuration, embedded Tomcat web server, dependency injection, and JPA management. |
| **Spring Data JPA / Hibernate** | 3.2.5 | Object-Relational Mapping (ORM) and declarative derived query execution against MySQL relational database. |
| **Spring Security 6 & JJWT** | 0.11.5 | Stateless session security with HMAC-SHA256 JWT tokens, password hashing with BCrypt (`10` rounds), and custom filter chain interception. |
| **MySQL** | 8.x | Robust relational database ensuring ACID transactional integrity across relational tables with foreign key constraints. |
| **Apache POI** | 5.2.5 | Enterprise spreadsheet manipulation library capable of streaming, parsing, and modifying both `.xlsx` (XSSF) and `.xls` (HSSF) files without destroying cell styles. |
| **Angular** | 17+ | Modern frontend framework utilizing standalone components, reactive forms, RxJS observables, typed TypeScript models, and client-side routing. |
| **Lombok** | 1.18.30 | Compile-time bytecode annotation processing eliminating boilerplate getters, setters, builders, and constructors. |
| **Maven** | 3.9+ | Build lifecycle, dependency management, and automated test execution. |

---

## Section C: End-to-End Application Flow

```
+-----------------------------------------------------------------------------------------+
|                                    1. USER BROWSER                                      |
|  User logs in with POC credentials, views dashboard, creates cohorts, and maps SMEs     |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                               2. ANGULAR FRONTEND LAYER                                 |
|  • Components: LoginComponent, HomeComponent, CohortsComponent, MappingComponent        |
|  • Services:   AuthService, CohortService, CandidateService, EvaluatorService           |
|  • Interceptors: AuthInterceptor automatically attaches 'Authorization: Bearer <jwt>'   |
|  • Guards:       AuthGuard protects all internal routes against unauthenticated access  |
+-------------------------------------------+---------------------------------------------+
                                            | (HTTP REST Calls with JSON / Multipart)
                                            v
+-----------------------------------------------------------------------------------------+
|                             3. SPRING SECURITY & FILTER CHAIN                           |
|  • JwtAuthenticationFilter extracts token, validates claims via JwtUtil, loads context. |
|  • SecurityConfig & CorsConfig allow authenticated origins and handle 401/403 errors.   |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                               4. REST CONTROLLER LAYER                                  |
|  • AuthController, CohortController, CandidateController, EvaluatorController, etc.     |
|  • Deserializes JSON payloads, validates parameters, and returns ResponseEntity (200,   |
|    201, 204, 400, 404, 409).                                                            |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                                 5. SERVICE / BUSINESS LAYER                             |
|  • EvaluatorService: Evaluates date-overlap logic for temporary vs permanent status.    |
|  • CandidateService: Parses candidate Excel via POI & persists batch-specific trainees. |
|  • ExcelUploadService: Row-level POI update on master roster matching unique empId.     |
|  • MappingService: Enforces 4 core isolation rules + workload balancing auto-map.      |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                              6. DATA ACCESS / REPOSITORY LAYER                          |
|  • Spring Data JPA Repositories (CohortRepository, CandidateRepository, etc.)           |
|  • Executes relational queries against MySQL with transactional rollback guarantees.    |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                                  7. DATABASE (MySQL)                                    |
|  • Tables: users, cohorts, candidates, evaluators, evaluator_shortlist, mapping         |
+-----------------------------------------------------------------------------------------+
```

---

## Section D: Database Architecture & Relational Schema

Database Name: `evaluator_mapping_db` (Defined in `database/schema.sql`)

```
  +------------------+             +--------------------+
  |      USERS       | 1         * |      COHORTS       |
  |------------------| <---------- |--------------------|
  | user_id (PK)     | poc_id      | cohort_id (PK)     |
  | name             |             | cohort_name        |
  | email (UNIQUE)   |             | batch_code         |
  | password_hash    |             | poc_id (FK)        |
  | role             |             | candidate_count    |
  | created_at       |             | start_date         |
  +------------------+             | status             |
        |                          +--------------------+
        |                                    | 1
        |                                    |
        |                                    | *
        |                          +--------------------+
        |                          |     CANDIDATES     |
        |                          |--------------------|
        |                          | candidate_id (PK)  |
        |                          | cohort_id (FK)     |
        |                          | candidate_name     |
        |                          | created_at         |
        |                          +--------------------+
        |                                    | 1
        |                                    |
        |                                    | *
        |                          +--------------------+
        | *                      1 | EVALUATOR_MAPPING  |
  +----------------------+ ------> |--------------------|
  |  EVALUATOR_SHORTLIST |         | mapping_id (PK)    |
  |----------------------|         | cohort_id (FK)     |
  | shortlist_id (PK)    |         | candidate_id (FK)  |
  | cohort_id (FK)       |         | evaluator_id (FK)  |
  | evaluator_id (FK)    |         | round (INTERIM/FIN)|
  | added_by (FK)        |         | attempt (1, 2...)  |
  +----------------------+         | mapped_by (FK)     |
        | *                        | status             |
        |                          +--------------------+
        | 1                                  |
  +------------------+                       |
  |    EVALUATORS    |                       |
  |------------------|                       |
  | evaluator_id (PK)| 1                   * |
  | emp_id (UNIQUE)  | <---------------------+
  | name             |
  | vertical         |
  | domain           |
  | avail_status     |
  | unavailable_from |
  | unavailable_to   |
  | status_reason    |
  | is_permanent     |
  +------------------+
```

### Table Specifications
1. **`users`**: Contains POC and administrator accounts with email (unique) and BCrypt password hash.
2. **`cohorts`**: Represents a training batch (e.g., `Batch-4 (DevOps)`) owned by a POC.
3. **`candidates`**: Trainees assigned strictly to a single `cohort_id`. Candidates are isolated per cohort.
4. **`evaluators`**: Evaluator pool containing employee ID (`emp_id`), domain, availability status, leave window dates, status reason, and permanent flag.
5. **`evaluator_shortlist`**: Join table specifying which evaluators are shortlisted for a specific cohort.
6. **`evaluator_mapping`**: Assignment record mapping Candidate $\rightarrow$ Evaluator for a specific `round` (`INTERIM` or `FINAL`) and `attempt` number.

---

## Section E: Backend Architecture & Layered Design

```
Controller Layer (@RestController)
       │
       ▼
Service Layer (@Service, @Transactional)
       │
       ▼
Repository Layer (@Repository, JpaRepository)
       │
       ▼
Database Layer (MySQL / Entity)
```

### Layer Responsibilities
- **Controller Layer (`com.ems.controller.*`)**: Handles HTTP protocol concerns (request mapping, path variables, request bodies, HTTP headers, content negotiation, multipart file streams) and returns standard `ResponseEntity` with explicit status codes.
- **Service Layer (`com.ems.service.*`)**: Encapsulates core business rules (isolation rules, workload balancing, Excel parsing/sync, JWT token generation). Maintains transactional integrity via `@Transactional`.
- **Repository Layer (`com.ems.repository.*`)**: Spring Data JPA derived query methods (`findByCohortCohortId`, `findByEmpId`, etc.) abstracting SQL queries.
- **DTOs (`com.ems.dto.*`)**: Decouple network contract from internal entity models, avoiding circular serialization references and preventing entity overexposure.
- **Central Exception Handling (`GlobalExceptionHandler.java`)**: Uses `@RestControllerAdvice` to translate all exceptions into uniform `ErrorResponse` objects with timestamps, HTTP status codes, and user-friendly error messages.

---

## Section F: Frontend Architecture & Component Flow

The Angular application is constructed using modern Angular standalone components and an enterprise layout:

### Application Routes (`app.routes.ts`)
- `/login`: Unauthenticated split-hero login page.
- `/home`: Evaluator Pool Dashboard, 2-tier filter console, status reason editor, Master Excel upload/download.
- `/cohorts`: Cohort batch manager, candidate progress indicators, New Cohort Batch modal with Candidate Excel drag-and-drop.
- `/mapping/:cohortId`: Interactive Interim/Final stage mapping console with auto-map engine and rule conflict warnings.
- `/reports`: Audit trail records and CSV exporter.

### Core Frontend Services
- **`AuthService`**: Handles login requests, persists JWT in browser `localStorage`, and exposes `currentUser$` observable.
- **`AuthInterceptor`**: Angular `HttpInterceptorFn` that intercepts all outbound HTTP calls and appends `Authorization: Bearer <token>`.
- **`AuthGuard`**: Route guard preventing unauthenticated access to internal pages.
- **`CohortService`**: Manages cohorts, shortlists, and candidate Excel file upload dispatching.
- **`CandidateService`**: Direct Candidate API integration for candidate fetching and batch uploads.
- **`EvaluatorService`**: Handles evaluator filtering (with date query parameters), status reason updates, and Master Excel streaming.
- **`MappingService`**: Manages manual mapping, auto-map execution, and mapping confirmations.

---

## Section G: Excel Processing Workflows

EMS implements two independent Excel processing workflows using Apache POI:

```
+-----------------------------------------------------------------------------------------+
|                         WORKFLOW 1: MASTER EVALUATOR EXCEL                              |
|                                                                                         |
|  1. Upload: POC uploads master_evaluators.xlsx via Home page                            |
|  2. Disk Persistence: File saved as uploads/master_evaluators.xlsx                      |
|  3. Parse & Upsert: POI reads EMP ID, Name, Vertical, Domain, Status, Dates, Reason      |
|  4. Status Update: POC clicks "Update Status Reason" & saves in UI                      |
|  5. POI Row Sync: EvaluatorService updates DB + locates exact row in master Excel by   |
|     EMP ID (Col 0), updating only status columns while preserving all formatting        |
|  6. Download: POC clicks "Download Master Excel" to export updated spreadsheet           |
+-----------------------------------------------------------------------------------------+

+-----------------------------------------------------------------------------------------+
|                         WORKFLOW 2: COHORT CANDIDATE EXCEL                              |
|                                                                                         |
|  1. Modal: POC opens "+ New Cohort Batch" modal in Cohorts page                         |
|  2. File Selection: POC attaches candidates_sample.xlsx (or custom batch spreadsheet)  |
|  3. Cohort Creation: Backend creates Cohort record and obtains cohortId                 |
|  4. Upload Endpoint: Frontend posts file to POST /api/cohorts/{cohortId}/candidates/upld|
|  5. POI Extraction: Backend reads CANDIDATE NAME column, creates Candidate entities     |
|     with cohort_id foreign key, and updates cohort.candidateCount in DB                 |
|  6. Dynamic Isolation: Candidates immediately appear exclusively in this cohort batch   |
+-----------------------------------------------------------------------------------------+
```

---

## Section H: Evaluator Availability & Date-Overlap Logic

### The Overlap Formula
When a POC searches for available evaluators for an interview window $[\text{interviewFrom}, \text{interviewTo}]$, the system evaluates:

$$\text{overlaps} = \neg(\text{interviewFrom} > \text{unavailableTo}) \land \neg(\text{interviewTo} < \text{unavailableFrom})$$

### Concrete Examples

#### Scenario 1: Temporary Leave (No Overlap)
- **Evaluator Leave**: `2026-09-01` to `2026-09-10` (`isPermanent = false`)
- **Interview Window**: `2026-09-12` to `2026-09-15`
- **Calculation**: $\text{interviewFrom (09-12)} > \text{unavailableTo (09-10)} \implies \text{overlaps} = \text{false}$
- **Result**: Evaluator is **AVAILABLE** for this interview window.

#### Scenario 2: Temporary Leave (Overlap)
- **Evaluator Leave**: `2026-09-15` to `2026-09-25` (`isPermanent = false`)
- **Interview Window**: `2026-09-20` to `2026-09-22`
- **Calculation**: Dates fall inside leave window $\implies \text{overlaps} = \text{true}$
- **Result**: Evaluator is **UNAVAILABLE** and excluded from available pool.

#### Scenario 3: Permanent Inactive / Resigned
- **Evaluator Status**: `isPermanent = true` (e.g., "Left company", "Resigned")
- **Interview Window**: Any date range
- **Result**: Evaluator is **PERMANENTLY UNAVAILABLE** regardless of date parameters.

---

## Section I: REST API Catalog & HTTP Conventions

| Method | Endpoint | Controller | Purpose | Request Body / Params | Success | Error |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | `AuthController` | User authentication | `LoginRequest` (email, password) | **200 OK** | 400, 401 |
| **GET** | `/api/cohorts` | `CohortController` | List all cohorts | None | **200 OK** | 500 |
| **GET** | `/api/cohorts/{id}` | `CohortController` | Get cohort by ID | `@PathVariable Long id` | **200 OK** | 404 |
| **POST** | `/api/cohorts` | `CohortController` | Create cohort | `CohortDTO` | **201 CREATED** | 400 |
| **PUT** | `/api/cohorts/{id}` | `CohortController` | Update cohort | `CohortDTO` | **200 OK** | 400, 404 |
| **DELETE**| `/api/cohorts/{id}` | `CohortController` | Delete cohort | `@PathVariable Long id` | **204 NO CONTENT**| 404 |
| **GET** | `/api/cohorts/{cohortId}/candidates` | `CandidateController` | Get candidates in cohort | `@PathVariable Long cohortId` | **200 OK** | 404 |
| **POST** | `/api/cohorts/{cohortId}/candidates/upload` | `CandidateController` | Ingest Candidate Excel | MultipartFile `file` | **201 CREATED** | 400, 404 |
| **GET** | `/api/evaluators` | `EvaluatorController` | Filter evaluators | `vertical, domain, availability, interviewFrom, interviewTo` | **200 OK** | 400, 500 |
| **GET** | `/api/evaluators/{id}` | `EvaluatorController` | Get evaluator | `@PathVariable Long id` | **200 OK** | 404 |
| **PUT** | `/api/evaluators/{id}/status-reason` | `EvaluatorController` | Update status reason & Excel | `EvaluatorDTO` | **200 OK** | 400, 404 |
| **GET** | `/api/evaluators/export` | `EvaluatorController` | Download Master Excel | None | `byte[]` stream | **200 OK** | 404 |
| **POST** | `/api/evaluators/upload` | `EvaluatorController` | Upload Master Excel | MultipartFile `file` | **200 OK** | 400 |
| **GET** | `/api/cohorts/{cohortId}/shortlist` | `ShortlistController` | Get cohort shortlist | `@PathVariable Long cohortId` | **200 OK** | 404 |
| **POST** | `/api/cohorts/{cohortId}/evaluators/{evaluatorId}`| `ShortlistController` | Add to shortlist | `@PathVariable` IDs | **201 CREATED** | 404, 409 |
| **DELETE**| `/api/cohorts/{cohortId}/shortlist/{evaluatorId}` | `ShortlistController` | Remove from shortlist | `@PathVariable` IDs | **204 NO CONTENT**| 404 |
| **GET** | `/api/cohorts/{cohortId}/mappings` | `MappingController` | Get mappings by round | `@PathVariable Long cohortId`, `@RequestParam String round` | **200 OK** | 404 |
| **POST** | `/api/mappings` | `MappingController` | Create/update mapping | `MappingRequest` | **201 CREATED** | 404, 409 |
| **POST** | `/api/cohorts/{cohortId}/auto-map` | `MappingController` | Run auto-map algorithm | `@PathVariable Long cohortId`, `AutoMapRequest` | **201 CREATED** | 404, 409 |
| **PUT** | `/api/mappings/{id}/confirm` | `MappingController` | Confirm assignment | `@PathVariable Long id` | **200 OK** | 404, 409 |
| **GET** | `/api/reports/mappings` | `ReportController` | Audit trail list | None | **200 OK** | 500 |

---

## Section J: Business Logic & Rule Engine

Enforced in `MappingService.java` and `EvaluatorService.java`:

- **Rule 1 (Shortlist Prerequisite)**: An evaluator MUST be shortlisted in the cohort before being assigned to any candidate in that cohort.
- **Rule 2 (Availability Requirement)**: Evaluators marked `UNAVAILABLE` cannot be mapped.
- **Rule 3 (Interim-to-Final Isolation)**: If Evaluator A assessed Candidate X in the **Interim** round, Evaluator A is **strictly blocked** from assessing Candidate X in the **Final** round.
- **Rule 4 (Repeat Final Attempt Exclusion)**: If Candidate X fails Attempt 1 of their Final interview and retakes it in Attempt 2, the Attempt 1 evaluator is **permanently barred** from conducting the retake. A new evaluator is mandatory.
- **Rule 5 (Workload Balancing Auto-Map)**: The auto-mapping algorithm finds all eligible evaluators satisfying Rules 1–4 and assigns the evaluator who currently has the **lowest confirmed workload** for that round.
- **Rule 6 (Dynamic Date Overlap)**: Evaluators on temporary leave are automatically available for interview dates outside their leave window. Permanent inactive evaluators remain unavailable.
- **Rule 7 (Master Excel Two-Way Sync)**: Status reason updates in the UI update MySQL and write directly to the corresponding row in `uploads/master_evaluators.xlsx` by `empId`. Evaluators are never deleted.
- **Rule 8 (Cohort Candidate Strict Isolation)**: Candidates uploaded via Excel are strictly linked to their specific cohort ID and cannot be cross-mapped into other cohorts.

---

## Section K: Testing Suite & Verification

The project includes **30 automated backend tests** and full Angular production build verification:

- `CandidateServiceTest` (3 tests): Tests candidate Excel parsing, empty file validation, and cohort candidate counter updates.
- `CandidateControllerTest` (2 tests): Tests candidate listing and multipart Excel upload endpoints.
- `EvaluatorServiceTest` (8 tests): Tests date-overlap logic, temporary leave vs permanent inactive status, and input validation.
- `EvaluatorControllerTest` (5 tests): Tests evaluator filtering, status reason updates, and Excel export streams.
- `CohortControllerTest` (6 tests): Tests cohort CRUD operations and shortlist queries.
- `MappingControllerTest` (4 tests): Tests manual mapping, auto-mapping, and mapping confirmation.
- `SampleExcelGeneratorTest` (2 tests): Generates `master_evaluators_sample.xlsx` and `candidates_sample.xlsx`.
- **Frontend Verification**: `npm run build` generates production bundles with **0 errors and 0 warnings**.

---

## Section L: Team Member Responsibilities & Interview Q&A Guide

### Overview of Team Assignments

| Team Member | Primary Module | Secondary / Distributed Areas |
| :--- | :--- | :--- |
| **Ramya** | **Cohort Module** | Cohort Lifecycle, Status Transitions, Mapping Progress Calculation, Cohort UI & DTOs |
| **Nihar** | **Authentication / Login** | Spring Security 6, JWT Filter Chain, RBAC, BCrypt, AuthGuard, AuthInterceptor, Login UI |
| **Chavi** | **Evaluator Shortlist** | Cohort Shortlist Engine, Shortlist Modal, Mapping Business Rules (Rules 1, 3, 4, 5), Auto-Map Engine, Mapping UI |
| **Smitha** | **Evaluator Module** | Master Evaluator Pool, 2-Tier Filters, Dynamic Date-Overlap Engine, Leave & Status Reasons, Master Excel POI Sync |
| **Avinash** | **Candidate Module** | Candidate Isolation, Candidate Excel POI Ingestion, Sample Data Generator, Audit Reporting & Metrics |

---

### 1. Ramya &mdash; Cohort Module

#### Responsibilities
- End-to-end design and implementation of the Cohort Batch lifecycle (`Active`, `In Progress`, `Completed`, `Not Started`).
- Implementation of `CohortController`, `CohortService`, and `CohortRepository`.
- Construction of `CohortDTO` with validation constraints and model-to-DTO mapping logic.
- Built the Angular `CohortsComponent` (card grid, status badges, progress bars, search/filter, and modal dialogs).
- Calculated cohort-level mapping progress metrics (confirmed candidates vs total cohort candidate count).
- Implemented transactional cohort deletion with referential integrity verification.
- Authored backend unit and integration test suites for cohort management (`CohortControllerTest`).

#### Key Backend Classes & Interfaces
- `com.ems.controller.CohortController` &mdash; REST controller exposing cohort CRUD and summary endpoints.
- `com.ems.service.CohortService` &mdash; Business logic for cohort lifecycle, validation, and progress metric aggregation.
- `com.ems.repository.CohortRepository` &mdash; Spring Data JPA repository for cohort queries.
- `com.ems.entity.Cohort` &mdash; JPA entity mapping to `cohorts` database table.
- `com.ems.dto.CohortDTO` &mdash; Data transfer object carrying cohort metadata, POC details, and candidate counts.
- `com.ems.controller.CohortControllerTest` &mdash; Unit test suite verifying cohort API contracts.

#### Frontend Components & Services
- `src/app/pages/cohorts/cohorts.component.ts` &mdash; Cohort batch list controller, filter handlers, and modal logic.
- `src/app/pages/cohorts/cohorts.component.html` &mdash; Cohort card grid, progress trackers, and New Cohort modal template.
- `src/app/pages/cohorts/cohorts.component.css` &mdash; Card styling, status badge themes, and modal layout tokens.
- `src/app/services/cohort.service.ts` &mdash; Angular HTTP client service communicating with `/api/cohorts`.

#### Database Tables Used
- `cohorts` (Primary Key: `cohort_id`, Foreign Key: `poc_id` &rarr; `users.user_id`)
- `users` (Referenced for Point of Contact information)

#### REST APIs Handled
- `GET /api/cohorts` &mdash; Retrieve all cohorts with dynamic candidate counts and progress percentages.
- `GET /api/cohorts/{id}` &mdash; Retrieve detailed profile of a single cohort.
- `POST /api/cohorts` &mdash; Create a new cohort batch.
- `PUT /api/cohorts/{id}` &mdash; Update cohort parameters (name, dates, status).
- `DELETE /api/cohorts/{id}` &mdash; Delete a cohort batch.
- `GET /api/cohorts/{id}/summary` &mdash; Fetch cohort high-level statistics.

#### Module Workflow
1. The POC navigates to the Cohorts page (`/cohorts`).
2. Angular `CohortsComponent` triggers `CohortService.getCohorts()`, sending `GET /api/cohorts`.
3. `CohortService` queries `CohortRepository.findAll()`, maps entities to `CohortDTO`s, and computes real-time mapping completion percentages by querying confirmed records in `EvaluatorMappingRepository`.
4. When the POC clicks "+ New Cohort Batch" and submits the form, `CohortController.createCohort()` validates the input, attaches the authenticated POC, and persists the record into the `cohorts` table.

#### Inter-Module Connections
- **Nihar (Auth / User)**: Links the cohort's `poc_id` to the authenticated `User` entity created during login.
- **Avinash (Candidate Module)**: The `cohort_id` serves as the mandatory foreign key partition for candidate rosters.
- **Chavi (Evaluator Shortlist)**: The `cohort_id` establishes the context for shortlisting evaluators and executing interview mappings.
- **Smitha (Evaluator Module)**: Cohort interview start/end dates are matched against evaluator leave windows for availability calculations.

#### Likely Interview / Viva Questions & Answers

**Q1: How did you calculate the evaluator mapping progress bar for each cohort?**  
> *"In `CohortService.mapToDTO()`, I queried `EvaluatorMappingRepository` for all mappings in that cohort with status `'CONFIRMED'`. I counted the distinct candidate IDs mapped and divided that by the cohort's total candidate count (`candidateCount`). Multiplying by 100 yields the progress percentage, which binds directly to the Angular progress bar width style in `cohorts.component.html`."*

**Q2: What happens when a cohort is deleted?**  
> *"In `CohortService.deleteCohort()`, I implemented a transactional method with `@Transactional`. It first checks whether the cohort exists using `cohortRepository.findById()`, throwing a `ResourceNotFoundException` if absent. Before deletion, the service ensures that associated shortlists and candidates are handled gracefully according to database foreign key cascade configurations."*

**Q3: How are cohort statuses managed?**  
> *"Cohort status is represented as a string field (`Active`, `In Progress`, `Completed`, `Not Started`). When a cohort is created or updated, the status is validated and persisted. The frontend uses status mapping functions to display color-coded pill badges (`badge-success`, `badge-warning`, `badge-primary`)."*

---

### 2. Nihar &mdash; Authentication / Login Module

#### Responsibilities
- Architected the stateless authentication and security infrastructure using Spring Security 6 and JSON Web Tokens (JJWT).
- Built `AuthController`, `AuthService`, `JwtUtil`, and `JwtAuthenticationFilter`.
- Implemented BCrypt password hashing (`10` rounds) and Role-based Access Control (RBAC: `POC`, `PM`, `ADMIN`).
- Configured stateless session management, CORS configuration (`CorsConfig`), and CSRF bypass for REST.
- Developed the Angular `LoginComponent` with reactive form controls and error state handling.
- Built Angular `AuthService`, `AuthGuard` route security, and `AuthInterceptor` for automatic Bearer token injection.
- Handled security-level exceptions (`401 UNAUTHORIZED`, `403 FORBIDDEN`) in `GlobalExceptionHandler`.

#### Key Backend Classes & Interfaces
- `com.ems.config.SecurityConfig` &mdash; Spring Security 6 filter chain configuration, endpoint authorization rules, and session policy.
- `com.ems.config.CorsConfig` &mdash; Cross-Origin Resource Sharing configuration allowing Angular frontend origins.
- `com.ems.security.JwtUtil` &mdash; Utility for generating, signing (HMAC-SHA256), parsing, and validating JWT tokens.
- `com.ems.security.JwtAuthenticationFilter` &mdash; `OncePerRequestFilter` extracting Bearer token and populating `SecurityContextHolder`.
- `com.ems.security.CustomUserDetailsService` &mdash; Loads user credentials and granted authorities from `UserRepository`.
- `com.ems.controller.AuthController` &mdash; Endpoints for user authentication and profile retrieval.
- `com.ems.service.AuthService` &mdash; Business service orchestrating authentication, password hashing, and token dispatch.
- `com.ems.repository.UserRepository` &mdash; Spring Data JPA repository for user lookup by email.
- `com.ems.entity.User` & `com.ems.entity.Role` &mdash; User entity and role enumeration (`ROLE_POC`, `ROLE_PM`, `ROLE_ADMIN`).
- `com.ems.dto.LoginRequest` & `com.ems.dto.LoginResponse` &mdash; Payloads for login credentials and JWT token exchange.

#### Frontend Components & Services
- `src/app/pages/login/login.component.ts` &mdash; Reactive form handling, submit action, and authentication response handling.
- `src/app/pages/login/login.component.html` &mdash; Clean, modern executive login interface.
- `src/app/pages/login/login.component.css` &mdash; Card layout, typography, and responsive form styles.
- `src/app/services/auth.service.ts` &mdash; Manages token persistence in `localStorage`, user session state, and logout.
- `src/app/interceptors/auth.interceptor.ts` &mdash; Automatically attaches `Authorization: Bearer <token>` to outbound requests.
- `src/app/guards/auth.guard.ts` &mdash; Protects internal Angular routes (`/home`, `/cohorts`, `/mapping`, `/reports`).

#### Database Tables Used
- `users` (Primary Key: `user_id`, Unique Key: `email`, `password_hash`, `role`)

#### REST APIs Handled
- `POST /api/auth/login` &mdash; Validate credentials and return signed JWT token with user metadata.
- `POST /api/auth/register` &mdash; Register a new POC/PM user with BCrypt hashed password.
- `GET /api/auth/me` &mdash; Return profile information for the currently authenticated user.

#### Module Workflow
1. The user inputs their email and password into the Angular `LoginComponent`.
2. `AuthService.login()` sends a `POST` request to `/api/auth/login` with `LoginRequest`.
3. Backend `AuthService` delegates to Spring's `AuthenticationManager`, which invokes `CustomUserDetailsService` and verifies the password against `password_hash` using `BCryptPasswordEncoder`.
4. Upon successful authentication, `JwtUtil.generateToken()` constructs a signed HMAC-SHA256 JWT containing user claims and expiration time.
5. `AuthController` returns a `200 OK` with `LoginResponse` (token, email, fullName, role).
6. Angular `AuthService` saves the token to `localStorage` and redirects the user to the `/home` dashboard.
7. For all subsequent requests, `AuthInterceptor` appends `Authorization: Bearer <jwt>`, which `JwtAuthenticationFilter` intercepts, validates, and stores in `SecurityContextHolder`.

#### Inter-Module Connections
- **All Modules**: Protects all REST endpoints exposed by Ramya, Chavi, Smitha, and Avinash.
- **Ramya (Cohort Module)**: Supplies authenticated user details to assign the creating POC to new cohorts.
- **Frontend Navigation**: `AuthGuard` verifies active token presence before allowing route activation.

#### Likely Interview / Viva Questions & Answers

**Q1: How does your stateless JWT filter authenticate requests without relying on HTTP sessions?**  
> *"I configured Spring Security with `SessionCreationPolicy.STATELESS`. Every incoming HTTP request passes through `JwtAuthenticationFilter` (extending `OncePerRequestFilter`). It extracts the token from the `Authorization: Bearer <token>` header, validates the signature and expiration using `JwtUtil.validateToken()`, loads the user's details via `CustomUserDetailsService`, creates an authenticated `UsernamePasswordAuthenticationToken`, and sets it in Spring's `SecurityContextHolder`. This ensures zero server-side session memory consumption."*

**Q2: How is password security implemented in the backend?**  
> *"Passwords are never stored in plain text. I configured a `BCryptPasswordEncoder` bean with 10 salt rounds in `SecurityConfig`. When a user registers or is seeded, the raw password is automatically salted and hashed before persistence into the `users` table. During authentication, `AuthenticationManager` uses BCrypt's matching algorithm to verify incoming passwords against the stored hash."*

**Q3: How do `AuthGuard` and `AuthInterceptor` work together in Angular?**  
> *"In Angular 17, `AuthGuard` checks `authService.isLoggedIn()` by verifying the presence and non-expiration of the JWT token in `localStorage`. If absent, it redirects the browser to `/login`. Meanwhile, `AuthInterceptor` implements `HttpInterceptorFn`, cloning every outgoing `HttpRequest` to append the `Authorization: Bearer <token>` header so backend endpoints receive valid credentials automatically."*

---

### 3. Chavi &mdash; Evaluator Shortlist Module

#### Responsibilities
- Architected and implemented the Cohort Evaluator Shortlisting Engine connecting SMEs to specific batches.
- Built `ShortlistController`, `ShortlistService`, and `EvaluatorShortlistRepository`.
- Built the "Add to Shortlist" modal dialog on the Evaluator dashboard (`HomeComponent`).
- Implemented candidate-to-evaluator allocation state management and manual/automated mapping orchestration (`MappingController`, `MappingService`, `EvaluatorMappingRepository`).
- Implemented Core Business Rules:
  - **Rule 1 (Shortlist Prerequisite)**: An evaluator MUST be shortlisted in the cohort before any candidate assignment.
  - **Rule 3 (Interim vs Final Round Isolation)**: Prevents an evaluator who evaluated Candidate X in Interim from evaluating Candidate X in Final.
  - **Rule 4 (Repeat Final Attempt Exclusion)**: If Candidate X retakes Attempt 2 of a Final interview, the Attempt 1 evaluator is strictly barred.
  - **Rule 5 (Workload Balancing Algorithm)**: Auto-map assigns eligible shortlisted evaluators with the lowest confirmed workload.
- Developed the Angular `MappingComponent` with Interim/Final tabbed workspace, conflict warnings, and auto-mapping triggers.
- Authored unit and integration tests for shortlisting and mapping rules (`MappingControllerTest`).

#### Key Backend Classes & Interfaces
- `com.ems.controller.ShortlistController` &mdash; Endpoints for querying, adding, and removing cohort shortlist entries.
- `com.ems.service.ShortlistService` &mdash; Business logic managing cohort evaluator pools and duplicate validations.
- `com.ems.repository.EvaluatorShortlistRepository` &mdash; Repository with derived queries like `findByCohortCohortId()`.
- `com.ems.entity.EvaluatorShortlist` &mdash; JPA entity mapping to `evaluator_shortlist` table.
- `com.ems.controller.MappingController` &mdash; Endpoints for manual mapping, auto-mapping, and confirmation.
- `com.ems.service.MappingService` &mdash; Rule engine enforcing Rules 1–5 and workload-balanced candidate allocation.
- `com.ems.repository.EvaluatorMappingRepository` &mdash; Repository tracking candidate-evaluator assignment records.
- `com.ems.entity.EvaluatorMapping` &mdash; Entity mapping candidate, evaluator, cohort, round, attempt, and status.
- `com.ems.dto.MappingDTO` & `com.ems.dto.AutoMapRequest` &mdash; DTOs for mapping payloads and auto-map execution.
- `com.ems.controller.MappingControllerTest` &mdash; Unit test suite verifying mapping validation and auto-map algorithms.

#### Frontend Components & Services
- `src/app/pages/mapping/mapping.component.ts` &mdash; Mapping workspace controller, candidate-evaluator dropdowns, and round switcher.
- `src/app/pages/mapping/mapping.component.html` &mdash; Tabbed mapping tables, auto-map trigger button, and status indicators.
- `src/app/pages/mapping/mapping.component.css` &mdash; Layout styles, assignment chips, and action button styles.
- `src/app/services/mapping.service.ts` &mdash; Angular HTTP client service for mapping and auto-map APIs.
- Shortlist modal in `src/app/pages/home/home.component.ts` / `home.component.html` &mdash; Shortlisting dialog on the home page.

#### Database Tables Used
- `evaluator_shortlist` (Primary Key: `shortlist_id`, Foreign Keys: `cohort_id` &rarr; `cohorts`, `evaluator_id` &rarr; `evaluators`)
- `evaluator_mapping` (Primary Key: `mapping_id`, Foreign Keys: `cohort_id`, `candidate_id`, `evaluator_id`)

#### REST APIs Handled
- `GET /api/cohorts/{cohortId}/shortlist` &mdash; Retrieve all shortlisted evaluators for a cohort.
- `POST /api/cohorts/{cohortId}/evaluators/{evaluatorId}` &mdash; Add an evaluator to a cohort's shortlist.
- `DELETE /api/cohorts/{cohortId}/shortlist/{evaluatorId}` &mdash; Remove an evaluator from a cohort's shortlist.
- `GET /api/cohorts/{cohortId}/mappings` &mdash; Fetch mappings filtered by cohort and round (`INTERIM`, `FINAL`).
- `POST /api/mappings` &mdash; Create or update a candidate-evaluator assignment.
- `POST /api/cohorts/{cohortId}/auto-map` &mdash; Execute the automated workload-balanced mapping algorithm.
- `PUT /api/mappings/{id}/confirm` &mdash; Confirm a proposed candidate assignment.

#### Module Workflow
1. On the Evaluator dashboard (`/home`), the POC identifies an available SME and clicks "Add to Shortlist", selecting a cohort.
2. `ShortlistController.addToShortlist()` invokes `ShortlistService`, verifying the evaluator is not already shortlisted in that cohort, and persists the entry into `evaluator_shortlist`.
3. In the Mapping workspace (`/mapping`), the POC selects a cohort and round (e.g. `FINAL`, `Attempt 1`).
4. The POC can assign an evaluator manually or click "Auto-Map Candidates".
5. When executing auto-map, `MappingService.autoMapCandidates()`:
   - Fetches all candidates in the cohort and all shortlisted evaluators.
   - For each candidate, filters out evaluators who assessed them in the Interim round (Rule 3) or in Attempt 1 of the Final round (Rule 4).
   - From eligible candidates, selects the evaluator with the lowest active workload count (Rule 5).
   - Saves all assignments with status `DRAFT` or `CONFIRMED`.

#### Inter-Module Connections
- **Smitha (Evaluator Module)**: Shortlists evaluators from the master evaluator pool.
- **Ramya (Cohort Module)**: Associates shortlisted evaluators to specific cohort batches.
- **Avinash (Candidate Module)**: Maps shortlisted evaluators to batch candidates.

#### Likely Interview / Viva Questions & Answers

**Q1: Why is an evaluator shortlist table necessary instead of mapping directly from the master pool?**  
> *"A company-wide master roster may contain hundreds of evaluators across dozens of domains. However, a specific cohort (e.g. Java Fullstack Batch 10) only utilizes 5–10 approved SMEs. The `evaluator_shortlist` table isolates an approved pool for that batch. Rule 1 in `MappingService` verifies that an evaluator exists in `evaluator_shortlist` before permitting any manual or automated candidate assignment, ensuring strict operational governance."*

**Q2: How does the auto-mapping algorithm prevent evaluator repetition between Interim and Final rounds?**  
> *"In `MappingService.autoMapCandidates()`, when mapping for the `FINAL` round, the engine queries `evaluatorMappingRepository.findByCandidateCandidateIdAndRound(candidateId, 'INTERIM')`. If an Interim evaluator is found, their ID is added to an exclusion set for that candidate. The algorithm also queries previous final attempts to enforce Rule 4. The assignment candidate pool is then filtered against this exclusion set, guaranteeing zero repetition."*

**Q3: How does the workload balancing logic work?**  
> *"For each eligible evaluator who passes Rules 1–4, `MappingService` calculates their current confirmed assignment count for that round. It sorts eligible evaluators in ascending order of workload and assigns the evaluator with the minimum count (`minBy(Comparator.comparingInt(workloadMap::get))`), ensuring balanced distribution across all SMEs."*

---

### 4. Smitha &mdash; Evaluator Module

#### Responsibilities
- Architected the Master Evaluator Pool management and 2-tier search & filtering engine.
- Built `EvaluatorController`, `EvaluatorService`, `ExcelUploadService`, and `EvaluatorRepository`.
- Implemented dynamic date-overlap availability calculation (evaluating temporary leave windows `unavailableFrom`–`unavailableTo` against cohort interview windows `interviewFrom`–`interviewTo`).
- Implemented Evaluator Status Reason and Permanent Exit logic (`isPermanent`, `statusReason`).
- Implemented two-way row-level Master Excel spreadsheet synchronization using Apache POI (`ExcelUploadService`, `WorkbookFactory`).
- Built the Master Excel export generator (`/api/evaluators/export`) and sample dataset generator (`master_evaluators_sample.xlsx`).
- Developed the Angular `HomeComponent` Evaluator Dashboard with filter controls and leave status editor modal.
- Authored backend unit and integration test suites for evaluator filtering, date overlap, and status updates (`EvaluatorServiceTest`, `EvaluatorControllerTest`).

#### Key Backend Classes & Interfaces
- `com.ems.controller.EvaluatorController` &mdash; Endpoints for evaluator search, filtering, status updates, and Excel export.
- `com.ems.service.EvaluatorService` &mdash; Business logic for multi-parameter filtering, date-overlap evaluation, and leave calculations.
- `com.ems.service.ExcelUploadService` &mdash; Apache POI service reading and writing rows in `uploads/master_evaluators.xlsx` matching `empId`.
- `com.ems.repository.EvaluatorRepository` &mdash; Spring Data JPA repository for evaluator queries.
- `com.ems.entity.Evaluator` &mdash; JPA entity mapping to `evaluators` table.
- `com.ems.dto.EvaluatorDTO` &mdash; DTO transferring evaluator metadata, leave windows, and dynamic availability flags.
- `com.ems.service.EvaluatorServiceTest` & `com.ems.controller.EvaluatorControllerTest` &mdash; Unit test suites covering date overlap and status logic.

#### Frontend Components & Services
- `src/app/pages/home/home.component.ts` &mdash; Evaluator dashboard controller, 2-tier filter logic, and status reason dialog.
- `src/app/pages/home/home.component.html` &mdash; Evaluator roster table, filter bar, date pickers, and status editor modal.
- `src/app/pages/home/home.component.css` &mdash; Filter console styles, availability badge colors, and table styling.
- `src/app/services/evaluator.service.ts` &mdash; Angular HTTP client service communicating with `/api/evaluators`.

#### Database Tables Used
- `evaluators` (Primary Key: `evaluator_id`, Unique Keys: `emp_id`, `email`)

#### REST APIs Handled
- `GET /api/evaluators` &mdash; Filter evaluators with search term, vertical, domain, availability, and interview date range.
- `GET /api/evaluators/{id}` &mdash; Fetch detailed profile for an individual evaluator.
- `PUT /api/evaluators/{id}/status-reason` &mdash; Update evaluator leave/permanent exit status and sync to Master Excel.
- `POST /api/evaluators/upload` &mdash; Ingest Master Evaluator roster spreadsheet (`.xlsx`).
- `GET /api/evaluators/export` &mdash; Download the live Master Evaluator Excel spreadsheet.

#### Module Workflow
1. The POC opens the Home page (`/home`) and views the Master Evaluator roster.
2. The POC filters by domain (e.g. `Cloud DevOps`), vertical (e.g. `Healthcare`), and enters interview dates (`2026-05-10` to `2026-05-15`).
3. `EvaluatorService.getEvaluators()` applies filters in memory and evaluates availability:
   - If `isPermanent == true` (e.g. Resigned / Inactive), status is permanently `UNAVAILABLE`.
   - If `unavailableFrom` and `unavailableTo` are set, the service checks date overlap: `!interviewFrom.isAfter(unavailableTo) && !interviewTo.isBefore(unavailableFrom)`. If there is no overlap, the evaluator is dynamically marked `AVAILABLE`.
4. When the POC updates an evaluator's status reason in the modal, `EvaluatorService.updateStatusReason()` updates MySQL and calls `ExcelUploadService.updateEvaluatorStatusInExcel()`, which uses Apache POI to find the row matching `empId` in `uploads/master_evaluators.xlsx` and updates the status cells without modifying other data.

#### Inter-Module Connections
- **Chavi (Evaluator Shortlist)**: Supplies the pool of active, verified evaluators for cohort shortlisting.
- **Ramya (Cohort Module)**: Evaluator leave windows are cross-referenced with cohort interview dates.
- **Nihar (Auth / Security)**: All evaluator queries and status update endpoints are secured via JWT.

#### Likely Interview / Viva Questions & Answers

**Q1: How does your dynamic date-overlap calculation differentiate between temporary leave and permanent inactive status?**  
> *"In `EvaluatorService.calculateDynamicAvailability()`, if an evaluator has `isPermanent == true` (e.g. Resigned or Long-term Sabbatical), they are strictly marked `UNAVAILABLE` regardless of the date range. If `isPermanent == false` and the evaluator has a temporary leave window (`unavailableFrom` to `unavailableTo`), the service checks whether the interview window overlaps using standard interval math: `!interviewFrom.isAfter(unavailableTo) && !interviewTo.isBefore(unavailableFrom)`. If there is no overlap, the evaluator is dynamically marked `AVAILABLE` for that cohort's interview window."*

**Q2: How does Apache POI update the Master Excel file without corrupting other rows?**  
> *"In `ExcelUploadService.updateEvaluatorStatusInExcel()`, we open the master workbook using `WorkbookFactory.create()`. We iterate through the rows, inspect Column 0 for the evaluator's unique `empId`, and update only the relevant status cells (`STATUS_REASON`, `PERMANENT`, `UNAVAILABLE_FROM`, `UNAVAILABLE_TO`, `AVAILABILITY_STATUS`). We preserve all existing cell styles, font formatting, headers, and adjacent evaluator records, then write back to disk via `FileOutputStream`."*

**Q3: Why must evaluators never be deleted from the database or master Excel?**  
> *"In enterprise talent operations, the Master Excel file is an official roster containing past evaluation records and employee history. Deleting an evaluator breaks historical mapping integrity and audit trails. Instead, evaluators who leave the organization are marked with `isPermanent = true` and a status reason (e.g. `'Resigned'`), preventing them from being shortlisted in new cohorts while preserving historical data."*

---

### 5. Avinash &mdash; Candidate Module

#### Responsibilities
- Architected the Candidate entity schema, cohort candidate isolation, and candidate persistence.
- Implemented `CandidateController`, `CandidateService`, and `CandidateRepository`.
- Built the Candidate Excel POI parser ingesting batch spreadsheets (`.xlsx`) during cohort creation.
- Dynamically synchronized candidate counts with the `Cohort` entity upon upload and deletion.
- Developed the drag-and-drop Candidate Excel dropzone within the New Cohort Batch modal (`CohortsComponent`).
- Created and maintained sample test datasets (`sample-data/candidates_sample.xlsx` and `sample-data/README.md`).
- Implemented the Audit & Analytics Dashboard and Reporting Module (`ReportController`, `ReportService`, `ReportsComponent`, CSV export).
- Authored backend unit and integration test suites for candidate upload and Excel parsing (`CandidateServiceTest`, `CandidateControllerTest`, `SampleExcelGeneratorTest`).

#### Key Backend Classes & Interfaces
- `com.ems.controller.CandidateController` &mdash; Endpoints for candidate listing and multipart Excel upload.
- `com.ems.service.CandidateService` &mdash; Business logic for candidate persistence and Apache POI Excel parsing.
- `com.ems.repository.CandidateRepository` &mdash; Spring Data JPA repository for cohort-scoped candidate queries.
- `com.ems.entity.Candidate` &mdash; JPA entity mapping to `candidates` table.
- `com.ems.dto.CandidateDTO` &mdash; Data transfer object carrying candidate details, domain, and cohort ID.
- `com.ems.controller.ReportController` & `com.ems.service.ReportService` &mdash; Audit reporting and summary metrics.
- `com.ems.service.CandidateServiceTest` & `com.ems.controller.CandidateControllerTest` &mdash; Unit test suites covering candidate operations and Excel parsing.
- `com.ems.util.SampleExcelGeneratorTest` &mdash; Automated test generating realistic `candidates_sample.xlsx` test files.

#### Frontend Components & Services
- `src/app/services/candidate.service.ts` &mdash; Angular HTTP client service for candidate endpoints and multipart upload.
- `src/app/pages/reports/reports.component.ts` &mdash; Audit reporting controller and CSV export triggers.
- `src/app/pages/reports/reports.component.html` &mdash; Mapping audit table, round breakdown, and metrics summary.
- `src/app/pages/reports/reports.component.css` &mdash; Report table styling and summary card layouts.
- `src/app/services/report.service.ts` &mdash; Angular service communicating with `/api/reports`.
- Candidate dropzone UI in `src/app/pages/cohorts/cohorts.component.html` &mdash; Excel file upload area in the cohort modal.

#### Database Tables Used
- `candidates` (Primary Key: `candidate_id`, Foreign Key: `cohort_id` &rarr; `cohorts.cohort_id`)
- `cohorts` (Referenced for candidate count synchronization)

#### REST APIs Handled
- `GET /api/cohorts/{cohortId}/candidates` &mdash; List all candidates strictly belonging to a cohort.
- `POST /api/cohorts/{cohortId}/candidates/upload` &mdash; Ingest Candidate Excel file (`.xlsx`) via MultipartFile.
- `POST /api/candidates` &mdash; Create an individual candidate record manually.
- `GET /api/reports/mappings` &mdash; Retrieve the full mapping audit trail across all cohorts.
- `GET /api/reports/summary` &mdash; Get aggregate mapping and evaluation statistics.

#### Module Workflow
1. When creating a new cohort batch in the UI, the POC attaches a Candidate Excel file (`.xlsx`).
2. Upon submitting the cohort creation form, the frontend receives the new `cohortId` and calls `CandidateService.uploadCandidatesFromExcel()`, sending the file via `POST /api/cohorts/{cohortId}/candidates/upload`.
3. `CandidateService` opens the spreadsheet via Apache POI `WorkbookFactory`, validates headers (`CANDIDATE NAME`, `CANDIDATE EMAIL`, `DOMAIN`, `VERTICAL`, `BRANCH / LOCATION`), parses candidate rows, sets `candidate.setCohort(cohort)`, and persists the records via `candidateRepository.saveAll()`.
4. `CandidateService` updates `cohort.setCandidateCount()` to match the number of parsed candidates.
5. In the Reports dashboard (`/reports`), the POC views live candidate mapping statistics and downloads CSV audit reports.

#### Inter-Module Connections
- **Ramya (Cohort Module)**: Links every candidate to Ramya's cohort entity and updates the cohort's candidate counter.
- **Chavi (Evaluator Shortlist)**: Candidate records are the targets mapped to shortlisted evaluators in the mapping workspace.
- **Nihar (Auth / Security)**: Candidate upload and report endpoints require JWT authentication.

#### Likely Interview / Viva Questions & Answers

**Q1: How are candidates ingested from Excel when creating a new cohort?**  
> *"When a POC creates a cohort and attaches an Excel file, the frontend invokes `POST /api/cohorts/{cohortId}/candidates/upload`. In `CandidateService.uploadCandidatesFromExcel()`, Apache POI parses the sheet, dynamically locates the `CANDIDATE NAME` header column index, maps each row to a `Candidate` entity, binds the entity to the given `cohortId`, performs a batch insert via `candidateRepository.saveAll()`, and updates the cohort's `candidateCount` in the database."*

**Q2: How do you enforce candidate isolation between cohorts?**  
> *"Candidates belong strictly to the cohort they were uploaded into via a non-nullable foreign key `cohort_id` in the `candidates` table. All candidate queries use `candidateRepository.findByCohortCohortId(cohortId)`. Candidates from Batch A cannot be viewed, shortlisted, or mapped into Batch B."*

**Q3: How do you validate uploaded Candidate Excel files against malformed data?**  
> *"I validate that the uploaded `MultipartFile` is non-empty, has a valid `.xlsx`/`.xls` content type, contains a recognizable header row with `CANDIDATE NAME`, and contains at least one non-empty candidate data row. If any validation fails, the service throws an `IllegalArgumentException`, which `GlobalExceptionHandler` converts to an HTTP `400 BAD_REQUEST` with a detailed error message."*

---

### "If the Interviewer Asks: Explain the Complete Project (1–2 Minutes)"

> *"The **Evaluator Mapping System (EMS)** is an enterprise talent operations web application built with Spring Boot 3, Angular 17, and MySQL 8. It automates and enforces governance over how Subject Matter Expert (SME) evaluators are shortlisted, verified for availability, and assigned to student candidates across Interim and Final interview rounds.
>
> In our architecture, POCs authenticate via stateless JWT tokens. They manage Master Evaluator rosters uploaded via Excel, where availability is calculated dynamically based on scheduled interview date ranges versus temporary leave windows or permanent exits.
>
> When creating training cohorts, POCs upload candidate rosters via Excel, and candidates are strictly isolated to their batch. POCs shortlist approved evaluators for each cohort. When assigning evaluators, our business rule engine enforces strict compliance: preventing the same evaluator from assessing a candidate in both Interim and Final rounds, and requiring a mandatory new evaluator if a candidate retakes a failed final attempt.
>
> The system also includes a one-click workload balancing auto-map algorithm, audit reporting with CSV export, and two-way Excel synchronization using Apache POI. We have verified the system with 30 backend unit/integration tests and automated Angular production builds."*

---

### "If the Interviewer Asks: What Was YOUR Contribution?"

#### For Ramya:
> *"I was responsible for the **Cohort Module**. I designed and implemented the complete cohort batch lifecycle (`Active`, `In Progress`, `Completed`, `Not Started`), built `CohortController`, `CohortService`, and `CohortRepository`, developed the Angular `CohortsComponent` with dynamic mapping progress bars, and integrated cohort lifecycle management with validation rules."*

#### For Nihar:
> *"I was responsible for the **Authentication / Login Module**. I implemented stateless JWT authentication using Spring Security 6, built `JwtAuthenticationFilter` and `JwtUtil` claims handling, configured role-based access control (`POC`, `PM`, `ADMIN`), hashed passwords with BCrypt, and implemented the Angular `LoginComponent`, `AuthGuard`, and `AuthInterceptor`."*

#### For Chavi:
> *"I was responsible for the **Evaluator Shortlist Module**. I built the cohort-specific shortlisting engine (`ShortlistController`, `ShortlistService`, `EvaluatorShortlistRepository`), implemented the shortlist modal in the UI, and orchestrated candidate-to-evaluator allocation state including business rules for Interim-to-Final evaluator isolation, attempt retake exclusion, and workload balancing auto-mapping."*

#### For Smitha:
> *"I was responsible for the **Evaluator Module**. I built the master evaluator pool management and 2-tier search/filter console, engineered the dynamic interview date-overlap availability engine for temporary leave windows, implemented permanent inactive status handling, and built two-way row-level Master Excel synchronization using Apache POI."*

#### For Avinash:
> *"I was responsible for the **Candidate Module**. I designed the candidate entity and cohort isolation architecture, built the Apache POI Candidate Excel upload parser (`POST /api/cohorts/{cohortId}/candidates/upload`), synchronized cohort candidate counters, implemented candidate API endpoints and tests, and built the Audit & Analytics reporting module."*

---

## Section M: Step-by-Step Manual Recreation Plan (16 Stages)

To rebuild this application from scratch, follow this exact sequence:

1. **Stage 1 &mdash; MySQL Database Schema**: Create `evaluator_mapping_db` and execute `database/schema.sql` (6 tables with foreign keys and unique constraints).
2. **Stage 2 &mdash; Spring Boot Maven Setup**: Initialize Spring Boot 3 with Web, JPA, Security, MySQL Connector, Lombok, POI, Validation, JJWT.
3. **Stage 3 &mdash; JPA Entities**: Create `User`, `Cohort`, `Candidate`, `Evaluator`, `EvaluatorShortlist`, `EvaluatorMapping`.
4. **Stage 4 &mdash; Spring Data JPA Repositories**: Create repositories for each entity with custom derived finders.
5. **Stage 5 &mdash; DTOs**: Create `CohortDTO`, `CandidateDTO`, `EvaluatorDTO`, `MappingDTO`, `LoginRequest`, `LoginResponse`, `ApiResponse`, `ErrorResponse`.
6. **Stage 6 &mdash; Exception Handling**: Create `ResourceNotFoundException`, `BusinessRuleException`, and `@RestControllerAdvice GlobalExceptionHandler`.
7. **Stage 7 &mdash; Security & JWT**: Implement `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`, `CorsConfig`, and `CustomUserDetailsService`.
8. **Stage 8 &mdash; Business Services**: Implement `CohortService`, `CandidateService` (with Excel parser), `EvaluatorService` (with date overlap), `ExcelUploadService` (POI sync), `ShortlistService`, `MappingService` (rules & auto-map), and `AuthService`.
9. **Stage 9 &mdash; REST Controllers**: Implement `AuthController`, `CohortController`, `CandidateController`, `EvaluatorController`, `ShortlistController`, `MappingController`, `ReportController`.
10. **Stage 10 &mdash; Backend Testing**: Write tests for controllers and services covering all business rules and Excel ingestion.
11. **Stage 11 &mdash; Angular Setup & Models**: Initialize Angular standalone project, configure routing, and create TypeScript models mirroring DTOs.
12. **Stage 12 &mdash; Angular Services & Interceptors**: Create `AuthService`, `AuthInterceptor`, `AuthGuard`, `CohortService`, `CandidateService`, `EvaluatorService`, `MappingService`, `ReportService`.
13. **Stage 13 &mdash; Design System & Global Styles**: Setup `styles.css` with CSS custom properties, button hierarchy, tables, and modal overlay tokens.
14. **Stage 14 &mdash; Angular Pages**: Build `LoginComponent`, `HomeComponent` (evaluator dashboard), `CohortsComponent` (with candidate Excel dropzone), `MappingComponent` (interim/final tabs), `ReportsComponent`.
15. **Stage 15 &mdash; Sample Datasets**: Generate `master_evaluators_sample.xlsx` and `candidates_sample.xlsx` in `sample-data/`.
16. **Stage 16 &mdash; End-to-End Verification**: Run `mvn test` (30/30 passed) and `ng build` (0 warnings, 0 errors). Test all workflows in browser.
