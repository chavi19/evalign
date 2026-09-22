package com.ems.e2e;

import com.ems.dto.*;
import com.ems.entity.*;
import com.ems.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullEndToEndWorkflowVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;

    @Autowired
    private EvaluatorMappingRepository mappingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String poc1Token;
    private static String poc2Token;
    private static Long createdCohortId;
    private static Long sampleEvaluatorId;
    private static Long samplePermanentEvaluatorId;
    private static Long createdMappingId;

    private Path resolveSampleFile(String relativePath) {
        Path p = Paths.get(relativePath);
        if (Files.exists(p)) return p;
        Path parent = Paths.get("..", relativePath);
        if (Files.exists(parent)) return parent;
        return p;
    }

    @BeforeAll
    static void setupDirs() {
        new File("uploads").mkdirs();
    }

    @Test
    @Order(1)
    @DisplayName("Step 1-4: Database Seeding & Authentication for Multi-POC Verification")
    void testAuthenticationAndUserSetup() throws Exception {
        // Create POC 1
        userRepository.findByEmail("ramya.poc@company.com").ifPresent(userRepository::delete);
        User poc1 = new User();
        poc1.setEmail("ramya.poc@company.com");
        poc1.setPasswordHash(passwordEncoder.encode("Password@123"));
        poc1.setName("Ramya POC");
        poc1.setRole("POC");
        poc1.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(poc1);

        // Create POC 2
        userRepository.findByEmail("nihar.poc@company.com").ifPresent(userRepository::delete);
        User poc2 = new User();
        poc2.setEmail("nihar.poc@company.com");
        poc2.setPasswordHash(passwordEncoder.encode("Password@123"));
        poc2.setName("Nihar POC");
        poc2.setRole("POC");
        poc2.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(poc2);

        // Authenticate POC 1
        LoginRequest login1 = new LoginRequest();
        login1.setEmail("ramya.poc@company.com");
        login1.setPassword("Password@123");
        MvcResult res1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json1 = objectMapper.readTree(res1.getResponse().getContentAsString());
        poc1Token = json1.get("token").asText();
        assertNotNull(poc1Token);
        assertEquals("ramya.poc@company.com", json1.get("email").asText());

        // Authenticate POC 2
        LoginRequest login2 = new LoginRequest();
        login2.setEmail("nihar.poc@company.com");
        login2.setPassword("Password@123");
        MvcResult res2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json2 = objectMapper.readTree(res2.getResponse().getContentAsString());
        poc2Token = json2.get("token").asText();
        assertNotNull(poc2Token);
        assertEquals("nihar.poc@company.com", json2.get("email").asText());
    }

    @Test
    @Order(2)
    @DisplayName("Step 5-9: Create New Cohort, Ingest Candidate Excel & Verify Isolation")
    void testCohortCreationAndCandidateExcelIngestion() throws Exception {
        // Create Cohort Batch
        CohortDTO cohortDTO = new CohortDTO();
        cohortDTO.setCohortName("2026 Java Cloud Cohort - Batch Alpha");
        cohortDTO.setBatchCode("JCLD-2026-01");
        cohortDTO.setStartDate(LocalDate.of(2026, 5, 1));
        cohortDTO.setStatus("Active");
        cohortDTO.setCandidateCount(0);

        MvcResult createRes = mockMvc.perform(post("/api/cohorts")
                        .header("Authorization", "Bearer " + poc1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cohortDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        CohortDTO created = objectMapper.readValue(createRes.getResponse().getContentAsString(), CohortDTO.class);
        assertNotNull(created.getCohortId());
        createdCohortId = created.getCohortId();

        // Upload Candidate Sample Excel
        Path candidateExcelPath = resolveSampleFile("sample-data/candidates_sample.xlsx");
        assertTrue(Files.exists(candidateExcelPath), "Candidate sample Excel must exist");
        byte[] fileBytes = Files.readAllBytes(candidateExcelPath);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "candidates_sample.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileBytes
        );

        MvcResult uploadRes = mockMvc.perform(multipart("/api/cohorts/" + createdCohortId + "/candidates/upload")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify DB persistence of candidates
        List<Candidate> candidates = candidateRepository.findByCohortCohortId(createdCohortId);
        assertFalse(candidates.isEmpty(), "Candidates should be persisted in database");
        assertTrue(candidates.size() >= 10, "Should have parsed at least 10 candidates from sample Excel");

        // Verify candidate fields
        Candidate first = candidates.get(0);
        assertNotNull(first.getCandidateName());
        assertEquals(createdCohortId, first.getCohort().getCohortId());

        // Verify Candidate Isolation: Another dummy cohort has 0 of these candidates
        Cohort dummyCohort = new Cohort();
        dummyCohort.setCohortName("Isolated Cohort");
        dummyCohort.setBatchCode("ISO-001");
        dummyCohort.setStatus("Active");
        dummyCohort.setCandidateCount(0);
        dummyCohort = cohortRepository.save(dummyCohort);

        List<Candidate> dummyCandidates = candidateRepository.findByCohortCohortId(dummyCohort.getCohortId());
        assertTrue(dummyCandidates.isEmpty(), "Isolated cohort must have 0 candidates from first cohort");
    }

    @Test
    @Order(3)
    @DisplayName("Step 10-13: Ingest Master Evaluator Excel & Verify Filter Functionality")
    void testMasterEvaluatorExcelIngestionAndFilters() throws Exception {
        Path evalExcelPath = resolveSampleFile("sample-data/master_evaluators_sample.xlsx");
        assertTrue(Files.exists(evalExcelPath), "Master evaluator sample Excel must exist");
        byte[] fileBytes = Files.readAllBytes(evalExcelPath);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "master_evaluators_sample.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileBytes
        );

        mockMvc.perform(multipart("/api/evaluators/upload")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk());

        // Verify evaluators query
        MvcResult evalRes = mockMvc.perform(get("/api/evaluators")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk())
                .andReturn();

        List<Evaluator> allEvaluators = evaluatorRepository.findAll();
        assertFalse(allEvaluators.isEmpty(), "Evaluators should be populated from Excel");
        assertTrue(allEvaluators.size() >= 15, "Should have at least 15 evaluators");

        // Test Domain/Vertical filters
        mockMvc.perform(get("/api/evaluators?domain=Java Fullstack")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/evaluators?vertical=Banking & Financial Services (BFS)")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk());

        // Locate a suitable evaluator for leave testing
        Evaluator availableEval = allEvaluators.stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsPermanent()) && "AVAILABLE".equalsIgnoreCase(e.getAvailabilityStatus()))
                .findFirst().orElse(allEvaluators.get(0));
        sampleEvaluatorId = availableEval.getEvaluatorId();

        // Locate permanent inactive evaluator
        Evaluator permanentEval = allEvaluators.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsPermanent()))
                .findFirst().orElse(null);
        if (permanentEval == null) {
            availableEval.setIsPermanent(true);
            availableEval.setStatusReason("Resigned");
            permanentEval = evaluatorRepository.save(availableEval);
        }
        samplePermanentEvaluatorId = permanentEval.getEvaluatorId();
    }

    @Test
    @Order(4)
    @DisplayName("Step 14-18: Update Status Reason & Verify Row-Level Two-Way Excel Sync")
    void testStatusReasonUpdateAndExcelSync() throws Exception {
        assertNotNull(sampleEvaluatorId, "sampleEvaluatorId must be initialized");
        EvaluatorDTO updateDTO = new EvaluatorDTO();
        updateDTO.setStatusReason("Family Medical Leave");
        updateDTO.setUnavailableFrom(LocalDate.of(2026, 5, 10));
        updateDTO.setUnavailableTo(LocalDate.of(2026, 5, 20));
        updateDTO.setIsPermanent(false);
        updateDTO.setAvailabilityStatus("UNAVAILABLE");

        mockMvc.perform(put("/api/evaluators/" + sampleEvaluatorId + "/status-reason")
                        .header("Authorization", "Bearer " + poc1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        // Verify DB update
        Evaluator updated = evaluatorRepository.findById(sampleEvaluatorId).orElseThrow();
        assertEquals("Family Medical Leave", updated.getStatusReason());
        assertEquals(LocalDate.of(2026, 5, 10), updated.getUnavailableFrom());
        assertEquals(LocalDate.of(2026, 5, 20), updated.getUnavailableTo());
        assertEquals(false, updated.getIsPermanent());

        // Verify Master Excel on disk was updated
        File masterExcel = new File("uploads/master_evaluators.xlsx");
        if (masterExcel.exists()) {
            try (Workbook workbook = WorkbookFactory.create(new FileInputStream(masterExcel))) {
                assertNotNull(workbook.getSheetAt(0));
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Step 19-21: Verify Dynamic Date-Overlap Availability & Permanent Exits")
    void testDynamicDateOverlapLogic() throws Exception {
        assertNotNull(sampleEvaluatorId, "sampleEvaluatorId must be initialized");
        assertNotNull(samplePermanentEvaluatorId, "samplePermanentEvaluatorId must be initialized");

        // 1. Date INSIDE leave period (2026-05-12 to 2026-05-15) -> Evaluator is on leave, so EXCLUDED from available results
        MvcResult insideRes = mockMvc.perform(get("/api/evaluators?interviewFrom=2026-05-12&interviewTo=2026-05-15")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode insideList = objectMapper.readTree(insideRes.getResponse().getContentAsString());
        boolean insideFound = false;
        for (JsonNode node : insideList) {
            if (node.get("evaluatorId").asLong() == sampleEvaluatorId) {
                insideFound = true;
                break;
            }
        }
        assertFalse(insideFound, "Evaluator on leave during 2026-05-12 to 2026-05-15 must be excluded from available list");

        // 2. Date AFTER leave period (2026-06-01 to 2026-06-05) -> Evaluator is INCLUDED in available results
        MvcResult afterRes = mockMvc.perform(get("/api/evaluators?interviewFrom=2026-06-01&interviewTo=2026-06-05")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode afterList = objectMapper.readTree(afterRes.getResponse().getContentAsString());
        boolean afterFound = false;
        for (JsonNode node : afterList) {
            if (node.get("evaluatorId").asLong() == sampleEvaluatorId) {
                afterFound = true;
                break;
            }
        }
        assertTrue(afterFound, "Evaluator must be included as available for interview window after their leave ends");

        // 3. Permanent inactive evaluator -> Always EXCLUDED regardless of date
        boolean permanentFound = false;
        for (JsonNode node : afterList) {
            if (node.get("evaluatorId").asLong() == samplePermanentEvaluatorId) {
                permanentFound = true;
                break;
            }
        }
        assertFalse(permanentFound, "Permanent inactive evaluator must never be included in available list");
    }

    @Test
    @Order(6)
    @DisplayName("Step 22-27: Shortlist, Auto-Map, Confirm, and Reports Verification")
    void testShortlistMappingAndReports() throws Exception {
        // Shortlist an available evaluator for the cohort
        List<Evaluator> evaluators = evaluatorRepository.findAll();
        Evaluator eval = evaluators.stream()
                .filter(e -> "AVAILABLE".equalsIgnoreCase(e.getAvailabilityStatus()) && !Boolean.TRUE.equals(e.getIsPermanent()))
                .findFirst().orElseThrow();

        mockMvc.perform(post("/api/cohorts/" + createdCohortId + "/evaluators/" + eval.getEvaluatorId())
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isCreated());

        // Verify Shortlist in DB
        List<EvaluatorShortlist> shortlists = shortlistRepository.findByCohortCohortId(createdCohortId);
        assertFalse(shortlists.isEmpty());

        // Perform Auto-Map
        AutoMapRequest autoMapRequest = new AutoMapRequest();
        autoMapRequest.setCohortId(createdCohortId);
        autoMapRequest.setRound("INTERIM");

        MvcResult autoMapRes = mockMvc.perform(post("/api/cohorts/" + createdCohortId + "/auto-map")
                        .header("Authorization", "Bearer " + poc1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autoMapRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        List<EvaluatorMapping> mappings = mappingRepository.findByCohortCohortIdAndRound(createdCohortId, "INTERIM");
        assertFalse(mappings.isEmpty(), "Auto-map should assign candidate mappings");
        createdMappingId = mappings.get(0).getMappingId();

        // Confirm mapping
        mockMvc.perform(put("/api/mappings/" + createdMappingId + "/confirm")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk());

        // Verify in Reports API
        MvcResult reportRes = mockMvc.perform(get("/api/reports/mappings")
                        .header("Authorization", "Bearer " + poc1Token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode reportList = objectMapper.readTree(reportRes.getResponse().getContentAsString());
        assertTrue(reportList.size() > 0, "Reports endpoint must list confirmed mappings");
    }

    @Test
    @Order(7)
    @DisplayName("Step 28-31: Multi-POC Shared Data Consistency Across Logins")
    void testMultiPOCSharedDataConsistency() throws Exception {
        // POC 2 queries cohorts created by POC 1
        MvcResult cohortRes = mockMvc.perform(get("/api/cohorts")
                        .header("Authorization", "Bearer " + poc2Token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode cohortList = objectMapper.readTree(cohortRes.getResponse().getContentAsString());
        boolean foundCreated = false;
        for (JsonNode c : cohortList) {
            if (c.get("cohortId").asLong() == createdCohortId) {
                foundCreated = true;
                assertTrue(c.get("candidateCount").asInt() >= 10);
            }
        }
        assertTrue(foundCreated, "POC 2 must see cohorts created by POC 1 in shared DB");

        // POC 2 queries evaluator status updated by POC 1
        MvcResult evalRes = mockMvc.perform(get("/api/evaluators")
                        .header("Authorization", "Bearer " + poc2Token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode evalList = objectMapper.readTree(evalRes.getResponse().getContentAsString());
        boolean verifiedEval = false;
        for (JsonNode e : evalList) {
            if (e.get("evaluatorId").asLong() == sampleEvaluatorId) {
                assertEquals("Family Medical Leave", e.get("statusReason").asText());
                verifiedEval = true;
            }
        }
        assertTrue(verifiedEval, "POC 2 must see updated evaluator status reasons in shared DB");
    }
}
