package com.vbmvalidator.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.DocumentProcessor;

@Service
public class ExcelProcessor implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(ExcelProcessor.class);

    @Override
    public boolean canProcess(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.toLowerCase().endsWith(".xlsx") || 
                                   filename.toLowerCase().endsWith(".xls"));
    }

    @Override
    public SOBData extractSOBData(MultipartFile file) throws IOException {
        log.info("Processing SOB Excel file: {}", file.getOriginalFilename());
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // Using a streaming approach for large files is recommended
            return parseSOBFromExcel(sheet, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error processing SOB Excel file: {}", e.getMessage());
            throw new IOException("Error processing SOB file: " + e.getMessage(), e);
        }
    }

    @Override
    public VendorMatrixData extractVendorMatrixData(MultipartFile file) throws IOException {
        log.info("Processing Vendor Matrix Excel file: {}", file.getOriginalFilename());
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // Using a streaming approach for large files is recommended
            return parseVendorMatrixFromExcel(sheet, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error processing Vendor Matrix Excel file: {}", e.getMessage());
            throw new IOException("Error processing Vendor Matrix file: " + e.getMessage(), e);
        }
    }
    

    


    private SOBData parseSOBFromExcel(Sheet sheet, String fileName) {
        SOBData.SOBDataBuilder builder = SOBData.builder();
        List<SOBBenefit> benefits = new ArrayList<>();
        Map<String, String> rawData = new HashMap<>();

        // Efficiently iterate through rows
        for (Row row : sheet) {
            if (row == null) continue;

            // Metadata parsing (first few rows)
            if (row.getRowNum() <= 20) {
                Cell keyCell = row.getCell(0);
                Cell valueCell = row.getCell(1);
                if (keyCell != null && valueCell != null) {
                    String key = getCellValueAsString(keyCell).trim();
                    String value = getCellValueAsString(valueCell).trim();
                    if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                        parseMetadataField(key, value, builder, rawData);
                    }
                }
            }

            // Benefits parsing
            Row headerRow = findBenefitsHeaderRow(sheet);
            if (headerRow != null && row.getRowNum() > headerRow.getRowNum()) {
                Map<String, Integer> columnMap = createColumnMapping(headerRow);
                SOBBenefit benefit = parseBenefitFromRow(row, columnMap);
                if (benefit != null) {
                    benefits.add(benefit);
                }
            }
        }

        return builder
                .benefits(benefits)
                .rawData(rawData)
                .sourceFileName(fileName)
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }

    private VendorMatrixData parseVendorMatrixFromExcel(Sheet sheet, String fileName) {
        Row headerRow = sheet.getRow(0);
        Row dataRow = sheet.getRow(1);
        
        if (headerRow == null || dataRow == null) {
            throw new IllegalArgumentException("Excel file must have at least 2 rows (header + data)");
        }
        
        Map<String, String> columnData = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell headerCell = headerRow.getCell(i);
            Cell dataCell = dataRow.getCell(i);
            if (headerCell != null) {
                String header = getCellValueAsString(headerCell).trim();
                String value = dataCell != null ? getCellValueAsString(dataCell).trim() : "";
                columnData.put(header, value);
            }
        }
        
        return VendorMatrixData.builder()
                .productName(columnData.get("Product Name"))
                .productId(columnData.get("2025 Product ID"))
                .allColumns(columnData)
                .sourceFileName(fileName)
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }

    private void parseMetadataField(String key, String value, SOBData.SOBDataBuilder builder, Map<String, String> rawData) {
        switch (key) {
            case "Plan Name" -> builder.planName(value);
            case "Effective Date" -> builder.effectiveDate(value);
            case "Product ID" -> builder.productId(value);
            case "CMS Contract Number/PBP" -> builder.contractNumber(value);
            case "Service Area" -> builder.serviceArea(value);
            case "Formulary" -> builder.formulary(value);
            case "Provider Network Name" -> builder.providerNetwork(value);
            case "Maximum Out of Pocket cost (MOOP)" -> builder.moop(value);
            case "Monthly Premium" -> builder.monthlyPremium(value);
            default -> { /* No action for unknown keys */ }
        }
        rawData.put(key, value);
    }

    private Row findBenefitsHeaderRow(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            Cell firstCell = row.getCell(0);
            if (firstCell != null && "PBP Category".equals(getCellValueAsString(firstCell).trim())) {
                return row;
            }
        }
        return null;
    }

    private List<SOBBenefit> parseBenefitsFromExcel(Sheet sheet, int startRow) {
        List<SOBBenefit> benefits = new ArrayList<>();
        
        // Get header row to understand column positions
        Row headerRow = sheet.getRow(startRow);
        Map<String, Integer> columnMap = createColumnMapping(headerRow);
        
        // Parse data rows
        for (int i = startRow + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            Cell pbpCell = row.getCell(columnMap.getOrDefault("PBP Category", 0));
            if (pbpCell == null || StringUtils.isBlank(getCellValueAsString(pbpCell))) {
                continue;
            }
            
            SOBBenefit benefit = parseBenefitFromRow(row, columnMap);
            if (benefit != null) {
                benefits.add(benefit);
            }
        }
        
        return benefits;
    }

    private Map<String, Integer> createColumnMapping(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = getCellValueAsString(cell).trim();
                columnMap.put(header, i);
            }
        }
        
        return columnMap;
    }

    private SOBBenefit parseBenefitFromRow(Row row, Map<String, Integer> columnMap) {
        try {
            String pbpCategory = getCellValue(row, columnMap, "PBP Category");
            String benefitName = getCellValue(row, columnMap, "Benefit");
            String costSharing = getCellValue(row, columnMap, "In Network Member Cost Sharing");
            String notations = getCellValue(row, columnMap, "Notations");
            
            Boolean supplementalBenefit = parseBooleanValue(getCellValue(row, columnMap, "Supplemental Benefit"));
            Boolean paRequired = parseBooleanValue(getCellValue(row, columnMap, "PA Required"));
            Boolean referralRequired = parseBooleanValue(getCellValue(row, columnMap, "Referral Required"));
            Boolean moopApplicable = parseBooleanValue(getCellValue(row, columnMap, "MOOP applicable"));
            Boolean deductibleApplicable = parseBooleanValue(getCellValue(row, columnMap, "Deductible applicable"));
            
            return SOBBenefit.builder()
                    .pbpCategory(pbpCategory)
                    .benefitCategory(pbpCategory)  // Set benefitCategory same as pbpCategory for validation
                    .benefitName(benefitName)
                    .costSharing(costSharing)
                    .notations(notations)
                    .supplementalBenefit(supplementalBenefit)
                    .paRequired(paRequired)
                    .paNotes(notations)  // Use notations as PA notes
                    .referralRequired(referralRequired)
                    .moopApplicable(moopApplicable)
                    .deductibleApplicable(deductibleApplicable)
                    .rawText("%s|%s|%s|%s".formatted(pbpCategory, benefitName, costSharing, notations))
                    .build();
        } catch (Exception e) {
            log.warn("Error parsing benefit from row {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }

    private String getCellValue(Row row, Map<String, Integer> columnMap, String columnName) {
        Integer columnIndex = columnMap.get(columnName);
        if (columnIndex == null) return "";
        
        Cell cell = row.getCell(columnIndex);
        return cell != null ? getCellValueAsString(cell).trim() : "";
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Handle numeric values
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        yield String.valueOf((long) numericValue);
                    } else {
                        yield String.valueOf(numericValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private Boolean parseBooleanValue(String value) {
        if (StringUtils.isBlank(value)) return null;
        String cleanValue = value.trim().toUpperCase();
        return "Y".equals(cleanValue) || "YES".equals(cleanValue) || 
               "TRUE".equals(cleanValue) || "1".equals(cleanValue);
    }


    

    


    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".xlsx", ".xls"};
    }
} 
