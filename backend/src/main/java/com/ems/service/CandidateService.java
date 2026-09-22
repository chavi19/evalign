package com.ems.service;

import com.ems.dto.CandidateDTO;
import com.ems.entity.Candidate;
import com.ems.entity.Cohort;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.CandidateRepository;
import com.ems.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private CohortRepository cohortRepository;

    public List<CandidateDTO> getCandidatesByCohort(Long cohortId) {
        if (!cohortRepository.existsById(cohortId)) {
            throw new ResourceNotFoundException("Cohort not found with id: " + cohortId);
        }
        return candidateRepository.findByCohortCohortId(cohortId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }
    
    @Transactional
    public CandidateDTO createCandidate(CandidateDTO dto) {
        Cohort cohort = cohortRepository.findById(dto.getCohortId())
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + dto.getCohortId()));
        Candidate candidate = new Candidate();
        candidate.setCohort(cohort);
        candidate.setCandidateName(dto.getCandidateName());
        return mapToDTO(candidateRepository.save(candidate));
    }
    
    @Transactional
    public CandidateDTO updateCandidate(Long id, CandidateDTO dto) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
        candidate.setCandidateName(dto.getCandidateName());
        return mapToDTO(candidateRepository.save(candidate));
    }
    
    @Transactional
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }

    @Transactional
    public List<CandidateDTO> uploadCandidatesFromExcel(Long cohortId, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Candidate Excel file cannot be empty");
        }

        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + cohortId));

        List<Candidate> candidates = new java.util.ArrayList<>();

        try (java.io.InputStream is = file.getInputStream();
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(is)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("Candidate Excel sheet is empty or contains no records");
            }

            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Candidate Excel sheet has no header row");
            }

            int nameCol = -1;
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    String val = getCellString(cell);
                    if (val != null) {
                        val = val.trim().toUpperCase();
                        if (val.contains("CANDIDATE") && val.contains("NAME")) {
                            nameCol = c;
                            break;
                        } else if (val.contains("NAME") || val.contains("TRAINEE") || val.contains("STUDENT")) {
                            nameCol = c;
                        }
                    }
                }
            }

            if (nameCol == -1) {
                if (headerRow.getLastCellNum() > 1) {
                    nameCol = 1;
                } else {
                    nameCol = 0;
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                org.apache.poi.ss.usermodel.Cell cell = row.getCell(nameCol);
                String candidateName = getCellString(cell);
                if (candidateName != null && !candidateName.trim().isEmpty()) {
                    Candidate candidate = new Candidate();
                    candidate.setCohort(cohort);
                    candidate.setCandidateName(candidateName.trim());
                    candidates.add(candidate);
                }
            }

            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("No valid candidate records found in Excel file");
            }

            List<Candidate> saved = candidateRepository.saveAll(candidates);

            int totalCount = candidateRepository.findByCohortCohortId(cohortId).size();
            cohort.setCandidateCount(totalCount);
            cohortRepository.save(cohort);

            return saved.stream().map(this::mapToDTO).collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Candidate Excel file: " + e.getMessage(), e);
        }
    }

    private String getCellString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return null;
    }

    private CandidateDTO mapToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setCandidateId(candidate.getCandidateId());
        dto.setCohortId(candidate.getCohort() != null ? candidate.getCohort().getCohortId() : null);
        dto.setCandidateName(candidate.getCandidateName());
        return dto;
    }
}
