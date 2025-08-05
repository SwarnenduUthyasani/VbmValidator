package com.sobvalidator.model;

public class ValidationError {
    private String errorId;
    private ErrorType errorType;
    private ErrorSeverity severity;
    private String benefitCategory;     // e.g., "1a - Inpatient Hospital"
    private String fieldName;           // e.g., "Cost Sharing", "PA Required"
    private String sobValue;             // Value from SOB
    private String vendorMatrixValue;    // Value from Vendor Matrix
    private String expectedValue;        // What the correct value should be
    private String description;          // Human-readable description of the error
    private String recommendation;       // How to fix this error
    private boolean selected;            // For UI selection

    // Constructors
    public ValidationError() {}

    public ValidationError(String errorId, ErrorType errorType, ErrorSeverity severity, 
                          String benefitCategory, String fieldName, String sobValue, 
                          String vendorMatrixValue, String expectedValue, String description, 
                          String recommendation, boolean selected) {
        this.errorId = errorId;
        this.errorType = errorType;
        this.severity = severity;
        this.benefitCategory = benefitCategory;
        this.fieldName = fieldName;
        this.sobValue = sobValue;
        this.vendorMatrixValue = vendorMatrixValue;
        this.expectedValue = expectedValue;
        this.description = description;
        this.recommendation = recommendation;
        this.selected = selected;
    }

    // Builder pattern
    public static ValidationErrorBuilder builder() {
        return new ValidationErrorBuilder();
    }

    // Getters and Setters
    public String getErrorId() { return errorId; }
    public void setErrorId(String errorId) { this.errorId = errorId; }

    public ErrorType getErrorType() { return errorType; }
    public void setErrorType(ErrorType errorType) { this.errorType = errorType; }

    public ErrorSeverity getSeverity() { return severity; }
    public void setSeverity(ErrorSeverity severity) { this.severity = severity; }

    public String getBenefitCategory() { return benefitCategory; }
    public void setBenefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getSobValue() { return sobValue; }
    public void setSobValue(String sobValue) { this.sobValue = sobValue; }

    public String getVendorMatrixValue() { return vendorMatrixValue; }
    public void setVendorMatrixValue(String vendorMatrixValue) { this.vendorMatrixValue = vendorMatrixValue; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Alias for expectedValue to match template usage
    public String getCorrectedValue() { return expectedValue; }

    // Builder class
    public static class ValidationErrorBuilder {
        private String errorId;
        private ErrorType errorType;
        private ErrorSeverity severity;
        private String benefitCategory;
        private String fieldName;
        private String sobValue;
        private String vendorMatrixValue;
        private String expectedValue;
        private String description;
        private String recommendation;
        private boolean selected;

        public ValidationErrorBuilder errorId(String errorId) { this.errorId = errorId; return this; }
        public ValidationErrorBuilder errorType(ErrorType errorType) { this.errorType = errorType; return this; }
        public ValidationErrorBuilder severity(ErrorSeverity severity) { this.severity = severity; return this; }
        public ValidationErrorBuilder benefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; return this; }
        public ValidationErrorBuilder fieldName(String fieldName) { this.fieldName = fieldName; return this; }
        public ValidationErrorBuilder sobValue(String sobValue) { this.sobValue = sobValue; return this; }
        public ValidationErrorBuilder vendorMatrixValue(String vendorMatrixValue) { this.vendorMatrixValue = vendorMatrixValue; return this; }
        public ValidationErrorBuilder expectedValue(String expectedValue) { this.expectedValue = expectedValue; return this; }
        public ValidationErrorBuilder description(String description) { this.description = description; return this; }
        public ValidationErrorBuilder recommendation(String recommendation) { this.recommendation = recommendation; return this; }
        public ValidationErrorBuilder selected(boolean selected) { this.selected = selected; return this; }

        public ValidationError build() {
            return new ValidationError(errorId, errorType, severity, benefitCategory, fieldName, 
                                     sobValue, vendorMatrixValue, expectedValue, description, recommendation, selected);
        }
    }
} 