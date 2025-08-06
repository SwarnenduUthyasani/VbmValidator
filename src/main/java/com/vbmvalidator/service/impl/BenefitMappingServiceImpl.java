package com.vbmvalidator.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.BenefitMappingService;
import com.vbmvalidator.service.SOBTypeProcessor;

/**
 * Comprehensive benefit mapping service implementation
 * Uses SOB type-specific processors for high-accuracy benefit analysis
 */
@Service
public class BenefitMappingServiceImpl implements BenefitMappingService {
    
    private static final Logger log = LoggerFactory.getLogger(BenefitMappingServiceImpl.class);
    
    private final Map<SOBType, SOBTypeProcessor> processors = new HashMap<>();
    
    public BenefitMappingServiceImpl(List<SOBTypeProcessor> sobTypeProcessors) {
        // Register all SOB type processors
        for (SOBTypeProcessor processor : sobTypeProcessors) {
            processors.put(processor.getSupportedSOBType(), processor);
            log.info("Registered SOB type processor for: {}", processor.getSupportedSOBType());
        }
    }
    
    @Override
    public List<BenefitMapping> mapBenefits(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType) {
        log.info("Starting comprehensive benefit mapping for SOB type: {}", sobType);
        
        SOBTypeProcessor processor = processors.get(sobType);
        if (processor == null) {
            log.error("No processor found for SOB type: {}", sobType);
            return Collections.emptyList();
        }
        
        List<BenefitMapping> mappings = new ArrayList<>();
        
        // First, analyze VBM column structure
        SOBTypeProcessor.ColumnDetectionResult columnDetection = processor.detectVBMColumns(vendorMatrixData);
        log.info("Column detection completed - Detected: {}, Unmatched: {}, Ambiguous: {}", 
                columnDetection.getDetectedColumns().size(),
                columnDetection.getUnmatched().size(),
                columnDetection.getAmbiguous().size());
        
        // Map each SOB benefit to VBM columns
        for (SOBBenefit sobBenefit : sobData.getBenefits()) {
            BenefitMapping mapping = processor.findBestColumnMatch(sobBenefit, vendorMatrixData);
            
            if (mapping != null) {
                // Validate the mapping
                List<SOBTypeProcessor.ValidationIssue> issues = processor.validateConditions(
                    mapping.getConditions(), sobBenefit);
                
                if (issues.isEmpty() || mapping.getConfidence() >= 0.8) {
                    mappings.add(mapping);
                    log.debug("Successfully mapped benefit '{}' to column '{}' with confidence {:.2f}", 
                             sobBenefit.getBenefitName(), mapping.getVbmColumn(), mapping.getConfidence());
                } else {
                    log.warn("Mapping for benefit '{}' has validation issues: {}", 
                            sobBenefit.getBenefitName(), issues.size());
                    // Still add mapping but log the issues
                    mappings.add(mapping);
                }
            } else {
                log.warn("No suitable mapping found for SOB benefit: '{}'", sobBenefit.getBenefitName());
            }
        }
        
        log.info("Completed benefit mapping - Successfully mapped {}/{} benefits", 
                mappings.size(), sobData.getBenefits().size());
        
        return mappings;
    }
    
    @Override
    public BenefitConditions extractConditions(SOBBenefit sobBenefit, String vbmValue, SOBType sobType) {
        SOBTypeProcessor processor = processors.get(sobType);
        if (processor == null) {
            log.error("No processor found for SOB type: {}", sobType);
            return new BenefitConditions(null, null, null, null, null, null, new HashMap<>());
        }
        
        return processor.extractConditions(vbmValue, sobBenefit);
    }
    
    @Override
    public double calculateMappingConfidence(SOBBenefit sobBenefit, String vbmColumn, String vbmValue) {
        // This is a simplified confidence calculation
        // In practice, this would use the processor's detailed scoring
        
        String benefitName = sobBenefit.getBenefitName().toLowerCase();
        String columnName = vbmColumn.toLowerCase();
        
        double score = 0.0;
        
        // Exact substring match
        if (columnName.contains(benefitName) || benefitName.contains(columnName)) {
            score += 0.8;
        }
        
        // Common medical term matching
        Map<String, String[]> medicalTerms = Map.of(
            "inpatient", new String[]{"inpt", "admission", "hospital"},
            "emergency", new String[]{"er", "urgent"},
            "primary care", new String[]{"pcp", "physician"},
            "laboratory", new String[]{"lab", "test"},
            "skilled nursing", new String[]{"snf", "nursing"}
        );
        
        for (Map.Entry<String, String[]> entry : medicalTerms.entrySet()) {
            if (benefitName.contains(entry.getKey())) {
                for (String synonym : entry.getValue()) {
                    if (columnName.contains(synonym)) {
                        score += 0.4;
                        break;
                    }
                }
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Get mapping statistics for debugging and monitoring
     */
    public MappingStatistics getMappingStatistics(List<BenefitMapping> mappings) {
        if (mappings.isEmpty()) {
            return new MappingStatistics(0, 0, 0, 0, 0.0);
        }
        
        int totalMappings = mappings.size();
        int highConfidence = (int) mappings.stream().filter(m -> m.getConfidence() >= 0.8).count();
        int mediumConfidence = (int) mappings.stream().filter(m -> m.getConfidence() >= 0.6 && m.getConfidence() < 0.8).count();
        int lowConfidence = (int) mappings.stream().filter(m -> m.getConfidence() < 0.6).count();
        double averageConfidence = mappings.stream().mapToDouble(BenefitMapping::getConfidence).average().orElse(0.0);
        
        return new MappingStatistics(totalMappings, highConfidence, mediumConfidence, lowConfidence, averageConfidence);
    }
    
    /**
     * Statistics about mapping quality
     */
    public static class MappingStatistics {
        private final int totalMappings;
        private final int highConfidence;
        private final int mediumConfidence;
        private final int lowConfidence;
        private final double averageConfidence;
        
        public MappingStatistics(int totalMappings, int highConfidence, int mediumConfidence, 
                               int lowConfidence, double averageConfidence) {
            this.totalMappings = totalMappings;
            this.highConfidence = highConfidence;
            this.mediumConfidence = mediumConfidence;
            this.lowConfidence = lowConfidence;
            this.averageConfidence = averageConfidence;
        }
        
        // Getters
        public int getTotalMappings() { return totalMappings; }
        public int getHighConfidence() { return highConfidence; }
        public int getMediumConfidence() { return mediumConfidence; }
        public int getLowConfidence() { return lowConfidence; }
        public double getAverageConfidence() { return averageConfidence; }
        
        @Override
        public String toString() {
            return "MappingStats{total=%d, high=%d, medium=%d, low=%d, avg=%.2f}".formatted(
                    totalMappings, highConfidence, mediumConfidence, lowConfidence, averageConfidence);
        }
    }
} 
