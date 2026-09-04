package com.ems.service;

import com.ems.dto.CohortDTO;
import com.ems.entity.Cohort;
import com.ems.entity.User;
import com.ems.repository.CohortRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CohortService {

    @Autowired
    private CohortRepository cohortRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<CohortDTO> getAllCohorts() {
        return cohortRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    public CohortDTO getCohortById(Long id) {
        return cohortRepository.findById(id).map(this::mapToDTO).orElse(null);
    }
    
    public CohortDTO createCohort(CohortDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User poc = userRepository.findByEmail(email).orElse(null);
        
        Cohort cohort = new Cohort();
        cohort.setCohortName(dto.getCohortName());
        cohort.setBatchCode(dto.getBatchCode());
        cohort.setCandidateCount(dto.getCandidateCount());
        cohort.setStartDate(dto.getStartDate());
        cohort.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        cohort.setPoc(poc);
        
        return mapToDTO(cohortRepository.save(cohort));
    }
    
    public CohortDTO updateCohort(Long id, CohortDTO dto) {
        return cohortRepository.findById(id).map(cohort -> {
            cohort.setCohortName(dto.getCohortName());
            cohort.setBatchCode(dto.getBatchCode());
            cohort.setCandidateCount(dto.getCandidateCount());
            cohort.setStartDate(dto.getStartDate());
            cohort.setStatus(dto.getStatus());
            return mapToDTO(cohortRepository.save(cohort));
        }).orElse(null);
    }
    
    public void deleteCohort(Long id) {
        cohortRepository.deleteById(id);
    }
    
    @Autowired
    private com.ems.repository.CandidateRepository candidateRepository;

    @Autowired
    private com.ems.repository.EvaluatorMappingRepository mappingRepository;

    private CohortDTO mapToDTO(Cohort cohort) {
        CohortDTO dto = new CohortDTO();
        dto.setCohortId(cohort.getCohortId());
        dto.setCohortName(cohort.getCohortName());
        dto.setBatchCode(cohort.getBatchCode());
        int realCandCount = candidateRepository.findByCohortCohortId(cohort.getCohortId()).size();
        dto.setCandidateCount(realCandCount > 0 ? realCandCount : (cohort.getCandidateCount() != null ? cohort.getCandidateCount() : 0));
        dto.setStartDate(cohort.getStartDate());
        dto.setStatus(cohort.getStatus());
        long mappedCount = mappingRepository.findByCohortCohortId(cohort.getCohortId()).stream()
                .filter(m -> "CONFIRMED".equalsIgnoreCase(m.getStatus()))
                .map(m -> m.getCandidate().getCandidateId()).distinct().count();
        dto.setEvaluatorsMapped((int) mappedCount);
        return dto;
    }
}
