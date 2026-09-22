package com.ems.controller;

import com.ems.config.JwtAuthenticationFilter;
import com.ems.config.JwtUtil;
import com.ems.dto.CandidateDTO;
import com.ems.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidateController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CandidateService candidateService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testGetCandidatesByCohort() throws Exception {
        CandidateDTO c1 = new CandidateDTO();
        c1.setCandidateId(1L);
        c1.setCohortId(10L);
        c1.setCandidateName("Aarav Mehta");

        Mockito.when(candidateService.getCandidatesByCohort(10L)).thenReturn(Arrays.asList(c1));

        mockMvc.perform(get("/api/cohorts/10/candidates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Aarav Mehta"));
    }

    @Test
    public void testUploadCandidatesExcel() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "candidates.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy content".getBytes()
        );

        CandidateDTO c1 = new CandidateDTO();
        c1.setCandidateId(1L);
        c1.setCohortId(10L);
        c1.setCandidateName("Aarav Mehta");

        CandidateDTO c2 = new CandidateDTO();
        c2.setCandidateId(2L);
        c2.setCohortId(10L);
        c2.setCandidateName("Diya Sen");

        Mockito.when(candidateService.uploadCandidatesFromExcel(eq(10L), any()))
                .thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(multipart("/api/cohorts/10/candidates/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully uploaded 2 candidates for cohort"));
    }
}
