package com.sobvalidator.model;

import java.time.LocalDateTime;
import java.util.List;

public class ValidationResult {
    private String validationId;
    private SOBType sobType;
    private String sobFileName;
    private String vendorMatrixFileName;
    private LocalDateTime validatedAt;
    
    // Overall validation status
    private ValidationStatus status;
    private int totalErrors;
    private int totalWarnings;
    
    // Detailed errors by category
    private List<ValidationError> errors;
    
    // Summary statistics
    private ValidationSummary summary;
    
    // Comparison data for UI display
    private List<BenefitComparison> benefitComparisons;

    // Constructors
    public ValidationResult() {}

    public ValidationResult(String validationId, SOBType sobType, String sobFileName, 
                           String vendorMatrixFileName, LocalDateTime validatedAt, ValidationStatus status,
                           int totalErrors, int totalWarnings, List<ValidationError> errors,
                           ValidationSummary summary, List<BenefitComparison> benefitComparisons) {
        this.validationId = validationId;
        this.sobType = sobType;
        this.sobFileName = sobFileName;
        this.vendorMatrixFileName = vendorMatrixFileName;
        this.validatedAt = validatedAt;
        this.status = status;
        this.totalErrors = totalErrors;
        this.totalWarnings = totalWarnings;
        this.errors = errors;
        this.summary = summary;
        this.benefitComparisons = benefitComparisons;
    }

    // Builder pattern static method
    public static ValidationResultBuilder builder() {
        return new ValidationResultBuilder();
    }

    // Getters and Setters
    public String getValidationId() { return validationId; }
    public void setValidationId(String validationId) { this.validationId = validationId; }

    public SOBType getSobType() { return sobType; }
    public void setSobType(SOBType sobType) { this.sobType = sobType; }

    public String getSobFileName() { return sobFileName; }
    public void setSobFileName(String sobFileName) { this.sobFileName = sobFileName; }

    public String getVendorMatrixFileName() { return vendorMatrixFileName; }
    public void setVendorMatrixFileName(String vendorMatrixFileName) { this.vendorMatrixFileName = vendorMatrixFileName; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }

    public ValidationStatus getStatus() { return status; }
    public void setStatus(ValidationStatus status) { this.status = status; }

    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }

    public int getTotalWarnings() { return totalWarnings; }
    public void setTotalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; }

    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }

    public ValidationSummary getSummary() { return summary; }
    public void setSummary(ValidationSummary summary) { this.summary = summary; }

    public List<BenefitComparison> getBenefitComparisons() { return benefitComparisons; }
    public void setBenefitComparisons(List<BenefitComparison> benefitComparisons) { this.benefitComparisons = benefitComparisons; }

    // Builder class
    public static class ValidationResultBuilder {
        private String validationId;
        private SOBType sobType;
        private String sobFileName;
        private String vendorMatrixFileName;
        private LocalDateTime validatedAt;
        private ValidationStatus status;
        private int totalErrors;
        private int totalWarnings;
        private List<ValidationError> errors;
        private ValidationSummary summary;
        private List<BenefitComparison> benefitComparisons;

        public ValidationResultBuilder validationId(String validationId) { this.validationId = validationId; return this; }
        public ValidationResultBuilder sobType(SOBType sobType) { this.sobType = sobType; return this; }
        public ValidationResultBuilder sobFileName(String sobFileName) { this.sobFileName = sobFileName; return this; }
        public ValidationResultBuilder vendorMatrixFileName(String vendorMatrixFileName) { this.vendorMatrixFileName = vendorMatrixFileName; return this; }
        public ValidationResultBuilder validatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; return this; }
        public ValidationResultBuilder status(ValidationStatus status) { this.status = status; return this; }
        public ValidationResultBuilder totalErrors(int totalErrors) { this.totalErrors = totalErrors; return this; }
        public ValidationResultBuilder totalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; return this; }
        public ValidationResultBuilder errors(List<ValidationError> errors) { this.errors = errors; return this; }
        public ValidationResultBuilder summary(ValidationSummary summary) { this.summary = summary; return this; }
        public ValidationResultBuilder benefitComparisons(List<BenefitComparison> benefitComparisons) { this.benefitComparisons = benefitComparisons; return this; }

        public ValidationResult build() {
            return new ValidationResult(validationId, sobType, sobFileName, vendorMatrixFileName, validatedAt,
                    status, totalErrors, totalWarnings, errors, summary, benefitComparisons);
        }
    }
} 