# Evaluator Mapping System (EMS) — Complete Developer & Project Guide

> **Target Audience:** Developers familiar with core Java, Spring Boot, SQL, TypeScript, and Angular who want to understand the exact architecture, flow, and implementation details of this project in order to manually recreate it from scratch.

---

## Table of Contents
1. [Section A: Overall Project Flow](#section-a-overall-project-flow)
2. [Section B: Recommended Reading Order](#section-b-recommended-reading-order)
3. [Section C: Backend Architecture & Class Catalog](#section-c-backend-architecture--class-catalog)
4. [Section D: REST API Map & HTTP Status Conventions](#section-d-rest-api-map--http-status-conventions)
5. [Section E: Business Logic & Rule Engine](#section-e-business-logic--rule-engine)
6. [Section F: Authentication & JWT Security Flow](#section-f-authentication--jwt-security-flow)
7. [Section G: Frontend Architecture & Component Flow](#section-g-frontend-architecture--component-flow)
8. [Section H: Database Schema & Entity Relationships](#section-h-database-schema--entity-relationships)
9. [Section I: Mentor Q&A — 15 Practical Project Questions](#section-i-mentor-qa--15-practical-project-questions)
10. [Section J: Step-by-Step Manual Recreation Plan (14 Stages)](#section-j-step-by-step-manual-recreation-plan-14-stages)

---

## Section A: Overall Project Flow

The Evaluator Mapping System is built upon a standard client-server architecture. Here is an end-to-end trace of a user action (e.g., manually assigning an evaluator to a candidate):

```
+-----------------------------------------------------------------------------------------+
|                                    1. USER BROWSER                                      |
|  User selects an Evaluator from dropdown in Mapping Component and clicks "Reassign"     |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                               2. ANGULAR FRONTEND LAYER                                 |
|  • Component: MappingComponent (frontend/src/app/pages/mapping/mapping.component.ts)    |
|  • Service:   MappingService   (frontend/src/app/services/mapping.service.ts)           |
|  • Interceptor: AuthInterceptor adds "Authorization: Bearer <jwt>"                      |
|  • HTTP Call: POST /api/mappings with payload { cohortId, candidateId, evaluatorId... }|
+-------------------------------------------+---------------------------------------------+
                                            | (HTTP Request via Network)
                                            v
+-----------------------------------------------------------------------------------------+
|                             3. SPRING BOOT SECURITY & FILTER                            |
|  • JwtAuthenticationFilter: Extracts token, validates claims via JwtUtil,               |
|    loads UserPrincipal into SecurityContextHolder.                                      |
|  • CorsConfig / SecurityConfig: Validates origin, allows authorized endpoint.           |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                               4. REST CONTROLLER LAYER                                  |
|  • MappingController (backend/src/main/java/com/ems/controller/MappingController.java)  |
|  • Method: createManualMapping(@RequestBody MappingRequest request)                     |
|  • Returns: ResponseEntity<MappingDTO> with HTTP 201 CREATED                            |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                                 5. SERVICE / BUSINESS LAYER                             |
|  • MappingService (backend/src/main/java/com/ems/service/MappingService.java)           |
|  • Executes validateMappingRules():                                                     |
|    - Evaluator in cohort shortlist?                                                     |
|    - Evaluator status is AVAILABLE?                                                     |
|    - Not used in candidate's Interim round if round is FINAL?                           |
|    - Not used in previous candidate Final attempts if round is FINAL?                   |
|  • If invalid: Throws BusinessRuleException -> Caught by GlobalExceptionHandler (409)  |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                              6. DATA ACCESS / REPOSITORY LAYER                          |
|  • EvaluatorMappingRepository, CohortRepository, CandidateRepository, EvaluatorRepository|
|  • Spring Data JPA executes SQL queries against MySQL database.                         |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                                  7. DATABASE (MySQL)                                    |
|  • Database: evaluator_mapping_db                                                       |
|  • Table: evaluator_mapping                                                             |
|  • Row inserted/updated with status 'SUGGESTED' or 'CONFIRMED'                          |
+-------------------------------------------+---------------------------------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------------+
|                             8. RESPONSE BACK TO CLIENT                                  |
|  • Service maps Entity -> MappingDTO                                                    |
|  • Controller returns ResponseEntity.status(HttpStatus.CREATED).body(mappingDTO)        |
|  • Angular Service receives Observable<Mapping>, updates component view reactively    |
+-----------------------------------------------------------------------------------------+
```

---

## Section B: Recommended Reading Order

When studying or recreating this project, read files in this exact sequence:

1. **`database/schema.sql`**
   - *What to understand:* The 6 core tables (`users`, `cohorts`, `candidates`, `evaluators`, `evaluator_shortlist`, `evaluator_mapping`), their data types, foreign keys, and unique constraints.
   - *Why now:* All business logic and domain modeling rely on the underlying relational data structure.

2. **Backend Domain Entities (`com.ems.entity.*`)**
   - *Files:* `User.java`, `Cohort.java`, `Candidate.java`, `Evaluator.java`, `EvaluatorShortlist.java`, `EvaluatorMapping.java`.
   - *What to understand:* JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@JoinColumn`).
   - *Why now:* Bridges the database schema to Java objects.

3. **Backend Repositories (`com.ems.repository.*`)**
   - *Files:* `UserRepository.java`, `CohortRepository.java`, `CandidateRepository.java`, `EvaluatorRepository.java`, `EvaluatorShortlistRepository.java`, `EvaluatorMappingRepository.java`.
   - *What to understand:* Spring Data JPA derived query methods (e.g., `findByCohortCohortId`, `findByCandidateCandidateId`).
   - *Why now:* Defines how data is retrieved from database tables without writing raw SQL.

4. **Data Transfer Objects (`com.ems.dto.*`) & Exceptions (`com.ems.exception.*`)**
   - *Files:* `MappingDTO.java`, `CohortDTO.java`, `EvaluatorDTO.java`, `CandidateDTO.java`, `LoginRequest.java`, `LoginResponse.java`, `ApiResponse.java`, `ErrorResponse.java`, `BusinessRuleException.java`, `ResourceNotFoundException.java`, `GlobalExceptionHandler.java`.
   - *What to understand:* Decoupling API request/response payloads from JPA entity models and centralizing error formatting.
   - *Why now:* Services transform entities to DTOs and raise custom exceptions.

5. **Backend Business Services (`com.ems.service.*`)**
   - *Files:* `MappingService.java`, `ShortlistService.java`, `CohortService.java`, `EvaluatorService.java`, `CandidateService.java`, `ExcelUploadService.java`, `AuthService.java`, `CustomUserDetailsService.java`.
   - *What to understand:* Core business rules (Interim-Final evaluator isolation, attempt exclusion, availability checks), Apache POI Excel parsing, and `@Transactional` boundaries.
   - *Why now:* Heart of the application logic.

6. **Backend Security & Config (`com.ems.config.*`)**
   - *Files:* `JwtUtil.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java`, `CorsConfig.java`, `DataSeeder.java`.
   - *What to understand:* Stateless JWT authentication, password hashing with `BCryptPasswordEncoder`, and test seed data initialization.
   - *Why now:* Secures REST endpoints before exposing them to the web layer.

7. **REST API Controllers (`com.ems.controller.*`) & Tests (`src/test/java/com/ems/controller/*`)**
   - *Files:* `AuthController.java`, `CohortController.java`, `CandidateController.java`, `EvaluatorController.java`, `MappingController.java`, `ShortlistController.java`, `ReportController.java`.
   - *What to understand:* `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PathVariable`, `@RequestBody`, `@RequestParam`, `ResponseEntity` with explicit HTTP status codes (200, 201, 204, 404, 409).
   - *Why now:* Exposes backend services as clean RESTful endpoints.

8. **Frontend Core & Models (`frontend/src/app/models/*`, `guards/*`, `interceptors/*`)**
   - *Files:* `user.model.ts`, `cohort.model.ts`, `candidate.model.ts`, `evaluator.model.ts`, `mapping.model.ts`, `auth.guard.ts`, `auth.interceptor.ts`.
   - *What to understand:* TypeScript interfaces mirroring backend DTOs, route protection, and automatic JWT token attachment.

9. **Frontend Services (`frontend/src/app/services/*`)**
   - *Files:* `auth.service.ts`, `cohort.service.ts`, `evaluator.service.ts`, `mapping.service.ts`, `report.service.ts`.
   - *What to understand:* Reactive HTTP client calls returning RxJS `Observable` streams.

10. **Frontend UI Pages & Routing (`frontend/src/app/pages/*`, `app.routes.ts`, `app.component.*`)**
    - *Files:* `login/`, `home/`, `cohorts/`, `mapping/`, `reports/`, `app.routes.ts`, `app.component.html`, `app.component.css`.
    - *What to understand:* Standalone Angular components, Reactive/Template forms, two-way data binding, tab navigation, and UI rule feedback.

---

## Section C: Backend Architecture & Class Catalog

| File / Class | Package | Purpose | What to Understand |
| :--- | :--- | :--- | :--- |
| **`User.java`** | `com.ems.entity` | JPA Entity for users table | Contains `userId`, `name`, `email`, `passwordHash`, `role` (POC/Admin). |
| **`Cohort.java`** | `com.ems.entity` | JPA Entity for cohorts table | Represents a training batch with `cohortId`, `cohortName`, `batchCode`, `poc`, `startDate`, `status`. |
| **`Candidate.java`** | `com.ems.entity` | JPA Entity for candidates | Represents trainees mapped to a specific `Cohort`. |
| **`Evaluator.java`** | `com.ems.entity` | JPA Entity for evaluators | Represents SME evaluators with `empId`, `name`, `vertical`, `domain`, `availabilityStatus`, and date ranges. |
| **`EvaluatorShortlist.java`** | `com.ems.entity` | JPA Entity for shortlist join table | Represents an evaluator assigned/shortlisted to a particular cohort. |
| **`EvaluatorMapping.java`** | `com.ems.entity` | JPA Entity for mapping table | Maps candidate -> evaluator for a specific `round` (INTERIM/FINAL) and `attempt`. Status: `SUGGESTED` or `CONFIRMED`. |
| **`MappingDTO.java`** | `com.ems.dto` | Unified DTO for mapping records | Flattens entity graph for frontend; includes `interimEvaluatorName`, `ruleWarning`, and `evaluatorAvailability`. |
| **`CohortDTO.java`** | `com.ems.dto` | DTO for cohort list and creation | Contains batch summary plus dynamically calculated `evaluatorsMapped` count. |
| **`EvaluatorDTO.java`** | `com.ems.dto` | DTO for evaluator profiles | Transports evaluator data and availability update payloads. |
| **`CandidateDTO.java`** | `com.ems.dto` | DTO for candidate info | Holds `candidateId`, `cohortId`, `candidateName`. |
| **`LoginRequest.java` / `LoginResponse.java`** | `com.ems.dto` | Auth DTOs | Request carries email/password; Response returns JWT token, userId, name, email, role. |
| **`ApiResponse.java` / `ErrorResponse.java`** | `com.ems.dto` | Standardized API / Error responses | Uniform payload for success messages and structured error feedback (`timestamp`, `status`, `error`, `message`, `path`). |
| **`UserRepository.java`** | `com.ems.repository` | Spring Data Repository | Look up user by email for authentication (`findByEmail`). |
| **`CohortRepository.java`** | `com.ems.repository` | Spring Data Repository | CRUD queries on `cohorts` table. |
| **`CandidateRepository.java`** | `com.ems.repository` | Spring Data Repository | Custom query `findByCohortCohortId(Long cohortId)`. |
| **`EvaluatorRepository.java`** | `com.ems.repository` | Spring Data Repository | Queries for distinct verticals/domains and evaluator lookup. |
| **`EvaluatorShortlistRepository.java`** | `com.ems.repository` | Spring Data Repository | Shortlist queries (`findByCohortCohortId`, `findByCohortCohortIdAndEvaluatorEvaluatorId`). |
| **`EvaluatorMappingRepository.java`** | `com.ems.repository` | Spring Data Repository | Queries mappings by cohort, candidate, round, and calculates evaluator workload. |
| **`MappingService.java`** | `com.ems.service` | Core mapping & rule engine | Implements `validateMappingRules()`, manual mapping, auto-map with workload balancing, and mapping confirmation. |
| **`ShortlistService.java`** | `com.ems.service` | Evaluator shortlist service | Handles adding/removing evaluators to/from cohort shortlists with validation. |
| **`CohortService.java`** | `com.ems.service` | Cohort management service | Cohort creation, retrieval, updates, and live candidate/mapped metric calculations. |
| **`EvaluatorService.java`** | `com.ems.service` | Evaluator management service | Evaluator filtering by vertical/domain/availability, availability toggle, and profile retrieval. |
| **`CandidateService.java`** | `com.ems.service` | Candidate management service | Manages student records assigned to cohorts. |
| **`ExcelUploadService.java`** | `com.ems.service` | Apache POI Excel parser | Parses uploaded `.xlsx`/`.xls` spreadsheets to bulk insert/update evaluator rosters. |
| **`AuthService.java`** | `com.ems.service` | Authentication service | Authenticates credentials with `AuthenticationManager` and issues signed JWT tokens. |
| **`CustomUserDetailsService.java`** | `com.ems.service` | Spring Security UserDetailsService | Loads user details from database by email. |
| **`AuthController.java`** | `com.ems.controller` | Auth REST Controller | `@PostMapping("/api/auth/login")` -> Authenticates and returns JWT. |
| **`CohortController.java`** | `com.ems.controller` | Cohort REST Controller | CRUD operations on cohorts with 200, 201, 204, 404 responses. |
| **`CandidateController.java`** | `com.ems.controller` | Candidate REST Controller | CRUD operations on candidates. |
| **`EvaluatorController.java`** | `com.ems.controller` | Evaluator REST Controller | Evaluator retrieval, filters, availability update, and Excel upload. |
| **`MappingController.java`** | `com.ems.controller` | Mapping REST Controller | Endpoints for retrieving mappings, manual assignment (201), auto-mapping (201), and confirmation (200). |
| **`ShortlistController.java`** | `com.ems.controller` | Shortlist REST Controller | Endpoints for adding (201), viewing (200), and deleting (204) evaluators in a cohort shortlist. |
| **`ReportController.java`** | `com.ems.controller` | Audit Report REST Controller | Retrieves mapping audit records for reporting and CSV export. |
| **`SecurityConfig.java`** | `com.ems.config` | Spring Security 6 Configuration | Disables CSRF, configures stateless session policy, JWT filter chain, and password encoders. |
| **`CorsConfig.java`** | `com.ems.config` | Central CORS Configuration | Configures global CORS allowances for Angular client communication. |
| **`JwtUtil.java`** | `com.ems.config` | JWT Token Utility | Generates, parses, and validates HMAC-SHA256 JWT tokens with expiration. |
| **`JwtAuthenticationFilter.java`** | `com.ems.config` | Spring Web Filter | Intercepts HTTP requests, parses `Authorization: Bearer <token>`, and sets SecurityContext. |
| **`DataSeeder.java`** | `com.ems.config` | Database Seeder | Seeds default POC user, 3 cohorts, 7 candidates, 8 evaluators, shortlists, and sample mappings on startup. |
| **`BusinessRuleException.java`** | `com.ems.exception` | Custom Runtime Exception | Thrown when business rules (e.g. same evaluator as Interim) are violated. |
| **`ResourceNotFoundException.java`** | `com.ems.exception` | Custom Runtime Exception | Thrown when an entity ID (cohort, candidate, evaluator, mapping) does not exist. |
| **`GlobalExceptionHandler.java`** | `com.ems.exception` | Central Exception Handler | `@RestControllerAdvice` mapping exceptions to 400, 401, 403, 404, 409, and 500 status codes with structured `ErrorResponse`. |

---

## Section D: REST API Map & HTTP Status Conventions

### Endpoints Table

| HTTP Method | Endpoint | Controller | Purpose | Request Body / Params | Response Body | Success Status | Error Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | `AuthController` | Authenticate user & get JWT | `LoginRequest` (email, password) | `LoginResponse` (token, user details) | **200 OK** | 401 Unauthorized, 400 Bad Request |
| **GET** | `/api/cohorts` | `CohortController` | List all cohorts | None | `List<CohortDTO>` | **200 OK** | 500 Internal Server Error |
| **GET** | `/api/cohorts/{id}` | `CohortController` | Get cohort by ID | `@PathVariable Long id` | `CohortDTO` | **200 OK** | 404 Not Found |
| **POST** | `/api/cohorts` | `CohortController` | Create a new cohort | `CohortDTO` | `CohortDTO` | **201 CREATED** | 400 Bad Request |
| **PUT** | `/api/cohorts/{id}` | `CohortController` | Update cohort details | `@PathVariable Long id`, `CohortDTO` | `CohortDTO` | **200 OK** | 404 Not Found, 400 Bad Request |
| **DELETE** | `/api/cohorts/{id}` | `CohortController` | Delete a cohort | `@PathVariable Long id` | None | **204 NO CONTENT**| 404 Not Found |
| **GET** | `/api/cohorts/{cohortId}/candidates` | `CandidateController` | Get candidates in cohort | `@PathVariable Long cohortId` | `List<CandidateDTO>` | **200 OK** | 404 Not Found |
| **POST** | `/api/candidates` | `CandidateController` | Create new candidate | `CandidateDTO` | `CandidateDTO` | **201 CREATED** | 404 Not Found, 400 Bad Request |
| **PUT** | `/api/candidates/{id}` | `CandidateController` | Update candidate name | `@PathVariable Long id`, `CandidateDTO` | `CandidateDTO` | **200 OK** | 404 Not Found |
| **DELETE** | `/api/candidates/{id}` | `CandidateController` | Delete candidate | `@PathVariable Long id` | None | **204 NO CONTENT**| 404 Not Found |
| **GET** | `/api/evaluators` | `EvaluatorController` | List evaluators with filters | `@RequestParam` vertical, domain, availability, interviewFrom, interviewTo | `List<EvaluatorDTO>` | **200 OK** | 400 Bad Request, 500 Internal Server Error |
| **GET** | `/api/evaluators/{id}` | `EvaluatorController` | Get evaluator by ID | `@PathVariable Long id` | `EvaluatorDTO` | **200 OK** | 404 Not Found |
| **PUT** | `/api/evaluators/{id}/availability` | `EvaluatorController` | Toggle evaluator availability | `@PathVariable Long id`, `EvaluatorDTO` | `EvaluatorDTO` | **200 OK** | 404 Not Found |
| **PUT** | `/api/evaluators/{id}/status-reason` | `EvaluatorController` | Update evaluator status reason & dates | `@PathVariable Long id`, `EvaluatorDTO` | `EvaluatorDTO` | **200 OK** | 400 Bad Request, 404 Not Found |
| **GET** | `/api/evaluators/verticals` | `EvaluatorController` | Get distinct verticals | None | `List<String>` | **200 OK** | 500 Internal Server Error |
| **GET** | `/api/evaluators/domains` | `EvaluatorController` | Get distinct domains | None | `List<String>` | **200 OK** | 500 Internal Server Error |
| **POST** | `/api/evaluators/upload` | `EvaluatorController` | Bulk upload roster via Excel | MultipartFile `file` | `ApiResponse` | **200 OK** | 400 Bad Request |
| **GET** | `/api/evaluators/export` | `EvaluatorController` | Download Master Excel roster | None | `byte[]` (Excel stream) | **200 OK** | 404 Not Found, 500 Internal Server Error |
| **GET** | `/api/cohorts/{cohortId}/shortlist` | `ShortlistController` | Get cohort shortlist | `@PathVariable Long cohortId` | `List<Evaluator>` | **200 OK** | 404 Not Found |
| **POST** | `/api/cohorts/{cohortId}/shortlist/{evaluatorId}` | `ShortlistController` | Add evaluator to cohort | `@PathVariable` cohortId, evaluatorId | `ApiResponse` | **201 CREATED** | 404 Not Found, 409 Conflict |
| **DELETE** | `/api/cohorts/{cohortId}/shortlist/{evaluatorId}` | `ShortlistController` | Remove evaluator from shortlist | `@PathVariable` cohortId, evaluatorId | None | **204 NO CONTENT**| 404 Not Found |
| **GET** | `/api/cohorts/{cohortId}/mappings` | `MappingController` | Get mappings by cohort & round | `@PathVariable Long cohortId`, `@RequestParam String round` | `List<MappingDTO>` | **200 OK** | 404 Not Found |
| **POST** | `/api/mappings` | `MappingController` | Create/update manual mapping | `MappingRequest` | `MappingDTO` | **201 CREATED** | 404 Not Found, 409 Conflict |
| **POST** | `/api/cohorts/{cohortId}/auto-map` | `MappingController` | Execute auto-map algorithm | `@PathVariable Long cohortId`, `AutoMapRequest` | `List<MappingDTO>` | **201 CREATED** | 404 Not Found, 409 Conflict |
| **PUT** | `/api/mappings/{id}/confirm` | `MappingController` | Confirm mapping assignment | `@PathVariable Long id` | `MappingDTO` | **200 OK** | 404 Not Found, 409 Conflict |
| **GET** | `/api/reports/mappings` | `ReportController` | Get audit trail for reports | None | `List<MappingDTO>` | **200 OK** | 500 Internal Server Error |

---

### HTTP Status Codes Used in This Project

- **`200 OK`**: Returned for successful data queries (`GET`), successful resource updates (`PUT` on cohorts, candidates, evaluators, mapping confirmation), and Excel upload.
- **`201 CREATED`**: Returned for successful resource creation (`POST` on `/api/cohorts`, `/api/candidates`, `/api/mappings`, `/api/cohorts/{id}/auto-map`, and `/api/cohorts/{id}/shortlist/{id}`).
- **`204 NO_CONTENT`**: Returned for successful deletion where no response body is sent (`DELETE` on cohorts, candidates, shortlist removal).
- **`400 BAD_REQUEST`**: Returned when input validation fails, malformed arguments are provided, or an unreadable Excel file is uploaded.
- **`401 UNAUTHORIZED`**: Returned by `BadCredentialsException` or when an invalid/missing JWT token is supplied.
- **`403 FORBIDDEN`**: Returned by `AccessDeniedException` when a user attempts an action outside their granted role.
- **`404 NOT_FOUND`**: Returned by `ResourceNotFoundException` when a requested ID (cohort, candidate, evaluator, mapping) does not exist in the database.
- **`409 CONFLICT`**: Returned by `BusinessRuleException` when a business constraint is violated (e.g., trying to map the same evaluator from Interim to Final, repeat Final attempt evaluator conflict, or duplicate shortlist assignment).
- **`500 INTERNAL_SERVER_ERROR`**: Returned for unhandled system-level server errors.

---

## Section E: Business Logic & Rule Engine

The business rules are implemented centrally in `backend/src/main/java/com/ems/service/MappingService.java`.

### Rule 1: Cohort Shortlist Requirement
- **Implementation:** `MappingService.java` -> `validateMappingRules()` (Lines 152–157)
- **Logic:**
  ```java
  Optional<EvaluatorShortlist> shortlist = shortlistRepository
      .findByCohortCohortIdAndEvaluatorEvaluatorId(cohort.getCohortId(), evaluator.getEvaluatorId());
  if (shortlist.isEmpty()) {
      throw new BusinessRuleException("Evaluator is not assigned to this cohort");
  }
  ```
- **Explanation:** An evaluator must be explicitly added to a cohort's shortlist before being assigned to any candidate in that cohort.

---

### Rule 2: Evaluator Availability Requirement
- **Implementation:** `MappingService.java` -> `validateMappingRules()` (Lines 159–162)
- **Logic:**
  ```java
  if (!"AVAILABLE".equalsIgnoreCase(evaluator.getAvailabilityStatus())) {
      throw new BusinessRuleException("Evaluator is not available");
  }
  ```
- **Explanation:** Evaluators marked `UNAVAILABLE` cannot be assigned manually or by the auto-map algorithm.

---

### Rule 3: Interim to Final Isolation (No Repetition)
- **Implementation:** `MappingService.java` -> `validateMappingRules()` (Lines 164–175)
- **Logic:**
  ```java
  for (EvaluatorMapping m : existingMappings) {
      if (m.getEvaluator().getEvaluatorId().equals(evaluator.getEvaluatorId())) {
          if ("FINAL".equalsIgnoreCase(round) && "INTERIM".equalsIgnoreCase(m.getRound())) {
              throw new BusinessRuleException("Blocked: same evaluator as Interim");
          }
          if ("INTERIM".equalsIgnoreCase(round) && "FINAL".equalsIgnoreCase(m.getRound())) {
              throw new BusinessRuleException("Blocked: same evaluator as Final");
          }
      }
  }
  ```
- **Explanation:** A student evaluated by Evaluator A in their Interim interview MUST NOT be evaluated by Evaluator A in their Final interview.

---

### Rule 4: Repeat Final Attempt Evaluator Exclusion
- **Implementation:** `MappingService.java` -> `validateMappingRules()` (Lines 176–183)
- **Logic:**
  ```java
  if ("FINAL".equalsIgnoreCase(round) && "FINAL".equalsIgnoreCase(m.getRound())) {
      throw new BusinessRuleException("New evaluator required");
  }
  ```
- **Explanation:** If a candidate fails Attempt 1 of their Final interview and requires Attempt 2 (retake), the evaluator who conducted Attempt 1 is permanently excluded for that candidate. A new evaluator is mandatory.

---

### Rule 5: Auto-Mapping Workload Balancing Algorithm
- **Implementation:** `MappingService.java` -> `autoMap()` (Lines 78–139)
- **Logic:**
  1. Retrieve all candidates in the cohort.
  2. Filter cohort shortlisted evaluators who are currently `AVAILABLE`.
  3. For each candidate, determine their next attempt number.
  4. Iterate through available evaluators, test `canMap()` against Rules 1–4.
  5. Select the eligible evaluator with the lowest current workload (`mappingRepository.countByEvaluatorEvaluatorIdAndRound()`).
  6. Create or update the `SUGGESTED` mapping record.

---

### Rule 6: Interview Date Range Availability Overlap Logic
- **Implementation:** `EvaluatorService.java` -> `isAvailableForDateRange(Evaluator e, LocalDate interviewFrom, LocalDate interviewTo)`
- **Logic:**
  1. If `isPermanent == true` or `availabilityStatus == UNAVAILABLE` with no date range set, the evaluator is permanently unavailable.
  2. If the evaluator is marked `UNAVAILABLE` for a temporary leave window `[unavailableFrom, unavailableTo]`:
     - Calculate date overlap: `boolean overlaps = !interviewFrom.isAfter(e.getUnavailableTo()) && !interviewTo.isBefore(e.getUnavailableFrom());`
     - If the interview dates overlap with the leave window (`overlaps == true`), the evaluator is **UNAVAILABLE** for this interview window.
     - If the interview dates fall outside the leave window (`overlaps == false`), the evaluator is dynamically considered **AVAILABLE** for this interview window.
  3. Input Validation: If `interviewFrom > interviewTo`, throws `IllegalArgumentException` / HTTP 400.

---

### Rule 7: Status Reason Management & Master Excel Two-Way Sync
- **Implementation:** `EvaluatorService.java` -> `updateStatusReason()`, `ExcelUploadService.java` -> `updateEvaluatorStatusInMasterExcel()`
- **Logic:**
  1. When an evaluator becomes unavailable (temporary leave, resigned, maternity leave, project deadline), the POC clicks `"Update Status Reason"` on the dashboard.
  2. The POC selects or types a reason, specifies whether it is Temporary (with From/To dates) or Permanent (no dates), and saves.
  3. The database record is updated (`status_reason`, `is_permanent`, `unavailable_from`, `unavailable_to`, `availability_status`).
  4. **Excel Preservation**: Using Apache POI, the system opens the master Excel file (`uploads/master_evaluators.xlsx`), locates the exact evaluator row matching their unique `empId` (Column 0), updates only their status columns (`STATUS_REASON`, `PERMANENT`, `UNAVAILABLE_FROM`, `UNAVAILABLE_TO`, `AVAILABILITY_STATUS`), and writes the file back preserving cell styles, sheet structure, and all other evaluator rows.
  5. Evaluators are NEVER deleted when unavailable.
  6. The updated Master Excel file can be exported/downloaded at any time via `GET /api/evaluators/export`.

---

## Section F: Authentication & JWT Security Flow

```
+------------------+         POST /api/auth/login        +--------------------+
|  LoginComponent  | ----------------------------------> |   AuthController   |
+------------------+                                     +---------+----------+
                                                                   |
                                                                   v
+------------------+       Generate Signed Token         +--------------------+
|     JwtUtil      | <---------------------------------- |    AuthService     |
+------------------+                                     +---------+----------+
         |                                                         |
         | Return Token                                            v
         +-------------------------------------------------> AuthenticationManager
                                                             (BCrypt password match)
                                                                   |
+-------------------------+     Response with JWT & UserInfo       |
|  Angular AuthService    | <--------------------------------------+
|  Stores in localStorage |
+------------+------------+
             |
             v
+-------------------------+     Attach "Bearer <token>"   +--------------------+
|    AuthInterceptor      | ----------------------------> | Protected Endpoints|
+-------------------------+                               +---------+----------+
                                                                    |
                                                                    v
                                                          JwtAuthenticationFilter
                                                          (Validates token & sets
                                                           SecurityContext)
```

1. **User Submits Credentials:** `LoginComponent` captures email & password and calls `AuthService.login()`.
2. **Controller Delegates:** `AuthController` receives `LoginRequest` and calls `AuthService.login()`.
3. **Authentication Verification:** Spring Security `AuthenticationManager` verifies the raw password against the stored BCrypt hash in `users` table.
4. **Token Generation:** `JwtUtil.generateToken()` signs an HMAC-SHA256 JWT containing the user's email as subject and 24-hour expiration.
5. **Client Storage:** Angular `AuthService` saves token and user metadata in browser `localStorage`.
6. **HTTP Interceptor:** `AuthInterceptor` intercepts all subsequent Angular `HttpClient` requests and appends `Authorization: Bearer <token>` header.
7. **Filter Validation:** `JwtAuthenticationFilter` validates the signature, extracts the email, and sets an authenticated `UsernamePasswordAuthenticationToken` in Spring's `SecurityContextHolder`.

---

## Section G: Frontend Architecture & Component Flow

### Application Routing (`app.routes.ts`)
```typescript
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'cohorts', component: CohortsComponent, canActivate: [AuthGuard] },
  { path: 'mapping/:cohortId', component: MappingComponent, canActivate: [AuthGuard] },
  { path: 'reports', component: ReportsComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/home' }
];
```

### Component Breakdown
1. **`AppComponent` (`app.component.*`):**
   - Renders the dark blue sidebar navigation (`#0b1c3d`) with links: **Home**, **Cohorts**, **Mapping**, **Reports**, and **Logout**.
   - Listens to `router.events` to hide sidebar on `/login`.
   - Displays user profile badge (e.g., `VJ` / `Vijayalakshmi J. (POC)`).

2. **`LoginComponent` (`login.component.*`):**
   - Clean split-screen layout with value proposition cards on the left and form card on the right.
   - Pre-fills demo credentials (`admin@example.com` / `admin123`) for rapid evaluation.

3. **`HomeComponent` (`home.component.*`):**
   - Displays 3 high-level metric cards: **Total Evaluators**, **Available Evaluators**, **Active Cohorts**.
   - Full Evaluator Roster table with search bar, Vertical/Domain dropdown filters, and Availability filter.
   - Interactive availability toggle button with immediate backend sync.
   - Excel roster upload button.
   - "Add to Cohort" modal dialog.

4. **`CohortsComponent` (`cohorts.component.*`):**
   - Lists all batches with candidate counts, start dates, status tags, and dynamically calculated `evaluatorsMapped` progress.
   - "New Cohort" modal form.
   - "Open Mapping" button directing to `/mapping/:cohortId`.

5. **`MappingComponent` (`mapping.component.*`):**
   - Tab switch between **Interim Interview** and **Final Interview**.
   - Auto-Map trigger button.
   - Table displaying: Candidate Name, Attempt Number, Assigned Evaluator, Locked Interim Evaluator badge, Inline constraint warning badges, and Action buttons (**Confirm**, **Reassign**).
   - Bottom constraint rules reminder callout.

6. **`ReportsComponent` (`reports.component.*`):**
   - Full audit trail table of all mapping records.
   - Filter by search query, cohort, stage, and confirmation status.
   - Client-side CSV export button.

---

## Section H: Database Schema & Entity Relationships

- **Database Name:** `evaluator_mapping_db`
- **Schema Script:** `database/schema.sql`
- **Data Source Config:** `backend/src/main/resources/application.properties`

```
  +------------------+             +--------------------+
  |      USERS       | 1         * |      COHORTS       |
  |------------------| <---------- |--------------------|
  | user_id (PK)     | poc_id      | cohort_id (PK)     |
  | name             |             | cohort_name        |
  | email (UNIQUE)   |             | batch_code         |
  | password_hash    |             | poc_id (FK)        |
  | role             |             | candidate_count    |
  +------------------+             | status             |
        |       |                  +--------------------+
        |       |                            | 1
        |       |                            |
        |       |                            | *
        |       |                  +--------------------+
        |       |                  |     CANDIDATES     |
        |       |                  |--------------------|
        |       |                  | candidate_id (PK)  |
        |       |                  | cohort_id (FK)     |
        |       |                  | candidate_name     |
        |       |                  +--------------------+
        |       |                            | 1
        |       |                            |
        |       | *                          | *
  +----------------------+         +--------------------+
  |  EVALUATOR_SHORTLIST | *     1 | EVALUATOR_MAPPING  |
  |----------------------| ------> |--------------------|
  | shortlist_id (PK)    |         | mapping_id (PK)    |
  | cohort_id (FK)       |         | cohort_id (FK)     |
  | evaluator_id (FK)    |         | candidate_id (FK)  |
  | added_by (FK)        |         | evaluator_id (FK)  |
  +----------------------+         | round (INTERIM/FIN)|
        | *                        | attempt (1, 2...)  |
        |                          | mapped_by (FK)     |
        | 1                        | status             |
  +------------------+             +--------------------+
  |    EVALUATORS    |                       |
  |------------------|                       |
  | evaluator_id (PK)| 1                   * |
  | emp_id (UNIQUE)  | <---------------------+
  | name             |
  | vertical         |
  | domain           |
  | avail_status     |
  +------------------+
```

---

## Section I: Mentor Q&A — 15 Practical Project Questions

### 1. Why did you use a Controller instead of handling HTTP requests directly in Services?
Controllers represent the presentation/web layer in Spring MVC. They handle HTTP protocol concerns (request mapping, path variables, request bodies, HTTP status codes, and header negotiation) and delegate business execution to Services. This maintains separation of concerns.

### 2. What is the purpose of the Service layer?
The Service layer contains core business logic, transaction boundaries (`@Transactional`), validation rules, and orchestration between multiple repositories. It keeps business rules independent of HTTP or database specifics.

### 3. What is the role of Spring Data Repositories?
Repositories provide data access abstraction over relational database tables. By extending `JpaRepository`, Spring generates implementations for standard CRUD and derived query methods at runtime.

### 4. Why use `ResponseEntity` rather than returning raw DTO objects?
`ResponseEntity` gives explicit control over the HTTP response headers and HTTP status codes (such as `201 CREATED` for new resources or `204 NO_CONTENT` on deletion), adhering strictly to REST specifications.

### 5. Why use POST for `/api/mappings` instead of GET or PUT?
`POST` represents creating a new mapping resource, which is non-idempotent and involves sending a JSON request body with entity IDs. `GET` is strictly for safe, idempotent data retrieval.

### 6. What happens if a requested ID (Cohort, Candidate, Evaluator) does not exist?
The service throws `ResourceNotFoundException("... not found with id: X")`. The `@RestControllerAdvice` (`GlobalExceptionHandler`) catches this exception and returns an HTTP `404 NOT_FOUND` with a clean JSON error response, avoiding 500 errors or NullPointerExceptions.

### 7. What is the purpose of `@RestController`?
`@RestController` is a convenience annotation combining `@Controller` and `@ResponseBody`. It indicates that every method returns domain objects serialized directly into HTTP response bodies (JSON) rather than rendering HTML views.

### 8. What is the difference between `@PathVariable`, `@RequestParam`, and `@RequestBody`?
- `@PathVariable`: Extracts values embedded in the URI path (e.g., `/api/cohorts/{id}`).
- `@RequestParam`: Extracts query parameters (e.g., `/api/evaluators?vertical=Finance`).
- `@RequestBody`: Deserializes the JSON HTTP request body into a Java DTO object.

### 9. Why do we need Global Exception Handling (`@RestControllerAdvice`)?
Without central exception handling, unexpected runtime errors would produce standard Spring HTML whitelabel error pages or 500 status codes with exposed stack traces. `GlobalExceptionHandler` ensures all client errors (400, 401, 403, 404, 409) return consistent, safe JSON payloads.

### 10. How does JWT authentication work in this application?
On login, the backend verifies user credentials and generates a signed JWT token containing the user's email. The client sends this token in the `Authorization: Bearer <token>` header for subsequent requests. `JwtAuthenticationFilter` intercepts and verifies the token on every request, establishing an authenticated session.

### 11. Where and how are business rules enforced?
All 5 business constraints (cohort shortlist membership, availability status, Interim-to-Final isolation, and repeat Final attempt exclusion) are enforced in `MappingService.java` inside `validateMappingRules()`. If violated, a `BusinessRuleException` is thrown, returning an HTTP `409 CONFLICT`.

### 12. How does Angular communicate with Spring Boot?
Angular uses its built-in `HttpClient` service within dedicated service classes (`CohortService`, `MappingService`, etc.) to make asynchronous HTTP requests returning RxJS `Observable` streams. `AuthInterceptor` automatically attaches JWT headers.

### 13. Why do we use DTOs instead of exposing JPA Entities directly?
Exposing entities directly can cause infinite recursion during JSON serialization (due to bidirectional relationships), leaks internal database structure, and prevents decoupling UI payload requirements from database schemas.

### 14. How does the Evaluator Auto-Mapping algorithm work?
The algorithm queries all candidates in the cohort and all eligible shortlisted evaluators who are `AVAILABLE`. It validates each candidate against all 4 isolation rules and picks the eligible evaluator with the lowest current workload, balancing assignments evenly across the pool.

### 15. Why is CORS configuration necessary?
Because the Angular frontend runs on port 4200 and the Spring Boot backend runs on port 8080, browsers enforce the Same-Origin Policy. `CorsConfig` explicitly permits cross-origin requests from the client.

---

## Section J: Step-by-Step Manual Recreation Plan (14 Stages)

### Stage 1 — Database Setup
- **What to create:** MySQL database `evaluator_mapping_db` and execute `database/schema.sql`.
- **Why:** Sets up relational tables and foreign keys.
- **Reference:** `database/schema.sql`.

### Stage 2 — Spring Boot Project Setup
- **What to create:** Spring Boot 3 Maven project with dependencies: Spring Web, Spring Data JPA, Spring Security, MySQL Connector, Lombok, Apache POI, Spring Boot Starter Validation, and JJWT.
- **Why:** Establishes build dependencies and application starter.
- **Reference:** `backend/pom.xml`, `backend/src/main/resources/application.properties`.

### Stage 3 — JPA Domain Entities
- **What to create:** `User`, `Cohort`, `Candidate`, `Evaluator`, `EvaluatorShortlist`, `EvaluatorMapping`.
- **Why:** Maps database tables to Java domain models.
- **Reference:** `backend/src/main/java/com/ems/entity/*`.

### Stage 4 — Spring Data JPA Repositories
- **What to create:** `UserRepository`, `CohortRepository`, `CandidateRepository`, `EvaluatorRepository`, `EvaluatorShortlistRepository`, `EvaluatorMappingRepository`.
- **Why:** Provides data persistence operations and custom finder methods.
- **Reference:** `backend/src/main/java/com/ems/repository/*`.

### Stage 5 — Data Transfer Objects (DTOs)
- **What to create:** `MappingDTO`, `CohortDTO`, `EvaluatorDTO`, `CandidateDTO`, `LoginRequest`, `LoginResponse`, `ApiResponse`, `ErrorResponse`.
- **Why:** Defines clean network contract between client and server.
- **Reference:** `backend/src/main/java/com/ems/dto/*`.

### Stage 6 — Exception Classes & Global Exception Handler
- **What to create:** `BusinessRuleException`, `ResourceNotFoundException`, and `GlobalExceptionHandler`.
- **Why:** Centralizes error handling and ensures consistent HTTP 400, 401, 403, 404, 409 responses.
- **Reference:** `backend/src/main/java/com/ems/exception/*`.

### Stage 7 — Business Services & Rule Engine
- **What to create:** `MappingService` (isolation rules & auto-map), `ShortlistService`, `CohortService`, `EvaluatorService` (date-range filtering & status reason updates), `CandidateService`, `ExcelUploadService` (POI parsing, row-level sync, and export), `AuthService`.
- **Why:** Implements core business logic, date-overlap calculations, Excel preservation, and transactional boundaries.
- **Reference:** `backend/src/main/java/com/ems/service/*`.

### Stage 8 — Security & JWT Infrastructure
- **What to create:** `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`, `CorsConfig`, and `DataSeeder`.
- **Why:** Secures API endpoints with stateless JWT authentication and seeds initial test data.
- **Reference:** `backend/src/main/java/com/ems/config/*`.

### Stage 9 — REST API Controllers
- **What to create:** `AuthController`, `CohortController`, `CandidateController`, `EvaluatorController` (with date params, `status-reason` PUT, and `export` GET), `ShortlistController`, `MappingController`, `ReportController`.
- **Why:** Exposes REST endpoints with proper annotations and HTTP status codes.
- **Reference:** `backend/src/main/java/com/ems/controller/*`.

### Stage 10 — Backend Testing
- **What to create:** `CohortControllerTest`, `EvaluatorControllerTest`, `EvaluatorServiceTest`, `MappingControllerTest` using `MockMvc`, `@WebMvcTest`, `@MockBean`, `jsonPath()`.
- **Why:** Verifies 200, 201, 204, 400, 404, and 409 REST behavior, plus all 12 date range and status reason business scenarios.
- **Reference:** `backend/src/test/java/com/ems/*`.

### Stage 11 — Angular Project Setup & Models
- **What to create:** Angular standalone application, install dependencies, create models (`cohort.model.ts`, `evaluator.model.ts` with `statusReason`/`isPermanent`, `mapping.model.ts`, etc.).
- **Why:** Establishes client TypeScript foundation matching backend DTOs.
- **Reference:** `frontend/src/app/models/*`.

### Stage 12 — Angular Services, Interceptors & Guards
- **What to create:** `AuthService`, `CohortService`, `EvaluatorService` (with date filtering, status reason update, export API), `MappingService`, `ReportService`, `AuthInterceptor`, `AuthGuard`.
- **Why:** Handles API communication, token attachment, and route protection.
- **Reference:** `frontend/src/app/services/*`, `guards/*`, `interceptors/*`.

### Stage 13 — Angular Pages & Routing
- **What to create:** `LoginComponent`, `HomeComponent` (metrics, evaluator roster with Interview Date filters, Status Reason column, "Update Status Reason" modal, Master Excel upload and download), `CohortsComponent`, `MappingComponent` (interim/final tabs & rule badges), `ReportsComponent`, and `app.routes.ts`.
- **Why:** Provides user interface for batch owners to manage mappings.
- **Reference:** `frontend/src/app/pages/*`, `app.routes.ts`, `app.component.*`.

### Stage 14 — Integration & End-to-End Verification
- **What to verify:**
  1. Run `mvn test` in backend (all 24 tests pass, including `SampleExcelGeneratorTest`).
  2. Run `ng build` in frontend (0 warnings, 0 errors).
  3. Start backend (`mvn spring-boot:run`) and frontend (`npm start`).
  4. Log in with `admin@example.com` / `admin123`.
  5. Test uploading `sample-data/master_evaluators_sample.xlsx`, managing evaluator availability, updating status reasons with date ranges vs permanent, testing interview date filters, downloading master Excel, auto-mapping, verifying Interim/Final conflict alerts, and exporting audit reports.
