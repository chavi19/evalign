package com.ems.service;

import com.ems.dto.CandidateDTO;
import com.ems.entity.Candidate;
import com.ems.entity.Cohort;
import com.ems.repository.CandidateRepository;
import com.ems.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private CohortRepository cohortRepository;

    public List<CandidateDTO> getCandidatesByCohort(Long cohortId) {
        return candidateRepository.findByCohortCohortId(cohortId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }
    
    public CandidateDTO createCandidate(CandidateDTO dto) {
        Cohort cohort = cohortRepository.findById(dto.getCohortId()).orElseThrow();
        Candidate candidate = new Candidate();
        candidate.setCohort(cohort);
        candidate.setCandidateName(dto.getCandidateName());
        return mapToDTO(candidateRepository.save(candidate));
    }
    
    public CandidateDTO updateCandidate(Long id, CandidateDTO dto) {
        return candidateRepository.findById(id).map(candidate -> {
            candidate.setCandidateName(dto.getCandidateName());
            return mapToDTO(candidateRepository.save(candidate));
        }).orElse(null);
    }
    
    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

    private CandidateDTO mapToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setCandidateId(candidate.getCandidateId());
        dto.setCohortId(candidate.getCohort().getCohortId());
        dto.setCandidateName(candidate.getCandidateName());
        return dto;
    }
}
