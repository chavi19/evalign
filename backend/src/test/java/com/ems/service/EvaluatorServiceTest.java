package com.ems.service;

import com.ems.dto.EvaluatorDTO;
import com.ems.entity.Evaluator;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EvaluatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EvaluatorServiceTest {

    @Mock
    private EvaluatorRepository evaluatorRepository;

    @Mock
    private ExcelUploadService excelUploadService;

    @InjectMocks
    private EvaluatorService evaluatorService;

    private Evaluator availableEval;
    private Evaluator temporaryLeaveEval;
    private Evaluator permanentLeaveEval;

    @BeforeEach
    void setUp() {
        availableEval = new Evaluator();
        availableEval.setEvaluatorId(1L);
        availableEval.setEmpId("EMP-1001");
        availableEval.setName("Rahul Menon");
        availableEval.setVertical("Finance");
        availableEval.setDomain("Corporate Banking");
        availableEval.setAvailabilityStatus("AVAILABLE");

        temporaryLeaveEval = new Evaluator();
        temporaryLeaveEval.setEvaluatorId(2L);
        temporaryLeaveEval.setEmpId("EMP-1002");
        temporaryLeaveEval.setName("Sneha Iyer");
        temporaryLeaveEval.setVertical("Finance");
        temporaryLeaveEval.setDomain("Risk & Audit");
        temporaryLeaveEval.setAvailabilityStatus("UNAVAILABLE");
        temporaryLeaveEval.setUnavailableFrom(LocalDate.of(2026, 9, 10));
        temporaryLeaveEval.setUnavailableTo(LocalDate.of(2026, 9, 16));
        temporaryLeaveEval.setStatusReason("On Leave");
        temporaryLeaveEval.setIsPermanent(false);

        permanentLeaveEval = new Evaluator();
        permanentLeaveEval.setEvaluatorId(3L);
        permanentLeaveEval.setEmpId("EMP-1003");
        permanentLeaveEval.setName("Ananya Roy");
        permanentLeaveEval.setVertical("Finance");
        permanentLeaveEval.setDomain("Corporate Banking");
        permanentLeaveEval.setAvailabilityStatus("UNAVAILABLE");
        permanentLeaveEval.setStatusReason("Left company");
        permanentLeaveEval.setIsPermanent(true);
    }

    @Test
    @DisplayName("SCENARIO 1: Evaluator is available -> Appears in dashboard")
    void shouldIncludeAvailableEvaluator() {
        when(evaluatorRepository.findAll()).thenReturn(Arrays.asList(availableEval, temporaryLeaveEval, permanentLeaveEval));

        List<EvaluatorDTO> list = evaluatorService.getEvaluators("Finance", null, null, null, null);
        assertEquals(3, list.size());

        boolean isAvail = evaluatorService.isAvailableForDateRange(availableEval, LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 12));
        assertTrue(isAvail);
    }

    @Test
    @DisplayName("SCENARIO 2: Evaluator on leave Sept 10 to Sept 16, Interview Sept 12 -> NOT available")
    void shouldExcludeEvaluatorDuringLeave() {
        LocalDate interviewDate = LocalDate.of(2026, 9, 12);
        boolean isAvail = evaluatorService.isAvailableForDateRange(temporaryLeaveEval, interviewDate, interviewDate);
        assertFalse(isAvail);
    }

    @Test
    @DisplayName("SCENARIO 3: Same evaluator on leave Sept 10 to Sept 16, Interview Sept 17 -> Available again")
    void shouldIncludeEvaluatorAfterLeaveEnds() {
        LocalDate interviewDate = LocalDate.of(2026, 9, 17);
        boolean isAvail = evaluatorService.isAvailableForDateRange(temporaryLeaveEval, interviewDate, interviewDate);
        assertTrue(isAvail);
    }

    @Test
    @DisplayName("SCENARIO 4: Interview range Sept 15 to Sept 18 overlaps leave -> Excluded")
    void shouldExcludeEvaluatorWhenRangeOverlaps() {
        LocalDate from = LocalDate.of(2026, 9, 15);
        LocalDate to = LocalDate.of(2026, 9, 18);
        boolean isAvail = evaluatorService.isAvailableForDateRange(temporaryLeaveEval, from, to);
        assertFalse(isAvail);
    }

    @Test
    @DisplayName("SCENARIO 5: Evaluator has permanent reason 'Left company' -> Remains unavailable regardless of date")
    void shouldExcludePermanentUnavailableEvaluator() {
        LocalDate futureDate = LocalDate.of(2026, 12, 1);
        boolean isAvail = evaluatorService.isAvailableForDateRange(permanentLeaveEval, futureDate, futureDate);
        assertFalse(isAvail);
    }

    @Test
    @DisplayName("SCENARIO 6 & 7: Update Evaluator A reason -> Only Evaluator A is changed")
    void shouldUpdateOnlyTargetEvaluatorStatusReason() {
        when(evaluatorRepository.findById(2L)).thenReturn(Optional.of(temporaryLeaveEval));
        when(evaluatorRepository.save(any(Evaluator.class))).thenAnswer(i -> i.getArgument(0));

        EvaluatorDTO updateReq = new EvaluatorDTO();
        updateReq.setStatusReason("Medical Leave Extension");
        updateReq.setIsPermanent(false);
        updateReq.setAvailabilityStatus("UNAVAILABLE");
        updateReq.setUnavailableFrom(LocalDate.of(2026, 9, 10));
        updateReq.setUnavailableTo(LocalDate.of(2026, 9, 25));

        EvaluatorDTO result = evaluatorService.updateStatusReason(2L, updateReq);

        assertEquals("Medical Leave Extension", result.getStatusReason());
        assertEquals(LocalDate.of(2026, 9, 25), result.getUnavailableTo());
        verify(excelUploadService, times(1)).updateEvaluatorStatusInExcel(any(Evaluator.class));
        
        // Ensure permanentLeaveEval was not touched
        assertEquals("Left company", permanentLeaveEval.getStatusReason());
    }

    @Test
    @DisplayName("SCENARIO 12: Interview From Date after To Date -> throws IllegalArgumentException")
    void shouldThrowExceptionWhenFromDateAfterToDate() {
        LocalDate from = LocalDate.of(2026, 9, 20);
        LocalDate to = LocalDate.of(2026, 9, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            evaluatorService.getEvaluators(null, null, null, from, to);
        });
        assertEquals("Interview From Date cannot be after To Date", ex.getMessage());
    }

    @Test
    @DisplayName("Missing Evaluator ID -> throws ResourceNotFoundException")
    void shouldThrowWhenEvaluatorNotFound() {
        when(evaluatorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            evaluatorService.getEvaluatorById(999L);
        });
    }
}
