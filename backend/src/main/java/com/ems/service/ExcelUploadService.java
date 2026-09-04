package com.ems.service;

import com.ems.entity.Evaluator;
import com.ems.repository.EvaluatorRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelUploadService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    public void processExcel(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Evaluator> evaluators = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String empId = getStringCellValue(row.getCell(0));
                if (empId == null || empId.trim().isEmpty()) continue;
                
                String name = getStringCellValue(row.getCell(1));
                String vertical = getStringCellValue(row.getCell(2));
                String domain = getStringCellValue(row.getCell(3));
                String availability = getStringCellValue(row.getCell(4));
                
                Evaluator eval = evaluatorRepository.findByEmpId(empId).orElse(new Evaluator());
                eval.setEmpId(empId);
                eval.setName(name);
                eval.setVertical(vertical);
                eval.setDomain(domain);
                eval.setAvailabilityStatus(availability != null ? availability.toUpperCase() : "AVAILABLE");
                
                // Simplified date handling
                if ("UNAVAILABLE".equals(eval.getAvailabilityStatus())) {
                    if (row.getCell(5) != null && row.getCell(5).getCellType() == CellType.NUMERIC) {
                        eval.setUnavailableFrom(row.getCell(5).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    }
                    if (row.getCell(6) != null && row.getCell(6).getCellType() == CellType.NUMERIC) {
                        eval.setUnavailableTo(row.getCell(6).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    }
                } else {
                    eval.setUnavailableFrom(null);
                    eval.setUnavailableTo(null);
                }

                evaluators.add(eval);
            }
            
            evaluatorRepository.saveAll(evaluators);
        }
    }
    
    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long)cell.getNumericCellValue());
        }
        return null;
    }
}
