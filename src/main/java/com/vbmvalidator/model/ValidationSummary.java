package com.vbmvalidator.model;

public class ValidationSummary {
    private int benefitsValidated;
    private int benefitsWithErrors;
    private int totalDiscrepancies;
    private int criticalErrors;
    private int highErrors;
    private int mediumErrors;
    private int lowErrors;
    private int costSharingErrors;
    private int priorAuthErrors;
    private int moopErrors;
    private int deductibleErrors;

    // Constructors
    public ValidationSummary() {}

    public ValidationSummary(int benefitsValidated, int benefitsWithErrors, int totalDiscrepancies,
                           int criticalErrors, int highErrors, int mediumErrors, int lowErrors,
                           int costSharingErrors, int priorAuthErrors, int moopErrors, int deductibleErrors) {
        this.benefitsValidated = benefitsValidated;
        this.benefitsWithErrors = benefitsWithErrors;
        this.totalDiscrepancies = totalDiscrepancies;
        this.criticalErrors = criticalErrors;
        this.highErrors = highErrors;
        this.mediumErrors = mediumErrors;
        this.lowErrors = lowErrors;
        this.costSharingErrors = costSharingErrors;
        this.priorAuthErrors = priorAuthErrors;
        this.moopErrors = moopErrors;
        this.deductibleErrors = deductibleErrors;
    }

    // Builder pattern
    public static ValidationSummaryBuilder builder() {
        return new ValidationSummaryBuilder();
    }

    // Getters and Setters
    public int getBenefitsValidated() { return benefitsValidated; }
    public void setBenefitsValidated(int benefitsValidated) { this.benefitsValidated = benefitsValidated; }

    public int getBenefitsWithErrors() { return benefitsWithErrors; }
    public void setBenefitsWithErrors(int benefitsWithErrors) { this.benefitsWithErrors = benefitsWithErrors; }

    public int getTotalDiscrepancies() { return totalDiscrepancies; }
    public void setTotalDiscrepancies(int totalDiscrepancies) { this.totalDiscrepancies = totalDiscrepancies; }

    public int getCriticalErrors() { return criticalErrors; }
    public void setCriticalErrors(int criticalErrors) { this.criticalErrors = criticalErrors; }

    public int getHighErrors() { return highErrors; }
    public void setHighErrors(int highErrors) { this.highErrors = highErrors; }

    public int getMediumErrors() { return mediumErrors; }
    public void setMediumErrors(int mediumErrors) { this.mediumErrors = mediumErrors; }

    public int getLowErrors() { return lowErrors; }
    public void setLowErrors(int lowErrors) { this.lowErrors = lowErrors; }

    public int getCostSharingErrors() { return costSharingErrors; }
    public void setCostSharingErrors(int costSharingErrors) { this.costSharingErrors = costSharingErrors; }

    public int getPriorAuthErrors() { return priorAuthErrors; }
    public void setPriorAuthErrors(int priorAuthErrors) { this.priorAuthErrors = priorAuthErrors; }

    public int getMoopErrors() { return moopErrors; }
    public void setMoopErrors(int moopErrors) { this.moopErrors = moopErrors; }

    public int getDeductibleErrors() { return deductibleErrors; }
    public void setDeductibleErrors(int deductibleErrors) { this.deductibleErrors = deductibleErrors; }

    // Builder class
    public static class ValidationSummaryBuilder {
        private int benefitsValidated;
        private int benefitsWithErrors;
        private int totalDiscrepancies;
        private int criticalErrors;
        private int highErrors;
        private int mediumErrors;
        private int lowErrors;
        private int costSharingErrors;
        private int priorAuthErrors;
        private int moopErrors;
        private int deductibleErrors;

        public ValidationSummaryBuilder benefitsValidated(int benefitsValidated) { this.benefitsValidated = benefitsValidated; return this; }
        public ValidationSummaryBuilder benefitsWithErrors(int benefitsWithErrors) { this.benefitsWithErrors = benefitsWithErrors; return this; }
        public ValidationSummaryBuilder totalDiscrepancies(int totalDiscrepancies) { this.totalDiscrepancies = totalDiscrepancies; return this; }
        public ValidationSummaryBuilder criticalErrors(int criticalErrors) { this.criticalErrors = criticalErrors; return this; }
        public ValidationSummaryBuilder highErrors(int highErrors) { this.highErrors = highErrors; return this; }
        public ValidationSummaryBuilder mediumErrors(int mediumErrors) { this.mediumErrors = mediumErrors; return this; }
        public ValidationSummaryBuilder lowErrors(int lowErrors) { this.lowErrors = lowErrors; return this; }
        public ValidationSummaryBuilder costSharingErrors(int costSharingErrors) { this.costSharingErrors = costSharingErrors; return this; }
        public ValidationSummaryBuilder priorAuthErrors(int priorAuthErrors) { this.priorAuthErrors = priorAuthErrors; return this; }
        public ValidationSummaryBuilder moopErrors(int moopErrors) { this.moopErrors = moopErrors; return this; }
        public ValidationSummaryBuilder deductibleErrors(int deductibleErrors) { this.deductibleErrors = deductibleErrors; return this; }

        public ValidationSummary build() {
            return new ValidationSummary(benefitsValidated, benefitsWithErrors, totalDiscrepancies,
                                       criticalErrors, highErrors, mediumErrors, lowErrors,
                                       costSharingErrors, priorAuthErrors, moopErrors, deductibleErrors);
        }
    }
} 
