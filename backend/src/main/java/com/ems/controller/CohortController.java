package com.ems.controller;

import com.ems.dto.CohortDTO;
import com.ems.service.CohortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.ok(cohortService.getCohortById(id));
    }

    @PostMapping
    public ResponseEntity<CohortDTO> createCohort(@RequestBody CohortDTO dto) {
        CohortDTO created = cohortService.createCohort(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CohortDTO> updateCohort(@PathVariable Long id, @RequestBody CohortDTO dto) {
        return ResponseEntity.ok(cohortService.updateCohort(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCohort(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.noContent().build();
    }
}
