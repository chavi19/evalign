package com.ems.controller;

import com.ems.config.JwtAuthenticationFilter;
import com.ems.config.JwtUtil;
import com.ems.dto.MappingDTO;
import com.ems.dto.MappingRequest;
import com.ems.exception.BusinessRuleException;
import com.ems.exception.GlobalExceptionHandler;
import com.ems.service.MappingService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MappingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class MappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MappingService mappingService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("GET /api/cohorts/{cohortId}/mappings - Should return 200 OK with list of mappings")
    void shouldGetMappings() throws Exception {
        MappingDTO m = new MappingDTO();
        m.setMappingId(1L);
        m.setCandidateName("Priya Sharma");
        m.setEvaluatorName("Rajesh Kumar");
        m.setRound("INTERIM");
        m.setStatus("CONFIRMED");

        when(mappingService.getMappingsByCohortAndRound(1L, "INTERIM")).thenReturn(Arrays.asList(m));

        mockMvc.perform(get("/api/cohorts/1/mappings?round=INTERIM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mappingId").value(1))
                .andExpect(jsonPath("$[0].candidateName").value("Priya Sharma"))
                .andExpect(jsonPath("$[0].evaluatorName").value("Rajesh Kumar"))
                .andExpect(jsonPath("$[0].round").value("INTERIM"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/mappings - Should return 201 CREATED when manual mapping is created")
    void shouldCreateManualMapping() throws Exception {
        MappingRequest req = new MappingRequest();
        req.setCohortId(1L);
        req.setCandidateId(2L);
        req.setEvaluatorId(3L);
        req.setRound("FINAL");

        MappingDTO m = new MappingDTO();
        m.setMappingId(5L);
        m.setCandidateName("Rahul Verma");
        m.setEvaluatorName("Sneha Roy");
        m.setRound("FINAL");
        m.setStatus("SUGGESTED");

        when(mappingService.createManualMapping(any(MappingRequest.class))).thenReturn(m);

        mockMvc.perform(post("/api/mappings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mappingId").value(5))
                .andExpect(jsonPath("$.candidateName").value("Rahul Verma"))
                .andExpect(jsonPath("$.status").value("SUGGESTED"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/mappings - Should return 409 CONFLICT on business rule violation")
    void shouldReturn409OnBusinessRuleViolation() throws Exception {
        MappingRequest req = new MappingRequest();
        req.setCohortId(1L);
        req.setCandidateId(2L);
        req.setEvaluatorId(3L);
        req.setRound("FINAL");

        when(mappingService.createManualMapping(any(MappingRequest.class)))
                .thenThrow(new BusinessRuleException("Blocked: same evaluator as Interim"));

        mockMvc.perform(post("/api/mappings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Business Rule Conflict"))
                .andExpect(jsonPath("$.message").value("Blocked: same evaluator as Interim"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/mappings/{id}/confirm - Should return 200 OK when mapping is confirmed")
    void shouldConfirmMapping() throws Exception {
        MappingDTO m = new MappingDTO();
        m.setMappingId(1L);
        m.setStatus("CONFIRMED");

        when(mappingService.confirmMapping(eq(1L))).thenReturn(m);

        mockMvc.perform(put("/api/mappings/1/confirm")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mappingId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
