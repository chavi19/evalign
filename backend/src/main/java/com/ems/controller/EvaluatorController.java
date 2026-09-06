package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.EvaluatorDTO;
import com.ems.service.EvaluatorService;
import com.ems.service.ExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/evaluators")
public class EvaluatorController {

    @Autowired
    private EvaluatorService evaluatorService;
    
    @Autowired
    private ExcelUploadService excelUploadService;

    @GetMapping
    public ResponseEntity<List<EvaluatorDTO>> getEvaluators(
            @RequestParam(required = false) String vertical,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String availability) {
        return ResponseEntity.ok(evaluatorService.getEvaluators(vertical, domain, availability));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluatorDTO> getEvaluatorById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluatorService.getEvaluatorById(id));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<EvaluatorDTO> updateAvailability(@PathVariable Long id, @RequestBody EvaluatorDTO dto) {
        return ResponseEntity.ok(evaluatorService.updateAvailability(id, dto));
    }
    
    @GetMapping("/verticals")
    public ResponseEntity<List<String>> getVerticals() {
        return ResponseEntity.ok(evaluatorService.getVerticals());
    }

    @GetMapping("/domains")
    public ResponseEntity<List<String>> getDomains() {
        return ResponseEntity.ok(evaluatorService.getDomains());
    }
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            excelUploadService.processExcel(file);
            return ResponseEntity.ok(new ApiResponse(true, "Excel file uploaded and processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to process Excel file: " + e.getMessage()));
        }
    }
}
