package com.ems.service;

import com.ems.entity.Cohort;
import com.ems.entity.Evaluator;
import com.ems.entity.EvaluatorShortlist;
import com.ems.entity.User;
import com.ems.exception.BusinessRuleException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.CohortRepository;
import com.ems.repository.EvaluatorRepository;
import com.ems.repository.EvaluatorShortlistRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShortlistService {

    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Evaluator> getShortlistByCohort(Long cohortId) {
        if (!cohortRepository.existsById(cohortId)) {
            throw new ResourceNotFoundException("Cohort not found with id: " + cohortId);
        }
        return shortlistRepository.findByCohortCohortId(cohortId).stream()
                .map(EvaluatorShortlist::getEvaluator)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToShortlist(Long cohortId, Long evaluatorId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found with id: " + cohortId));
        Evaluator evaluator = evaluatorRepository.findById(evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + evaluatorId));

        if (shortlistRepository.findByCohortCohortIdAndEvaluatorEvaluatorId(cohortId, evaluatorId).isPresent()) {
            throw new BusinessRuleException("Evaluator is already shortlisted for this cohort");
        }

        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "admin@example.com";
        User poc = userRepository.findByEmail(email).orElse(null);

        EvaluatorShortlist shortlist = new EvaluatorShortlist();
        shortlist.setCohort(cohort);
        shortlist.setEvaluator(evaluator);
        shortlist.setAddedBy(poc);
        shortlist.setAddedAt(LocalDateTime.now());
        shortlistRepository.save(shortlist);
    }

    @Transactional
    public void removeFromShortlist(Long cohortId, Long evaluatorId) {
        EvaluatorShortlist shortlist = shortlistRepository.findByCohortCohortIdAndEvaluatorEvaluatorId(cohortId, evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist entry not found for cohort " + cohortId + " and evaluator " + evaluatorId));
        shortlistRepository.delete(shortlist);
    }
}
