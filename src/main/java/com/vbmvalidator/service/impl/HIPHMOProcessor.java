package com.vbmvalidator.service.impl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
 * HIP HMO specific processor with detailed benefit analysis
 * Handles HIP HMO column detection, benefit mapping, and condition validation
 */
@Service
public class HIPHMOProcessor implements SOBTypeProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(HIPHMOProcessor.class);
    
    // HIP HMO specific benefit name patterns
    private static final Map<String, List<String>> BENEFIT_NAME_PATTERNS = createBenefitNamePatterns();
    
    private static Map<String, List<String>> createBenefitNamePatterns() {
        Map<String, List<String>> patterns = new HashMap<>();
        patterns.put("INPATIENT_HOSPITAL", Arrays.asList("inpatient hospital", "inpt admission", "hospital admission", "acute inpatient"));
        patterns.put("SKILLED_NURSING", Arrays.asList("skilled nursing", "snf", "skilled nursing facility", "nursing facility"));
        patterns.put("EMERGENCY", Arrays.asList("emergency", "er", "emergency room", "emergency care", "emergency services"));
        patterns.put("URGENT_CARE", Arrays.asList("urgent care", "urgently needed care", "urgent care center"));
        patterns.put("PRIMARY_CARE", Arrays.asList("primary care", "pcp", "primary care physician", "primary doctor"));
        patterns.put("SPECIALIST", Arrays.asList("specialist", "specialist visit", "specialty care"));
        patterns.put("PODIATRY", Arrays.asList("podiatry", "podiatrist", "foot care"));
        patterns.put("LAB_SERVICES", Arrays.asList("lab services", "laboratory", "lab work", "diagnostic tests"));
        patterns.put("AMBULANCE", Arrays.asList("ambulance", "emergency transport", "medical transport"));
        patterns.put("DME", Arrays.asList("durable medical equipment", "dme", "medical equipment"));
        patterns.put("PROSTHETICS", Arrays.asList("prosthetics", "prosthetic devices", "artificial limbs"));
        patterns.put("PREVENTIVE", Arrays.asList("preventive", "annual physical", "wellness", "screening"));
        patterns.put("DIALYSIS", Arrays.asList("dialysis", "renal dialysis", "kidney dialysis"));
        patterns.put("OBSERVATION", Arrays.asList("observation", "observation services", "23-hour stay"));
        patterns.put("AMBULATORY_SURGERY", Arrays.asList("ambulatory surgery", "outpatient surgery", "same day surgery"));
        return patterns;
    }
    
    // HIP HMO specific VBM column patterns
    private static final Map<String, List<String>> VBM_COLUMN_PATTERNS = createVBMColumnPatterns();
    
    private static Map<String, List<String>> createVBMColumnPatterns() {
        Map<String, List<String>> patterns = new HashMap<>();
        patterns.put("INPATIENT_HOSPITAL", Arrays.asList("INN Inpt. Admission", "OON Inpt. Admission", "Inpt Admission", "Inpatient Hospital"));
        patterns.put("SKILLED_NURSING", Arrays.asList("INN Skilled Nursing Days", "OON Skilled Nursing Days", "Skilled Nursing", "SNF"));
        patterns.put("EMERGENCY", Arrays.asList("INN ER", "OON ER", "Emergency", "Emergency Room"));
        patterns.put("URGENT_CARE", Arrays.asList("INN Urgent Care", "OON Urgent Care", "Urgent Care Center"));
        patterns.put("PRIMARY_CARE", Arrays.asList("INN PCP", "OON PCP", "Primary Care", "PCP Visit"));
        patterns.put("SPECIALIST", Arrays.asList("INN Specialist", "OON Specialist", "Specialty Care"));
        patterns.put("PODIATRY", Arrays.asList("INN Podiatry", "OON Podiatry", "Podiatry Medicare Covered"));
        patterns.put("LAB_SERVICES", Arrays.asList("INN Lab Services Medicare Covered", "OON Lab Services", "Laboratory"));
        patterns.put("AMBULANCE", Arrays.asList("Ambulance", "Emergency Transport", "Medical Transport"));
        patterns.put("DME", Arrays.asList("DME", "Durable Medical Equipment", "Medical Equipment"));
        patterns.put("PROSTHETICS", Arrays.asList("Prosthetics", "Prosthetic Devices", "External Prosthetic"));
        patterns.put("PREVENTIVE", Arrays.asList("INN Annual Physical Exam", "Preventive Care", "Wellness"));
        patterns.put("DIALYSIS", Arrays.asList("INN Dialysis", "OON Dialysis", "Renal Dialysis"));
        patterns.put("OBSERVATION", Arrays.asList("INN Observation Services", "OON Observation", "Observation Room"));
        patterns.put("AMBULATORY_SURGERY", Arrays.asList("INN Ambulatory Surgery Services", "OON Ambulatory Surgery", "Outpatient Surgery"));
        return patterns;
    }
    
    // Cost extraction patterns
    private static final Pattern COST_PATTERN = Pattern.compile("\\$([0-9,]+(?:\\.[0-9]{2})?)\\s*(copay|coinsurance|per day|per admission|per visit)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([0-9]+)%\\s*(coinsurance|of cost)?", Pattern.CASE_INSENSITIVE);
    
    // Condition detection patterns
    private static final Pattern PA_PATTERN = Pattern.compile("prior auth|authorization|PA\\s+required|pre-authorization", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEDUCTIBLE_PATTERN = Pattern.compile("subject to deductible|deductible applies|after deductible", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOOP_PATTERN = Pattern.compile("does not apply to MOOP|not subject to MOOP|MOOP does not apply", Pattern.CASE_INSENSITIVE);
    
    @Override
    public SOBType getSupportedSOBType() {
        return SOBType.HIP_HMO;
    }
    
    @Override
    public ColumnDetectionResult detectVBMColumns(VendorMatrixData vendorMatrixData) {
        log.info("Detecting VBM columns for HIP HMO");
        
        Map<String, String> detectedColumns = new HashMap<>();
        Map<String, Double> confidenceScores = new HashMap<>();
        List<String> unmatched = new ArrayList<>();
        List<String> ambiguous = new ArrayList<>();
        
        Map<String, String> allColumns = vendorMatrixData.getAllColumns();
        
        // Analyze each VBM column to determine its benefit category
        for (Map.Entry<String, String> entry : allColumns.entrySet()) {
            String columnName = entry.getKey();
            String columnValue = entry.getValue();
            
            BenefitCategoryMatch bestMatch = findBestBenefitCategory(columnName, columnValue);
            
            if (bestMatch.confidence >= 0.7) {
                detectedColumns.put(bestMatch.category, columnName);
                confidenceScores.put(columnName, bestMatch.confidence);
                log.debug("Mapped column '{}' to category '{}' with confidence {:.2f}", 
                         columnName, bestMatch.category, bestMatch.confidence);
            } else if (bestMatch.confidence >= 0.4) {
                ambiguous.add(columnName);
                log.debug("Ambiguous mapping for column '{}' to category '{}' with confidence {:.2f}", 
                         columnName, bestMatch.category, bestMatch.confidence);
            } else {
                unmatched.add(columnName);
                log.debug("No good match found for column '{}'", columnName);
            }
        }
        
        return new ColumnDetectionResult(detectedColumns, confidenceScores, unmatched, ambiguous);
    }
    
    @Override
    public BenefitMapping findBestColumnMatch(SOBBenefit sobBenefit, VendorMatrixData vendorMatrixData) {
        String benefitName = sobBenefit.getBenefitName();
        log.debug("Finding best column match for SOB benefit: '{}'", benefitName);
        
        String bestColumn = null;
        double bestScore = 0.0;
        List<String> matchingReasons = new ArrayList<>();
        
        Map<String, String> allColumns = vendorMatrixData.getAllColumns();
        
        for (String columnName : allColumns.keySet()) {
            double score = calculateColumnMatchScore(benefitName, columnName, allColumns.get(columnName));
            
            if (score > bestScore) {
                bestScore = score;
                bestColumn = columnName;
                matchingReasons.clear();
                matchingReasons.addAll(getMatchingReasons(benefitName, columnName, score));
            }
        }
        
        if (bestColumn != null && bestScore >= 0.4) {
            String vbmValue = allColumns.get(bestColumn);
            BenefitConditions conditions = extractConditions(vbmValue, sobBenefit);
            
            return new BenefitMapping(sobBenefit, bestColumn, vbmValue, bestScore, conditions, matchingReasons);
        }
        
        log.warn("No suitable column match found for SOB benefit: '{}'", benefitName);
        return null;
    }
    
    @Override
    public BenefitConditions extractConditions(String vbmValue, SOBBenefit sobBenefit) {
        if (vbmValue == null || vbmValue.trim().isEmpty()) {
            return new BenefitConditions(null, null, null, null, null, null, new HashMap<>());
        }
        
        String cleanValue = vbmValue.toLowerCase().trim();
        
        // Extract cost amount
        String costAmount = extractCostAmount(cleanValue);
        
        // Extract conditions
        Boolean priorAuthRequired = PA_PATTERN.matcher(cleanValue).find();
        Boolean subjectToDeductible = DEDUCTIBLE_PATTERN.matcher(cleanValue).find();
        Boolean moopApplicable = !MOOP_PATTERN.matcher(cleanValue).find(); // Default true unless explicitly stated
        
        // Extract PA notes
        String paNotes = extractPANotes(cleanValue);
        
        // Extract limitations
        String limitations = extractLimitations(cleanValue);
        
        // Additional fields
        Map<String, String> additionalFields = extractAdditionalFields(cleanValue);
        
        return new BenefitConditions(costAmount, priorAuthRequired, subjectToDeductible, 
                                   moopApplicable, paNotes, limitations, additionalFields);
    }
    
    @Override
    public List<ValidationIssue> validateConditions(BenefitConditions vbmConditions, SOBBenefit sobBenefit) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Validate Prior Authorization
        if (sobBenefit.getPaRequired() != null && vbmConditions.getPriorAuthRequired() != null) {
            if (!sobBenefit.getPaRequired().equals(vbmConditions.getPriorAuthRequired())) {
                issues.add(new ValidationIssue(
                    "PRIOR_AUTH_MISMATCH",
                    "Prior Authorization requirement mismatch",
                    sobBenefit.getPaRequired().toString(),
                    vbmConditions.getPriorAuthRequired().toString(),
                    "HIGH"
                ));
            }
        }
        
        // Validate Deductible
        if (sobBenefit.getDeductibleApplicable() != null && vbmConditions.getSubjectToDeductible() != null) {
            if (!sobBenefit.getDeductibleApplicable().equals(vbmConditions.getSubjectToDeductible())) {
                issues.add(new ValidationIssue(
                    "DEDUCTIBLE_MISMATCH",
                    "Deductible applicability mismatch",
                    sobBenefit.getDeductibleApplicable().toString(),
                    vbmConditions.getSubjectToDeductible().toString(),
                    "HIGH"
                ));
            }
        }
        
        // Validate MOOP
        if (sobBenefit.getMoopApplicable() != null && vbmConditions.getMoopApplicable() != null) {
            if (!sobBenefit.getMoopApplicable().equals(vbmConditions.getMoopApplicable())) {
                issues.add(new ValidationIssue(
                    "MOOP_MISMATCH",
                    "MOOP applicability mismatch",
                    sobBenefit.getMoopApplicable().toString(),
                    vbmConditions.getMoopApplicable().toString(),
                    "MEDIUM"
                ));
            }
        }
        
        // Validate Cost Sharing
        if (sobBenefit.getCostSharing() != null && vbmConditions.getCostAmount() != null) {
            if (!compareCostValues(sobBenefit.getCostSharing(), vbmConditions.getCostAmount())) {
                issues.add(new ValidationIssue(
                    "COST_SHARING_MISMATCH",
                    "Cost sharing values do not match",
                    sobBenefit.getCostSharing(),
                    vbmConditions.getCostAmount(),
                    "HIGH"
                ));
            }
        }
        
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
    
    // Helper methods
    private BenefitCategoryMatch findBestBenefitCategory(String columnName, String columnValue) {
        double bestScore = 0.0;
        String bestCategory = null;
        
        for (Map.Entry<String, List<String>> entry : VBM_COLUMN_PATTERNS.entrySet()) {
            String category = entry.getKey();
            List<String> patterns = entry.getValue();
            
            double score = calculateCategoryMatchScore(columnName, patterns);
            if (score > bestScore) {
                bestScore = score;
                bestCategory = category;
            }
        }
        
        return new BenefitCategoryMatch(bestCategory, bestScore);
    }
    
    private double calculateColumnMatchScore(String benefitName, String columnName, String columnValue) {
        double score = 0.0;
        String lowerBenefitName = benefitName.toLowerCase();
        String lowerColumnName = columnName.toLowerCase();
        
        // Exact substring match
        if (lowerColumnName.contains(lowerBenefitName) || lowerBenefitName.contains(lowerColumnName)) {
            score += 0.8;
        }
        
        // Pattern-based matching
        for (Map.Entry<String, List<String>> entry : BENEFIT_NAME_PATTERNS.entrySet()) {
            List<String> patterns = entry.getValue();
            if (patterns.stream().anyMatch(pattern -> lowerBenefitName.contains(pattern))) {
                List<String> columnPatterns = VBM_COLUMN_PATTERNS.get(entry.getKey());
                if (columnPatterns != null && columnPatterns.stream().anyMatch(pattern -> 
                    lowerColumnName.contains(pattern.toLowerCase()))) {
                    score += 0.6;
                    break;
                }
            }
        }
        
        // Medical terminology scoring
        score += calculateMedicalTermScore(lowerBenefitName, lowerColumnName);
        
        return Math.min(score, 1.0);
    }
    
    private double calculateCategoryMatchScore(String columnName, List<String> patterns) {
        String lowerColumnName = columnName.toLowerCase();
        
        for (String pattern : patterns) {
            if (lowerColumnName.contains(pattern.toLowerCase())) {
                return 0.9; // High confidence for direct pattern match
            }
        }
        
        // Check for partial matches
        for (String pattern : patterns) {
            String[] patternWords = pattern.toLowerCase().split("\\s+");
            int matchingWords = 0;
            for (String word : patternWords) {
                if (lowerColumnName.contains(word)) {
                    matchingWords++;
                }
            }
            if (matchingWords > 0) {
                return (double) matchingWords / patternWords.length * 0.7;
            }
        }
        
        return 0.0;
    }
    
    private double calculateMedicalTermScore(String benefitName, String columnName) {
        Map<String, String[]> medicalTerms = Map.of(
            "inpatient", new String[]{"inpt", "admission", "hospital"},
            "emergency", new String[]{"er", "urgent"},
            "physician", new String[]{"pcp", "doctor", "provider"},
            "laboratory", new String[]{"lab", "blood", "test"},
            "skilled nursing", new String[]{"snf", "nursing"},
            "durable medical", new String[]{"dme", "equipment"}
        );
        
        double score = 0.0;
        for (Map.Entry<String, String[]> entry : medicalTerms.entrySet()) {
            if (benefitName.contains(entry.getKey())) {
                for (String synonym : entry.getValue()) {
                    if (columnName.contains(synonym)) {
                        score += 0.3;
                        break;
                    }
                }
            }
        }
        
        return score;
    }
    
    private List<String> getMatchingReasons(String benefitName, String columnName, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (score >= 0.8) {
            reasons.add("High confidence substring match");
        } else if (score >= 0.6) {
            reasons.add("Pattern-based category match");
        } else if (score >= 0.4) {
            reasons.add("Medical terminology similarity");
        } else {
            reasons.add("Low confidence match");
        }
        
        return reasons;
    }
    
    private String extractCostAmount(String value) {
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
    
    private String extractPANotes(String value) {
        if (PA_PATTERN.matcher(value).find()) {
            // Extract surrounding context
            String[] sentences = value.split("[.;]");
            for (String sentence : sentences) {
                if (PA_PATTERN.matcher(sentence).find()) {
                    return sentence.trim();
                }
            }
        }
        return null;
    }
    
    private String extractLimitations(String value) {
        // Look for common limitation patterns
        Pattern limitPattern = Pattern.compile("(\\d+)\\s*(visit|day|limit|maximum)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = limitPattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
    
    private Map<String, String> extractAdditionalFields(String value) {
        Map<String, String> fields = new HashMap<>();
        
        // Extract network information
        if (value.contains("inn") || value.contains("in-network")) {
            fields.put("network", "INN");
        } else if (value.contains("oon") || value.contains("out-of-network")) {
            fields.put("network", "OON");
        }
        
        return fields;
    }
    
    private boolean compareCostValues(String sobCost, String vbmCost) {
        // Extract numeric values and compare
        String sobAmount = extractNumericAmount(sobCost);
        String vbmAmount = extractNumericAmount(vbmCost);
        
        if (sobAmount != null && vbmAmount != null) {
            return sobAmount.equals(vbmAmount);
        }
        
        // Fallback to string comparison
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
    
    // Inner classes
    private static class BenefitCategoryMatch {
        final String category;
        final double confidence;
        
        BenefitCategoryMatch(String category, double confidence) {
            this.category = category;
            this.confidence = confidence;
        }
    }
} 
