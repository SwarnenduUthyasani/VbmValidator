package com.vbmvalidator.model;

import java.util.List;
import java.util.Map;

public class SOBData {
    // Plan Metadata
    private String planName;
    private String effectiveDate;
    private String benefitSet;
    private String productId;
    private String contractNumber;
    private String serviceArea;
    private String formulary;
    private String providerNetwork;
    private String moop;
    private String deductible;
    private String monthlyPremium;
    
    // SOB Type
    private SOBType sobType;
    
    // Benefit Details
    private List<SOBBenefit> benefits;
    
    // Raw data for reference
    private Map<String, String> rawData;
    
    // Source file information
    private String sourceFileName;
    private String uploadedAt;

    // Constructors
    public SOBData() {}

    public SOBData(String planName, String effectiveDate, String benefitSet, String productId, 
                   String contractNumber, String serviceArea, String formulary, String providerNetwork,
                   String moop, String deductible, String monthlyPremium, SOBType sobType,
                   List<SOBBenefit> benefits, Map<String, String> rawData, String sourceFileName, String uploadedAt) {
        this.planName = planName;
        this.effectiveDate = effectiveDate;
        this.benefitSet = benefitSet;
        this.productId = productId;
        this.contractNumber = contractNumber;
        this.serviceArea = serviceArea;
        this.formulary = formulary;
        this.providerNetwork = providerNetwork;
        this.moop = moop;
        this.deductible = deductible;
        this.monthlyPremium = monthlyPremium;
        this.sobType = sobType;
        this.benefits = benefits;
        this.rawData = rawData;
        this.sourceFileName = sourceFileName;
        this.uploadedAt = uploadedAt;
    }

    // Builder pattern static method
    public static SOBDataBuilder builder() {
        return new SOBDataBuilder();
    }

    // Getters and Setters
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getBenefitSet() { return benefitSet; }
    public void setBenefitSet(String benefitSet) { this.benefitSet = benefitSet; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }

    public String getFormulary() { return formulary; }
    public void setFormulary(String formulary) { this.formulary = formulary; }

    public String getProviderNetwork() { return providerNetwork; }
    public void setProviderNetwork(String providerNetwork) { this.providerNetwork = providerNetwork; }

    public String getMoop() { return moop; }
    public void setMoop(String moop) { this.moop = moop; }

    public String getDeductible() { return deductible; }
    public void setDeductible(String deductible) { this.deductible = deductible; }

    public String getMonthlyPremium() { return monthlyPremium; }
    public void setMonthlyPremium(String monthlyPremium) { this.monthlyPremium = monthlyPremium; }

    public SOBType getSobType() { return sobType; }
    public void setSobType(SOBType sobType) { this.sobType = sobType; }

    public List<SOBBenefit> getBenefits() { return benefits; }
    public void setBenefits(List<SOBBenefit> benefits) { this.benefits = benefits; }

    public Map<String, String> getRawData() { return rawData; }
    public void setRawData(Map<String, String> rawData) { this.rawData = rawData; }

    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }

    /**
     * Finds a specific benefit by its category name.
     *
     * @param benefitCategory The category name to search for.
     * @return The SOBBenefit if found, otherwise null.
     */
    public SOBBenefit getBenefit(String benefitCategory) {
        if (this.benefits == null || benefitCategory == null) {
            return null;
        }
        return this.benefits.stream()
            .filter(b -> benefitCategory.equals(b.getBenefitCategory()))
            .findFirst()
            .orElse(null);
    }

    // Builder class
    public static class SOBDataBuilder {
        private String planName;
        private String effectiveDate;
        private String benefitSet;
        private String productId;
        private String contractNumber;
        private String serviceArea;
        private String formulary;
        private String providerNetwork;
        private String moop;
        private String deductible;
        private String monthlyPremium;
        private SOBType sobType;
        private List<SOBBenefit> benefits;
        private Map<String, String> rawData;
        private String sourceFileName;
        private String uploadedAt;

        public SOBDataBuilder planName(String planName) { this.planName = planName; return this; }
        public SOBDataBuilder effectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public SOBDataBuilder benefitSet(String benefitSet) { this.benefitSet = benefitSet; return this; }
        public SOBDataBuilder productId(String productId) { this.productId = productId; return this; }
        public SOBDataBuilder contractNumber(String contractNumber) { this.contractNumber = contractNumber; return this; }
        public SOBDataBuilder serviceArea(String serviceArea) { this.serviceArea = serviceArea; return this; }
        public SOBDataBuilder formulary(String formulary) { this.formulary = formulary; return this; }
        public SOBDataBuilder providerNetwork(String providerNetwork) { this.providerNetwork = providerNetwork; return this; }
        public SOBDataBuilder moop(String moop) { this.moop = moop; return this; }
        public SOBDataBuilder deductible(String deductible) { this.deductible = deductible; return this; }
        public SOBDataBuilder monthlyPremium(String monthlyPremium) { this.monthlyPremium = monthlyPremium; return this; }
        public SOBDataBuilder sobType(SOBType sobType) { this.sobType = sobType; return this; }
        public SOBDataBuilder benefits(List<SOBBenefit> benefits) { this.benefits = benefits; return this; }
        public SOBDataBuilder rawData(Map<String, String> rawData) { this.rawData = rawData; return this; }
        public SOBDataBuilder sourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; return this; }
        public SOBDataBuilder uploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public SOBData build() {
            return new SOBData(planName, effectiveDate, benefitSet, productId, contractNumber, serviceArea,
                    formulary, providerNetwork, moop, deductible, monthlyPremium, sobType, benefits,
                    rawData, sourceFileName, uploadedAt);
        }
    }
} 
