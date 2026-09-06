package com.ems.service;

import com.ems.dto.CohortDTO;
import com.ems.entity.Cohort;
import com.ems.entity.User;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.CandidateRepository;
import com.ems.repository.CohortRepository;
import com.ems.repository.EvaluatorMappingRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CohortService {

    @Autowired
    private CohortRepository cohortRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EvaluatorMappingRepository mappingRepository;

    public List<CohortDTO> getAllCohorts() {
        return cohortRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    public CohortDTO getCohortById(Long id) {
        Cohort cohort = cohortRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + id));
        return mapToDTO(cohort);
    }
    
    @Transactional
    public CohortDTO createCohort(CohortDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "admin@example.com";
        User poc = userRepository.findByEmail(email).orElse(null);
        
        Cohort cohort = new Cohort();
        cohort.setCohortName(dto.getCohortName());
        cohort.setBatchCode(dto.getBatchCode());
        cohort.setCandidateCount(dto.getCandidateCount() != null ? dto.getCandidateCount() : 0);
        cohort.setStartDate(dto.getStartDate());
        cohort.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        cohort.setPoc(poc);
        
        return mapToDTO(cohortRepository.save(cohort));
    }
    
    @Transactional
    public CohortDTO updateCohort(Long id, CohortDTO dto) {
        Cohort cohort = cohortRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + id));
        
        cohort.setCohortName(dto.getCohortName());
        cohort.setBatchCode(dto.getBatchCode());
        cohort.setCandidateCount(dto.getCandidateCount() != null ? dto.getCandidateCount() : cohort.getCandidateCount());
        cohort.setStartDate(dto.getStartDate());
        if (dto.getStatus() != null) {
            cohort.setStatus(dto.getStatus());
        }
        return mapToDTO(cohortRepository.save(cohort));
    }
    
    @Transactional
    public void deleteCohort(Long id) {
        if (!cohortRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cohort not found with id: " + id);
        }
        cohortRepository.deleteById(id);
    }

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
