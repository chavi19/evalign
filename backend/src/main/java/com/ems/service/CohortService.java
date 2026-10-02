package com.ems.service;

import com.ems.dto.CohortDTO;
import com.ems.entity.Cohort;
import com.ems.entity.User;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.CandidateRepository;
import com.ems.repository.CohortRepository;
import com.ems.repository.EvaluatorMappingRepository;
import com.ems.repository.EvaluatorShortlistRepository;
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

    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;

    public List<CohortDTO> getAllCohorts(Integer page, Integer size, String search, String status) {
        java.util.stream.Stream<Cohort> stream = cohortRepository.findAll().stream();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            stream = stream.filter(c ->
                (c.getCohortName() != null && c.getCohortName().toLowerCase().contains(q)) ||
                (c.getBatchCode() != null && c.getBatchCode().toLowerCase().contains(q))
            );
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            String s = status.trim().toLowerCase().replace('_', ' ');
            stream = stream.filter(c -> {
                String cs = (c.getStatus() != null ? c.getStatus() : "").trim().toLowerCase().replace('_', ' ');
                return cs.equalsIgnoreCase(s) || (s.equals("active") && cs.contains("active")) || (s.contains("progress") && cs.contains("progress"));
            });
        }

        if (page != null && size != null && page >= 0 && size > 0) {
            stream = stream.skip((long) page * size).limit(size);
        }

        return stream.map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CohortDTO> getAllCohorts() {
        return getAllCohorts(null, null, null, null);
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
        cohort.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");
        cohort.setPoc(poc);
        
        return mapToDTO(cohortRepository.save(cohort));
    }
    
    @Transactional
    public CohortDTO updateCohort(Long id, CohortDTO dto) {
        Cohort cohort = cohortRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + id));
        
        if (dto.getCohortName() != null) {
            cohort.setCohortName(dto.getCohortName());
        }
        if (dto.getBatchCode() != null) {
            cohort.setBatchCode(dto.getBatchCode());
        }
        if (dto.getCandidateCount() != null) {
            cohort.setCandidateCount(dto.getCandidateCount());
        }
        if (dto.getStartDate() != null) {
            cohort.setStartDate(dto.getStartDate());
        }
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
        mappingRepository.deleteByCohortCohortId(id);
        shortlistRepository.deleteByCohortCohortId(id);
        candidateRepository.deleteByCohortCohortId(id);
        cohortRepository.deleteById(id);
    }

    private CohortDTO mapToDTO(Cohort cohort) {
        CohortDTO dto = new CohortDTO();
        dto.setCohortId(cohort.getCohortId());
        dto.setCohortName(cohort.getCohortName());
        dto.setBatchCode(cohort.getBatchCode());
        int count = (int) candidateRepository.countByCohortCohortId(cohort.getCohortId());
        dto.setCandidateCount(count > 0 ? count : (cohort.getCandidateCount() != null ? cohort.getCandidateCount() : 0));
        dto.setStartDate(cohort.getStartDate());
        dto.setStatus(cohort.getStatus());
        long mappedCount = mappingRepository.countConfirmedMappedCandidatesByCohortCohortId(cohort.getCohortId());
        dto.setEvaluatorsMapped((int) mappedCount);
        return dto;
    }
}
