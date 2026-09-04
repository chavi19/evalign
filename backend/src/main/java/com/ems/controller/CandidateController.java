package com.ems.controller;

import com.ems.dto.CandidateDTO;
import com.ems.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping({ "/cohorts/{cohortId}/candidates", "/candidates/cohort/{cohortId}" })
    public ResponseEntity<List<CandidateDTO>> getCandidatesByCohort(@PathVariable Long cohortId) {
        return ResponseEntity.ok(candidateService.getCandidatesByCohort(cohortId));
    }

    @PostMapping("/candidates")
    public ResponseEntity<CandidateDTO> createCandidate(@RequestBody CandidateDTO dto) {
        return ResponseEntity.ok(candidateService.createCandidate(dto));
    }

    @PutMapping("/candidates/{id}")
    public ResponseEntity<CandidateDTO> updateCandidate(@PathVariable Long id, @RequestBody CandidateDTO dto) {
        CandidateDTO updated = candidateService.updateCandidate(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
}
