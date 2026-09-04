package com.ems.controller;

import com.ems.dto.CohortDTO;
import com.ems.service.CohortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cohorts")
public class CohortController {

    @Autowired
    private CohortService cohortService;

    @GetMapping
    public ResponseEntity<List<CohortDTO>> getAllCohorts() {
        return ResponseEntity.ok(cohortService.getAllCohorts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CohortDTO> getCohortById(@PathVariable Long id) {
        CohortDTO dto = cohortService.getCohortById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CohortDTO> createCohort(@RequestBody CohortDTO dto) {
        return ResponseEntity.ok(cohortService.createCohort(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CohortDTO> updateCohort(@PathVariable Long id, @RequestBody CohortDTO dto) {
        CohortDTO updated = cohortService.updateCohort(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCohort(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.noContent().build();
    }
}
