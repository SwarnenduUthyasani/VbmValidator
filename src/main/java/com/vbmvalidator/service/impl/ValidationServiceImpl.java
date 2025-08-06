package com.vbmvalidator.service.impl;

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

import com.vbmvalidator.model.BenefitComparison;
import com.vbmvalidator.model.ComparisonStatus;
import com.vbmvalidator.model.ErrorSeverity;
import com.vbmvalidator.model.ErrorType;
import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.ValidationError;
import com.vbmvalidator.model.ValidationResult;
import com.vbmvalidator.model.ValidationStatus;
import com.vbmvalidator.model.ValidationSummary;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.ValidationService;

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

    @Override
    public ValidationResult validateVendorMatrix(SOBData sobData, VendorMatrixData vendorMatrixData) {
        log.info("Starting generic validation without specific SOB type");
        return validateWithSOBType(sobData, vendorMatrixData, null);
    }

    @Override
    public ValidationResult validateWithSOBType(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType) {
        log.info("Starting validation for SOB type: {}", sobType);
        
        String validationId = generateValidationId();
        List<ValidationError> errors = new ArrayList<>();
        
        // Enhanced validation based on SOB type
        switch (sobType) {
            case HIP_HMO -> errors.addAll(validateHIPHMO(sobData, vendorMatrixData));
            case GHI -> errors.addAll(validateGHI(sobData, vendorMatrixData));
            default -> errors.addAll(validateGeneric(sobData, vendorMatrixData));
        }
        
        // Create validation summary
        ValidationSummary summary = createValidationSummary(sobData, errors);
        ValidationStatus status = determineValidationStatus(errors);
        
        // Create benefit comparisons for UI
        List<BenefitComparison> comparisons = createBenefitComparisons(sobData, vendorMatrixData, errors);
        
        return ValidationResult.builder()
                .validationId(validationId)
                .sobType(sobType)
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
        String vmValue = getVendorMatrixValueForBenefit(sobBenefit, vendorMatrixData);
        
        // Skip validation if no corresponding VM value found
        if (vmValue == null || vmValue.trim().isEmpty()) {
            // Only report error if this is a critical benefit
            if (isCriticalBenefit(sobBenefit.getPbpCategory())) {
                errors.add(createValidationError(
                    ErrorType.MISSING_DATA,
                    ErrorSeverity.HIGH,
                    sobBenefit.getBenefitCategory(),
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
                sobBenefit.getBenefitCategory(),
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
    
    private boolean isCriticalBenefit(String pbpCategory) {
        // Define which benefits are critical and must be present
        Set<String> criticalBenefits = Set.of("1a", "1b", "2", "4a", "4b", "7a", "7d");
        return criticalBenefits.contains(pbpCategory);
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
                    sobBenefit.getBenefitCategory(),
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
                        sobBenefit.getBenefitCategory(),
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
                    sobBenefit.getBenefitCategory(),
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
                    sobBenefit.getBenefitCategory(),
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
        
        String benefitKey = findVBMGuidelineKey(sobBenefit.getBenefitCategory());
        if (benefitKey != null) {
            String guideline = VBM_GUIDELINES.get(benefitKey);
            errors.addAll(applyVBMGuideline(sobBenefit, vmValue, benefitKey, guideline));
        }
        
        return errors;
    }

    private String findVBMGuidelineKey(String benefitCategory) {
        // Fuzzy matching to find the best VBM guideline key
        for (String key : VBM_GUIDELINES.keySet()) {
            if (key.toLowerCase().contains(benefitCategory.toLowerCase()) || 
                benefitCategory.toLowerCase().contains(key.toLowerCase().split("/")[0].toLowerCase().trim())) {
                return key;
            }
        }
        return null;
    }

    private List<ValidationError> applyVBMGuideline(SOBBenefit sobBenefit, String vmValue, String guidelineKey, String guideline) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (guideline.contains("6 limit") && sobBenefit.getBenefitCategory().toLowerCase().contains("medicare")) {
            // Check for 6 limit rule
            if (vmValue != null && !vmValue.contains("6")) {
                errors.add(createValidationError(
                    ErrorType.GUIDELINE_VIOLATION,
                    ErrorSeverity.MEDIUM,
                    sobBenefit.getBenefitCategory(),
                    "Medicare Limit",
                    "Should have 6 limit",
                    vmValue,
                    "Include 6 limit for medicare coverage",
                    "VBM Guideline violation: Medicare should have 6 limit",
                    "Update to include 6 limit as per VBM guidelines"
                ));
            }
        }
        
        if (guideline.contains("4 limit") && sobBenefit.getBenefitCategory().toLowerCase().contains("supplemental")) {
            // Check for 4 limit rule
            if (vmValue != null && !vmValue.contains("4")) {
                errors.add(createValidationError(
                    ErrorType.GUIDELINE_VIOLATION,
                    ErrorSeverity.MEDIUM,
                    sobBenefit.getBenefitCategory(),
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
                            sobBenefit.getBenefitCategory(),
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

    private String getVendorMatrixValueForBenefit(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData) {
        // Map SOB benefit to vendor matrix field
        Map<String, String> vmData = vendorMatrixData.getBenefitData();
        if (vmData == null) {
            return null;
        }
        
        String pbpCategory = sobBenefit.getPbpCategory();
        if (pbpCategory == null) {
            return null;
        }
        
        // Direct mapping based on PBP categories from the SOB
        switch (pbpCategory) {
            case "1a":
                return vmData.get("1a - Inpatient Hospital");
            case "1b":
                return vmData.get("INN Inpt. Mental Health/OON Inpt. Mental Health");
            case "2":
                return vmData.get("2 - SNF");
            case "3":
                return vmData.get("3 - Cardiac Rehab");
            case "3-2":
                return vmData.get("3-2 - Intensive Cardiac Rehab");
            case "3-3":
                return vmData.get("3-3 - Pulmonary Rehab");
            case "4a":
                return vmData.get("4a - Emergency Care");
            case "4b":
                return vmData.get("4b - Urgent Care");
            case "5a":
                return vmData.get("5a - Home Health");
            case "7a":
                return vmData.get("7a - PCP");
            case "7b":
                return vmData.get("7b - Chiropractic");
            case "7c":
                return vmData.get("7c - Occupational Therapy");
            case "7d":
                return vmData.get("7d - Specialist");
            case "8a":
                return vmData.get("8a - Lab Services");
            case "8b":
                return vmData.get("8b - Diagnostic Radiology");
            case "8c":
                return vmData.get("8c - Therapeutic Radiology");
            case "9a":
                return vmData.get("9a - Ambulance");
            case "12":
                return vmData.get("12 - DME");
            case "14":
                return vmData.get("14 - Dialysis");
            default:
                // For other categories, try to find a match in the VM data
                for (Map.Entry<String, String> entry : vmData.entrySet()) {
                    if (entry.getKey().startsWith(pbpCategory + " - ") || 
                        entry.getKey().contains(sobBenefit.getBenefitName())) {
                        return entry.getValue();
                    }
                }
        }
        
        return null;
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
                .map(ValidationError::getBenefitCategory)
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

    private List<BenefitComparison> createBenefitComparisons(SOBData sobData, VendorMatrixData vendorMatrixData, List<ValidationError> errors) {
        List<BenefitComparison> comparisons = new ArrayList<>();
        
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            String vmValue = getVendorMatrixValueForBenefit(sobBenefit, vendorMatrixData);
            
            List<ValidationError> benefitErrors = errors.stream()
                    .filter(e -> e.getBenefitCategory().equals(sobBenefit.getBenefitCategory()))
                    .collect(Collectors.toList());
            
            ComparisonStatus status = benefitErrors.isEmpty() ? ComparisonStatus.MATCH : 
                                    benefitErrors.stream().anyMatch(e -> e.getSeverity() == ErrorSeverity.CRITICAL || e.getSeverity() == ErrorSeverity.HIGH) ? 
                                    ComparisonStatus.MISMATCH : ComparisonStatus.PARTIAL_MATCH;
            
            comparisons.add(BenefitComparison.builder()
                    .benefitCategory(sobBenefit.getBenefitCategory())
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
