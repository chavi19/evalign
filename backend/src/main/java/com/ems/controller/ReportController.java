package com.ems.controller;

import com.ems.dto.MappingDTO;
import com.ems.repository.EvaluatorMappingRepository;
import com.ems.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private EvaluatorMappingRepository mappingRepository;

    @Autowired
    private MappingService mappingService;

    @GetMapping("/mappings")
    public ResponseEntity<List<MappingDTO>> getMappingsReport(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cohortName,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String status) {

        java.util.List<com.ems.entity.EvaluatorMapping> allMappings = mappingRepository.findAll();
        java.util.stream.Stream<com.ems.entity.EvaluatorMapping> stream = allMappings.stream();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            stream = stream.filter(m ->
                (m.getCandidate() != null && m.getCandidate().getCandidateName() != null && m.getCandidate().getCandidateName().toLowerCase().contains(q)) ||
                (m.getEvaluator() != null && m.getEvaluator().getName() != null && m.getEvaluator().getName().toLowerCase().contains(q))
            );
        }

        if (cohortName != null && !cohortName.trim().isEmpty() && !"All".equalsIgnoreCase(cohortName)) {
            stream = stream.filter(m -> m.getCohort() != null && cohortName.equalsIgnoreCase(m.getCohort().getCohortName()));
        }

        if (stage != null && !stage.trim().isEmpty() && !"All".equalsIgnoreCase(stage)) {
            stream = stream.filter(m -> m.getRound() != null && stage.equalsIgnoreCase(m.getRound()));
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            stream = stream.filter(m -> m.getStatus() != null && status.equalsIgnoreCase(m.getStatus()));
        }

        stream = stream.sorted(java.util.Comparator.comparing(com.ems.entity.EvaluatorMapping::getMappedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                .thenComparing(com.ems.entity.EvaluatorMapping::getMappingId, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        if (page != null && size != null && page >= 0 && size > 0) {
            stream = stream.skip((long) page * size).limit(size);
        }

        return ResponseEntity.ok(stream.map(mappingService::mapToDTO).collect(Collectors.toList()));
    }
}

