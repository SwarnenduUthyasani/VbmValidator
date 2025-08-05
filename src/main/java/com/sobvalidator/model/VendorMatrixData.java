package com.sobvalidator.model;

import java.util.Map;

public class VendorMatrixData {
    // Plan Identification
    private String productName;
    private String productId;
    private String basePlan;
    private String benefitSet;
    private String contractId;
    private String pbpNumber;
    private String segmentId;
    private String planYear;
    private String effectiveDate;
    private String company;
    private String licensure;
    
    // Benefit Cost Sharing Fields (mapped from column headers)
    private String inpatientAdmission;           // INN Inpt. Admission/ OON Inpt. Admission
    private String skilledNursingDays;           // INN Skilled Nursing Days/OON Skilled Nursing Days
    private String cardiacTherapy;               // INN Cardiac Outpt Therapy/OON Cardiac Outpt Therapy
    private String intensiveCardiacTherapy;      // INN Intensive Cardiac Outpt Therapy/OON Intensiv Cardiac Outpt Therapy
    private String superviseExerciseTherapy;     // Supervised Exercise Therapy (SET) for Symptomatic Peripheral Artery Disease (PAD)
    private String pulmonaryRehab;               // INN Pulmonary Rehab/OON Pulmonary RT
    private String emergencyServices;            // INN ER/OON ER
    private String urgentCare;                   // INN Urgent Care Center in Facility/OON Urgent Care Center in Facility
    private String homeHealthVisits;             // INN Home Care Visits/OON Home Care Visits
    private String pcpVisits;                    // INN PCP/OON PCP
    private String chiropracticServices;         // INN Chiropractic/OON Chiropractic
    private String occupationalTherapy;          // Rehabilitation/Habilitation Services INN Outpt. OT/OON Outpt. OT
    private String specialistVisits;             // INN Specialist/OON Specialist
    private String podiatryMedicare;             // INN Podiatry Medicare Covered/ OONPodiatry Medicare Covered
    private String podiatrySupplemental;         // INN Podiatry Supplemental/OONPodiatry  Supplemental
    private String physicalTherapy;              // Rehabilitation/Habilitation Services INN Outpt. PT/OON Outpt. PT
    private String speechTherapy;                // Rehabilitation/Habilitation Services INN Outpt. ST/OON Outpt. ST
    private String telehealth;                   // INN Telehealth/ OON Telehealth
    private String diagnosticTesting;            // INN Diagnostic Testing/OON Diagnostic Testing
    private String labServices;                  // INN Lab Services Medicare Covered/ONNLab Services Medicare Covered
    private String therapeuticRadiology;         // INN Therapeutic Radiology Services/OON Therapeutic Radiology Services
    private String diagnosticRadiology;          // INN Outpatient Diagnostic Radiology Medicare Covered/OON Outpatient Diagnostic Radiology Medicare Covered
    private String advancedImaging;              // INN Advanced Imaging Services/OON Advanced Imaging Services
    private String ambulanceEmergent;            // INN Ambulance Emergent/OON Ambulance Emergent
    private String ambulanceNonEmergent;         // INN Ambulance Non-Emergent/OON Ambulance Non-Emergent
    private String ambulanceAir;                 // INN Ambulance Air/OON Ambulance Air
    private String acupuncture;                  // INN Acupuncture/OON Acupuncture
    private String mentalHealthMedicare;         // INN Mental Health Medicare Covered/OON Mental Health Medicare Covered
    private String mentalHealthSupplemental;     // INN Mental Health Supplemental/OON Mental Health Supplemental
    private String substanceAbuseMedicare;       // INN Substance Abuse Medicare Covered/OON Substance Abuse Medicare Covered
    private String substanceAbuseSupplemental;   // INN Substance Abuse Supplemental/OON Substance Abuse Supplemental
    private String dialysisTreatment;            // INN Dialysis Treatment/OON Dialysis Treatment
    private String DMEProsthetics;               // INN DME/Prosthetics/OON DME/Prosthetics
    private String diabeticSupplies;             // INN Diabetic Testing Supplies/OON Diabetic Testing Supplies
    private String partBRx;                      // INN Part B Rx/OON Part B Rx
    private String chemotherapy;                 // INN Chemotherapy/OON Chemotherapy
    private String renalDialysis;                // INN Renal Dialysis/OON Renal Dialysis
    private String opioidTreatment;              // INN Opioid Treatment Program Services/OON Opioid Treatment Program Services
    private String partialHospitalization;      // INN Partial Hospitalization/OON Partial Hospitalization
    private String transportation;               // Transportation
    private String meals;                        // Meals
    private String utilities;                    // Utilities
    private String pestControl;                  // Pest Control
    private String compressionStockings;         // Compression Stockings
    private String firstAidKit;                  // First Aid Kit
    private String foodCard;                     // Food Card
    private String overTheCounterItems;          // Over-the-Counter Items
    private String personalEmergencyDevice;      // Personal Emergency Response Device
    private String smokingCessation;             // Smoking Cessation
    private String fittingContacts;              // Fitting/Contacts
    private String dentalProphylaxis;            // Dental Prophylaxis
    private String periodonticMaintenance;       // Periodontic Maintenance
    private String emergencyDental;              // Emergency Dental
    private String comprehensiveDental;          // Comprehensive Dental
    private String prosthodonticBasic;           // Prosthodontic Basic
    private String orthodonticServices;          // Orthodontic Services
    private String hearingExam;                  // Hearing Exam
    private String fittingEvaluation;            // Fitting/Evaluation for Hearing Aids
    private String hearingAids;                  // Hearing Aids
    private String routineEyeExam;               // Routine Eye Exam
    private String glaucoma;                     // Glaucoma
    private String diabeticRetinopathy;          // Diabetic Retinopathy
    private String allowancePrescription;        // Allowance- Prescription
    private String allowanceNonPrescription;     // Allowance- Non-Prescription
    private String visionHardware;               // Vision Hardware
    private String visionSoftware;               // Vision Software
    private String contactLenses;                // Contact Lenses
    private String lowVisionAids;                // Low Vision Aids
    private String fitnessProgram;               // Fitness Program
    private String nurseHotline;                 // Nurse Hotline
    
    // Metadata fields
    private Map<String, String> allColumns;
    private Map<String, String> benefitData;
    
    // Source file information
    private String sourceFileName;
    private String uploadedAt;

    // Constructors
    public VendorMatrixData() {}

    // Builder pattern
    public static VendorMatrixDataBuilder builder() {
        return new VendorMatrixDataBuilder();
    }

    // Getters and Setters for all fields
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getBasePlan() { return basePlan; }
    public void setBasePlan(String basePlan) { this.basePlan = basePlan; }

    public String getBenefitSet() { return benefitSet; }
    public void setBenefitSet(String benefitSet) { this.benefitSet = benefitSet; }

    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getPbpNumber() { return pbpNumber; }
    public void setPbpNumber(String pbpNumber) { this.pbpNumber = pbpNumber; }

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public String getPlanYear() { return planYear; }
    public void setPlanYear(String planYear) { this.planYear = planYear; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLicensure() { return licensure; }
    public void setLicensure(String licensure) { this.licensure = licensure; }

    // Benefit fields getters and setters
    public String getInpatientAdmission() { return inpatientAdmission; }
    public void setInpatientAdmission(String inpatientAdmission) { this.inpatientAdmission = inpatientAdmission; }

    public String getSkilledNursingDays() { return skilledNursingDays; }
    public void setSkilledNursingDays(String skilledNursingDays) { this.skilledNursingDays = skilledNursingDays; }

    public String getCardiacTherapy() { return cardiacTherapy; }
    public void setCardiacTherapy(String cardiacTherapy) { this.cardiacTherapy = cardiacTherapy; }

    public String getIntensiveCardiacTherapy() { return intensiveCardiacTherapy; }
    public void setIntensiveCardiacTherapy(String intensiveCardiacTherapy) { this.intensiveCardiacTherapy = intensiveCardiacTherapy; }

    public String getSuperviseExerciseTherapy() { return superviseExerciseTherapy; }
    public void setSuperviseExerciseTherapy(String superviseExerciseTherapy) { this.superviseExerciseTherapy = superviseExerciseTherapy; }

    public String getPulmonaryRehab() { return pulmonaryRehab; }
    public void setPulmonaryRehab(String pulmonaryRehab) { this.pulmonaryRehab = pulmonaryRehab; }

    public String getEmergencyServices() { return emergencyServices; }
    public void setEmergencyServices(String emergencyServices) { this.emergencyServices = emergencyServices; }

    public String getUrgentCare() { return urgentCare; }
    public void setUrgentCare(String urgentCare) { this.urgentCare = urgentCare; }

    public String getHomeHealthVisits() { return homeHealthVisits; }
    public void setHomeHealthVisits(String homeHealthVisits) { this.homeHealthVisits = homeHealthVisits; }

    public String getPcpVisits() { return pcpVisits; }
    public void setPcpVisits(String pcpVisits) { this.pcpVisits = pcpVisits; }

    public String getChiropracticServices() { return chiropracticServices; }
    public void setChiropracticServices(String chiropracticServices) { this.chiropracticServices = chiropracticServices; }

    public String getOccupationalTherapy() { return occupationalTherapy; }
    public void setOccupationalTherapy(String occupationalTherapy) { this.occupationalTherapy = occupationalTherapy; }

    public String getSpecialistVisits() { return specialistVisits; }
    public void setSpecialistVisits(String specialistVisits) { this.specialistVisits = specialistVisits; }

    public String getPodiatryMedicare() { return podiatryMedicare; }
    public void setPodiatryMedicare(String podiatryMedicare) { this.podiatryMedicare = podiatryMedicare; }

    public String getPodiatrySupplemental() { return podiatrySupplemental; }
    public void setPodiatrySupplemental(String podiatrySupplemental) { this.podiatrySupplemental = podiatrySupplemental; }

    public String getPhysicalTherapy() { return physicalTherapy; }
    public void setPhysicalTherapy(String physicalTherapy) { this.physicalTherapy = physicalTherapy; }

    public String getSpeechTherapy() { return speechTherapy; }
    public void setSpeechTherapy(String speechTherapy) { this.speechTherapy = speechTherapy; }

    public String getTelehealth() { return telehealth; }
    public void setTelehealth(String telehealth) { this.telehealth = telehealth; }

    public String getDiagnosticTesting() { return diagnosticTesting; }
    public void setDiagnosticTesting(String diagnosticTesting) { this.diagnosticTesting = diagnosticTesting; }

    public String getLabServices() { return labServices; }
    public void setLabServices(String labServices) { this.labServices = labServices; }

    public String getTherapeuticRadiology() { return therapeuticRadiology; }
    public void setTherapeuticRadiology(String therapeuticRadiology) { this.therapeuticRadiology = therapeuticRadiology; }

    public String getDiagnosticRadiology() { return diagnosticRadiology; }
    public void setDiagnosticRadiology(String diagnosticRadiology) { this.diagnosticRadiology = diagnosticRadiology; }

    public String getAdvancedImaging() { return advancedImaging; }
    public void setAdvancedImaging(String advancedImaging) { this.advancedImaging = advancedImaging; }

    public String getAmbulanceEmergent() { return ambulanceEmergent; }
    public void setAmbulanceEmergent(String ambulanceEmergent) { this.ambulanceEmergent = ambulanceEmergent; }

    public String getAmbulanceNonEmergent() { return ambulanceNonEmergent; }
    public void setAmbulanceNonEmergent(String ambulanceNonEmergent) { this.ambulanceNonEmergent = ambulanceNonEmergent; }

    public String getAmbulanceAir() { return ambulanceAir; }
    public void setAmbulanceAir(String ambulanceAir) { this.ambulanceAir = ambulanceAir; }

    public String getAcupuncture() { return acupuncture; }
    public void setAcupuncture(String acupuncture) { this.acupuncture = acupuncture; }

    public String getMentalHealthMedicare() { return mentalHealthMedicare; }
    public void setMentalHealthMedicare(String mentalHealthMedicare) { this.mentalHealthMedicare = mentalHealthMedicare; }

    public String getMentalHealthSupplemental() { return mentalHealthSupplemental; }
    public void setMentalHealthSupplemental(String mentalHealthSupplemental) { this.mentalHealthSupplemental = mentalHealthSupplemental; }

    public String getSubstanceAbuseMedicare() { return substanceAbuseMedicare; }
    public void setSubstanceAbuseMedicare(String substanceAbuseMedicare) { this.substanceAbuseMedicare = substanceAbuseMedicare; }

    public String getSubstanceAbuseSupplemental() { return substanceAbuseSupplemental; }
    public void setSubstanceAbuseSupplemental(String substanceAbuseSupplemental) { this.substanceAbuseSupplemental = substanceAbuseSupplemental; }

    public String getDialysisTreatment() { return dialysisTreatment; }
    public void setDialysisTreatment(String dialysisTreatment) { this.dialysisTreatment = dialysisTreatment; }

    public String getDMEProsthetics() { return DMEProsthetics; }
    public void setDMEProsthetics(String DMEProsthetics) { this.DMEProsthetics = DMEProsthetics; }

    public String getDiabeticSupplies() { return diabeticSupplies; }
    public void setDiabeticSupplies(String diabeticSupplies) { this.diabeticSupplies = diabeticSupplies; }

    public String getPartBRx() { return partBRx; }
    public void setPartBRx(String partBRx) { this.partBRx = partBRx; }

    public String getChemotherapy() { return chemotherapy; }
    public void setChemotherapy(String chemotherapy) { this.chemotherapy = chemotherapy; }

    public String getRenalDialysis() { return renalDialysis; }
    public void setRenalDialysis(String renalDialysis) { this.renalDialysis = renalDialysis; }

    public String getOpioidTreatment() { return opioidTreatment; }
    public void setOpioidTreatment(String opioidTreatment) { this.opioidTreatment = opioidTreatment; }

    public String getPartialHospitalization() { return partialHospitalization; }
    public void setPartialHospitalization(String partialHospitalization) { this.partialHospitalization = partialHospitalization; }

    public String getTransportation() { return transportation; }
    public void setTransportation(String transportation) { this.transportation = transportation; }

    public String getMeals() { return meals; }
    public void setMeals(String meals) { this.meals = meals; }

    public String getUtilities() { return utilities; }
    public void setUtilities(String utilities) { this.utilities = utilities; }

    public String getPestControl() { return pestControl; }
    public void setPestControl(String pestControl) { this.pestControl = pestControl; }

    public String getCompressionStockings() { return compressionStockings; }
    public void setCompressionStockings(String compressionStockings) { this.compressionStockings = compressionStockings; }

    public String getFirstAidKit() { return firstAidKit; }
    public void setFirstAidKit(String firstAidKit) { this.firstAidKit = firstAidKit; }

    public String getFoodCard() { return foodCard; }
    public void setFoodCard(String foodCard) { this.foodCard = foodCard; }

    public String getOverTheCounterItems() { return overTheCounterItems; }
    public void setOverTheCounterItems(String overTheCounterItems) { this.overTheCounterItems = overTheCounterItems; }

    public String getPersonalEmergencyDevice() { return personalEmergencyDevice; }
    public void setPersonalEmergencyDevice(String personalEmergencyDevice) { this.personalEmergencyDevice = personalEmergencyDevice; }

    public String getSmokingCessation() { return smokingCessation; }
    public void setSmokingCessation(String smokingCessation) { this.smokingCessation = smokingCessation; }

    public String getFittingContacts() { return fittingContacts; }
    public void setFittingContacts(String fittingContacts) { this.fittingContacts = fittingContacts; }

    public String getDentalProphylaxis() { return dentalProphylaxis; }
    public void setDentalProphylaxis(String dentalProphylaxis) { this.dentalProphylaxis = dentalProphylaxis; }

    public String getPeriodonticMaintenance() { return periodonticMaintenance; }
    public void setPeriodonticMaintenance(String periodonticMaintenance) { this.periodonticMaintenance = periodonticMaintenance; }

    public String getEmergencyDental() { return emergencyDental; }
    public void setEmergencyDental(String emergencyDental) { this.emergencyDental = emergencyDental; }

    public String getComprehensiveDental() { return comprehensiveDental; }
    public void setComprehensiveDental(String comprehensiveDental) { this.comprehensiveDental = comprehensiveDental; }

    public String getProsthodonticBasic() { return prosthodonticBasic; }
    public void setProsthodonticBasic(String prosthodonticBasic) { this.prosthodonticBasic = prosthodonticBasic; }

    public String getOrthodonticServices() { return orthodonticServices; }
    public void setOrthodonticServices(String orthodonticServices) { this.orthodonticServices = orthodonticServices; }

    public String getHearingExam() { return hearingExam; }
    public void setHearingExam(String hearingExam) { this.hearingExam = hearingExam; }

    public String getFittingEvaluation() { return fittingEvaluation; }
    public void setFittingEvaluation(String fittingEvaluation) { this.fittingEvaluation = fittingEvaluation; }

    public String getHearingAids() { return hearingAids; }
    public void setHearingAids(String hearingAids) { this.hearingAids = hearingAids; }

    public String getRoutineEyeExam() { return routineEyeExam; }
    public void setRoutineEyeExam(String routineEyeExam) { this.routineEyeExam = routineEyeExam; }

    public String getGlaucoma() { return glaucoma; }
    public void setGlaucoma(String glaucoma) { this.glaucoma = glaucoma; }

    public String getDiabeticRetinopathy() { return diabeticRetinopathy; }
    public void setDiabeticRetinopathy(String diabeticRetinopathy) { this.diabeticRetinopathy = diabeticRetinopathy; }

    public String getAllowancePrescription() { return allowancePrescription; }
    public void setAllowancePrescription(String allowancePrescription) { this.allowancePrescription = allowancePrescription; }

    public String getAllowanceNonPrescription() { return allowanceNonPrescription; }
    public void setAllowanceNonPrescription(String allowanceNonPrescription) { this.allowanceNonPrescription = allowanceNonPrescription; }

    public String getVisionHardware() { return visionHardware; }
    public void setVisionHardware(String visionHardware) { this.visionHardware = visionHardware; }

    public String getVisionSoftware() { return visionSoftware; }
    public void setVisionSoftware(String visionSoftware) { this.visionSoftware = visionSoftware; }

    public String getContactLenses() { return contactLenses; }
    public void setContactLenses(String contactLenses) { this.contactLenses = contactLenses; }

    public String getLowVisionAids() { return lowVisionAids; }
    public void setLowVisionAids(String lowVisionAids) { this.lowVisionAids = lowVisionAids; }

    public String getFitnessProgram() { return fitnessProgram; }
    public void setFitnessProgram(String fitnessProgram) { this.fitnessProgram = fitnessProgram; }

    public String getNurseHotline() { return nurseHotline; }
    public void setNurseHotline(String nurseHotline) { this.nurseHotline = nurseHotline; }

    // Additional missing fields that ExcelExportService expects
    public String getOutpatientHospital() { return inpatientAdmission; } // Using existing field
    public void setOutpatientHospital(String outpatientHospital) { this.inpatientAdmission = outpatientHospital; }

    public String getObservationRoom() { return inpatientAdmission; } // Using existing field
    public void setObservationRoom(String observationRoom) { this.inpatientAdmission = observationRoom; }

    public String getAmbulatorySurgery() { return emergencyServices; } // Using existing field
    public void setAmbulatorySurgery(String ambulatorySurgery) { this.emergencyServices = ambulatorySurgery; }

    public String getOutpatientBlood() { return labServices; } // Using existing field
    public void setOutpatientBlood(String outpatientBlood) { this.labServices = outpatientBlood; }

    public String getDme() { return DMEProsthetics; } // Using existing field
    public void setDme(String dme) { this.DMEProsthetics = dme; }

    public String getProsthetics() { return DMEProsthetics; } // Using existing field
    public void setProsthetics(String prosthetics) { this.DMEProsthetics = prosthetics; }

    public String getShoeInserts() { return DMEProsthetics; } // Using existing field
    public void setShoeInserts(String shoeInserts) { this.DMEProsthetics = shoeInserts; }

    public String getOtcMedications() { return overTheCounterItems; } // Using existing field
    public void setOtcMedications(String otcMedications) { this.overTheCounterItems = otcMedications; }

    public String getMealsBenefit() { return meals; } // Using existing field
    public void setMealsBenefit(String mealsBenefit) { this.meals = mealsBenefit; }

    public String getPreventiveCare() { return pcpVisits; } // Using existing field
    public void setPreventiveCare(String preventiveCare) { this.pcpVisits = preventiveCare; }

    public String getAnnualPhysical() { return pcpVisits; } // Using existing field
    public void setAnnualPhysical(String annualPhysical) { this.pcpVisits = annualPhysical; }

    public String getMoop() { return allColumns != null ? allColumns.get("moop") : null; } // Using metadata
    public void setMoop(String moop) { if (allColumns != null) allColumns.put("moop", moop); }

    public String getDeductible() { return allColumns != null ? allColumns.get("deductible") : null; } // Using metadata
    public void setDeductible(String deductible) { if (allColumns != null) allColumns.put("deductible", deductible); }

    // Metadata getters and setters
    public Map<String, String> getAllColumns() { return allColumns; }
    public void setAllColumns(Map<String, String> allColumns) { this.allColumns = allColumns; }

    public Map<String, String> getBenefitData() { return benefitData; }
    public void setBenefitData(Map<String, String> benefitData) { this.benefitData = benefitData; }

    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }

    // Builder class
    public static class VendorMatrixDataBuilder {
        private String productName;
        private String productId;
        private String basePlan;
        private String benefitSet;
        private String contractId;
        private String pbpNumber;
        private String segmentId;
        private String planYear;
        private String effectiveDate;
        private String company;
        private String licensure;
        private String inpatientAdmission;
        private String skilledNursingDays;
        private String cardiacTherapy;
        private String intensiveCardiacTherapy;
        private String superviseExerciseTherapy;
        private String pulmonaryRehab;
        private String emergencyServices;
        private String urgentCare;
        private String homeHealthVisits;
        private String pcpVisits;
        private String chiropracticServices;
        private String occupationalTherapy;
        private String specialistVisits;
        private String podiatryMedicare;
        private String podiatrySupplemental;
        private String physicalTherapy;
        private String speechTherapy;
        private String telehealth;
        private String diagnosticTesting;
        private String labServices;
        private String therapeuticRadiology;
        private String diagnosticRadiology;
        private String advancedImaging;
        private String ambulanceEmergent;
        private String ambulanceNonEmergent;
        private String ambulanceAir;
        private String acupuncture;
        private String mentalHealthMedicare;
        private String mentalHealthSupplemental;
        private String substanceAbuseMedicare;
        private String substanceAbuseSupplemental;
        private String dialysisTreatment;
        private String DMEProsthetics;
        private String diabeticSupplies;
        private String partBRx;
        private String chemotherapy;
        private String renalDialysis;
        private String opioidTreatment;
        private String partialHospitalization;
        private String transportation;
        private String meals;
        private String utilities;
        private String pestControl;
        private String compressionStockings;
        private String firstAidKit;
        private String foodCard;
        private String overTheCounterItems;
        private String personalEmergencyDevice;
        private String smokingCessation;
        private String fittingContacts;
        private String dentalProphylaxis;
        private String periodonticMaintenance;
        private String emergencyDental;
        private String comprehensiveDental;
        private String prosthodonticBasic;
        private String orthodonticServices;
        private String hearingExam;
        private String fittingEvaluation;
        private String hearingAids;
        private String routineEyeExam;
        private String glaucoma;
        private String diabeticRetinopathy;
        private String allowancePrescription;
        private String allowanceNonPrescription;
        private String visionHardware;
        private String visionSoftware;
        private String contactLenses;
        private String lowVisionAids;
        private String fitnessProgram;
        private String nurseHotline;
        private Map<String, String> allColumns;
        private Map<String, String> benefitData;
        private String sourceFileName;
        private String uploadedAt;

        public VendorMatrixDataBuilder productName(String productName) { this.productName = productName; return this; }
        public VendorMatrixDataBuilder productId(String productId) { this.productId = productId; return this; }
        public VendorMatrixDataBuilder basePlan(String basePlan) { this.basePlan = basePlan; return this; }
        public VendorMatrixDataBuilder benefitSet(String benefitSet) { this.benefitSet = benefitSet; return this; }
        public VendorMatrixDataBuilder contractId(String contractId) { this.contractId = contractId; return this; }
        public VendorMatrixDataBuilder pbpNumber(String pbpNumber) { this.pbpNumber = pbpNumber; return this; }
        public VendorMatrixDataBuilder segmentId(String segmentId) { this.segmentId = segmentId; return this; }
        public VendorMatrixDataBuilder planYear(String planYear) { this.planYear = planYear; return this; }
        public VendorMatrixDataBuilder effectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public VendorMatrixDataBuilder company(String company) { this.company = company; return this; }
        public VendorMatrixDataBuilder licensure(String licensure) { this.licensure = licensure; return this; }
        public VendorMatrixDataBuilder inpatientAdmission(String inpatientAdmission) { this.inpatientAdmission = inpatientAdmission; return this; }
        public VendorMatrixDataBuilder skilledNursingDays(String skilledNursingDays) { this.skilledNursingDays = skilledNursingDays; return this; }
        public VendorMatrixDataBuilder cardiacTherapy(String cardiacTherapy) { this.cardiacTherapy = cardiacTherapy; return this; }
        public VendorMatrixDataBuilder intensiveCardiacTherapy(String intensiveCardiacTherapy) { this.intensiveCardiacTherapy = intensiveCardiacTherapy; return this; }
        public VendorMatrixDataBuilder superviseExerciseTherapy(String superviseExerciseTherapy) { this.superviseExerciseTherapy = superviseExerciseTherapy; return this; }
        public VendorMatrixDataBuilder pulmonaryRehab(String pulmonaryRehab) { this.pulmonaryRehab = pulmonaryRehab; return this; }
        public VendorMatrixDataBuilder emergencyServices(String emergencyServices) { this.emergencyServices = emergencyServices; return this; }
        public VendorMatrixDataBuilder urgentCare(String urgentCare) { this.urgentCare = urgentCare; return this; }
        public VendorMatrixDataBuilder homeHealthVisits(String homeHealthVisits) { this.homeHealthVisits = homeHealthVisits; return this; }
        public VendorMatrixDataBuilder pcpVisits(String pcpVisits) { this.pcpVisits = pcpVisits; return this; }
        public VendorMatrixDataBuilder chiropracticServices(String chiropracticServices) { this.chiropracticServices = chiropracticServices; return this; }
        public VendorMatrixDataBuilder occupationalTherapy(String occupationalTherapy) { this.occupationalTherapy = occupationalTherapy; return this; }
        public VendorMatrixDataBuilder specialistVisits(String specialistVisits) { this.specialistVisits = specialistVisits; return this; }
        public VendorMatrixDataBuilder podiatryMedicare(String podiatryMedicare) { this.podiatryMedicare = podiatryMedicare; return this; }
        public VendorMatrixDataBuilder podiatrySupplemental(String podiatrySupplemental) { this.podiatrySupplemental = podiatrySupplemental; return this; }
        public VendorMatrixDataBuilder physicalTherapy(String physicalTherapy) { this.physicalTherapy = physicalTherapy; return this; }
        public VendorMatrixDataBuilder speechTherapy(String speechTherapy) { this.speechTherapy = speechTherapy; return this; }
        public VendorMatrixDataBuilder telehealth(String telehealth) { this.telehealth = telehealth; return this; }
        public VendorMatrixDataBuilder diagnosticTesting(String diagnosticTesting) { this.diagnosticTesting = diagnosticTesting; return this; }
        public VendorMatrixDataBuilder labServices(String labServices) { this.labServices = labServices; return this; }
        public VendorMatrixDataBuilder therapeuticRadiology(String therapeuticRadiology) { this.therapeuticRadiology = therapeuticRadiology; return this; }
        public VendorMatrixDataBuilder diagnosticRadiology(String diagnosticRadiology) { this.diagnosticRadiology = diagnosticRadiology; return this; }
        public VendorMatrixDataBuilder advancedImaging(String advancedImaging) { this.advancedImaging = advancedImaging; return this; }
        public VendorMatrixDataBuilder ambulanceEmergent(String ambulanceEmergent) { this.ambulanceEmergent = ambulanceEmergent; return this; }
        public VendorMatrixDataBuilder ambulanceNonEmergent(String ambulanceNonEmergent) { this.ambulanceNonEmergent = ambulanceNonEmergent; return this; }
        public VendorMatrixDataBuilder ambulanceAir(String ambulanceAir) { this.ambulanceAir = ambulanceAir; return this; }
        public VendorMatrixDataBuilder acupuncture(String acupuncture) { this.acupuncture = acupuncture; return this; }
        public VendorMatrixDataBuilder mentalHealthMedicare(String mentalHealthMedicare) { this.mentalHealthMedicare = mentalHealthMedicare; return this; }
        public VendorMatrixDataBuilder mentalHealthSupplemental(String mentalHealthSupplemental) { this.mentalHealthSupplemental = mentalHealthSupplemental; return this; }
        public VendorMatrixDataBuilder substanceAbuseMedicare(String substanceAbuseMedicare) { this.substanceAbuseMedicare = substanceAbuseMedicare; return this; }
        public VendorMatrixDataBuilder substanceAbuseSupplemental(String substanceAbuseSupplemental) { this.substanceAbuseSupplemental = substanceAbuseSupplemental; return this; }
        public VendorMatrixDataBuilder dialysisTreatment(String dialysisTreatment) { this.dialysisTreatment = dialysisTreatment; return this; }
        public VendorMatrixDataBuilder DMEProsthetics(String DMEProsthetics) { this.DMEProsthetics = DMEProsthetics; return this; }
        public VendorMatrixDataBuilder diabeticSupplies(String diabeticSupplies) { this.diabeticSupplies = diabeticSupplies; return this; }
        public VendorMatrixDataBuilder partBRx(String partBRx) { this.partBRx = partBRx; return this; }
        public VendorMatrixDataBuilder chemotherapy(String chemotherapy) { this.chemotherapy = chemotherapy; return this; }
        public VendorMatrixDataBuilder renalDialysis(String renalDialysis) { this.renalDialysis = renalDialysis; return this; }
        public VendorMatrixDataBuilder opioidTreatment(String opioidTreatment) { this.opioidTreatment = opioidTreatment; return this; }
        public VendorMatrixDataBuilder partialHospitalization(String partialHospitalization) { this.partialHospitalization = partialHospitalization; return this; }
        public VendorMatrixDataBuilder transportation(String transportation) { this.transportation = transportation; return this; }
        public VendorMatrixDataBuilder meals(String meals) { this.meals = meals; return this; }
        public VendorMatrixDataBuilder utilities(String utilities) { this.utilities = utilities; return this; }
        public VendorMatrixDataBuilder pestControl(String pestControl) { this.pestControl = pestControl; return this; }
        public VendorMatrixDataBuilder compressionStockings(String compressionStockings) { this.compressionStockings = compressionStockings; return this; }
        public VendorMatrixDataBuilder firstAidKit(String firstAidKit) { this.firstAidKit = firstAidKit; return this; }
        public VendorMatrixDataBuilder foodCard(String foodCard) { this.foodCard = foodCard; return this; }
        public VendorMatrixDataBuilder overTheCounterItems(String overTheCounterItems) { this.overTheCounterItems = overTheCounterItems; return this; }
        public VendorMatrixDataBuilder personalEmergencyDevice(String personalEmergencyDevice) { this.personalEmergencyDevice = personalEmergencyDevice; return this; }
        public VendorMatrixDataBuilder smokingCessation(String smokingCessation) { this.smokingCessation = smokingCessation; return this; }
        public VendorMatrixDataBuilder fittingContacts(String fittingContacts) { this.fittingContacts = fittingContacts; return this; }
        public VendorMatrixDataBuilder dentalProphylaxis(String dentalProphylaxis) { this.dentalProphylaxis = dentalProphylaxis; return this; }
        public VendorMatrixDataBuilder periodonticMaintenance(String periodonticMaintenance) { this.periodonticMaintenance = periodonticMaintenance; return this; }
        public VendorMatrixDataBuilder emergencyDental(String emergencyDental) { this.emergencyDental = emergencyDental; return this; }
        public VendorMatrixDataBuilder comprehensiveDental(String comprehensiveDental) { this.comprehensiveDental = comprehensiveDental; return this; }
        public VendorMatrixDataBuilder prosthodonticBasic(String prosthodonticBasic) { this.prosthodonticBasic = prosthodonticBasic; return this; }
        public VendorMatrixDataBuilder orthodonticServices(String orthodonticServices) { this.orthodonticServices = orthodonticServices; return this; }
        public VendorMatrixDataBuilder hearingExam(String hearingExam) { this.hearingExam = hearingExam; return this; }
        public VendorMatrixDataBuilder fittingEvaluation(String fittingEvaluation) { this.fittingEvaluation = fittingEvaluation; return this; }
        public VendorMatrixDataBuilder hearingAids(String hearingAids) { this.hearingAids = hearingAids; return this; }
        public VendorMatrixDataBuilder routineEyeExam(String routineEyeExam) { this.routineEyeExam = routineEyeExam; return this; }
        public VendorMatrixDataBuilder glaucoma(String glaucoma) { this.glaucoma = glaucoma; return this; }
        public VendorMatrixDataBuilder diabeticRetinopathy(String diabeticRetinopathy) { this.diabeticRetinopathy = diabeticRetinopathy; return this; }
        public VendorMatrixDataBuilder allowancePrescription(String allowancePrescription) { this.allowancePrescription = allowancePrescription; return this; }
        public VendorMatrixDataBuilder allowanceNonPrescription(String allowanceNonPrescription) { this.allowanceNonPrescription = allowanceNonPrescription; return this; }
        public VendorMatrixDataBuilder visionHardware(String visionHardware) { this.visionHardware = visionHardware; return this; }
        public VendorMatrixDataBuilder visionSoftware(String visionSoftware) { this.visionSoftware = visionSoftware; return this; }
        public VendorMatrixDataBuilder contactLenses(String contactLenses) { this.contactLenses = contactLenses; return this; }
        public VendorMatrixDataBuilder lowVisionAids(String lowVisionAids) { this.lowVisionAids = lowVisionAids; return this; }
        public VendorMatrixDataBuilder fitnessProgram(String fitnessProgram) { this.fitnessProgram = fitnessProgram; return this; }
        public VendorMatrixDataBuilder nurseHotline(String nurseHotline) { this.nurseHotline = nurseHotline; return this; }
        public VendorMatrixDataBuilder outpatientHospital(String outpatientHospital) { this.inpatientAdmission = outpatientHospital; return this; }
        public VendorMatrixDataBuilder observationRoom(String observationRoom) { this.inpatientAdmission = observationRoom; return this; }
        public VendorMatrixDataBuilder ambulatorySurgery(String ambulatorySurgery) { this.emergencyServices = ambulatorySurgery; return this; }
        public VendorMatrixDataBuilder outpatientBlood(String outpatientBlood) { this.labServices = outpatientBlood; return this; }
        public VendorMatrixDataBuilder dme(String dme) { this.DMEProsthetics = dme; return this; }
        public VendorMatrixDataBuilder prosthetics(String prosthetics) { this.DMEProsthetics = prosthetics; return this; }
        public VendorMatrixDataBuilder shoeInserts(String shoeInserts) { this.DMEProsthetics = shoeInserts; return this; }
        public VendorMatrixDataBuilder otcMedications(String otcMedications) { this.overTheCounterItems = otcMedications; return this; }
        public VendorMatrixDataBuilder mealsBenefit(String mealsBenefit) { this.meals = mealsBenefit; return this; }
        public VendorMatrixDataBuilder preventiveCare(String preventiveCare) { this.pcpVisits = preventiveCare; return this; }
        public VendorMatrixDataBuilder annualPhysical(String annualPhysical) { this.pcpVisits = annualPhysical; return this; }
        public VendorMatrixDataBuilder moop(String moop) { 
            if (this.allColumns == null) this.allColumns = new java.util.HashMap<>();
            this.allColumns.put("moop", moop); 
            return this; 
        }
        public VendorMatrixDataBuilder deductible(String deductible) { 
            if (this.allColumns == null) this.allColumns = new java.util.HashMap<>();
            this.allColumns.put("deductible", deductible); 
            return this; 
        }
        public VendorMatrixDataBuilder allColumns(Map<String, String> allColumns) { this.allColumns = allColumns; return this; }
        public VendorMatrixDataBuilder benefitData(Map<String, String> benefitData) { this.benefitData = benefitData; return this; }
        public VendorMatrixDataBuilder sourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; return this; }
        public VendorMatrixDataBuilder uploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public VendorMatrixData build() {
            VendorMatrixData data = new VendorMatrixData();
            data.setProductName(productName);
            data.setProductId(productId);
            data.setBasePlan(basePlan);
            data.setBenefitSet(benefitSet);
            data.setContractId(contractId);
            data.setPbpNumber(pbpNumber);
            data.setSegmentId(segmentId);
            data.setPlanYear(planYear);
            data.setEffectiveDate(effectiveDate);
            data.setCompany(company);
            data.setLicensure(licensure);
            data.setInpatientAdmission(inpatientAdmission);
            data.setSkilledNursingDays(skilledNursingDays);
            data.setCardiacTherapy(cardiacTherapy);
            data.setIntensiveCardiacTherapy(intensiveCardiacTherapy);
            data.setSuperviseExerciseTherapy(superviseExerciseTherapy);
            data.setPulmonaryRehab(pulmonaryRehab);
            data.setEmergencyServices(emergencyServices);
            data.setUrgentCare(urgentCare);
            data.setHomeHealthVisits(homeHealthVisits);
            data.setPcpVisits(pcpVisits);
            data.setChiropracticServices(chiropracticServices);
            data.setOccupationalTherapy(occupationalTherapy);
            data.setSpecialistVisits(specialistVisits);
            data.setPodiatryMedicare(podiatryMedicare);
            data.setPodiatrySupplemental(podiatrySupplemental);
            data.setPhysicalTherapy(physicalTherapy);
            data.setSpeechTherapy(speechTherapy);
            data.setTelehealth(telehealth);
            data.setDiagnosticTesting(diagnosticTesting);
            data.setLabServices(labServices);
            data.setTherapeuticRadiology(therapeuticRadiology);
            data.setDiagnosticRadiology(diagnosticRadiology);
            data.setAdvancedImaging(advancedImaging);
            data.setAmbulanceEmergent(ambulanceEmergent);
            data.setAmbulanceNonEmergent(ambulanceNonEmergent);
            data.setAmbulanceAir(ambulanceAir);
            data.setAcupuncture(acupuncture);
            data.setMentalHealthMedicare(mentalHealthMedicare);
            data.setMentalHealthSupplemental(mentalHealthSupplemental);
            data.setSubstanceAbuseMedicare(substanceAbuseMedicare);
            data.setSubstanceAbuseSupplemental(substanceAbuseSupplemental);
            data.setDialysisTreatment(dialysisTreatment);
            data.setDMEProsthetics(DMEProsthetics);
            data.setDiabeticSupplies(diabeticSupplies);
            data.setPartBRx(partBRx);
            data.setChemotherapy(chemotherapy);
            data.setRenalDialysis(renalDialysis);
            data.setOpioidTreatment(opioidTreatment);
            data.setPartialHospitalization(partialHospitalization);
            data.setTransportation(transportation);
            data.setMeals(meals);
            data.setUtilities(utilities);
            data.setPestControl(pestControl);
            data.setCompressionStockings(compressionStockings);
            data.setFirstAidKit(firstAidKit);
            data.setFoodCard(foodCard);
            data.setOverTheCounterItems(overTheCounterItems);
            data.setPersonalEmergencyDevice(personalEmergencyDevice);
            data.setSmokingCessation(smokingCessation);
            data.setFittingContacts(fittingContacts);
            data.setDentalProphylaxis(dentalProphylaxis);
            data.setPeriodonticMaintenance(periodonticMaintenance);
            data.setEmergencyDental(emergencyDental);
            data.setComprehensiveDental(comprehensiveDental);
            data.setProsthodonticBasic(prosthodonticBasic);
            data.setOrthodonticServices(orthodonticServices);
            data.setHearingExam(hearingExam);
            data.setFittingEvaluation(fittingEvaluation);
            data.setHearingAids(hearingAids);
            data.setRoutineEyeExam(routineEyeExam);
            data.setGlaucoma(glaucoma);
            data.setDiabeticRetinopathy(diabeticRetinopathy);
            data.setAllowancePrescription(allowancePrescription);
            data.setAllowanceNonPrescription(allowanceNonPrescription);
            data.setVisionHardware(visionHardware);
            data.setVisionSoftware(visionSoftware);
            data.setContactLenses(contactLenses);
            data.setLowVisionAids(lowVisionAids);
            data.setFitnessProgram(fitnessProgram);
            data.setNurseHotline(nurseHotline);
            data.setAllColumns(allColumns);
            data.setBenefitData(benefitData);
            data.setSourceFileName(sourceFileName);
            data.setUploadedAt(uploadedAt);
            return data;
        }
    }
} 