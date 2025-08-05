package com.sobvalidator.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sobvalidator.model.BenefitComparison;
import com.sobvalidator.model.ComparisonStatus;
import com.sobvalidator.model.ErrorSeverity;
import com.sobvalidator.model.ErrorType;
import com.sobvalidator.model.SOBBenefit;
import com.sobvalidator.model.SOBData;
import com.sobvalidator.model.SOBType;
import com.sobvalidator.model.ValidationError;
import com.sobvalidator.model.ValidationResult;
import com.sobvalidator.model.ValidationStatus;
import com.sobvalidator.model.ValidationSummary;
import com.sobvalidator.model.VendorMatrixData;
import com.sobvalidator.service.ValidationService;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationServiceImpl.class);

    // VBM Guidelines mapping - from the provided CSV
    private static final Map<String, String> VBM_GUIDELINES = createVBMGuidelines();
    
    private static Map<String, String> createVBMGuidelines() {
        Map<String, String> guidelines = new HashMap<>();
        guidelines.put("INN Inpt. Admission/ OON Inpt. Admission", "All values in column C and Highlighted notation in column D");
        guidelines.put("INN Skilled Nursing Days/OON Skilled Nursing Days", "All values in column C and Highlighted notation in column D");
        guidelines.put("INN ER/OON ER", "INN and OON will cover same cost share and notations");
        guidelines.put("INN Urgent Care Center in Facility/OON Urgent Care Center in Facility", "INN and OON will cover same cost share and notations");
        guidelines.put("INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered", "medicare should be 6 limit");
        guidelines.put("INN Podiatry Supplemental/OONPodiatry Supplemental", "supplemental should be 4 limit");
        guidelines.put("INN Telehealth/ OON Telehealth", "only check column D");
        guidelines.put("INN Ambulance Emergent/OON Ambulance Emergent", "Ambulance ground cost share will cover both INN and OON");
        guidelines.put("INN Ambulance Air/OON Ambulance Air", "cover both INN and OON");
        guidelines.put("INN Dialysis Treatment/OON Dialysis Treatment", "Reneal dialysis will cover INN and OON");
        guidelines.put("INN Acupuncture/OON Acupuncture", "check both supplemental and medicare");
        guidelines.put("INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered", "should cover dignostic radiology and X_ray cost share");
        guidelines.put("INN Advanced Imaging Services/OON Advanced Imaging Services", "should cover advanced diagnostic radiology cost share");
        return guidelines;
    }

    // Comprehensive Benefit Mapping Configuration
    private static final Map<String, Map<String, String>> SOB_TO_VBM_MAPPINGS = new HashMap<>();
    
    static {
        // HIP HMO Mappings (based on analysis of sample files)
        Map<String, String> hipHmoMappings = new HashMap<>();
        hipHmoMappings.put("inpatient hospital", "INN Inpt. Admission");
        hipHmoMappings.put("inpt. admission", "INN Inpt. Admission");
        hipHmoMappings.put("skilled nursing facility", "INN Skilled Nursing Days");
        hipHmoMappings.put("skilled nursing days", "INN Skilled Nursing Days");
        hipHmoMappings.put("emergency room", "INN ER");
        hipHmoMappings.put("er", "INN ER");
        hipHmoMappings.put("emergency", "INN ER");
        hipHmoMappings.put("urgent care", "INN Urgent Care");
        hipHmoMappings.put("primary care physician", "INN PCP");
        hipHmoMappings.put("pcp", "INN PCP");
        hipHmoMappings.put("specialist", "INN Specialist");
        hipHmoMappings.put("podiatry", "INN Podiatry");
        hipHmoMappings.put("lab services", "INN Lab Services Medicare Covered");
        hipHmoMappings.put("laboratory", "INN Lab Services Medicare Covered");
        hipHmoMappings.put("ambulance", "Ambulance");
        hipHmoMappings.put("durable medical equipment", "DME");
        hipHmoMappings.put("dme", "DME");
        hipHmoMappings.put("prosthetics", "Prosthetics");
        hipHmoMappings.put("preventive care", "INN Annual Physical Exam");
        hipHmoMappings.put("annual physical", "INN Annual Physical Exam");
        hipHmoMappings.put("dialysis", "INN Dialysis");
        hipHmoMappings.put("observation services", "INN Observation Services");
        hipHmoMappings.put("ambulatory surgery", "INN Ambulatory Surgery Services");
        hipHmoMappings.put("outpatient blood services", "INN Outpatient Blood Services");
        hipHmoMappings.put("shoe inserts", "Shoe Inserts");
        
        SOB_TO_VBM_MAPPINGS.put("HIP_HMO", hipHmoMappings);
        
        // GHI Mappings (based on analysis of sample files)
        Map<String, String> ghiMappings = new HashMap<>();
        ghiMappings.put("inpatient hospital", "INN Inpt. Admission");
        ghiMappings.put("inpt. admission", "INN Inpt. Admission");
        ghiMappings.put("skilled nursing facility", "INN Skilled Nursing Days");
        ghiMappings.put("skilled nursing days", "INN Skilled Nursing Days");
        ghiMappings.put("emergency room", "INN ER");
        ghiMappings.put("er", "INN ER");
        ghiMappings.put("emergency", "INN ER");
        ghiMappings.put("urgent care", "INN Urgent Care");
        ghiMappings.put("primary care physician", "INN PCP");
        ghiMappings.put("pcp", "INN PCP");
        ghiMappings.put("specialist", "INN Specialist");
        ghiMappings.put("podiatry", "INN Podiatry");
        ghiMappings.put("lab services", "INN Lab Services Medicare Covered");
        ghiMappings.put("laboratory", "INN Lab Services Medicare Covered");
        ghiMappings.put("ambulance", "Ambulance");
        ghiMappings.put("durable medical equipment", "DME");
        ghiMappings.put("dme", "DME");
        ghiMappings.put("prosthetics", "Prosthetics");
        ghiMappings.put("preventive care", "INN Annual Physical Exam");
        ghiMappings.put("annual physical", "INN Annual Physical Exam");
        ghiMappings.put("dialysis", "INN Dialysis");
        
        SOB_TO_VBM_MAPPINGS.put("GHI", ghiMappings);
    }

    @Override
    public ValidationResult validateVendorMatrix(SOBData sobData, VendorMatrixData vendorMatrixData) {
        log.info("Starting generic validation without specific SOB type");
        return validateWithSOBType(sobData, vendorMatrixData, null);
    }

    @Override
    public ValidationResult validateWithSOBType(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType) {
        log.info("Starting validation for SOB type: {}", sobType);
        
        return switch (sobType) {
            case HIP_HMO -> {
                log.info("Validating HIP HMO benefits");
                yield performStandardValidation(sobData, vendorMatrixData, sobType);
            }
            case GHI -> {
                log.info("Validating GHI benefits");
                yield performStandardValidation(sobData, vendorMatrixData, sobType);
            }
        };
    }

    private ValidationResult performStandardValidation(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType) {
        String validationId = generateValidationId();
        List<ValidationError> errors = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            errors.addAll(validateIndividualBenefit(sobBenefit, vendorMatrixData, sobType));
        }
        
        // Create validation summary
        ValidationSummary summary = createValidationSummary(sobData, errors);
        ValidationStatus status = determineValidationStatus(errors);
        
        // Create benefit comparisons for UI
        List<BenefitComparison> comparisons = createBenefitComparisons(sobData, vendorMatrixData, errors, sobType);
        
        return ValidationResult.builder()
                .validationId(validationId)
                .sobType(null) // No specific SOBType for generic validation
                .status(status)
                .errors(errors)
                .summary(summary)
                .benefitComparisons(comparisons)
                .totalErrors((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.CRITICAL || e.getSeverity() == ErrorSeverity.HIGH).count())
                .totalWarnings((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.MEDIUM || e.getSeverity() == ErrorSeverity.LOW).count())
                .validatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public VendorMatrixData generateCorrectedVendorMatrix(SOBData sobData, VendorMatrixData originalVendorMatrix, ValidationResult validationResult) {
        log.info("Generating corrected vendor matrix for validation: {}", validationResult.getValidationId());
        
        // This would typically load the original vendor matrix data and apply corrections
        // For now, return a placeholder implementation
        
        return VendorMatrixData.builder()
                .sourceFileName("corrected_vendor_matrix.xlsx")
                .productName("Corrected Product")
                .productId("CORRECTED-001")
                .effectiveDate("2026-01-01")
                .benefitData(new HashMap<>())
                .build();
    }

    private List<ValidationError> validateHIPHMO(SOBData sobData, VendorMatrixData vendorMatrixData) {
        log.info("Validating HIP HMO benefits");
        List<ValidationError> errors = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            errors.addAll(validateIndividualBenefit(sobBenefit, vendorMatrixData, SOBType.HIP_HMO));
        }
        
        return errors;
    }



    private List<ValidationError> validateGHI(SOBData sobData, VendorMatrixData vendorMatrixData) {
        log.info("Validating GHI benefits");
        List<ValidationError> errors = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            errors.addAll(validateIndividualBenefit(sobBenefit, vendorMatrixData, SOBType.GHI));
        }
        
        return errors;
    }

    private List<ValidationError> validateGeneric(SOBData sobData, VendorMatrixData vendorMatrixData) {
        log.info("Validating with generic rules");
        List<ValidationError> errors = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            errors.addAll(validateIndividualBenefit(sobBenefit, vendorMatrixData, null));
        }
        
        return errors;
    }

    private List<ValidationError> validateIndividualBenefit(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData, SOBType sobType) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Find corresponding vendor matrix field
        String vmValue = getVendorMatrixValueForBenefit(sobBenefit, vendorMatrixData, sobType);
        
        // Check if vmValue is missing or empty
        if (vmValue == null || vmValue.trim().isEmpty()) {
            if (isCriticalBenefit(sobBenefit.getBenefitName())) {  // Use benefit name
                errors.add(createValidationError(
                    ErrorType.MISSING_DATA,
                    ErrorSeverity.HIGH,
                    sobBenefit.getBenefitName(),  // Use benefit name instead of PBP category
                    "Benefit Data",
                    sobBenefit.getCostSharing(),
                    "Not found in Vendor Matrix",
                    sobBenefit.getCostSharing(),
                    "Required benefit missing from Vendor Matrix",
                    "Add this benefit to the Vendor Matrix"
                ));
            }
            return errors;
        }

        // 1. Cost Sharing Validation - Main validation for actual costs
        if (!isCostSharingMatch(sobBenefit.getCostSharing(), vmValue)) {
            errors.add(createValidationError(
                ErrorType.COST_SHARING_MISMATCH,
                ErrorSeverity.HIGH,
                sobBenefit.getBenefitName(),  // Use benefit name instead of PBP category
                "Cost Sharing",
                sobBenefit.getCostSharing(),
                vmValue,
                sobBenefit.getCostSharing(),
                "Cost sharing values do not match between SOB and Vendor Matrix",
                "Update Vendor Matrix to match SOB cost sharing: " + sobBenefit.getCostSharing()
            ));
        }
        
        // 2. Enhanced Prior Authorization Validation - Only if PA is required
        if (sobBenefit.getPaRequired() != null && sobBenefit.getPaRequired()) {
            errors.addAll(validatePriorAuthorizationEnhanced(sobBenefit, vmValue, sobType));
        }
        
        // 3. Enhanced Deductible Validation - Only if deductible applies
        if (sobBenefit.getDeductibleApplicable() != null && sobBenefit.getDeductibleApplicable()) {
            errors.addAll(validateDeductibleEnhanced(sobBenefit, vmValue));
        }
        
        // 4. Enhanced MOOP Validation - Only if MOOP does NOT apply
        if (sobBenefit.getMoopApplicable() != null && !sobBenefit.getMoopApplicable()) {
            errors.addAll(validateMOOPEnhanced(sobBenefit, vmValue));
        }
        
        // 5. VBM Guidelines Validation - Apply specific rules
        errors.addAll(validateWithVBMGuidelines(sobBenefit, vmValue));
        
        return errors;
    }
    
    private boolean isCriticalBenefit(String benefitName) {
        // Define which benefits are critical and must be present
        if (benefitName == null) return false;
        
        String lowerBenefit = benefitName.toLowerCase();
        return lowerBenefit.contains("inpatient hospital") ||
               lowerBenefit.contains("emergency") ||
               lowerBenefit.contains("urgent") ||
               lowerBenefit.contains("pcp") ||
               lowerBenefit.contains("specialist");
    }

    /**
     * Enhanced Prior Authorization validation
     * Check if PA is required and include details/notes
     */
    private List<ValidationError> validatePriorAuthorizationEnhanced(SOBBenefit sobBenefit, String vmValue, SOBType sobType) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check if PA is required in SOB
        if (sobBenefit.getPaRequired() != null && sobBenefit.getPaRequired()) {
            String sobPADetails = buildPADetails(sobBenefit);
            
            // Extract PA information from VM value
            boolean vmHasPA = vmValue != null && (
                vmValue.toLowerCase().contains("prior authorization") ||
                vmValue.toLowerCase().contains("pre-cert") ||
                vmValue.toLowerCase().contains("precert") ||
                vmValue.toLowerCase().contains("pa required")
            );
            
            if (!vmHasPA) {
                errors.add(createValidationError(
                    ErrorType.PRIOR_AUTH_MISMATCH,
                    ErrorSeverity.CRITICAL,
                    sobBenefit.getBenefitName(),  // Use benefit name
                    "Prior Authorization",
                    "Yes - " + sobPADetails,
                    vmValue,
                    "Should include prior authorization requirement",
                    "Prior authorization is required in SOB but not reflected in Vendor Matrix",
                    "Add prior authorization requirement to Vendor Matrix: " + sobPADetails
                ));
            } else {
                // Validate PA details match
                String vmPADetails = extractPADetailsFromVM(vmValue);
                if (!sobPADetails.toLowerCase().contains(vmPADetails.toLowerCase()) && 
                    !vmPADetails.toLowerCase().contains(sobPADetails.toLowerCase())) {
                    
                    errors.add(createValidationError(
                        ErrorType.PRIOR_AUTH_DETAILS_MISMATCH,
                        ErrorSeverity.MEDIUM,
                        sobBenefit.getBenefitName(),  // Use benefit name
                        "Prior Authorization Details",
                        sobPADetails,
                        vmPADetails,
                        sobPADetails,
                        "Prior authorization details differ between SOB and Vendor Matrix",
                        "Update Vendor Matrix PA details to match SOB: " + sobPADetails
                    ));
                }
            }
        }
        
        return errors;
    }

    /**
     * Enhanced Deductible validation
     * Check if deductible is applicable and include details
     */
    private List<ValidationError> validateDeductibleEnhanced(SOBBenefit sobBenefit, String vmValue) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check if deductible is applicable in SOB
        if (sobBenefit.getDeductibleApplicable() != null && sobBenefit.getDeductibleApplicable()) {
            String deductibleInfo = "Yes - Subject to deductible";
            
            // Check if VM reflects deductible applicability
            boolean vmHasDeductible = vmValue != null && (
                vmValue.toLowerCase().contains("deductible") ||
                vmValue.toLowerCase().contains("subject to") ||
                vmValue.toLowerCase().contains("after deductible")
            );
            
            if (!vmHasDeductible) {
                errors.add(createValidationError(
                    ErrorType.DEDUCTIBLE_MISMATCH,
                    ErrorSeverity.HIGH,
                    sobBenefit.getBenefitName(),  // Use benefit name
                    "Deductible Applicable",
                    deductibleInfo,
                    vmValue,
                    "Should indicate subject to deductible",
                    "Benefit is subject to deductible in SOB but not reflected in Vendor Matrix",
                    "Update Vendor Matrix to indicate this benefit is subject to deductible"
                ));
            }
        }
        
        return errors;
    }

    /**
     * Enhanced MOOP validation
     * Check if MOOP is not applicable and state accordingly
     */
    private List<ValidationError> validateMOOPEnhanced(SOBBenefit sobBenefit, String vmValue) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check if MOOP is not applicable in SOB
        if (sobBenefit.getMoopApplicable() != null && !sobBenefit.getMoopApplicable()) {
            String expectedMoopStatement = "Does not apply to MOOP";
            
            // Check if VM correctly states MOOP non-applicability
            boolean vmHasMoopStatement = vmValue != null && (
                vmValue.toLowerCase().contains("does not apply to moop") ||
                vmValue.toLowerCase().contains("not applicable to moop") ||
                vmValue.toLowerCase().contains("moop not applicable")
            );
            
            if (!vmHasMoopStatement) {
                errors.add(createValidationError(
                    ErrorType.MOOP_STATEMENT_MISSING,
                    ErrorSeverity.MEDIUM,
                    sobBenefit.getBenefitName(),  // Use benefit name
                    "MOOP Applicable",
                    "No - " + expectedMoopStatement,
                    vmValue,
                    expectedMoopStatement,
                    "Benefit does not apply to MOOP but statement is missing in Vendor Matrix",
                    "Add MOOP statement to Vendor Matrix: " + expectedMoopStatement
                ));
            }
        }
        
        return errors;
    }

    /**
     * Validate based on VBM Guidelines
     */
    private List<ValidationError> validateWithVBMGuidelines(SOBBenefit sobBenefit, String vmValue) {
        List<ValidationError> errors = new ArrayList<>();
        
        String benefitKey = findVBMGuidelineKey(sobBenefit.getBenefitName());  // Use benefit name
        if (benefitKey != null) {
            String guideline = VBM_GUIDELINES.get(benefitKey);
            errors.addAll(applyVBMGuideline(sobBenefit, vmValue, benefitKey, guideline));
        }
        
        return errors;
    }

    private String findVBMGuidelineKey(String benefitName) {
        // Map benefit names to VBM guideline keys
        for (String key : VBM_GUIDELINES.keySet()) {
            // Try direct mapping first
            if (key.toLowerCase().contains("inpt") && "inpatient hospital".equalsIgnoreCase(benefitName)) {
                return key;
            }
            if (key.toLowerCase().contains("snf") && "snf".equalsIgnoreCase(benefitName)) {
                return key;
            }
            if (key.toLowerCase().contains("er") && benefitName.toLowerCase().contains("emergency")) {
                return key;
            }
            if (key.toLowerCase().contains("urgent") && benefitName.toLowerCase().contains("urgent")) {
                return key;
            }
            if (key.toLowerCase().contains("pcp") && "pcp".equalsIgnoreCase(benefitName)) {
                return key;
            }
            if (key.toLowerCase().contains("specialist") && "specialist".equalsIgnoreCase(benefitName)) {
                return key;
            }
            if (key.toLowerCase().contains("podiatry medicare") && benefitName.toLowerCase().contains("podiatry")) {
                return key;
            }
            if (key.toLowerCase().contains("podiatry supplemental") && benefitName.toLowerCase().contains("podiatry")) {
                return key;
            }
            if (key.toLowerCase().contains("lab") && benefitName.toLowerCase().contains("lab")) {
                return key;
            }
            if (key.toLowerCase().contains("ambulance") && benefitName.toLowerCase().contains("ambulance")) {
                return key;
            }
            if (key.toLowerCase().contains("dme") && "dme".equalsIgnoreCase(benefitName)) {
                return key;
            }
            if (key.toLowerCase().contains("dialysis") && benefitName.toLowerCase().contains("dialysis")) {
                return key;
            }
        }
        return null;
    }

    private List<ValidationError> applyVBMGuideline(SOBBenefit sobBenefit, String vmValue, String guidelineKey, String guideline) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (guideline.contains("6 limit") && sobBenefit.getBenefitName().toLowerCase().contains("podiatry")) {
            // Check for 6 limit rule
            if (vmValue != null && !vmValue.contains("6")) {
                errors.add(createValidationError(
                    ErrorType.GUIDELINE_VIOLATION,
                    ErrorSeverity.MEDIUM,
                    sobBenefit.getBenefitName(),  // Use benefit name
                    "Medicare Limit",
                    "Should have 6 limit",
                    vmValue,
                    "Include 6 limit for medicare coverage",
                    "VBM Guideline violation: Medicare should have 6 limit",
                    "Update to include 6 limit as per VBM guidelines"
                ));
            }
        }
        
        if (guideline.contains("4 limit") && sobBenefit.getBenefitName().toLowerCase().contains("podiatry")) {
            // Check for 4 limit rule
            if (vmValue != null && !vmValue.contains("4")) {
                errors.add(createValidationError(
                    ErrorType.GUIDELINE_VIOLATION,
                    ErrorSeverity.MEDIUM,
                    sobBenefit.getBenefitName(),  // Use benefit name
                    "Supplemental Limit",
                    "Should have 4 limit",
                    vmValue,
                    "Include 4 limit for supplemental coverage",
                    "VBM Guideline violation: Supplemental should have 4 limit",
                    "Update to include 4 limit as per VBM guidelines"
                ));
            }
        }
        
        if (guideline.contains("INN and OON will cover same cost share")) {
            // Check for INN/OON consistency
            if (vmValue != null) {
                String[] parts = vmValue.split("OON|Out-of-Network");
                if (parts.length > 1) {
                    String innPart = parts[0].trim();
                    String oonPart = parts[1].trim();
                    if (!innPart.equals(oonPart)) {
                        errors.add(createValidationError(
                            ErrorType.INN_OON_MISMATCH,
                            ErrorSeverity.HIGH,
                            sobBenefit.getBenefitName(),  // Use benefit name
                            "INN/OON Cost Share",
                            "Should be same for INN and OON",
                            vmValue,
                            "Make INN and OON cost shares identical",
                            "VBM Guideline violation: INN and OON should have same cost share",
                            "Update to make INN and OON cost shares identical"
                        ));
                    }
                }
            }
        }
        
        return errors;
    }

    private String buildPADetails(SOBBenefit sobBenefit) {
        StringBuilder details = new StringBuilder();
        if (sobBenefit.getPaNotes() != null && !sobBenefit.getPaNotes().trim().isEmpty()) {
            details.append(sobBenefit.getPaNotes());
        } else {
            details.append("Prior authorization required");
        }
        return details.toString();
    }

    private String extractPADetailsFromVM(String vmValue) {
        if (vmValue == null) return "";
        
        // Extract PA-related information from VM value
        String[] sentences = vmValue.split("\\.");
        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains("prior authorization") || 
                sentence.toLowerCase().contains("pre-cert") ||
                sentence.toLowerCase().contains("precert")) {
                return sentence.trim();
            }
        }
        return "";
    }

    private boolean isCostSharingMatch(String sobCostSharing, String vmCostSharing) {
        if (sobCostSharing == null && vmCostSharing == null) return true;
        if (sobCostSharing == null || vmCostSharing == null) return false;
        
        // Normalize cost sharing for comparison
        String normalizedSOB = normalizeCostSharing(sobCostSharing);
        String normalizedVM = normalizeCostSharing(vmCostSharing);
        
        // Extract key values from both strings for comparison
        List<String> sobValues = extractCostValues(normalizedSOB);
        List<String> vmValues = extractCostValues(normalizedVM);
        
        // If we found numeric values, compare them
        if (!sobValues.isEmpty() && !vmValues.isEmpty()) {
            return sobValues.equals(vmValues);
        }
        
        // Otherwise fall back to normalized string comparison
        return normalizedSOB.equals(normalizedVM);
    }

    private String normalizeCostSharing(String costSharing) {
        if (costSharing == null) return "";
        
        // Keep more structure for better comparison
        return costSharing.toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("\\n", " ")
                .replaceAll("\\r", " ")
                .trim();
    }
    
    private List<String> extractCostValues(String text) {
        List<String> values = new ArrayList<>();
        // Match currency values like $290, $0, etc.
        Pattern pattern = Pattern.compile("\\$\\d+(?:,\\d{3})*(?:\\.\\d{2})?");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            values.add(matcher.group());
        }
        
        // Also match percentages
        pattern = Pattern.compile("\\d+%");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            values.add(matcher.group());
        }
        
        return values;
    }

    private String getVendorMatrixValueForBenefit(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData, SOBType sobType) {
        // Map SOB benefit names to vendor matrix column headers
        Map<String, String> allColumns = vendorMatrixData.getAllColumns();
        if (allColumns == null) {
            return null;
        }
        
        String benefitName = sobBenefit.getBenefitName();
        if (benefitName == null) {
            return null;
        }
        
        // Direct mapping based on benefit names from SOB to VBM column headers using SOB type
        String vmColumnName = mapBenefitNameToVMColumn(benefitName, sobType, vendorMatrixData);
        if (vmColumnName != null) {
            String value = allColumns.get(vmColumnName);
            if (value != null) {
                return value;
            }
        }
        
        // Fallback: try partial matching if direct mapping fails
        String normalizedBenefitName = benefitName.toLowerCase().trim();
        for (Map.Entry<String, String> entry : allColumns.entrySet()) {
            String columnName = entry.getKey().toLowerCase();
            
            // Try various matching strategies
            if (isColumnMatch(normalizedBenefitName, columnName)) {
                return entry.getValue();
            }
        }
        
        log.debug("No VM column found for SOB benefit: {}", benefitName);
        return null;
    }
    
    /**
     * Enhanced benefit mapping using configuration-driven approach
     * 1. Exact mapping table lookup
     * 2. Fuzzy matching with keywords
     * 3. Pattern-based fallback algorithms
     */
    private String mapBenefitNameToVMColumn(String sobBenefitName, SOBType sobType, VendorMatrixData vendorMatrixData) {
        if (sobBenefitName == null || vendorMatrixData == null) {
            log.warn("Null parameters in benefit mapping: sobBenefitName={}, vendorMatrixData={}", sobBenefitName, vendorMatrixData != null);
            return null;
        }

        String cleanBenefitName = sobBenefitName.toLowerCase().trim();
        String sobTypeKey = sobType != null ? sobType.name() : "GENERIC";
        
        log.debug("Mapping SOB benefit '{}' for type '{}' to VBM column", cleanBenefitName, sobTypeKey);

        // Step 1: Exact mapping table lookup
        Map<String, String> typeSpecificMappings = SOB_TO_VBM_MAPPINGS.get(sobTypeKey);
        if (typeSpecificMappings != null) {
            // Try exact match first
            String exactMatch = typeSpecificMappings.get(cleanBenefitName);
            if (exactMatch != null && vendorMatrixData.getAllColumns().containsKey(exactMatch)) {
                log.debug("Found exact mapping: '{}' -> '{}'", cleanBenefitName, exactMatch);
                return exactMatch;
            }
            
            // Try partial keyword matches
            for (Map.Entry<String, String> entry : typeSpecificMappings.entrySet()) {
                if (cleanBenefitName.contains(entry.getKey()) || entry.getKey().contains(cleanBenefitName)) {
                    String candidate = entry.getValue();
                    if (vendorMatrixData.getAllColumns().containsKey(candidate)) {
                        log.debug("Found keyword mapping: '{}' contains '{}' -> '{}'", cleanBenefitName, entry.getKey(), candidate);
                        return candidate;
                    }
                }
            }
        }

        // Step 2: Advanced fuzzy matching with VBM column headers
        String bestMatch = findBestVMColumnMatch(cleanBenefitName, vendorMatrixData);
        if (bestMatch != null) {
            log.debug("Found fuzzy match: '{}' -> '{}'", cleanBenefitName, bestMatch);
            return bestMatch;
        }

        // Step 3: Pattern-based fallback for unknown benefits
        String patternMatch = findPatternBasedMatch(cleanBenefitName, vendorMatrixData);
        if (patternMatch != null) {
            log.debug("Found pattern match: '{}' -> '{}'", cleanBenefitName, patternMatch);
            return patternMatch;
        }

        log.warn("No mapping found for SOB benefit: '{}'", sobBenefitName);
        return null;
    }

    /**
     * Advanced fuzzy matching algorithm using multiple scoring criteria
     */
    private String findBestVMColumnMatch(String sobBenefitName, VendorMatrixData vendorMatrixData) {
        String bestMatch = null;
        double bestScore = 0.0;
        final double MINIMUM_MATCH_SCORE = 0.4; // Threshold for accepting a match

        for (String vmColumn : vendorMatrixData.getAllColumns().keySet()) {
            double score = calculateMatchScore(sobBenefitName, vmColumn.toLowerCase());
            
            if (score > bestScore && score >= MINIMUM_MATCH_SCORE) {
                bestScore = score;
                bestMatch = vmColumn;
            }
        }

        if (bestMatch != null) {
            log.debug("Best fuzzy match for '{}': '{}' (score: {:.2f})", sobBenefitName, bestMatch, bestScore);
        }

        return bestMatch;
    }

    /**
     * Multi-criteria scoring for benefit name matching
     */
    private double calculateMatchScore(String sobName, String vmColumn) {
        double score = 0.0;
        
        // Exact substring match gets highest score
        if (vmColumn.contains(sobName) || sobName.contains(vmColumn)) {
            score += 0.8;
        }
        
        // Keyword overlap scoring
        String[] sobWords = sobName.split("\\s+");
        String[] vmWords = vmColumn.split("\\s+");
        
        int matchingWords = 0;
        for (String sobWord : sobWords) {
            for (String vmWord : vmWords) {
                if (sobWord.length() > 2 && vmWord.length() > 2) {
                    if (sobWord.equals(vmWord)) {
                        matchingWords++;
                    } else if (sobWord.contains(vmWord) || vmWord.contains(sobWord)) {
                        matchingWords++;
                    }
                }
            }
        }
        
        if (sobWords.length > 0) {
            score += 0.4 * ((double) matchingWords / sobWords.length);
        }
        
        // Special medical term matching
        score += calculateMedicalTermScore(sobName, vmColumn);
        
        return Math.min(score, 1.0); // Cap at 1.0
    }

    /**
     * Specialized scoring for medical terminology
     */
    private double calculateMedicalTermScore(String sobName, String vmColumn) {
        double score = 0.0;
        
        // Common medical abbreviations and terms
        Map<String, String[]> medicalTerms = Map.of(
            "inpatient", new String[]{"inpt", "admission", "hospital"},
            "emergency", new String[]{"er", "urgent"},
            "physician", new String[]{"pcp", "doctor", "provider"},
            "laboratory", new String[]{"lab", "blood", "test"},
            "skilled nursing", new String[]{"snf", "nursing"},
            "durable medical", new String[]{"dme", "equipment"},
            "ambulatory", new String[]{"outpatient", "surgery"}
        );
        
        for (Map.Entry<String, String[]> entry : medicalTerms.entrySet()) {
            if (sobName.contains(entry.getKey())) {
                for (String synonym : entry.getValue()) {
                    if (vmColumn.contains(synonym)) {
                        score += 0.3;
                        break;
                    }
                }
            }
        }
        
        return score;
    }

    /**
     * Pattern-based matching for completely unknown benefits
     */
    private String findPatternBasedMatch(String sobBenefitName, VendorMatrixData vendorMatrixData) {
        // Look for common patterns in VM columns
        for (String vmColumn : vendorMatrixData.getAllColumns().keySet()) {
            String lowerVmColumn = vmColumn.toLowerCase();
            
            // Pattern: INN/OON prefix matching
            if (lowerVmColumn.startsWith("inn ") || lowerVmColumn.startsWith("oon ")) {
                String vmSuffix = lowerVmColumn.substring(4);
                if (sobBenefitName.contains(vmSuffix) || vmSuffix.contains(sobBenefitName)) {
                    return vmColumn;
                }
            }
            
            // Pattern: Direct benefit type matching
            if (sobBenefitName.length() > 3 && lowerVmColumn.contains(sobBenefitName.substring(0, 4))) {
                return vmColumn;
            }
        }
        
        return null;
    }

    private boolean isColumnMatch(String benefitName, String columnName) {
        // Various matching strategies for partial matches
        
        // Direct substring matching
        if (columnName.contains(benefitName) || benefitName.contains(columnName)) {
            return true;
        }
        
        // Key word matching
        String[] benefitWords = benefitName.split("\\s+");
        String[] columnWords = columnName.split("\\s+");
        
        // Check if major keywords match
        for (String benefitWord : benefitWords) {
            if (benefitWord.length() > 3) { // Only significant words
                for (String columnWord : columnWords) {
                    if (columnWord.toLowerCase().contains(benefitWord.toLowerCase()) ||
                        benefitWord.toLowerCase().contains(columnWord.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        
        // Special cases
        if (benefitName.contains("inpatient") && columnName.contains("inpt")) return true;
        if (benefitName.contains("emergency") && columnName.contains("er")) return true;
        if (benefitName.contains("urgent") && columnName.contains("urgent")) return true;
        if (benefitName.contains("specialist") && columnName.contains("specialist")) return true;
        if (benefitName.contains("lab") && columnName.contains("lab")) return true;
        if (benefitName.contains("ambulance") && columnName.contains("ambulance")) return true;
        if (benefitName.contains("dme") && columnName.contains("dme")) return true;
        if (benefitName.contains("dialysis") && columnName.contains("dialysis")) return true;
        if (benefitName.contains("preventive") && columnName.contains("preventive")) return true;
        
        return false;
    }

    private ValidationError createValidationError(ErrorType errorType, ErrorSeverity severity, 
                                                String benefitCategory, String fieldName,
                                                String sobValue, String vendorMatrixValue, String expectedValue,
                                                String description, String recommendation) {
        return ValidationError.builder()
                .errorId(generateErrorId())
                .errorType(errorType)
                .severity(severity)
                .benefitCategory(benefitCategory)
                .fieldName(fieldName)
                .sobValue(sobValue)
                .vendorMatrixValue(vendorMatrixValue)
                .expectedValue(expectedValue)
                .description(description)
                .recommendation(recommendation)
                .selected(false)
                .build();
    }

    private ValidationSummary createValidationSummary(SOBData sobData, List<ValidationError> errors) {
        Set<String> benefitsWithErrors = errors.stream()
                .map(ValidationError::getBenefitCategory)  // This now refers to benefit names
                .collect(Collectors.toSet());
        
        return ValidationSummary.builder()
                .benefitsValidated(sobData.getBenefits().size())
                .benefitsWithErrors(benefitsWithErrors.size())
                .totalDiscrepancies(errors.size())
                .criticalErrors((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.CRITICAL).count())
                .highErrors((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.HIGH).count())
                .mediumErrors((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.MEDIUM).count())
                .lowErrors((int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.LOW).count())
                .build();
    }

    private List<BenefitComparison> createBenefitComparisons(SOBData sobData, VendorMatrixData vendorMatrixData, List<ValidationError> errors, SOBType sobType) {
        List<BenefitComparison> comparisons = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            String vmValue = getVendorMatrixValueForBenefit(sobBenefit, vendorMatrixData, sobType);
            
            List<ValidationError> benefitErrors = errors.stream()
                    .filter(e -> e.getBenefitCategory().equals(sobBenefit.getBenefitName()))  // Compare with benefit name
                    .collect(Collectors.toList());
            
            ComparisonStatus status = benefitErrors.isEmpty() ? ComparisonStatus.MATCH : 
                                    benefitErrors.stream().anyMatch(e -> e.getSeverity() == ErrorSeverity.CRITICAL || e.getSeverity() == ErrorSeverity.HIGH) ? 
                                    ComparisonStatus.MISMATCH : ComparisonStatus.PARTIAL_MATCH;
            
            comparisons.add(BenefitComparison.builder()
                    .benefitCategory(sobBenefit.getBenefitName())  // Use benefit name
                    .benefitName(sobBenefit.getBenefitName())
                    .sobBenefit(sobBenefit)
                    .vendorMatrixValue(vmValue)
                    .status(status)
                    .errors(benefitErrors)
                    .build());
        }
        
        return comparisons;
    }

    private ValidationStatus determineValidationStatus(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            return ValidationStatus.PASSED;
        }
        
        boolean hasCriticalOrHigh = errors.stream()
                .anyMatch(e -> e.getSeverity() == ErrorSeverity.CRITICAL || e.getSeverity() == ErrorSeverity.HIGH);
        
        return hasCriticalOrHigh ? ValidationStatus.FAILED_WITH_ERRORS : ValidationStatus.PASSED_WITH_WARNINGS;
    }

    private String generateValidationId() {
        return "VAL-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateErrorId() {
        return "ERR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
} 