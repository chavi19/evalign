package com.ems.service;

import com.ems.entity.Evaluator;
import com.ems.repository.EvaluatorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelUploadService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    private static final String UPLOAD_DIR = "uploads";
    private static final String MASTER_FILE_NAME = "master_evaluators.xlsx";

    private Path getMasterFilePath() {
        Path dir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                // fallback to current directory
                return Paths.get(MASTER_FILE_NAME);
            }
        }
        return dir.resolve(MASTER_FILE_NAME);
    }

    public void processExcel(MultipartFile file) throws Exception {
        Path targetPath = getMasterFilePath();
        // Save uploaded file to disk
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try (InputStream is = Files.newInputStream(targetPath);
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            List<Evaluator> evaluators = new ArrayList<>();

            // Identify header columns
            Row headerRow = sheet.getRow(0);
            int empIdCol = 0, nameCol = 1, verticalCol = 2, domainCol = 3, availCol = 4;
            int fromCol = 5, toCol = 6, reasonCol = 7, permCol = 8;

            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String val = getStringCellValue(headerRow.getCell(c));
                    if (val != null) {
                        val = val.trim().toUpperCase();
                        if (val.contains("EMP") && val.contains("ID")) empIdCol = c;
                        else if (val.contains("NAME")) nameCol = c;
                        else if (val.contains("VERTICAL")) verticalCol = c;
                        else if (val.contains("DOMAIN")) domainCol = c;
                        else if (val.contains("AVAILABILITY")) availCol = c;
                        else if (val.contains("FROM")) fromCol = c;
                        else if (val.contains("TO")) toCol = c;
                        else if (val.contains("REASON")) reasonCol = c;
                        else if (val.contains("PERMANENT")) permCol = c;
                    }
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String empId = getStringCellValue(row.getCell(empIdCol));
                if (empId == null || empId.trim().isEmpty()) continue;
                
                String name = getStringCellValue(row.getCell(nameCol));
                String vertical = getStringCellValue(row.getCell(verticalCol));
                String domain = getStringCellValue(row.getCell(domainCol));
                String availability = getStringCellValue(row.getCell(availCol));
                String statusReason = getStringCellValue(row.getCell(reasonCol));
                String isPermStr = getStringCellValue(row.getCell(permCol));
                boolean isPermanent = "YES".equalsIgnoreCase(isPermStr) || "TRUE".equalsIgnoreCase(isPermStr);

                Evaluator eval = evaluatorRepository.findByEmpId(empId).orElse(new Evaluator());
                eval.setEmpId(empId);
                eval.setName(name);
                eval.setVertical(vertical);
                eval.setDomain(domain);
                eval.setAvailabilityStatus(availability != null ? availability.toUpperCase() : "AVAILABLE");
                eval.setStatusReason(statusReason);
                eval.setIsPermanent(isPermanent);
                
                if ("UNAVAILABLE".equals(eval.getAvailabilityStatus())) {
                    eval.setUnavailableFrom(parseLocalDate(row.getCell(fromCol)));
                    eval.setUnavailableTo(parseLocalDate(row.getCell(toCol)));
                } else {
                    eval.setUnavailableFrom(null);
                    eval.setUnavailableTo(null);
                }

                evaluators.add(eval);
            }
            
            evaluatorRepository.saveAll(evaluators);
        }
    }

    public synchronized void updateEvaluatorStatusInExcel(Evaluator evaluator) {
        Path targetPath = getMasterFilePath();
        Workbook workbook = null;
        boolean fileExists = Files.exists(targetPath);

        try {
            if (fileExists) {
                try (InputStream is = Files.newInputStream(targetPath)) {
                    workbook = WorkbookFactory.create(is);
                }
            } else {
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Evaluators");
                Row header = sheet.createRow(0);
                String[] headers = {"EMP ID", "NAME", "VERTICAL", "DOMAIN", "AVAILABILITY", "UNAVAILABLE FROM", "UNAVAILABLE TO", "STATUS REASON", "PERMANENT"};
                
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Add all existing evaluators from db
                List<Evaluator> all = evaluatorRepository.findAll();
                int rIdx = 1;
                for (Evaluator e : all) {
                    Row r = sheet.createRow(rIdx++);
                    writeEvaluatorRow(r, e);
                }
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int empIdCol = 0, availCol = 4, fromCol = 5, toCol = 6, reasonCol = 7, permCol = 8;

            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String val = getStringCellValue(headerRow.getCell(c));
                    if (val != null) {
                        val = val.trim().toUpperCase();
                        if (val.contains("EMP") && val.contains("ID")) empIdCol = c;
                        else if (val.contains("AVAILABILITY")) availCol = c;
                        else if (val.contains("FROM")) fromCol = c;
                        else if (val.contains("TO")) toCol = c;
                        else if (val.contains("REASON")) reasonCol = c;
                        else if (val.contains("PERMANENT")) permCol = c;
                    }
                }
                
                // Ensure Reason and Permanent headers exist if needed
                if (reasonCol == -1 || reasonCol >= headerRow.getLastCellNum()) {
                    reasonCol = headerRow.getLastCellNum();
                    Cell c = headerRow.createCell(reasonCol);
                    c.setCellValue("STATUS REASON");
                }
                if (permCol == -1 || permCol >= headerRow.getLastCellNum()) {
                    permCol = headerRow.getLastCellNum();
                    Cell c = headerRow.createCell(permCol);
                    c.setCellValue("PERMANENT");
                }
            }

            // Find and update exact evaluator row
            boolean found = false;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String empId = getStringCellValue(row.getCell(empIdCol));
                if (evaluator.getEmpId().equalsIgnoreCase(empId)) {
                    // Update only this row
                    updateCell(row, availCol, evaluator.getAvailabilityStatus());
                    updateCell(row, fromCol, evaluator.getUnavailableFrom() != null ? evaluator.getUnavailableFrom().toString() : "");
                    updateCell(row, toCol, evaluator.getUnavailableTo() != null ? evaluator.getUnavailableTo().toString() : "");
                    updateCell(row, reasonCol, evaluator.getStatusReason() != null ? evaluator.getStatusReason() : "");
                    updateCell(row, permCol, Boolean.TRUE.equals(evaluator.getIsPermanent()) ? "YES" : "NO");
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Add new row if not present
                Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
                writeEvaluatorRow(newRow, evaluator);
            }

            try (OutputStream os = Files.newOutputStream(targetPath)) {
                workbook.write(os);
            }
        } catch (Exception ex) {
            System.err.println("Error updating Excel: " + ex.getMessage());
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException ignored) {}
            }
        }
    }

    public byte[] getMasterExcelBytes() throws IOException {
        Path targetPath = getMasterFilePath();
        if (Files.exists(targetPath)) {
            return Files.readAllBytes(targetPath);
        }

        // Generate dynamically from database
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Evaluators");
            Row header = sheet.createRow(0);
            String[] headers = {"EMP ID", "NAME", "VERTICAL", "DOMAIN", "AVAILABILITY", "UNAVAILABLE FROM", "UNAVAILABLE TO", "STATUS REASON", "PERMANENT"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Evaluator> all = evaluatorRepository.findAll();
            int rIdx = 1;
            for (Evaluator e : all) {
                Row r = sheet.createRow(rIdx++);
                writeEvaluatorRow(r, e);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            byte[] bytes = bos.toByteArray();
            
            // Also cache to disk
            Files.write(targetPath, bytes);
            return bytes;
        }
    }

    private void writeEvaluatorRow(Row r, Evaluator e) {
        updateCell(r, 0, e.getEmpId());
        updateCell(r, 1, e.getName());
        updateCell(r, 2, e.getVertical());
        updateCell(r, 3, e.getDomain());
        updateCell(r, 4, e.getAvailabilityStatus());
        updateCell(r, 5, e.getUnavailableFrom() != null ? e.getUnavailableFrom().toString() : "");
        updateCell(r, 6, e.getUnavailableTo() != null ? e.getUnavailableTo().toString() : "");
        updateCell(r, 7, e.getStatusReason() != null ? e.getStatusReason() : "");
        updateCell(r, 8, Boolean.TRUE.equals(e.getIsPermanent()) ? "YES" : "NO");
    }

    private void updateCell(Row row, int colIdx, String value) {
        if (colIdx < 0) return;
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }
        cell.setCellValue(value != null ? value : "");
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            return String.valueOf((long)cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return null;
    }

    private LocalDate parseLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            String str = cell.getStringCellValue().trim();
            if (!str.isEmpty()) {
                try {
                    return LocalDate.parse(str);
                } catch (Exception e) {
                    try {
                        return LocalDate.parse(str, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }
}
