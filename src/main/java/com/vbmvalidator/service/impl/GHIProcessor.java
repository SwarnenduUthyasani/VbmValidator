package com.vbmvalidator.service.impl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.BenefitMappingService.BenefitConditions;
import com.vbmvalidator.service.BenefitMappingService.BenefitMapping;
import com.vbmvalidator.service.SOBTypeProcessor;

/**
 * GHI specific processor with detailed benefit analysis
 * Handles GHI column detection, benefit mapping, and condition validation
 */
@Service
public class GHIProcessor implements SOBTypeProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(GHIProcessor.class);
    
    // GHI specific benefit name patterns (may differ from HIP HMO)
    private static final Map<String, List<String>> BENEFIT_NAME_PATTERNS = createBenefitNamePatterns();
    
    private static Map<String, List<String>> createBenefitNamePatterns() {
        Map<String, List<String>> patterns = new HashMap<>();
        patterns.put("INPATIENT_HOSPITAL", Arrays.asList("inpatient hospital", "hospital admission", "acute care", "inpatient medical"));
        patterns.put("SKILLED_NURSING", Arrays.asList("skilled nursing", "nursing facility", "snf", "skilled care"));
        patterns.put("EMERGENCY", Arrays.asList("emergency", "emergency room", "er visit", "emergency care"));
        patterns.put("URGENT_CARE", Arrays.asList("urgent care", "walk-in clinic", "urgent treatment"));
        patterns.put("PRIMARY_CARE", Arrays.asList("primary care", "family doctor", "general practitioner", "pcp"));
        patterns.put("SPECIALIST", Arrays.asList("specialist", "specialty care", "specialist visit"));
        patterns.put("PODIATRY", Arrays.asList("podiatry", "foot care", "podiatrist"));
        patterns.put("LAB_SERVICES", Arrays.asList("laboratory", "lab services", "blood work", "diagnostic lab"));
        patterns.put("AMBULANCE", Arrays.asList("ambulance", "emergency transport", "medical transport"));
        patterns.put("DME", Arrays.asList("durable medical equipment", "medical devices", "dme"));
        patterns.put("PROSTHETICS", Arrays.asList("prosthetics", "artificial limbs", "prosthetic devices"));
        patterns.put("PREVENTIVE", Arrays.asList("preventive care", "wellness", "annual exam", "screening"));
        patterns.put("DIALYSIS", Arrays.asList("dialysis", "kidney treatment", "renal care"));
        return patterns;
    }
    
    // GHI specific VBM column patterns
    private static final Map<String, List<String>> VBM_COLUMN_PATTERNS = createVBMColumnPatterns();
    
    private static Map<String, List<String>> createVBMColumnPatterns() {
        Map<String, List<String>> patterns = new HashMap<>();
        patterns.put("INPATIENT_HOSPITAL", Arrays.asList("Inpatient Hospital", "Hospital Admission", "INN Inpt", "OON Inpt"));
        patterns.put("SKILLED_NURSING", Arrays.asList("Skilled Nursing", "Nursing Facility", "SNF", "INN SNF", "OON SNF"));
        patterns.put("EMERGENCY", Arrays.asList("Emergency Room", "ER", "Emergency Care", "INN ER", "OON ER"));
        patterns.put("URGENT_CARE", Arrays.asList("Urgent Care", "Walk-in", "INN Urgent", "OON Urgent"));
        patterns.put("PRIMARY_CARE", Arrays.asList("Primary Care", "PCP", "Family Doctor", "INN PCP", "OON PCP"));
        patterns.put("SPECIALIST", Arrays.asList("Specialist", "Specialty Care", "INN Specialist", "OON Specialist"));
        patterns.put("PODIATRY", Arrays.asList("Podiatry", "Foot Care", "INN Podiatry", "OON Podiatry"));
        patterns.put("LAB_SERVICES", Arrays.asList("Laboratory", "Lab Services", "INN Lab", "OON Lab"));
        patterns.put("AMBULANCE", Arrays.asList("Ambulance", "Emergency Transport", "Medical Transport"));
        patterns.put("DME", Arrays.asList("DME", "Medical Equipment", "Durable Equipment"));
        patterns.put("PROSTHETICS", Arrays.asList("Prosthetics", "Artificial Limbs", "Prosthetic Devices"));
        patterns.put("PREVENTIVE", Arrays.asList("Preventive Care", "Wellness", "Annual Exam", "Screening"));
        patterns.put("DIALYSIS", Arrays.asList("Dialysis", "Kidney Treatment", "Renal Care"));
        return patterns;
    }
    
    // GHI specific patterns (may have different cost structures than HIP HMO)
    private static final Pattern COST_PATTERN = Pattern.compile("\\$([0-9,]+(?:\\.[0-9]{2})?)\\s*(copay|coinsurance|per day|per visit|deductible)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([0-9]+)%\\s*(coinsurance|coverage|of charges)?", Pattern.CASE_INSENSITIVE);
    
    // GHI specific condition patterns
    private static final Pattern PA_PATTERN = Pattern.compile("authorization required|prior approval|PA required|pre-auth", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEDUCTIBLE_PATTERN = Pattern.compile("deductible applies|after deductible|subject to deductible", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOOP_PATTERN = Pattern.compile("applies to out-of-pocket|counts toward MOOP|subject to MOOP", Pattern.CASE_INSENSITIVE);
    
    @Override
    public SOBType getSupportedSOBType() {
        return SOBType.GHI;
    }
    
    @Override
    public ColumnDetectionResult detectVBMColumns(VendorMatrixData vendorMatrixData) {
        log.info("Detecting VBM columns for GHI");
        
        Map<String, String> detectedColumns = new HashMap<>();
        Map<String, Double> confidenceScores = new HashMap<>();
        List<String> unmatched = new ArrayList<>();
        List<String> ambiguous = new ArrayList<>();
        
        Map<String, String> allColumns = vendorMatrixData.getAllColumns();
        
        for (Map.Entry<String, String> entry : allColumns.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();
            
            BenefitCategoryMatch bestMatch = findBestBenefitCategory(columnName, columnValue);
            
            if (bestMatch.confidence >= 0.7) {
                detectedColumns.put(bestMatch.category, columnName);
                confidenceScores.put(columnName, bestMatch.confidence);
                log.debug("GHI: Mapped column '{}' to category '{}' with confidence {:.2f}", 
                         columnName, bestMatch.category, bestMatch.confidence);
            } else if (bestMatch.confidence >= 0.4) {
                ambiguous.add(columnName);
            } else {
                unmatched.add(columnName);
            }
        }
        
        return new ColumnDetectionResult(detectedColumns, confidenceScores, unmatched, ambiguous);
    }
    
    @Override
    public BenefitMapping findBestColumnMatch(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData) {
        String benefitName = sobBenefit.getBenefitName();
        log.debug("GHI: Finding best column match for SOB benefit: '{}'", benefitName);
        
        String bestColumn = null;
        double bestScore = 0.0;
        List<String> matchingReasons = new ArrayList<>();
        
        Map<String, String> allColumns = vendorMatrixData.getAllColumns();
        
        for (String columnName : allColumns.keySet()) {
            double score = calculateGHIColumnMatchScore(benefitName, columnName, allColumns.get(columnName));
            
            if (score > bestScore) {
                bestScore = score;
                bestColumn = columnName;
                matchingReasons.clear();
                matchingReasons.addAll(getGHIMatchingReasons(benefitName, columnName, score));
            }
        }
        
        if (bestColumn != null && bestScore >= 0.4) {
            String vbmValue = allColumns.get(bestColumn);
            BenefitConditions conditions = extractConditions(vbmValue, sobBenefit);
            
            return new BenefitMapping(sobBenefit, bestColumn, vbmValue, bestScore, conditions, matchingReasons);
        }
        
        log.warn("GHI: No suitable column match found for SOB benefit: '{}'", benefitName);
        return null;
    }
    
    @Override
    public BenefitConditions extractConditions(String vbmValue, SOBBenefit sobBenefit) {
        if (vbmValue == null || vbmValue.trim().isEmpty()) {
            return new BenefitConditions(null, null, null, null, null, null, new HashMap<>());
        }
        
        String cleanValue = vbmValue.toLowerCase().trim();
        
        // Extract cost amount using GHI patterns
        String costAmount = extractGHICostAmount(cleanValue);
        
        // Extract conditions using GHI specific patterns
        Boolean priorAuthRequired = PA_PATTERN.matcher(cleanValue).find();
        Boolean subjectToDeductible = DEDUCTIBLE_PATTERN.matcher(cleanValue).find();
        Boolean moopApplicable = MOOP_PATTERN.matcher(cleanValue).find(); // GHI may use different MOOP logic
        
        String paNotes = extractGHIPANotes(cleanValue);
        String limitations = extractGHILimitations(cleanValue);
        Map<String, String> additionalFields = extractGHIAdditionalFields(cleanValue);
        
        return new BenefitConditions(costAmount, priorAuthRequired, subjectToDeductible, 
                                   moopApplicable, paNotes, limitations, additionalFields);
    }
    
    @Override
    public List<ValidationIssue> validateConditions(BenefitConditions vbmConditions, SOBBenefit sobBenefit) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // GHI specific validation rules
        validateGHIPriorAuth(vbmConditions, sobBenefit, issues);
        validateGHIDeductible(vbmConditions, sobBenefit, issues);
        validateGHIMOOP(vbmConditions, sobBenefit, issues);
        validateGHICostSharing(vbmConditions, sobBenefit, issues);
        
        return issues;
    }
    
    @Override
    public Map<String, List<String>> getBenefitNamePatterns() {
        return BENEFIT_NAME_PATTERNS;
    }
    
    @Override
    public Map<String, List<String>> getVBMColumnPatterns() {
        return VBM_COLUMN_PATTERNS;
    }
    
    // GHI specific helper methods
    private BenefitCategoryMatch findBestBenefitCategory(String columnName, String columnValue) {
        double bestScore = 0.0;
        String bestCategory = null;
        
        for (Map.Entry<String, List<String>> entry : VBM_COLUMN_PATTERNS.entrySet()) {
            String category = entry.getKey();
            List<String> patterns = entry.getValue();
            
            double score = calculateGHICategoryMatchScore(columnName, patterns);
            if (score > bestScore) {
                bestScore = score;
                bestCategory = category;
            }
        }
        
        return new BenefitCategoryMatch(bestCategory, bestScore);
    }
    
    private double calculateGHIColumnMatchScore(String benefitName, String columnName, String columnValue) {
        double score = 0.0;
        String lowerBenefitName = benefitName.toLowerCase();
        String lowerColumnName = columnName.toLowerCase();
        
        // GHI specific scoring logic
        if (lowerColumnName.contains(lowerBenefitName) || lowerBenefitName.contains(lowerColumnName)) {
            score += 0.9; // Higher weight for GHI exact matches
        }
        
        // GHI pattern-based matching
        for (Map.Entry<String, List<String>> entry : BENEFIT_NAME_PATTERNS.entrySet()) {
            List<String> patterns = entry.getValue();
            if (patterns.stream().anyMatch(pattern -> lowerBenefitName.contains(pattern))) {
                List<String> columnPatterns = VBM_COLUMN_PATTERNS.get(entry.getKey());
                if (columnPatterns != null && columnPatterns.stream().anyMatch(pattern -> 
                    lowerColumnName.contains(pattern.toLowerCase()))) {
                    score += 0.7;
                    break;
                }
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    private double calculateGHICategoryMatchScore(String columnName, List<String> patterns) {
        String lowerColumnName = columnName.toLowerCase();
        
        for (String pattern : patterns) {
            if (lowerColumnName.contains(pattern.toLowerCase())) {
                return 0.95; // Very high confidence for GHI pattern matches
            }
        }
        
        // Partial match scoring
        for (String pattern : patterns) {
            String[] patternWords = pattern.toLowerCase().split("\\s+");
            int matchingWords = 0;
            for (String word : patternWords) {
                if (lowerColumnName.contains(word)) {
                    matchingWords++;
                }
            }
            if (matchingWords > 0) {
                return (double) matchingWords / patternWords.length * 0.8;
            }
        }
        
        return 0.0;
    }
    
    private List<String> getGHIMatchingReasons(String benefitName, String columnName, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (score >= 0.9) {
            reasons.add("GHI: High confidence exact match");
        } else if (score >= 0.7) {
            reasons.add("GHI: Pattern-based category match");
        } else if (score >= 0.4) {
            reasons.add("GHI: Partial similarity match");
        } else {
            reasons.add("GHI: Low confidence match");
        }
        
        return reasons;
    }
    
    private String extractGHICostAmount(String value) {
        // GHI may have different cost formats
        Matcher matcher = COST_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        Matcher percentMatcher = PERCENTAGE_PATTERN.matcher(value);
        if (percentMatcher.find()) {
            return percentMatcher.group(0);
        }
        
        return null;
    }
    
    private String extractGHIPANotes(String value) {
        if (PA_PATTERN.matcher(value).find()) {
            String[] sentences = value.split("[.;]");
            for (String sentence : sentences) {
                if (PA_PATTERN.matcher(sentence).find()) {
                    return sentence.trim();
                }
            }
        }
        return null;
    }
    
    private String extractGHILimitations(String value) {
        Pattern limitPattern = Pattern.compile("(\\d+)\\s*(visit|day|limit|annual|maximum)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = limitPattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
    
    private Map<String, String> extractGHIAdditionalFields(String value) {
        Map<String, String> fields = new HashMap<>();
        
        // GHI specific field extraction
        if (value.contains("in-network") || value.contains("inn")) {
            fields.put("network", "In-Network");
        } else if (value.contains("out-of-network") || value.contains("oon")) {
            fields.put("network", "Out-of-Network");
        }
        
        // GHI may have specific coverage details
        if (value.contains("covered") && value.contains("100%")) {
            fields.put("coverage", "100% Covered");
        }
        
        return fields;
    }
    
    // GHI specific validation methods
    private void validateGHIPriorAuth(BenefitConditions vbmConditions, SOBBenefit sobBenefit, List<ValidationIssue> issues) {
        if (sobBenefit.getPaRequired() != null && vbmConditions.getPriorAuthRequired() != null) {
            if (!sobBenefit.getPaRequired().equals(vbmConditions.getPriorAuthRequired())) {
                issues.add(new ValidationIssue(
                    "GHI_PRIOR_AUTH_MISMATCH",
                    "GHI Prior Authorization requirement mismatch",
                    sobBenefit.getPaRequired().toString(),
                    vbmConditions.getPriorAuthRequired().toString(),
                    "HIGH"
                ));
            }
        }
    }
    
    private void validateGHIDeductible(BenefitConditions vbmConditions, SOBBenefit sobBenefit, List<ValidationIssue> issues) {
        if (sobBenefit.getDeductibleApplicable() != null && vbmConditions.getSubjectToDeductible() != null) {
            if (!sobBenefit.getDeductibleApplicable().equals(vbmConditions.getSubjectToDeductible())) {
                issues.add(new ValidationIssue(
                    "GHI_DEDUCTIBLE_MISMATCH",
                    "GHI Deductible applicability mismatch",
                    sobBenefit.getDeductibleApplicable().toString(),
                    vbmConditions.getSubjectToDeductible().toString(),
                    "HIGH"
                ));
            }
        }
    }
    
    private void validateGHIMOOP(BenefitConditions vbmConditions, SOBBenefit sobBenefit, List<ValidationIssue> issues) {
        if (sobBenefit.getMoopApplicable() != null && vbmConditions.getMoopApplicable() != null) {
            if (!sobBenefit.getMoopApplicable().equals(vbmConditions.getMoopApplicable())) {
                issues.add(new ValidationIssue(
                    "GHI_MOOP_MISMATCH",
                    "GHI MOOP applicability mismatch",
                    sobBenefit.getMoopApplicable().toString(),
                    vbmConditions.getMoopApplicable().toString(),
                    "MEDIUM"
                ));
            }
        }
    }
    
    private void validateGHICostSharing(BenefitConditions vbmConditions, SOBBenefit sobBenefit, List<ValidationIssue> issues) {
        if (sobBenefit.getCostSharing() != null && vbmConditions.getCostAmount() != null) {
            if (!compareGHICostValues(sobBenefit.getCostSharing(), vbmConditions.getCostAmount())) {
                issues.add(new ValidationIssue(
                    "GHI_COST_SHARING_MISMATCH",
                    "GHI Cost sharing values do not match",
                    sobBenefit.getCostSharing(),
                    vbmConditions.getCostAmount(),
                    "HIGH"
                ));
            }
        }
    }
    
    private boolean compareGHICostValues(String sobCost, String vbmCost) {
        // GHI specific cost comparison logic
        String sobAmount = extractNumericAmount(sobCost);
        String vbmAmount = extractNumericAmount(vbmCost);
        
        if (sobAmount != null && vbmAmount != null) {
            return sobAmount.equals(vbmAmount);
        }
        
        return sobCost.toLowerCase().trim().equals(vbmCost.toLowerCase().trim());
    }
    
    private String extractNumericAmount(String costString) {
        if (costString == null) return null;
        
        Pattern numericPattern = Pattern.compile("\\$?([0-9,]+(?:\\.[0-9]{2})?)");
        Matcher matcher = numericPattern.matcher(costString);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        }
        return null;
    }
    
    // Inner class
    private static class BenefitCategoryMatch {
        final String category;
        final double confidence;
        
        BenefitCategoryMatch(String category, double confidence) {
            this.category = category;
            this.confidence = confidence;
        }
    }
} 
