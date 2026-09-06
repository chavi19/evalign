package com.ems.controller;

import com.ems.config.JwtAuthenticationFilter;
import com.ems.config.JwtUtil;
import com.ems.dto.CohortDTO;
import com.ems.exception.GlobalExceptionHandler;
import com.ems.exception.ResourceNotFoundException;
import com.ems.service.CohortService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CohortController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CohortControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CohortService cohortService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("GET /api/cohorts - Should return 200 OK with list of cohorts")
    void shouldGetAllCohorts() throws Exception {
        CohortDTO c1 = new CohortDTO();
        c1.setCohortId(1L);
        c1.setCohortName("Batch-1 (DS Track)");
        c1.setBatchCode("DS-2024-01");

        when(cohortService.getAllCohorts()).thenReturn(Arrays.asList(c1));

        mockMvc.perform(get("/api/cohorts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cohortId").value(1))
                .andExpect(jsonPath("$[0].cohortName").value("Batch-1 (DS Track)"))
                .andExpect(jsonPath("$[0].batchCode").value("DS-2024-01"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/cohorts/{id} - Should return 200 OK when cohort exists")
    void shouldGetCohortById() throws Exception {
        CohortDTO c1 = new CohortDTO();
        c1.setCohortId(1L);
        c1.setCohortName("Batch-1 (DS Track)");

        when(cohortService.getCohortById(1L)).thenReturn(c1);

        mockMvc.perform(get("/api/cohorts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohortId").value(1))
                .andExpect(jsonPath("$.cohortName").value("Batch-1 (DS Track)"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/cohorts/{id} - Should return 404 NOT_FOUND when cohort does not exist")
    void shouldReturn404WhenCohortNotFound() throws Exception {
        when(cohortService.getCohortById(999L))
                .thenThrow(new ResourceNotFoundException("Cohort not found with id: 999"));

        mockMvc.perform(get("/api/cohorts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cohort not found with id: 999"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/cohorts - Should return 201 CREATED when cohort is created")
    void shouldCreateCohort() throws Exception {
        CohortDTO input = new CohortDTO();
        input.setCohortName("New Cohort");
        input.setBatchCode("NC-01");
        input.setCandidateCount(25);

        CohortDTO created = new CohortDTO();
        created.setCohortId(10L);
        created.setCohortName("New Cohort");
        created.setBatchCode("NC-01");
        created.setCandidateCount(25);

        when(cohortService.createCohort(any(CohortDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/cohorts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cohortId").value(10))
                .andExpect(jsonPath("$.cohortName").value("New Cohort"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/cohorts/{id} - Should return 200 OK when cohort is updated")
    void shouldUpdateCohort() throws Exception {
        CohortDTO input = new CohortDTO();
        input.setCohortName("Updated Cohort");

        CohortDTO updated = new CohortDTO();
        updated.setCohortId(1L);
        updated.setCohortName("Updated Cohort");

        when(cohortService.updateCohort(eq(1L), any(CohortDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/cohorts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohortId").value(1))
                .andExpect(jsonPath("$.cohortName").value("Updated Cohort"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/cohorts/{id} - Should return 204 NO_CONTENT on successful deletion")
    void shouldDeleteCohort() throws Exception {
        doNothing().when(cohortService).deleteCohort(1L);

        mockMvc.perform(delete("/api/cohorts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
