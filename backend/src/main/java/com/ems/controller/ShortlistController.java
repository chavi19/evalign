package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.entity.Evaluator;
import com.ems.entity.EvaluatorShortlist;
import com.ems.entity.User;
import com.ems.repository.CohortRepository;
import com.ems.repository.EvaluatorRepository;
import com.ems.repository.EvaluatorShortlistRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cohorts")
public class ShortlistController {

    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;
    @Autowired
    private CohortRepository cohortRepository;
    @Autowired
    private EvaluatorRepository evaluatorRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{cohortId}/shortlist")
    public ResponseEntity<List<Evaluator>> getShortlist(@PathVariable Long cohortId) {
        List<Evaluator> evaluators = shortlistRepository.findByCohortCohortId(cohortId).stream()
                .map(EvaluatorShortlist::getEvaluator)
                .collect(Collectors.toList());
        return ResponseEntity.ok(evaluators);
    }

    @PostMapping({ "/{cohortId}/shortlist/{evaluatorId}", "/{cohortId}/evaluators/{evaluatorId}" })
    public ResponseEntity<ApiResponse> addToShortlist(@PathVariable Long cohortId, @PathVariable Long evaluatorId) {
        if (shortlistRepository.findByCohortCohortIdAndEvaluatorEvaluatorId(cohortId, evaluatorId).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Evaluator already in shortlist"));
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User poc = userRepository.findByEmail(email).orElse(null);

        EvaluatorShortlist s = new EvaluatorShortlist();
        s.setCohort(cohortRepository.findById(cohortId).orElseThrow());
        s.setEvaluator(evaluatorRepository.findById(evaluatorId).orElseThrow());
        s.setAddedBy(poc);
        shortlistRepository.save(s);
        
        return ResponseEntity.ok(new ApiResponse(true, "Added to shortlist"));
    }

    @DeleteMapping("/{cohortId}/shortlist/{evaluatorId}")
    public ResponseEntity<ApiResponse> removeFromShortlist(@PathVariable Long cohortId, @PathVariable Long evaluatorId) {
        EvaluatorShortlist s = shortlistRepository.findByCohortCohortIdAndEvaluatorEvaluatorId(cohortId, evaluatorId).orElseThrow();
        shortlistRepository.delete(s);
        return ResponseEntity.ok(new ApiResponse(true, "Removed from shortlist"));
    }
}
