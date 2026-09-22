package com.ems.service;

import com.ems.dto.CandidateDTO;
import com.ems.entity.Candidate;
import com.ems.entity.Cohort;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.CandidateRepository;
import com.ems.repository.CohortRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CohortRepository cohortRepository;

    @InjectMocks
    private CandidateService candidateService;

    private Cohort cohort;

    @BeforeEach
    public void setup() {
        cohort = new Cohort();
        cohort.setCohortId(1L);
        cohort.setCohortName("Batch-1");
        cohort.setBatchCode("JAVA-TRACK");
        cohort.setCandidateCount(0);
    }

    @Test
    public void testUploadCandidatesFromExcel_Success() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Candidates");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("CANDIDATE ID");
            header.createCell(1).setCellValue("CANDIDATE NAME");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("CAND-01");
            r1.createCell(1).setCellValue("Aarav Mehta");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("CAND-02");
            r2.createCell(1).setCellValue("Diya Sen");

            wb.write(bos);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "candidates.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );

        Mockito.when(cohortRepository.findById(1L)).thenReturn(Optional.of(cohort));
        Mockito.when(candidateRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Candidate> list = invocation.getArgument(0);
            for (long i = 0; i < list.size(); i++) {
                list.get((int) i).setCandidateId(i + 1);
            }
            return list;
        });

        List<Candidate> cohortCandidates = new ArrayList<>();
        Candidate c1 = new Candidate(); c1.setCandidateId(1L); c1.setCandidateName("Aarav Mehta"); c1.setCohort(cohort);
        Candidate c2 = new Candidate(); c2.setCandidateId(2L); c2.setCandidateName("Diya Sen"); c2.setCohort(cohort);
        cohortCandidates.add(c1);
        cohortCandidates.add(c2);
        Mockito.when(candidateRepository.findByCohortCohortId(1L)).thenReturn(cohortCandidates);

        List<CandidateDTO> result = candidateService.uploadCandidatesFromExcel(1L, file);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Aarav Mehta", result.get(0).getCandidateName());
        assertEquals("Diya Sen", result.get(1).getCandidateName());
        Mockito.verify(cohortRepository).save(cohort);
        assertEquals(2, cohort.getCandidateCount());
    }

    @Test
    public void testUploadCandidates_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx", "application/vnd.ms-excel", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> candidateService.uploadCandidatesFromExcel(1L, file));
    }

    @Test
    public void testUploadCandidates_CohortNotFound_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "candidates.xlsx", "application/vnd.ms-excel", "content".getBytes());
        Mockito.when(cohortRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> candidateService.uploadCandidatesFromExcel(99L, file));
    }
}
