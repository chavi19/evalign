package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.entity.Evaluator;
import com.ems.service.ShortlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cohorts")
public class ShortlistController {

    @Autowired
    private ShortlistService shortlistService;

    @GetMapping("/{cohortId}/shortlist")
    public ResponseEntity<List<Evaluator>> getShortlist(@PathVariable Long cohortId) {
        return ResponseEntity.ok(shortlistService.getShortlistByCohort(cohortId));
    }

    @PostMapping({ "/{cohortId}/shortlist/{evaluatorId}", "/{cohortId}/evaluators/{evaluatorId}" })
    public ResponseEntity<ApiResponse> addToShortlist(@PathVariable Long cohortId, @PathVariable Long evaluatorId) {
        shortlistService.addToShortlist(cohortId, evaluatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Evaluator added to cohort shortlist"));
    }

    @DeleteMapping("/{cohortId}/shortlist/{evaluatorId}")
    public ResponseEntity<Void> removeFromShortlist(@PathVariable Long cohortId, @PathVariable Long evaluatorId) {
        shortlistService.removeFromShortlist(cohortId, evaluatorId);
        return ResponseEntity.noContent().build();
    }
}
