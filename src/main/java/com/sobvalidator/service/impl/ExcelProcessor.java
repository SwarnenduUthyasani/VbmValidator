package com.sobvalidator.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.sobvalidator.model.SOBBenefit;
import com.sobvalidator.model.SOBData;
import com.sobvalidator.model.SOBType;
import com.sobvalidator.model.VendorMatrixData;
import com.sobvalidator.service.DocumentProcessor;

@Service
public class ExcelProcessor implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(ExcelProcessor.class);

    @Override
    public boolean canProcess(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.toLowerCase().endsWith(".xlsx") || 
                                   filename.toLowerCase().endsWith(".xls") ||
                                   filename.toLowerCase().endsWith(".csv")); // Temporarily allow CSV for testing
    }

    @Override
    public SOBData extractSOBData(MultipartFile file) throws IOException {
        log.info("Processing SOB Excel file: {}", file.getOriginalFilename());
        
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            return parseSOBFromCSV(file);
        }
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            return parseSOBFromExcel(sheet, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error processing SOB Excel file: {}", e.getMessage());
            throw new IOException("Error processing SOB file: " + e.getMessage(), e);
        }
    }

    @Override
    public VendorMatrixData extractVendorMatrixData(MultipartFile file) throws IOException {
        log.info("Processing Vendor Matrix Excel file: {}", file.getOriginalFilename());
        
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            return parseVendorMatrixFromCSV(file);
        }
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            return parseVendorMatrixFromExcel(sheet, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error processing Vendor Matrix Excel file: {}", e.getMessage());
            throw new IOException("Error processing Vendor Matrix file: " + e.getMessage(), e);
        }
    }
    
    private SOBData parseSOBFromCSV(MultipartFile file) throws IOException {
        log.info("Processing SOB CSV file: {}", file.getOriginalFilename());
        
        // Simple CSV parsing for testing - assumes CSV format similar to Excel structure
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(","));
            }
            
            // Convert to sheet-like structure and reuse existing Excel parsing logic
            return parseSOBFromRows(rows, file.getOriginalFilename());
        }
    }
    
    private VendorMatrixData parseVendorMatrixFromCSV(MultipartFile file) throws IOException {
        log.info("Processing Vendor Matrix CSV file: {}", file.getOriginalFilename());
        
        // Simple CSV parsing for testing
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(","));
            }
            
            // Convert to sheet-like structure and reuse existing Excel parsing logic
            return parseVendorMatrixFromRows(rows, file.getOriginalFilename());
        }
    }

    private SOBData parseSOBFromExcel(Sheet sheet, String fileName) {
        SOBData.SOBDataBuilder builder = SOBData.builder();
        List<SOBBenefit> benefits = new ArrayList<>();
        Map<String, String> rawData = new HashMap<>();
        
        // Find metadata in the first rows
        for (int i = 0; i <= 20 && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
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
        
        // Find benefits section
        Row headerRow = findBenefitsHeaderRow(sheet);
        if (headerRow != null) {
            benefits = parseBenefitsFromExcel(sheet, headerRow.getRowNum());
        }
        
        // Detect SOB type
        SOBType sobType = SOBType.detectFromPlanName(builder.build().getPlanName());
        
        return builder
                .sobType(sobType)
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
        
        // Map headers to values
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell headerCell = headerRow.getCell(i);
            Cell dataCell = dataRow.getCell(i);
            
            if (headerCell != null) {
                String header = getCellValueAsString(headerCell).trim();
                String value = dataCell != null ? getCellValueAsString(dataCell).trim() : "";
                columnData.put(header, value);
            }
        }
        
        // Note: benefitDataMap is no longer needed as validation now uses benefit names
        // to map directly to allColumns (columnData) which contains all VBM column headers
        
        return VendorMatrixData.builder()
                .productName(columnData.get("Product Name"))
                .productId(columnData.get("2025 Product ID"))
                .basePlan(columnData.get("Base Plan"))
                .benefitSet(columnData.get("Benefit Set"))
                .contractId(columnData.get("Contract ID"))
                .pbpNumber(columnData.get("PBP #"))
                .segmentId(columnData.get("Segment ID"))
                .planYear(columnData.get("Plan Year"))
                .effectiveDate(columnData.get("Effective Date"))
                .company(columnData.get("Company"))
                .licensure(columnData.get("INN Licensure"))
                
                // Map benefit columns
                .inpatientAdmission(columnData.get("INN Inpt. Admission/ OON Inpt. Admission"))
                .skilledNursingDays(columnData.get("INN Skilled Nursing Days/OON Skilled Nursing Days"))
                .cardiacTherapy(columnData.get("INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy"))
                .intensiveCardiacTherapy(columnData.get("INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy"))
                .superviseExerciseTherapy(columnData.get("Supervised Exercise Therapy (SET) for Symptomatic Peripheral Artery Disease (PAD)"))
                .pulmonaryRehab(columnData.get("INN Pulmonary Rehab/OON Pulmonary RT"))
                .emergencyServices(columnData.get("INN ER/OON ER"))
                .urgentCare(columnData.get("INN Urgent Care Center in Facility/OON Urgent Care Center in Facility"))
                .homeHealthVisits(columnData.get("INN Home Care Visits/OON Home Care Visits"))
                .pcpVisits(columnData.get("INN PCP/OON PCP"))
                .chiropracticServices(columnData.get("INN Chiropractic/OON Chiropractic"))
                .occupationalTherapy(columnData.get("Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT"))
                .specialistVisits(columnData.get("INN Specialist/OON Specialist"))
                .podiatryMedicare(columnData.get("INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered"))
                .podiatrySupplemental(columnData.get("INN Podiatry Supplemental/OONPodiatry  Supplemental"))
                .physicalTherapy(columnData.get("Rehabilitation/Habilitation Services INN Outpt. PT/OON Outpt. PT"))
                .speechTherapy(columnData.get("Rehabilitation/Habilitation Services INN Outpt. ST/OON Outpt. ST"))
                .telehealth(columnData.get("INN Telehealth/ OON Telehealth"))
                .diagnosticTesting(columnData.get("INN Diagnostic Testing/OON Diagnostic Testing"))
                .labServices(columnData.get("INN Lab Services Medicare Covered/ONNLab Services Medicare Covered"))
                .therapeuticRadiology(columnData.get("INN Therapeutic Radiology Services/OON Therapeutic Radiology Services"))
                .diagnosticRadiology(columnData.get("INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered"))
                .outpatientHospital(columnData.get("INN Outpatient Hospital Services/OON Outpatient Hospital Services Outpatient"))
                .observationRoom(columnData.get("INN Observation room/OON Observation room"))
                .ambulatorySurgery(columnData.get("INN Amb. Surgery Center/OON Amb. Surgery Center"))
                .outpatientBlood(columnData.get("INN Outpatient Blood Services/ONN Outpatient Blood Services (processing and handling services for every unit of blood the beneficiary receives and the Part B)"))
                .ambulanceEmergent(columnData.get("INN Ambulance Emergent/OON Ambulance Emergent"))
                .ambulanceAir(columnData.get("INN Ambulance Air/OON Ambulance Air"))
                .transportation(columnData.get("Transportation (INN Only)"))
                .dme(columnData.get("INN DME/OON DME"))
                .prosthetics(columnData.get("INN External Prosthetic Devices/OON External Prosthetic Devices"))
                .diabeticSupplies(columnData.get("INN Diabetic Supply/OON Diabetic Supply"))
                .shoeInserts(columnData.get("INN Shoe Inserts/OON Shoe Inserts"))
                .dialysisTreatment(columnData.get("INN Dialysis Treatment/OON Dialysis Treatment"))
                .acupuncture(columnData.get("INN Acupuncture/OON Acupuncture"))
                .otcMedications(columnData.get("INN Over the Counter Meds/OON Over the Counter Meds"))
                .mealsBenefit(columnData.get("INN Meals/OON Meals"))
                .preventiveCare(columnData.get("INN Preventive Care/OON Preventive Care"))
                .annualPhysical(columnData.get("INN Supplemental Annual Physical Exam/OON Supplemental Annual Physical Exam"))
                .moop(columnData.get("Ind MOOP"))
                .deductible(columnData.get("INN Deductible Ind/OON Deductible Ind"))
                
                .allColumns(columnData) // This contains all VBM column headers and values for validation
                .benefitData(new HashMap<>()) // Empty map as validation now uses allColumns directly
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
                    .rawText(String.format("%s|%s|%s|%s", pbpCategory, benefitName, costSharing, notations))
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

    private SOBData parseSOBFromRows(List<String[]> rows, String fileName) {
        // Adapt the existing parseSOBFromExcel logic to work with CSV rows
        SOBData.SOBDataBuilder builder = SOBData.builder();
        Map<String, String> rawData = new HashMap<>();
        List<SOBBenefit> benefits = new ArrayList<>();
        
        boolean inBenefitSection = false;
        String[] headers = null;
        
        for (String[] row : rows) {
            if (row.length == 0) continue;
            
            String firstCell = row[0].trim();
            
            // Check if this is the header row for benefits
            if (firstCell.equals("PBP Category") && row.length > 1 && row[1].equals("Benefit")) {
                inBenefitSection = true;
                headers = row;
                continue;
            }
            
            if (inBenefitSection && headers != null) {
                // Parse benefit row
                SOBBenefit benefit = parseBenefitFromRowArray(row, headers);
                if (benefit != null) {
                    benefits.add(benefit);
                }
            } else {
                // Parse metadata
                if (row.length >= 2) {
                    String key = firstCell;
                    String value = row[1].trim();
                    rawData.put(key, value);
                    parseMetadataField(key, value, builder, rawData);
                }
            }
        }
        
        return builder
                .benefits(benefits)
                .sourceFileName(fileName)
                .uploadedAt(LocalDateTime.now().toString())
                .rawData(rawData)
                .build();
    }
    
    private VendorMatrixData parseVendorMatrixFromRows(List<String[]> rows, String fileName) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }
        
        // First row should be headers
        String[] headers = rows.get(0);
        
        // Second row should be data
        if (rows.size() < 2) {
            throw new IllegalArgumentException("CSV file must have at least header and data rows");
        }
        
        String[] dataRow = rows.get(1);
        
        // Create column data map
        Map<String, String> columnData = new HashMap<>();
        for (int i = 0; i < Math.min(headers.length, dataRow.length); i++) {
            String header = headers[i].trim();
            String value = i < dataRow.length ? dataRow[i].trim() : "";
            columnData.put(header, value);
        }
        
        // Note: benefitDataMap is no longer needed as validation now uses benefit names
        // to map directly to allColumns (columnData) which contains all VBM column headers
        
                 return VendorMatrixData.builder()
                .productName(parseMetadataField(dataRow, "Product Name"))
                .productId(parseMetadataField(dataRow, "2025 Product ID"))
                .basePlan(parseMetadataField(dataRow, "Base Plan"))
                .benefitSet(parseMetadataField(dataRow, "Benefit Set"))
                .contractId(parseMetadataField(dataRow, "Contract ID"))
                .pbpNumber(parseMetadataField(dataRow, "PBP #"))
                .segmentId(parseMetadataField(dataRow, "Segment ID"))
                .planYear(parseMetadataField(dataRow, "Plan Year"))
                .effectiveDate(parseMetadataField(dataRow, "Effective Date"))
                .company(parseMetadataField(dataRow, "Company"))
                .inpatientAdmission(columnData.get("INN Inpt. Admission/ OON Inpt. Admission"))
                .skilledNursingDays(columnData.get("INN Skilled Nursing Days/OON Skilled Nursing Days"))
                .cardiacTherapy(columnData.get("INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy"))
                .intensiveCardiacTherapy(columnData.get("INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy"))
                .pulmonaryRehab(columnData.get("INN Pulmonary Rehab/OON Pulmonary RT"))
                .emergencyServices(columnData.get("INN ER/OON ER"))
                .urgentCare(columnData.get("INN Urgent Care Center in Facility/OON Urgent Care Center in Facility"))
                .homeHealthVisits(columnData.get("INN Home Care Visits/OON Home Care Visits"))
                .pcpVisits(columnData.get("INN PCP/OON PCP"))
                .chiropracticServices(columnData.get("INN Chiropractic/OON Chiropractic"))
                .occupationalTherapy(columnData.get("Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT"))
                .specialistVisits(columnData.get("INN Specialist/OON Specialist"))
                .labServices(columnData.get("INN Lab Services Medicare Covered/ONNLab Services Medicare Covered"))
                .diagnosticRadiology(columnData.get("INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered"))
                .therapeuticRadiology(columnData.get("INN Therapeutic Radiology Services/OON Therapeutic Radiology Services"))
                .outpatientHospital(columnData.get("INN Outpatient Hospital Services/OON Outpatient Hospital Services Outpatient"))
                .observationRoom(columnData.get("INN Observation room/OON Observation room"))
                .ambulatorySurgery(columnData.get("INN Amb. Surgery Center/OON Amb. Surgery Center"))
                .ambulanceEmergent(columnData.get("INN Ambulance Emergent/OON Ambulance Emergent"))
                .ambulanceAir(columnData.get("INN Ambulance Air/OON Ambulance Air"))
                .dme(columnData.get("INN DME/OON DME"))
                .prosthetics(columnData.get("INN External Prosthetic Devices/OON External Prosthetic Devices"))
                .dialysisTreatment(columnData.get("INN Dialysis Treatment/OON Dialysis Treatment"))
                .acupuncture(columnData.get("INN Acupuncture/OON Acupuncture"))
                .preventiveCare(columnData.get("INN Preventive Care/OON Preventive Care"))
                .moop(columnData.get("Ind MOOP"))
                .deductible(columnData.get("INN Deductible Ind/OON Deductible Ind"))
                
                .allColumns(columnData) // This contains all VBM column headers and values for validation
                .benefitData(new HashMap<>()) // Empty map as validation now uses allColumns directly
                .sourceFileName(fileName)
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }
    
    private SOBBenefit parseBenefitFromRowArray(String[] row, String[] headers) {
        if (row.length < 2) return null;
        
        // Create a map for easy access
        Map<String, String> rowData = new HashMap<>();
        for (int i = 0; i < Math.min(headers.length, row.length); i++) {
            rowData.put(headers[i], row[i]);
        }
        
        String pbpCategory = rowData.getOrDefault("PBP Category", "").trim();
        String benefitName = rowData.getOrDefault("Benefit", "").trim();
        String costSharing = rowData.getOrDefault("In Network Member Cost Sharing", "").trim();
        String notations = rowData.getOrDefault("Notations", "").trim();
        String supplementalBenefit = rowData.getOrDefault("Supplemental Benefit", "").trim();
        String paRequired = rowData.getOrDefault("PA Required?", "").trim();
        String referralRequired = rowData.getOrDefault("Referral Required", "").trim();
        String moopApplicable = rowData.getOrDefault("MOOP Applicable", "").trim();
        String deductibleApplicable = rowData.getOrDefault("Deductible Applicable", "").trim();
        
        if (pbpCategory.isEmpty() || benefitName.isEmpty()) {
            return null;
        }
        
                 return SOBBenefit.builder()
                .pbpCategory(pbpCategory)
                .benefitCategory(pbpCategory)  // Set benefitCategory same as pbpCategory for validation
                .benefitName(benefitName)
                .costSharing(costSharing)
                .notations(notations)
                .supplementalBenefit(parseBoolean(supplementalBenefit))
                .paRequired(parseBoolean(paRequired))
                .paNotes(notations)  // Use notations as PA notes
                .referralRequired(parseBoolean(referralRequired))
                .moopApplicable(parseBoolean(moopApplicable))
                .deductibleApplicable(parseBoolean(deductibleApplicable))
                .build();
    }

         private String parseMetadataField(String[] row, String key) {
         if (row.length >= 2) {
             String value = row[1].trim();
             return value;
         }
         return "";
     }
     
     private Boolean parseBoolean(String value) {
         if (value == null || value.trim().isEmpty()) return null;
         String cleanValue = value.trim().toUpperCase();
         return "Y".equals(cleanValue) || "YES".equals(cleanValue) || 
                "TRUE".equals(cleanValue) || "1".equals(cleanValue);
     }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".xlsx", ".xls"};
    }
} 