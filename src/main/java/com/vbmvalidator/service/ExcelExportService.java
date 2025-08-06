package com.vbmvalidator.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vbmvalidator.model.ValidationError;
import com.vbmvalidator.model.ValidationResult;
import com.vbmvalidator.model.VendorMatrixData;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    public byte[] generateCorrectedVendorMatrix(VendorMatrixData originalData, 
                                              ValidationResult validationResult, 
                                              List<String> selectedErrorIds, 
                                              boolean highlightChanges) throws IOException {
        
        log.info("Generating corrected Vendor Matrix for {} selected errors", selectedErrorIds.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Corrected Vendor Matrix");
            
            // Create styles for highlighting
            CellStyle highlightStyle = createHighlightStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Get selected errors
            Set<String> selectedErrorSet = Set.copyOf(selectedErrorIds);
            List<ValidationError> selectedErrors = validationResult.getErrors().stream()
                    .filter(error -> selectedErrorSet.contains(error.getErrorId()))
                    .collect(Collectors.toList());
            
            // Create corrected data
            VendorMatrixData correctedData = applyCorrections(originalData, selectedErrors);
            
            // Generate Excel content
            createHeaderRow(sheet, headerStyle);
            createDataRow(sheet, correctedData, selectedErrors, highlightStyle, normalStyle, highlightChanges);
            
            // Auto-size columns
            autoSizeColumns(sheet);
            
            // Note: Removed change summary sheet as per user request
            
            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private VendorMatrixData applyCorrections(VendorMatrixData originalData, List<ValidationError> selectedErrors) {
        // Create a deep copy of all original data
        Map<String, String> correctedAllColumns = new HashMap<>();
        if (originalData.getAllColumns() != null) {
            correctedAllColumns.putAll(originalData.getAllColumns());
        }
        
        Map<String, String> correctedBenefitData = new HashMap<>();
        if (originalData.getBenefitData() != null) {
            correctedBenefitData.putAll(originalData.getBenefitData());
        }
        
        VendorMatrixData.VendorMatrixDataBuilder builder = VendorMatrixData.builder()
                .productName(originalData.getProductName())
                .productId(originalData.getProductId())
                .basePlan(originalData.getBasePlan())
                .benefitSet(originalData.getBenefitSet())
                .contractId(originalData.getContractId())
                .pbpNumber(originalData.getPbpNumber())
                .segmentId(originalData.getSegmentId())
                .planYear(originalData.getPlanYear())
                .effectiveDate(originalData.getEffectiveDate())
                .company(originalData.getCompany())
                .licensure(originalData.getLicensure())
                .sourceFileName(originalData.getSourceFileName() + "_corrected")
                .uploadedAt(originalData.getUploadedAt())
                
                // Copy all the benefit fields from original data
                .inpatientAdmission(originalData.getInpatientAdmission())
                .skilledNursingDays(originalData.getSkilledNursingDays())
                .cardiacTherapy(originalData.getCardiacTherapy())
                .intensiveCardiacTherapy(originalData.getIntensiveCardiacTherapy())
                .superviseExerciseTherapy(originalData.getSuperviseExerciseTherapy())
                .pulmonaryRehab(originalData.getPulmonaryRehab())
                .emergencyServices(originalData.getEmergencyServices())
                .urgentCare(originalData.getUrgentCare())
                .homeHealthVisits(originalData.getHomeHealthVisits())
                .pcpVisits(originalData.getPcpVisits())
                .chiropracticServices(originalData.getChiropracticServices())
                .occupationalTherapy(originalData.getOccupationalTherapy())
                .specialistVisits(originalData.getSpecialistVisits())
                .podiatryMedicare(originalData.getPodiatryMedicare())
                .podiatrySupplemental(originalData.getPodiatrySupplemental())
                .physicalTherapy(originalData.getPhysicalTherapy())
                .speechTherapy(originalData.getSpeechTherapy())
                .telehealth(originalData.getTelehealth())
                .diagnosticTesting(originalData.getDiagnosticTesting())
                .labServices(originalData.getLabServices())
                .therapeuticRadiology(originalData.getTherapeuticRadiology())
                .diagnosticRadiology(originalData.getDiagnosticRadiology())
                .outpatientHospital(originalData.getOutpatientHospital())
                .observationRoom(originalData.getObservationRoom())
                .ambulatorySurgery(originalData.getAmbulatorySurgery())
                .outpatientBlood(originalData.getOutpatientBlood())
                .ambulanceEmergent(originalData.getAmbulanceEmergent())
                .ambulanceAir(originalData.getAmbulanceAir())
                .transportation(originalData.getTransportation())
                .dme(originalData.getDme())
                .prosthetics(originalData.getProsthetics())
                .diabeticSupplies(originalData.getDiabeticSupplies())
                .shoeInserts(originalData.getShoeInserts())
                .dialysisTreatment(originalData.getDialysisTreatment())
                .acupuncture(originalData.getAcupuncture())
                .otcMedications(originalData.getOtcMedications())
                .mealsBenefit(originalData.getMealsBenefit())
                .preventiveCare(originalData.getPreventiveCare())
                .annualPhysical(originalData.getAnnualPhysical())
                .moop(originalData.getMoop())
                .deductible(originalData.getDeductible())
                
                // Copy data maps
                .allColumns(correctedAllColumns)
                .benefitData(correctedBenefitData);
        
        // Apply corrections for each selected error
        for (ValidationError error : selectedErrors) {
            String fieldName = error.getFieldName();
            String correctedValue = error.getExpectedValue();
            String benefitCategory = error.getBenefitCategory();
            
            log.info("Applying correction: {} - {} -> {}", benefitCategory, fieldName, correctedValue);
            
            // Update the appropriate field based on benefit category and field name
            applyFieldCorrection(builder, originalData, benefitCategory, fieldName, correctedValue, 
                               correctedAllColumns, correctedBenefitData);
        }
        
        return builder.build();
    }

    private void applyFieldCorrection(VendorMatrixData.VendorMatrixDataBuilder builder, 
                                    VendorMatrixData originalData, String benefitCategory, 
                                    String fieldName, String correctedValue,
                                    Map<String, String> correctedAllColumns, 
                                    Map<String, String> correctedBenefitData) {
        
        // Update benefit data map (keeping for compatibility but not used in new validation)
        correctedBenefitData.put(benefitCategory, correctedValue);
        builder.benefitData(correctedBenefitData);
        
        // Map benefit names (not PBP categories) to specific vendor matrix column headers
        // benefitCategory now contains the benefit name from SOB
        String vmColumnName = mapBenefitNameToVMColumn(benefitCategory);
        if (vmColumnName != null) {
            // Update the correct column in allColumns
            correctedAllColumns.put(vmColumnName, correctedValue);
            
            // Also update specific builder fields for key benefits
            updateBuilderFields(builder, vmColumnName, correctedValue);
        } else {
            // Fallback: apply to all columns map directly
            correctedAllColumns.put(benefitCategory, correctedValue);
            log.info("Applied fallback correction for benefit: {} -> {}", benefitCategory, correctedValue);
        }
        
        // Update the allColumns in builder
        builder.allColumns(correctedAllColumns);
    }
    
    private String mapBenefitNameToVMColumn(String benefitName) {
        // Same mapping logic as in ValidationServiceImpl
        return switch (benefitName.toLowerCase().trim()) {
            case "inpatient hospital" -> "INN Inpt. Admission/ OON Inpt. Admission";
            case "inpatient mental health (including inpatient substance abuse)", 
                 "inpatient mental health -medicare covered" -> "INN Inpt. Mental Health/OON Inpt. Mental Health";
            case "snf" -> "INN Skilled Nursing Days/OON Skilled Nursing Days";
            case "cardiac rehabilitation" -> "INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy";
            case "intensive cardiac rehab" -> "INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy";
            case "supervised exercise therapy (set) for symptomatic peripheral artery disease (pad)" -> 
                "Supervised Exercise Therapy (SET) for Symptomatic Peripheral Artery Disease (PAD)";
            case "pulmonary rehab" -> "INN Pulmonary Rehab/OON Pulmonary RT";
            case "er - emergency care", "emergency care" -> "INN ER/OON ER";
            case "urgently needed care" -> "INN Urgent Care Center in Facility/OON Urgent Care Center in Facility";
            case "partial hospitalization" -> "INN Partial Hospitalization/ONN Partial Hospitalization";
            case "intensive outpatient program" -> "INN Partial Hospitalization/ONN Partial Hospitalization";
            case "home health" -> "INN Home Care Visits/OON Home Care Visits";
            case "pcp" -> "INN PCP/OON PCP";
            case "chiro" -> "INN Chiropractic/OON Chiropractic";
            case "occupational therapy (ot)" -> "Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT";
            case "specialist" -> "INN Specialist/OON Specialist";
            case "outpatient mh" -> "INN Specialist/OON Specialist";
            case "podiatry (medicare covered and supplemental)" -> "INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered";
            case "other health care" -> "INN Specialist/OON Specialist";
            case "acupuncture (medicare covered)" -> "INN Acupuncture/OON Acupuncture";
            case "psychiatric services" -> "Psychiatric Services(INN Only)";
            case "physical and speech therapy (pt, st)" -> "Rehabilitation/Habilitation Services INN Outpt. PT/OON Outpt. PT";
            case "additional telehealth" -> "INN Telehealth/ OON Telehealth";
            case "opioid treatment" -> "Opioid Therapy(INN Only)";
            case "diagnostic procedures and tests (medicare covered" -> "INN Diagnostic Testing/OON Diagnostic Testing";
            case "lab services (medicare covered)" -> "INN Lab Services Medicare Covered/ONNLab Services Medicare Covered";
            case "outpatient therapeutic radiology (medicare covered)" -> "INN Therapeutic Radiology Services/OON Therapeutic Radiology Services";
            case "outpatient advanced / diagnostic radiology (medicare covered)" -> 
                "INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered";
            case "outpatient x-ray (medicare covered)" -> "INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered";
            case "outpatient hospital services" -> "INN Outpatient Hospital Services/OON Outpatient Hospital Services Outpatient";
            case "outpatient observation services" -> "INN Observation room/OON Observation room";
            case "ambulatory surgery centers" -> "INN Amb. Surgery Center/OON Amb. Surgery Center";
            case "outpatient substance abuse" -> "Outpt Substance Abuse INN Provider/Outpt Substance Abuse OON Provider";
            case "outpatient blood services" -> "INN Outpatient Blood Services/ONN Outpatient Blood Services (processing and handling services for every unit of blood the beneficiary receives and the Part B)";
            case "ambulance" -> "INN Ambulance Emergent/OON Ambulance Emergent";
            case "ambulance air" -> "INN Ambulance Air/OON Ambulance Air";
            case "transportation" -> "Transportation (INN Only)";
            case "dme" -> "INN DME/OON DME";
            case "prosthetics/orthotics" -> "INN External Prosthetic Devices/OON External Prosthetic Devices";
            case "diabetic supplies and services (non partd)/ thereapeutic shoes or inserts" -> "INN Diabetic Supply/OON Diabetic Supply";
            case "renal dialysis" -> "INN Dialysis Treatment/OON Dialysis Treatment";
            case "acupuncture" -> "INN Acupuncture/OON Acupuncture";
            case "over the counter meds" -> "INN Over the Counter Meds/OON Over the Counter Meds";
            case "meals benefit" -> "INN Meals/OON Meals";
            case "medicare preventive services" -> "INN Preventive Care/OON Preventive Care";
            case "supplemental annual physical exam" -> "INN Supplemental Annual Physical Exam/OON Supplemental Annual Physical Exam";
            case "moop" -> "Ind MOOP";
            case "deductible" -> "INN Deductible Ind/OON Deductible Ind";
            default -> null;
        };
    }
    
    private void updateBuilderFields(VendorMatrixData.VendorMatrixDataBuilder builder, String vmColumnName, String correctedValue) {
        // Update specific builder fields based on VM column name
        switch (vmColumnName) {
            case "INN Inpt. Admission/ OON Inpt. Admission" -> builder.inpatientAdmission(correctedValue);
            case "INN Inpt. Mental Health/OON Inpt. Mental Health" -> {} // No specific field
            case "INN Skilled Nursing Days/OON Skilled Nursing Days" -> builder.skilledNursingDays(correctedValue);
            case "INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy" -> builder.cardiacTherapy(correctedValue);
            case "INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy" -> builder.intensiveCardiacTherapy(correctedValue);
            case "INN Pulmonary Rehab/OON Pulmonary RT" -> builder.pulmonaryRehab(correctedValue);
            case "INN ER/OON ER" -> builder.emergencyServices(correctedValue);
            case "INN Urgent Care Center in Facility/OON Urgent Care Center in Facility" -> builder.urgentCare(correctedValue);
            case "INN Partial Hospitalization/ONN Partial Hospitalization" -> {} // No specific field
            case "INN Home Care Visits/OON Home Care Visits" -> builder.homeHealthVisits(correctedValue);
            case "INN PCP/OON PCP" -> builder.pcpVisits(correctedValue);
            case "INN Chiropractic/OON Chiropractic" -> builder.chiropracticServices(correctedValue);
            case "Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT" -> builder.occupationalTherapy(correctedValue);
            case "INN Specialist/OON Specialist" -> builder.specialistVisits(correctedValue);
            case "INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered" -> {} // No specific field
            case "INN Acupuncture/OON Acupuncture" -> builder.acupuncture(correctedValue);
            case "INN Lab Services Medicare Covered/ONNLab Services Medicare Covered" -> builder.labServices(correctedValue);
            case "INN Therapeutic Radiology Services/OON Therapeutic Radiology Services" -> builder.therapeuticRadiology(correctedValue);
            case "INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered" -> builder.diagnosticRadiology(correctedValue);
            case "INN Outpatient Hospital Services/OON Outpatient Hospital Services Outpatient" -> builder.outpatientHospital(correctedValue);
            case "INN Observation room/OON Observation room" -> builder.observationRoom(correctedValue);
            case "INN Amb. Surgery Center/OON Amb. Surgery Center" -> builder.ambulatorySurgery(correctedValue);
            case "INN Ambulance Emergent/OON Ambulance Emergent" -> builder.ambulanceEmergent(correctedValue);
            case "INN Ambulance Air/OON Ambulance Air" -> builder.ambulanceAir(correctedValue);
            case "INN DME/OON DME" -> builder.dme(correctedValue);
            case "INN External Prosthetic Devices/OON External Prosthetic Devices" -> builder.prosthetics(correctedValue);
            case "INN Dialysis Treatment/OON Dialysis Treatment" -> builder.dialysisTreatment(correctedValue);
            case "INN Preventive Care/OON Preventive Care" -> builder.preventiveCare(correctedValue);
            case "INN Supplemental Annual Physical Exam/OON Supplemental Annual Physical Exam" -> {} // No specific field 
            case "Ind MOOP" -> builder.moop(correctedValue);
            case "INN Deductible Ind/OON Deductible Ind" -> builder.deductible(correctedValue);
            default -> {} // No specific field mapping
        }
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        
        String[] headers = {
            "Product Name", "2025 Product ID", "Base Plan", "Benefit Set", "Contract ID", "PBP #",
            "Segment ID", "Plan Year", "Sent Date", "Rider Option", "LOB", "Origin Code", "Effective Date",
            "Company", "INN Licensure", "INN Inpt. Admission/ OON Inpt. Admission",
            "INN Skilled Nursing Days/OON Skilled Nursing Days", "INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy",
            "INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy",
            "Supervised Exercise Therapy (SET) for Symptomatic Peripheral Artery Disease (PAD)",
            "INN Pulmonary Rehab/OON Pulmonary RT", "INN ER/OON ER",
            "INN Urgent Care Center in Facility/OON Urgent Care Center in Facility",
            "INN Home Care Visits/OON Home Care Visits", "INN PCP/OON PCP", "INN Chiropractic/OON Chiropractic",
            "Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT", "INN Specialist/OON Specialist",
            "INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered",
            "INN Podiatry Supplemental/OONPodiatry  Supplemental",
            "Rehabilitation/Habilitation Services INN Outpt. PT/OON Outpt. PT",
            "Rehabilitation/Habilitation Services INN Outpt. ST/OON Outpt. ST",
            "INN Telehealth/ OON Telehealth", "INN Diagnostic Testing/OON Diagnostic Testing",
            "INN Lab Services Medicare Covered/ONNLab Services Medicare Covered",
            "INN Therapeutic Radiology Services/OON Therapeutic Radiology Services",
            "INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered",
            "INN Outpatient Hospital Services/OON Outpatient Hospital Services Outpatient",
            "INN Observation room/OON Observation room", "INN Amb. Surgery Center/OON Amb. Surgery Center",
            "INN Outpatient Blood Services/ONN Outpatient Blood Services", "INN Ambulance Emergent/OON Ambulance Emergent",
            "INN Ambulance Air/OON Ambulance Air", "Transportation (INN Only)", "INN DME/OON DME",
            "INN External Prosthetic Devices/OON External Prosthetic Devices", "INN Diabetic Supply/OON Diabetic Supply",
            "INN Shoe Inserts/OON Shoe Inserts", "INN Dialysis Treatment/OON Dialysis Treatment",
            "INN Acupuncture/OON Acupuncture", "INN Over the Counter Meds/OON Over the Counter Meds",
            "INN Meals/OON Meals", "INN Preventive Care/OON Preventive Care",
            "INN Supplemental Annual Physical Exam/OON Supplemental Annual Physical Exam", "Ind MOOP",
            "INN Deductible Ind/OON Deductible Ind"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createDataRow(Sheet sheet, VendorMatrixData data, List<ValidationError> correctedErrors,
                             CellStyle highlightStyle, CellStyle normalStyle, boolean highlightChanges) {
        Row dataRow = sheet.createRow(1);
        
        // Create a set of corrected field names for highlighting
        Set<String> correctedFields = correctedErrors.stream()
                .map(error -> error.getBenefitCategory() + " - " + error.getFieldName())
                .collect(Collectors.toSet());
        
        Object[] values = {
            data.getProductName(), data.getProductId(), data.getBasePlan(), data.getBenefitSet(),
            data.getContractId(), data.getPbpNumber(), data.getSegmentId(), data.getPlanYear(),
            "", "", "", "", data.getEffectiveDate(), data.getCompany(), data.getLicensure(),
            data.getInpatientAdmission(), data.getSkilledNursingDays(), data.getCardiacTherapy(),
            data.getIntensiveCardiacTherapy(), data.getSuperviseExerciseTherapy(), data.getPulmonaryRehab(),
            data.getEmergencyServices(), data.getUrgentCare(), data.getHomeHealthVisits(),
            data.getPcpVisits(), data.getChiropracticServices(), data.getOccupationalTherapy(),
            data.getSpecialistVisits(), data.getPodiatryMedicare(), data.getPodiatrySupplemental(),
            data.getPhysicalTherapy(), data.getSpeechTherapy(), data.getTelehealth(),
            data.getDiagnosticTesting(), data.getLabServices(), data.getTherapeuticRadiology(),
            data.getDiagnosticRadiology(), data.getOutpatientHospital(), data.getObservationRoom(),
            data.getAmbulatorySurgery(), data.getOutpatientBlood(), data.getAmbulanceEmergent(),
            data.getAmbulanceAir(), data.getTransportation(), data.getDme(), data.getProsthetics(),
            data.getDiabeticSupplies(), data.getShoeInserts(), data.getDialysisTreatment(),
            data.getAcupuncture(), data.getOtcMedications(), data.getMealsBenefit(),
            data.getPreventiveCare(), data.getAnnualPhysical(), data.getMoop(), data.getDeductible()
        };
        
        for (int i = 0; i < values.length; i++) {
            Cell cell = dataRow.createCell(i);
            if (values[i] != null) {
                cell.setCellValue(values[i].toString());
            }
            
            // Apply highlighting if this field was corrected
            if (highlightChanges && shouldHighlightColumn(i, correctedFields)) {
                cell.setCellStyle(highlightStyle);
            } else {
                cell.setCellStyle(normalStyle);
            }
        }
    }

    private boolean shouldHighlightColumn(int columnIndex, Set<String> correctedFields) {
        // Map column indices to benefit categories
        // This is a simplified mapping - in production, you'd want a more comprehensive mapping
        return switch (columnIndex) {
            case 1 -> correctedFields.contains("Plan Metadata - Product ID");
            case 12 -> correctedFields.contains("Plan Metadata - Effective Date");
            case 15 -> correctedFields.contains("1a - Inpatient Hospital - Cost Sharing");
            case 16 -> correctedFields.contains("2 - SNF - Cost Sharing");
            case 17 -> correctedFields.contains("3 - Cardiac Rehab - Cost Sharing");
            case 21 -> correctedFields.contains("4a - Emergency Care - Cost Sharing");
            case 22 -> correctedFields.contains("4b - Urgent Care - Cost Sharing");
            case 24 -> correctedFields.contains("7a - PCP - Cost Sharing");
            case 27 -> correctedFields.contains("7d - Specialist - Cost Sharing");
            case 42 -> correctedFields.contains("Plan Metadata - MOOP");
            default -> false;
        };
    }

    private void createChangeSummarySheet(Workbook workbook, List<ValidationError> correctedErrors) {
        Sheet summarySheet = workbook.createSheet("Change Summary");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        
        // Create header
        Row headerRow = summarySheet.createRow(0);
        String[] headers = {"Benefit Category", "Field Name", "Original Value", "Corrected Value", "Description"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add change details
        for (int i = 0; i < correctedErrors.size(); i++) {
            ValidationError error = correctedErrors.get(i);
            Row row = summarySheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(error.getBenefitCategory());
            row.createCell(1).setCellValue(error.getFieldName());
            row.createCell(2).setCellValue(error.getVendorMatrixValue());
            row.createCell(3).setCellValue(error.getExpectedValue());
            row.createCell(4).setCellValue(error.getDescription());
            
            // Apply normal style to all cells
            for (int j = 0; j < 5; j++) {
                if (row.getCell(j) != null) {
                    row.getCell(j).setCellStyle(normalStyle);
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            summarySheet.autoSizeColumn(i);
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        if (sheet.getRow(0) != null) {
            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
                
                // Set maximum column width to prevent extremely wide columns
                int columnWidth = sheet.getColumnWidth(i);
                if (columnWidth > 15000) { // 15000 units ≈ reasonable width
                    sheet.setColumnWidth(i, 15000);
                }
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
} 
