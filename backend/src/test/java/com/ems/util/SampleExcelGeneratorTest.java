package com.ems.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SampleExcelGeneratorTest {

    @Test
    public void generateSampleExcel() throws Exception {
        Path sampleDir = Paths.get("..", "sample-data");
        if (!Files.exists(sampleDir)) {
            Files.createDirectories(sampleDir);
        }
        Path targetPath = sampleDir.resolve("master_evaluators_sample.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Master Evaluators");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Available Style
            CellStyle availStyle = workbook.createCellStyle();
            availStyle.cloneStyleFrom(dataStyle);
            Font availFont = workbook.createFont();
            availFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            availFont.setBold(true);
            availStyle.setFont(availFont);

            // Unavailable Style
            CellStyle unavailStyle = workbook.createCellStyle();
            unavailStyle.cloneStyleFrom(dataStyle);
            Font unavailFont = workbook.createFont();
            unavailFont.setColor(IndexedColors.DARK_RED.getIndex());
            unavailFont.setBold(true);
            unavailStyle.setFont(unavailFont);

            String[] headers = {
                "EMP ID", "NAME", "VERTICAL", "DOMAIN", "AVAILABILITY",
                "UNAVAILABLE FROM", "UNAVAILABLE TO", "STATUS REASON", "PERMANENT"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 18 realistic sample evaluators covering various scenarios
            Object[][] data = {
                {"EMP101", "Rajesh Kumar", "BFSI", "Java Full Stack", "AVAILABLE", "", "", "", "NO"},
                {"EMP102", "Sneha Rao", "Healthcare", "Cloud & DevOps", "AVAILABLE", "", "", "", "NO"},
                {"EMP103", "Amit Verma", "Retail", "Data Engineering", "UNAVAILABLE", "2026-09-01", "2026-09-10", "On Leave", "NO"},
                {"EMP104", "Priya Nair", "Technology", "Python & AI", "UNAVAILABLE", "", "", "Resigned / Left Company", "YES"},
                {"EMP105", "Vikram Singh", "Automotive", "QA Automation", "AVAILABLE", "", "", "", "NO"},
                {"EMP106", "Ananya Sen", "BFSI", "Cybersecurity", "UNAVAILABLE", "2026-09-15", "2026-09-25", "Client Project Work", "NO"},
                {"EMP107", "Karthik Iyer", "Healthcare", "Java Full Stack", "AVAILABLE", "", "", "", "NO"},
                {"EMP108", "Meera Joshi", "Retail", "Cloud & DevOps", "UNAVAILABLE", "2026-08-01", "2026-11-30", "Maternity Leave", "NO"},
                {"EMP109", "Rohan Deshmukh", "Technology", "Data Engineering", "AVAILABLE", "", "", "", "NO"},
                {"EMP110", "Sunita Sharma", "Automotive", "Python & AI", "UNAVAILABLE", "", "", "Long-term Sabbatical", "YES"},
                {"EMP111", "Suresh Menon", "BFSI", "QA Automation", "AVAILABLE", "", "", "", "NO"},
                {"EMP112", "Kavita Patel", "Healthcare", "Cybersecurity", "UNAVAILABLE", "2026-09-05", "2026-09-08", "Medical Leave", "NO"},
                {"EMP113", "Deepak Gupta", "Retail", "Java Full Stack", "AVAILABLE", "", "", "", "NO"},
                {"EMP114", "Divya Nair", "Technology", "Cloud & DevOps", "UNAVAILABLE", "2026-09-18", "2026-09-22", "Training / Upskilling", "NO"},
                {"EMP115", "Sandeep Reddy", "Automotive", "Data Engineering", "AVAILABLE", "", "", "", "NO"},
                {"EMP116", "Pooja Hegde", "BFSI", "Python & AI", "UNAVAILABLE", "", "", "Contract Ended", "YES"},
                {"EMP117", "Arvind Swami", "Healthcare", "QA Automation", "AVAILABLE", "", "", "", "NO"},
                {"EMP118", "Neha Bansal", "Retail", "Cybersecurity", "AVAILABLE", "", "", "", "NO"}
            };

            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r + 1);
                row.setHeightInPoints(20);
                for (int c = 0; c < data[r].length; c++) {
                    Cell cell = row.createCell(c);
                    String val = (String) data[r][c];
                    cell.setCellValue(val);
                    if (c == 4) {
                        cell.setCellStyle("AVAILABLE".equals(val) ? availStyle : unavailStyle);
                    } else {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 3800));
            }

            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                workbook.write(fos);
            }
            System.out.println("Generated sample Excel at: " + targetPath.toAbsolutePath());
        }
    }

    @Test
    public void generateCandidateSampleExcel() throws Exception {
        Path sampleDir = Paths.get("..", "sample-data");
        if (!Files.exists(sampleDir)) {
            Files.createDirectories(sampleDir);
        }
        Path targetPath = sampleDir.resolve("candidates_sample.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cohort Candidates");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"CANDIDATE ID", "CANDIDATE NAME", "EMAIL", "TRACK / DOMAIN"};

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 12 realistic sample candidates
            Object[][] data = {
                {"CAND-201", "Aarav Mehta", "aarav.mehta@example.com", "Java Full Stack"},
                {"CAND-202", "Diya Sen", "diya.sen@example.com", "Cloud & DevOps"},
                {"CAND-203", "Varun Kapoor", "varun.kapoor@example.com", "Data Engineering"},
                {"CAND-204", "Ishaan Bhat", "ishaan.bhat@example.com", "Python & AI"},
                {"CAND-205", "Tanvi Joshi", "tanvi.joshi@example.com", "QA Automation"},
                {"CAND-206", "Aditya Rao", "aditya.rao@example.com", "Cybersecurity"},
                {"CAND-207", "Riya Saxena", "riya.saxena@example.com", "Java Full Stack"},
                {"CAND-208", "Manish Kulkarni", "manish.k@example.com", "Cloud & DevOps"},
                {"CAND-209", "Nandini Shah", "nandini.shah@example.com", "Data Engineering"},
                {"CAND-210", "Karan Malhotra", "karan.m@example.com", "Python & AI"},
                {"CAND-211", "Anika Pillai", "anika.pillai@example.com", "QA Automation"},
                {"CAND-212", "Siddharth Jain", "siddharth.j@example.com", "Cybersecurity"}
            };

            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r + 1);
                row.setHeightInPoints(20);
                for (int c = 0; c < data[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue((String) data[r][c]);
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 4200));
            }

            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                workbook.write(fos);
            }
            System.out.println("Generated candidate sample Excel at: " + targetPath.toAbsolutePath());
        }
    }
}
