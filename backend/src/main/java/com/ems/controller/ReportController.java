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
    public ResponseEntity<List<MappingDTO>> getMappingsReport() {
        return ResponseEntity.ok(mappingRepository.findAll().stream()
                .map(mappingService::mapToDTO)
                .collect(Collectors.toList()));
    }
}

