# Sample Test Data Roster Files

This directory contains realistic sample spreadsheets for testing the **Evaluator Mapping System (EMS)** end-to-end.

---

## 1. Candidate Roster Sample (`candidates_sample.xlsx`)

### Purpose
Used when creating a new Cohort Batch in the **Cohorts** page (`/cohorts`). The system parses this file, extracts all candidate records, persists them to the `CANDIDATES` table in MySQL associated with the new cohort, and updates the cohort's candidate count automatically.

### File Location
- **Path**: `sample-data/candidates_sample.xlsx`

### Expected Columns
1. `CANDIDATE ID` (Column 0): Unique trainee identifier (e.g., `CAND-201`, `CAND-202`)
2. `CANDIDATE NAME` (Column 1): Full Name of the candidate trainee (e.g., `Aarav Mehta`, `Diya Sen`)
3. `EMAIL` (Column 2): Candidate corporate email address (e.g., `aarav.mehta@example.com`)
4. `TRACK / DOMAIN` (Column 3): Technical track (e.g., `Java Full Stack`, `Cloud & DevOps`)

### How to Upload when Creating a New Cohort
1. Navigate to the **Cohorts** page in the left sidebar.
2. Click **"+ New Cohort Batch"** in the top-right corner.
3. Enter the **Cohort Name** (e.g. `Batch-5 (Java Full Stack)`) and **Batch Code** (e.g. `BATCH-05`).
4. Select the **Start Date** and **Status**.
5. Under **Upload Candidate Roster (Excel)**, click the file area and select `sample-data/candidates_sample.xlsx`.
6. Click **"Create Cohort Batch"**.
7. The system creates the cohort, uploads the Excel, extracts all 12 candidate records, associates them with the new cohort, and immediately updates the candidate counter and table.

---

## 2. Master Evaluator Roster Sample (`master_evaluators_sample.xlsx`)

### Purpose
Used on the **Home / Evaluator Dashboard** (`/home`) via **"Upload Excel"** to import the master pool of Subject Matter Experts (SMEs).

### File Location
- **Path**: `sample-data/master_evaluators_sample.xlsx`

### Expected Columns
1. `EMP ID` (Column 0): Unique Employee Identifier (e.g., `EMP101` to `EMP118`)
2. `NAME` (Column 1): Full Name of the Evaluator
3. `VERTICAL` (Column 2): Business Vertical (`BFSI`, `Healthcare`, `Retail`, `Technology`, `Automotive`)
4. `DOMAIN` (Column 3): Technical Domain (`Java Full Stack`, `Cloud & DevOps`, `Data Engineering`, `Python & AI`, `QA Automation`, `Cybersecurity`)
5. `AVAILABILITY` (Column 4): `AVAILABLE` or `UNAVAILABLE`
6. `UNAVAILABLE FROM` (Column 5): Start Date of Temporary Leave (`YYYY-MM-DD`)
7. `UNAVAILABLE TO` (Column 6): End Date of Temporary Leave (`YYYY-MM-DD`)
8. `STATUS REASON` (Column 7): Explanation / Notes (e.g., `On Leave`, `Client Project Work`, `Maternity Leave`, `Medical Leave`, `Resigned / Left Company`)
9. `PERMANENT` (Column 8): `YES` (Permanent exit/inactive) or `NO` (Temporary leave)

### Test Scenarios in Evaluator Roster
- **EMP103 (Amit Verma)**: Temporary leave `2026-09-01` to `2026-09-10`. Available for interviews outside this window.
- **EMP104 (Priya Nair)**: Permanent exit (`isPermanent = true`, "Resigned / Left Company"). Excluded from all date searches.
- **EMP106 (Ananya Sen)**: Temporary project work `2026-09-15` to `2026-09-25`. Excluded only if interview dates overlap.
- **EMP108 (Meera Joshi)**: Maternity leave `2026-08-01` to `2026-11-30`.
- **EMP110 (Sunita Sharma)**: Permanent sabbatical.
