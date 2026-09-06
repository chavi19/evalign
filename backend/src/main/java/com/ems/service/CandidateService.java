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

    private CandidateDTO mapToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setCandidateId(candidate.getCandidateId());
        dto.setCohortId(candidate.getCohort().getCohortId());
        dto.setCandidateName(candidate.getCandidateName());
        return dto;
    }
}
