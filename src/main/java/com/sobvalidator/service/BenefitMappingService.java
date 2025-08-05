package com.sobvalidator.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sobvalidator.model.SOBBenefit;
import com.sobvalidator.model.SOBData;
import com.sobvalidator.model.SOBType;
import com.sobvalidator.model.VendorMatrixData;

/**
 * Comprehensive benefit mapping service with SOB type-specific logic
 */
public interface BenefitMappingService {
    
    /**
     * Map SOB benefits to VBM columns with high accuracy
     */
    List<BenefitMapping> mapBenefits(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType);
    
    /**
     * Extract and validate benefit conditions (PA, Deductible, MOOP)
     */
    BenefitConditions extractConditions(SOBBenefit sobBenefit, String vbmValue, SOBType sobType);
    
    /**
     * Calculate confidence score for a mapping
     */
    double calculateMappingConfidence(SOBBenefit sobBenefit, String vbmColumn, String vbmValue);
    
    /**
     * Represents a mapping between SOB benefit and VBM column
     */
    class BenefitMapping {
        private final SOBBenefit sobBenefit;
        private final String vbmColumn;
        private final String vbmValue;
        private final double confidence;
        private final BenefitConditions conditions;
        private final List<String> matchingReasons;
        
        public BenefitMapping(SOBBenefit sobBenefit, String vbmColumn, String vbmValue, 
                             double confidence, BenefitConditions conditions, List<String> matchingReasons) {
            this.sobBenefit = sobBenefit;
            this.vbmColumn = vbmColumn;
            this.vbmValue = vbmValue;
            this.confidence = confidence;
            this.conditions = conditions;
            this.matchingReasons = matchingReasons;
        }
        
        // Getters
        public SOBBenefit getSobBenefit() { return sobBenefit; }
        public String getVbmColumn() { return vbmColumn; }
        public String getVbmValue() { return vbmValue; }
        public double getConfidence() { return confidence; }
        public BenefitConditions getConditions() { return conditions; }
        public List<String> getMatchingReasons() { return matchingReasons; }
    }
    
    /**
     * Extracted benefit conditions from VBM
     */
    class BenefitConditions {
        private final String costAmount;
        private final Boolean priorAuthRequired;
        private final Boolean subjectToDeductible;
        private final Boolean moopApplicable;
        private final String paNotes;
        private final String limitations;
        private final Map<String, String> additionalFields;
        
        public BenefitConditions(String costAmount, Boolean priorAuthRequired, Boolean subjectToDeductible,
                               Boolean moopApplicable, String paNotes, String limitations, Map<String, String> additionalFields) {
            this.costAmount = costAmount;
            this.priorAuthRequired = priorAuthRequired;
            this.subjectToDeductible = subjectToDeductible;
            this.moopApplicable = moopApplicable;
            this.paNotes = paNotes;
            this.limitations = limitations;
            this.additionalFields = additionalFields;
        }
        
        // Getters
        public String getCostAmount() { return costAmount; }
        public Boolean getPriorAuthRequired() { return priorAuthRequired; }
        public Boolean getSubjectToDeductible() { return subjectToDeductible; }
        public Boolean getMoopApplicable() { return moopApplicable; }
        public String getPaNotes() { return paNotes; }
        public String getLimitations() { return limitations; }
        public Map<String, String> getAdditionalFields() { return additionalFields; }
    }
} 