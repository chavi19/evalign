# Sample Master Evaluator Roster (`master_evaluators_sample.xlsx`)

This directory contains the realistic sample master Excel roster for the **Evaluator Mapping System (EMS)**.

## File Location
- **Path**: `sample-data/master_evaluators_sample.xlsx`

## Expected Columns
1. `EMP ID` (Column 0): Unique Employee Identifier (e.g., `EMP101` to `EMP118`)
2. `NAME` (Column 1): Full Name of the Evaluator
3. `VERTICAL` (Column 2): Business Vertical (e.g., `BFSI`, `Healthcare`, `Retail`, `Technology`, `Automotive`)
4. `DOMAIN` (Column 3): Technical Domain (e.g., `Java Full Stack`, `Cloud & DevOps`, `Data Engineering`, `Python & AI`, `QA Automation`, `Cybersecurity`)
5. `AVAILABILITY` (Column 4): `AVAILABLE` or `UNAVAILABLE`
6. `UNAVAILABLE FROM` (Column 5): Start Date of Temporary Leave (`YYYY-MM-DD`)
7. `UNAVAILABLE TO` (Column 6): End Date of Temporary Leave (`YYYY-MM-DD`)
8. `STATUS REASON` (Column 7): Explanation / Notes (e.g., `On Leave`, `Client Project Work`, `Maternity Leave`, `Medical Leave`, `Resigned / Left Company`)
9. `PERMANENT` (Column 8): `YES` or `NO` (Flag indicating permanent vs temporary status)

---

## Test Scenarios Included in Sample Roster

| Emp ID | Name | Vertical | Domain | Status | Leave From | Leave To | Reason | Permanent | Test Scenario |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **EMP101** | Rajesh Kumar | BFSI | Java Full Stack | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP102** | Sneha Rao | Healthcare | Cloud & DevOps | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP103** | Amit Verma | Retail | Data Engineering | UNAVAILABLE | 2026-09-01 | 2026-09-10 | On Leave | NO | **Early-month temporary leave**: Available for interviews on/after `2026-09-11`. Unavailable if interview falls between `2026-09-01` and `2026-09-10`. |
| **EMP104** | Priya Nair | Technology | Python & AI | UNAVAILABLE | - | - | Resigned / Left Company | YES | **Permanent Inactive**: Never available regardless of interview date filters. |
| **EMP105** | Vikram Singh | Automotive | QA Automation | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP106** | Ananya Sen | BFSI | Cybersecurity | UNAVAILABLE | 2026-09-15 | 2026-09-25 | Client Project Work | NO | **Mid/Late-month project engagement**: Available for interviews before `2026-09-15` or after `2026-09-25`. |
| **EMP107** | Karthik Iyer | Healthcare | Java Full Stack | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP108** | Meera Joshi | Retail | Cloud & DevOps | UNAVAILABLE | 2026-08-01 | 2026-11-30 | Maternity Leave | NO | **Long-term temporary leave**: Spanning full quarter. |
| **EMP109** | Rohan Deshmukh | Technology | Data Engineering | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP110** | Sunita Sharma | Automotive | Python & AI | UNAVAILABLE | - | - | Long-term Sabbatical | YES | **Permanent Inactive**: Never available. |
| **EMP111** | Suresh Menon | BFSI | QA Automation | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP112** | Kavita Patel | Healthcare | Cybersecurity | UNAVAILABLE | 2026-09-05 | 2026-09-08 | Medical Leave | NO | **Short temporary medical leave**. |
| **EMP113** | Deepak Gupta | Retail | Java Full Stack | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP114** | Divya Nair | Technology | Cloud & DevOps | UNAVAILABLE | 2026-09-18 | 2026-09-22 | Training / Upskilling | NO | **Temporary training window**. |
| **EMP115** | Sandeep Reddy | Automotive | Data Engineering | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP116** | Pooja Hegde | BFSI | Python & AI | UNAVAILABLE | - | - | Contract Ended | YES | **Permanent Inactive**: Contract ended. |
| **EMP117** | Arvind Swami | Healthcare | QA Automation | AVAILABLE | - | - | - | NO | Standard available evaluator |
| **EMP118** | Neha Bansal | Retail | Cybersecurity | AVAILABLE | - | - | - | NO | Standard available evaluator |

---

## Complete End-to-End Testing Flow

1. **Upload Excel**:
   - Navigate to Home (`/home`).
   - Click **Upload Excel** and select `sample-data/master_evaluators_sample.xlsx`.
   - Roster populates with all 18 evaluators.
2. **Filter by Vertical / Domain**:
   - Filter by `Vertical: BFSI` -> Displays EMP101, EMP106, EMP111, EMP116.
   - Filter by `Domain: Cybersecurity` -> Displays EMP106, EMP112, EMP118.
3. **Test Date-Range Filter**:
   - Set Interview From Date = `2026-09-12`, To Date = `2026-09-14`, Filter = `AVAILABLE`.
   - **Amit Verma (`EMP103`)** appears as AVAILABLE because his leave (`2026-09-01` to `2026-09-10`) is outside the interview window.
   - **Ananya Sen (`EMP106`)** appears as AVAILABLE because her project work (`2026-09-15` to `2026-09-25`) is outside the interview window.
   - Change Interview From Date = `2026-09-15`, To Date = `2026-09-20`.
   - **Ananya Sen (`EMP106`)** is excluded because dates overlap with her leave window.
   - **Priya Nair (`EMP104`)** and **Sunita Sharma (`EMP110`)** remain UNAVAILABLE in all date searches because `isPermanent = true`.
4. **Update Status Reason**:
   - Locate an unavailable evaluator (e.g., `EMP103` Amit Verma).
   - Click **Update Status Reason**.
   - Select reason, update dates or toggle Permanent, then click **Save Reason**.
5. **Download Master Excel**:
   - Click **Download Master Excel**.
   - Open downloaded file to verify that only the edited evaluator's row was updated, preserving formatting and all other rows.
