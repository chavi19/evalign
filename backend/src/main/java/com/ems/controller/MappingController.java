package com.ems.controller;

import com.ems.dto.AutoMapRequest;
import com.ems.dto.MappingDTO;
import com.ems.dto.MappingRequest;
import com.ems.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MappingController {

    @Autowired
    private MappingService mappingService;

    @GetMapping({ "/cohorts/{cohortId}/mappings", "/mappings/cohort/{cohortId}" })
    public ResponseEntity<List<MappingDTO>> getMappings(
            @PathVariable Long cohortId, 
            @RequestParam(required = false, defaultValue = "INTERIM") String round) {
        return ResponseEntity.ok(mappingService.getMappingsByCohortAndRound(cohortId, round));
    }

    @PostMapping("/mappings")
    public ResponseEntity<MappingDTO> createManualMapping(@RequestBody MappingRequest request) {
        MappingDTO created = mappingService.createManualMapping(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping({ "/cohorts/{cohortId}/auto-map", "/mappings/auto-map" })
    public ResponseEntity<List<MappingDTO>> autoMap(
            @PathVariable(required = false) Long cohortId, 
            @RequestBody AutoMapRequest request) {
        Long targetCohortId = cohortId != null ? cohortId : request.getCohortId();
        List<MappingDTO> mappings = mappingService.autoMap(targetCohortId, request.getRound());
        return ResponseEntity.status(HttpStatus.CREATED).body(mappings);
    }

    @PutMapping("/mappings/{id}/confirm")
    public ResponseEntity<MappingDTO> confirmMapping(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.confirmMapping(id));
    }
}
