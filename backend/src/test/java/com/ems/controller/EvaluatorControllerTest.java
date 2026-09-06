package com.ems.controller;

import com.ems.config.JwtAuthenticationFilter;
import com.ems.config.JwtUtil;
import com.ems.dto.EvaluatorDTO;
import com.ems.exception.GlobalExceptionHandler;
import com.ems.exception.ResourceNotFoundException;
import com.ems.service.EvaluatorService;
import com.ems.service.ExcelUploadService;
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

@WebMvcTest(EvaluatorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class EvaluatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluatorService evaluatorService;

    @MockBean
    private ExcelUploadService excelUploadService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("GET /api/evaluators - Should return 200 OK with list of evaluators")
    void shouldGetEvaluators() throws Exception {
        EvaluatorDTO e1 = new EvaluatorDTO();
        e1.setEvaluatorId(1L);
        e1.setEmpId("EMP1001");
        e1.setName("Rajesh Kumar");
        e1.setVertical("Finance");
        e1.setAvailabilityStatus("AVAILABLE");

        when(evaluatorService.getEvaluators(null, null, null)).thenReturn(Arrays.asList(e1));

        mockMvc.perform(get("/api/evaluators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].evaluatorId").value(1))
                .andExpect(jsonPath("$[0].empId").value("EMP1001"))
                .andExpect(jsonPath("$[0].name").value("Rajesh Kumar"))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/evaluators/{id} - Should return 404 NOT_FOUND when evaluator does not exist")
    void shouldReturn404WhenEvaluatorNotFound() throws Exception {
        when(evaluatorService.getEvaluatorById(999L))
                .thenThrow(new ResourceNotFoundException("Evaluator not found with id: 999"));

        mockMvc.perform(get("/api/evaluators/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Evaluator not found with id: 999"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/evaluators/{id}/availability - Should return 200 OK when availability updated")
    void shouldUpdateAvailability() throws Exception {
        EvaluatorDTO input = new EvaluatorDTO();
        input.setAvailabilityStatus("UNAVAILABLE");

        EvaluatorDTO updated = new EvaluatorDTO();
        updated.setEvaluatorId(1L);
        updated.setAvailabilityStatus("UNAVAILABLE");

        when(evaluatorService.updateAvailability(eq(1L), any(EvaluatorDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/evaluators/1/availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluatorId").value(1))
                .andExpect(jsonPath("$.availabilityStatus").value("UNAVAILABLE"));
    }
}
