package com.vbmvalidator.service;

import java.util.List;
import java.util.Map;

import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.BenefitMappingService.BenefitConditions;
import com.vbmvalidator.service.BenefitMappingService.BenefitMapping;

/**
 * SOB Type-specific processor for detailed benefit analysis
 * Each SOB type (HIP_HMO, GHI) has different column structures and validation rules
 */
public interface SOBTypeProcessor {
    
    /**
     * Get the SOB type this processor handles
     */
    SOBType getSupportedSOBType();
    
    /**
     * Detect and map VBM columns for this SOB type
     * Returns column detection results with confidence scores
     */
    ColumnDetectionResult detectVBMColumns(VendorMatrixData vendorMatrixData);
    
    /**
     * Find the best VBM column match for a SOB benefit
     */
    BenefitMapping findBestColumnMatch(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData);
    
    /**
     * Extract conditions from VBM value using SOB type-specific rules
     */
    BenefitConditions extractConditions(String vbmValue, SOBBenefit sobBenefit);
    
    /**
     * Validate extracted conditions against SOB benefit
     */
    List<ValidationIssue> validateConditions(BenefitConditions vbmConditions, SOBBenefit sobBenefit);
    
    /**
     * Get SOB type-specific benefit name patterns
     */
    Map<String, List<String>> getBenefitNamePatterns();
    
    /**
     * Get SOB type-specific VBM column patterns
     */
    Map<String, List<String>> getVBMColumnPatterns();
    
    /**
     * Column detection results
     */
    class ColumnDetectionResult {
        private final Map<String, String> detectedColumns; // benefit category -> VBM column
        private final Map<String, Double> confidenceScores; // column -> confidence
        private final List<String> unmatched; // unmatched SOB benefits
        private final List<String> ambiguous; // ambiguous matches
        
        public ColumnDetectionResult(Map<String, String> detectedColumns, Map<String, Double> confidenceScores,
                                   List<String> unmatched, List<String> ambiguous) {
            this.detectedColumns = detectedColumns;
            this.confidenceScores = confidenceScores;
            this.unmatched = unmatched;
            this.ambiguous = ambiguous;
        }
        
        // Getters
        public Map<String, String> getDetectedColumns() { return detectedColumns; }
        public Map<String, Double> getConfidenceScores() { return confidenceScores; }
        public List<String> getUnmatched() { return unmatched; }
        public List<String> getAmbiguous() { return ambiguous; }
    }
    
    /**
     * Validation issue found during condition validation
     */
    class ValidationIssue {
        private final String type;
        private final String description;
        private final String sobValue;
        private final String vbmValue;
        private final String severity; // HIGH, MEDIUM, LOW
        
        public ValidationIssue(String type, String description, String sobValue, String vbmValue, String severity) {
            this.type = type;
            this.description = description;
            this.sobValue = sobValue;
            this.vbmValue = vbmValue;
            this.severity = severity;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getSobValue() { return sobValue; }
        public String getVbmValue() { return vbmValue; }
        public String getSeverity() { return severity; }
    }
} 
