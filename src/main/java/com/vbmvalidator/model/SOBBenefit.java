package com.vbmvalidator.model;

public class SOBBenefit {
    private String pbpCategory;     // e.g., "1a", "1b", "2", "3"
    private String benefitName;     // e.g., "Inpatient Hospital"
    private String benefitCategory; // Benefit category for validation mapping
    private String costSharing;     // In-network cost sharing details
    private String oocCostSharing;  // Out-of-network cost sharing
    private String notations;       // Additional notes and details
    private Boolean supplementalBenefit;
    private Boolean paRequired;     // Prior Authorization required
    private String paNotes;         // Prior Authorization notes/details
    private Boolean referralRequired;
    private Boolean moopApplicable;
    private Boolean deductibleApplicable;
    
    // Raw extracted text for reference
    private String rawText;

    // Constructors
    public SOBBenefit() {}

    public SOBBenefit(String pbpCategory, String benefitName, String benefitCategory, 
                     String costSharing, String oocCostSharing, String notations, 
                     Boolean supplementalBenefit, Boolean paRequired, String paNotes, 
                     Boolean referralRequired, Boolean moopApplicable, Boolean deductibleApplicable, 
                     String rawText) {
        this.pbpCategory = pbpCategory;
        this.benefitName = benefitName;
        this.benefitCategory = benefitCategory;
        this.costSharing = costSharing;
        this.oocCostSharing = oocCostSharing;
        this.notations = notations;
        this.supplementalBenefit = supplementalBenefit;
        this.paRequired = paRequired;
        this.paNotes = paNotes;
        this.referralRequired = referralRequired;
        this.moopApplicable = moopApplicable;
        this.deductibleApplicable = deductibleApplicable;
        this.rawText = rawText;
    }

    // Builder pattern
    public static SOBBenefitBuilder builder() {
        return new SOBBenefitBuilder();
    }

    // Getters and Setters
    public String getPbpCategory() { return pbpCategory; }
    public void setPbpCategory(String pbpCategory) { this.pbpCategory = pbpCategory; }

    public String getBenefitName() { return benefitName; }
    public void setBenefitName(String benefitName) { this.benefitName = benefitName; }

    public String getBenefitCategory() { return benefitCategory; }
    public void setBenefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; }

    public String getCostSharing() { return costSharing; }
    public void setCostSharing(String costSharing) { this.costSharing = costSharing; }

    public String getOocCostSharing() { return oocCostSharing; }
    public void setOocCostSharing(String oocCostSharing) { this.oocCostSharing = oocCostSharing; }

    public String getNotations() { return notations; }
    public void setNotations(String notations) { this.notations = notations; }

    public Boolean getSupplementalBenefit() { return supplementalBenefit; }
    public void setSupplementalBenefit(Boolean supplementalBenefit) { this.supplementalBenefit = supplementalBenefit; }

    public Boolean getPaRequired() { return paRequired; }
    public void setPaRequired(Boolean paRequired) { this.paRequired = paRequired; }

    public String getPaNotes() { return paNotes; }
    public void setPaNotes(String paNotes) { this.paNotes = paNotes; }

    public Boolean getReferralRequired() { return referralRequired; }
    public void setReferralRequired(Boolean referralRequired) { this.referralRequired = referralRequired; }

    public Boolean getMoopApplicable() { return moopApplicable; }
    public void setMoopApplicable(Boolean moopApplicable) { this.moopApplicable = moopApplicable; }

    public Boolean getDeductibleApplicable() { return deductibleApplicable; }
    public void setDeductibleApplicable(Boolean deductibleApplicable) { this.deductibleApplicable = deductibleApplicable; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    // Builder class
    public static class SOBBenefitBuilder {
        private String pbpCategory;
        private String benefitName;
        private String benefitCategory;
        private String costSharing;
        private String oocCostSharing;
        private String notations;
        private Boolean supplementalBenefit;
        private Boolean paRequired;
        private String paNotes;
        private Boolean referralRequired;
        private Boolean moopApplicable;
        private Boolean deductibleApplicable;
        private String rawText;

        public SOBBenefitBuilder pbpCategory(String pbpCategory) { this.pbpCategory = pbpCategory; return this; }
        public SOBBenefitBuilder benefitName(String benefitName) { this.benefitName = benefitName; return this; }
        public SOBBenefitBuilder benefitCategory(String benefitCategory) { this.benefitCategory = benefitCategory; return this; }
        public SOBBenefitBuilder costSharing(String costSharing) { this.costSharing = costSharing; return this; }
        public SOBBenefitBuilder oocCostSharing(String oocCostSharing) { this.oocCostSharing = oocCostSharing; return this; }
        public SOBBenefitBuilder notations(String notations) { this.notations = notations; return this; }
        public SOBBenefitBuilder supplementalBenefit(Boolean supplementalBenefit) { this.supplementalBenefit = supplementalBenefit; return this; }
        public SOBBenefitBuilder paRequired(Boolean paRequired) { this.paRequired = paRequired; return this; }
        public SOBBenefitBuilder paNotes(String paNotes) { this.paNotes = paNotes; return this; }
        public SOBBenefitBuilder referralRequired(Boolean referralRequired) { this.referralRequired = referralRequired; return this; }
        public SOBBenefitBuilder moopApplicable(Boolean moopApplicable) { this.moopApplicable = moopApplicable; return this; }
        public SOBBenefitBuilder deductibleApplicable(Boolean deductibleApplicable) { this.deductibleApplicable = deductibleApplicable; return this; }
        public SOBBenefitBuilder rawText(String rawText) { this.rawText = rawText; return this; }

        public SOBBenefit build() {
            return new SOBBenefit(pbpCategory, benefitName, benefitCategory, costSharing, 
                                oocCostSharing, notations, supplementalBenefit, paRequired, 
                                paNotes, referralRequired, moopApplicable, deductibleApplicable, rawText);
        }
    }
} 
