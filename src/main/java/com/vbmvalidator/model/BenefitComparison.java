package com.vbmvalidator.model;

import java.util.List;

public class BenefitComparison {
    private String benefitCategory;
    private String benefitName;
    private SOBBenefit sobBenefit;
    private String vendorMatrixValue;
    private ComparisonStatus status;
    private List<ValidationError> errors;

    // Constructors
    public BenefitComparison() {}

    public BenefitComparison(String benefitCategory, String benefitName, SOBBenefit sobBenefit,
                           String vendorMatrixValue, ComparisonStatus status, List<ValidationError> errors) {
        this.benefitCategory = benefitCategory;
        this.benefitName = benefitName;
        this.sobBenefit = sobBenefit;
        this.vendorMatrixValue = vendorMatrixValue;
        this.status = status;
        this.errors = errors;
    }

    // Builder pattern
    public static BenefitComparisonBuilder builder() {
        return new BenefitComparisonBuilder();
    }

    // Getters and Setters
    public String getBenefitCategory() { return benefitCategory; }
    public void setBenefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; }

    public String getBenefitName() { return benefitName; }
    public void setBenefitName(String benefitName) { this.benefitName = benefitName; }

    public SOBBenefit getSobBenefit() { return sobBenefit; }
    public void setSobBenefit(SOBBenefit sobBenefit) { this.sobBenefit = sobBenefit; }

    public String getVendorMatrixValue() { return vendorMatrixValue; }
    public void setVendorMatrixValue(String vendorMatrixValue) { this.vendorMatrixValue = vendorMatrixValue; }

    public ComparisonStatus getStatus() { return status; }
    public void setStatus(ComparisonStatus status) { this.status = status; }

    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }

    // Builder class
    public static class BenefitComparisonBuilder {
        private String benefitCategory;
        private String benefitName;
        private SOBBenefit sobBenefit;
        private String vendorMatrixValue;
        private ComparisonStatus status;
        private List<ValidationError> errors;

        public BenefitComparisonBuilder benefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; return this; }
        public BenefitComparisonBuilder benefitName(String benefitName) { this.benefitName = benefitName; return this; }
        public BenefitComparisonBuilder sobBenefit(SOBBenefit sobBenefit) { this.sobBenefit = sobBenefit; return this; }
        public BenefitComparisonBuilder vendorMatrixValue(String vendorMatrixValue) { this.vendorMatrixValue = vendorMatrixValue; return this; }
        public BenefitComparisonBuilder status(ComparisonStatus status) { this.status = status; return this; }
        public BenefitComparisonBuilder errors(List<ValidationError> errors) { this.errors = errors; return this; }

        public BenefitComparison build() {
            return new BenefitComparison(benefitCategory, benefitName, sobBenefit, vendorMatrixValue, status, errors);
        }
    }
} 
