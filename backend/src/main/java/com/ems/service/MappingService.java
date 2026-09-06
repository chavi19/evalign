package com.ems.service;

import com.ems.dto.MappingDTO;
import com.ems.dto.MappingRequest;
import com.ems.entity.*;
import com.ems.exception.BusinessRuleException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MappingService {

    @Autowired
    private EvaluatorMappingRepository mappingRepository;
    @Autowired
    private CohortRepository cohortRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private EvaluatorRepository evaluatorRepository;
    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;
    @Autowired
    private UserRepository userRepository;

    public List<MappingDTO> getMappingsByCohortAndRound(Long cohortId, String round) {
        if (!cohortRepository.existsById(cohortId)) {
            throw new ResourceNotFoundException("Cohort not found with id: " + cohortId);
        }
        List<EvaluatorMapping> existingMappings = mappingRepository.findByCohortCohortIdAndRound(cohortId, round);

        List<MappingDTO> result = new ArrayList<>();
        for (EvaluatorMapping m : existingMappings) {
            result.add(mapToDTO(m));
        }

        return result;
    }

    @Transactional
    public MappingDTO createManualMapping(MappingRequest request) {
        Cohort cohort = cohortRepository.findById(request.getCohortId())
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + request.getCohortId()));
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + request.getCandidateId()));
        Evaluator evaluator = evaluatorRepository.findById(request.getEvaluatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + request.getEvaluatorId()));
        
        validateMappingRules(cohort, candidate, evaluator, request.getRound());
        
        String email = SecurityContextHolder.getContext().getAuthentication() != null ? 
                SecurityContextHolder.getContext().getAuthentication().getName() : "admin@example.com";
        User poc = userRepository.findByEmail(email).orElse(null);

        int attempt = request.getAttempt() != null ? request.getAttempt() : 1;
        List<EvaluatorMapping> candidateMappings = mappingRepository.findByCandidateCandidateId(candidate.getCandidateId());
        
        Optional<EvaluatorMapping> existingSuggested = candidateMappings.stream()
                .filter(m -> request.getRound().equalsIgnoreCase(m.getRound()) && m.getAttempt().equals(attempt) && !"CONFIRMED".equalsIgnoreCase(m.getStatus()))
                .findFirst();

        EvaluatorMapping mapping = existingSuggested.orElse(new EvaluatorMapping());
        mapping.setCohort(cohort);
        mapping.setCandidate(candidate);
        mapping.setEvaluator(evaluator);
        mapping.setRound(request.getRound());
        mapping.setAttempt(attempt);
        mapping.setStatus("SUGGESTED");
        mapping.setMappedBy(poc);
        mapping.setMappedAt(LocalDateTime.now());

        return mapToDTO(mappingRepository.save(mapping));
    }

    @Transactional
    public List<MappingDTO> autoMap(Long cohortId, String round) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + cohortId));
        List<Candidate> candidates = candidateRepository.findByCohortCohortId(cohortId);
        List<EvaluatorShortlist> shortlist = shortlistRepository.findByCohortCohortId(cohortId);
        
        String email = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "admin@example.com";
        User poc = userRepository.findByEmail(email).orElse(null);

        List<Evaluator> availableEvaluators = shortlist.stream()
                .map(EvaluatorShortlist::getEvaluator)
                .filter(e -> "AVAILABLE".equalsIgnoreCase(e.getAvailabilityStatus()))
                .collect(Collectors.toList());

        if (availableEvaluators.isEmpty()) {
            throw new BusinessRuleException("Evaluator is not available");
        }

        for (Candidate candidate : candidates) {
            List<EvaluatorMapping> candidateMappings = mappingRepository.findByCandidateCandidateId(candidate.getCandidateId());
            
            long confirmedCount = candidateMappings.stream()
                    .filter(m -> round.equalsIgnoreCase(m.getRound()) && "CONFIRMED".equalsIgnoreCase(m.getStatus()))
                    .count();

            int nextAttempt = (int) confirmedCount + 1;

            // Find best evaluator with lowest workload
            Evaluator bestEvaluator = null;
            long minWorkload = Long.MAX_VALUE;

            for (Evaluator eval : availableEvaluators) {
                if (canMap(cohort, candidate, eval, round)) {
                    long workload = mappingRepository.countByEvaluatorEvaluatorIdAndRound(eval.getEvaluatorId(), round);
                    if (workload < minWorkload) {
                        minWorkload = workload;
                        bestEvaluator = eval;
                    }
                }
            }

            if (bestEvaluator != null) {
                Optional<EvaluatorMapping> existingSuggested = candidateMappings.stream()
                        .filter(m -> round.equalsIgnoreCase(m.getRound()) && m.getAttempt().equals(nextAttempt) && !"CONFIRMED".equalsIgnoreCase(m.getStatus()))
                        .findFirst();

                EvaluatorMapping mapping = existingSuggested.orElse(new EvaluatorMapping());
                mapping.setCohort(cohort);
                mapping.setCandidate(candidate);
                mapping.setEvaluator(bestEvaluator);
                mapping.setRound(round);
                mapping.setAttempt(nextAttempt);
                mapping.setStatus("SUGGESTED");
                mapping.setMappedBy(poc);
                mapping.setMappedAt(LocalDateTime.now());
                mappingRepository.save(mapping);
            }
        }

        return getMappingsByCohortAndRound(cohortId, round);
    }
    
    @Transactional
    public MappingDTO confirmMapping(Long mappingId) {
        EvaluatorMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + mappingId));
        
        validateMappingRules(mapping.getCohort(), mapping.getCandidate(), mapping.getEvaluator(), mapping.getRound());
        
        mapping.setStatus("CONFIRMED");
        mapping.setMappedAt(LocalDateTime.now());
        return mapToDTO(mappingRepository.save(mapping));
    }

    public void validateMappingRules(Cohort cohort, Candidate candidate, Evaluator evaluator, String round) {
        // RULE 1: Only evaluators assigned to the selected cohort can be mapped.
        Optional<EvaluatorShortlist> shortlist = shortlistRepository.findByCohortCohortIdAndEvaluatorEvaluatorId(cohort.getCohortId(), evaluator.getEvaluatorId());
        if (shortlist.isEmpty()) {
            throw new BusinessRuleException("Evaluator is not assigned to this cohort");
        }

        // RULE 2: Only available/eligible evaluators can be mapped.
        if (!"AVAILABLE".equalsIgnoreCase(evaluator.getAvailabilityStatus())) {
            throw new BusinessRuleException("Evaluator is not available");
        }

        // RULE 3 & RULE 4:
        List<EvaluatorMapping> existingMappings = mappingRepository.findByCandidateCandidateId(candidate.getCandidateId());
        
        for (EvaluatorMapping m : existingMappings) {
            if (m.getEvaluator().getEvaluatorId().equals(evaluator.getEvaluatorId())) {
                // RULE 3: The evaluator used for a candidate's Interim interview MUST NOT be used for that candidate's Final interview.
                if ("FINAL".equalsIgnoreCase(round) && "INTERIM".equalsIgnoreCase(m.getRound())) {
                    throw new BusinessRuleException("Blocked: same evaluator as Interim");
                }
                if ("INTERIM".equalsIgnoreCase(round) && "FINAL".equalsIgnoreCase(m.getRound())) {
                    throw new BusinessRuleException("Blocked: same evaluator as Final");
                }
                // RULE 4: If a candidate has another Final attempt, every evaluator previously used for that candidate's Final attempts MUST be excluded.
                if ("FINAL".equalsIgnoreCase(round) && "FINAL".equalsIgnoreCase(m.getRound())) {
                    throw new BusinessRuleException("New evaluator required");
                }
                if ("INTERIM".equalsIgnoreCase(round) && "INTERIM".equalsIgnoreCase(m.getRound()) && "CONFIRMED".equalsIgnoreCase(m.getStatus())) {
                    throw new BusinessRuleException("New evaluator required");
                }
            }
        }
    }

    public boolean canMap(Cohort cohort, Candidate candidate, Evaluator evaluator, String round) {
        try {
            validateMappingRules(cohort, candidate, evaluator, round);
            return true;
        } catch (BusinessRuleException e) {
            return false;
        }
    }

    public String checkWarning(Cohort cohort, Candidate candidate, Evaluator evaluator, String round) {
        try {
            validateMappingRules(cohort, candidate, evaluator, round);
            return null;
        } catch (BusinessRuleException e) {
            return e.getMessage();
        }
    }

    public MappingDTO mapToDTO(EvaluatorMapping m) {
        MappingDTO dto = new MappingDTO();
        dto.setMappingId(m.getMappingId());
        dto.setCohortId(m.getCohort() != null ? m.getCohort().getCohortId() : null);
        dto.setCohortName(m.getCohort() != null ? m.getCohort().getCohortName() : "");
        dto.setCandidateId(m.getCandidate() != null ? m.getCandidate().getCandidateId() : null);
        dto.setCandidateName(m.getCandidate() != null ? m.getCandidate().getCandidateName() : "");
        dto.setEvaluatorId(m.getEvaluator() != null ? m.getEvaluator().getEvaluatorId() : null);
        dto.setEvaluatorName(m.getEvaluator() != null ? m.getEvaluator().getName() : "");
        dto.setRound(m.getRound());
        dto.setAttempt(m.getAttempt());
        dto.setStatus(m.getStatus());
        dto.setMappedByName(m.getMappedBy() != null ? m.getMappedBy().getName() : "");
        dto.setMappedAt(m.getMappedAt());
        dto.setEvaluatorAvailability(m.getEvaluator() != null ? m.getEvaluator().getAvailabilityStatus() : "");

        if (m.getCandidate() != null) {
            List<EvaluatorMapping> candidateMappings = mappingRepository.findByCandidateCandidateId(m.getCandidate().getCandidateId());
            candidateMappings.stream()
                    .filter(map -> "INTERIM".equalsIgnoreCase(map.getRound()))
                    .findFirst()
                    .ifPresent(interimMap -> {
                        dto.setInterimEvaluatorId(interimMap.getEvaluator().getEvaluatorId());
                        dto.setInterimEvaluatorName(interimMap.getEvaluator().getName());
                    });
        }

        if (m.getCohort() != null && m.getCandidate() != null && m.getEvaluator() != null) {
            dto.setRuleWarning(checkWarning(m.getCohort(), m.getCandidate(), m.getEvaluator(), m.getRound()));
        }

        return dto;
    }
}
