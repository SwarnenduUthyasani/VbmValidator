package com.vbmvalidator.service;

import com.vbmvalidator.model.*;

public interface ValidationService {
    
    /**
     * Validate Vendor Matrix against SOB data
     */
    ValidationResult validateVendorMatrix(SOBData sobData, VendorMatrixData vendorMatrixData);
    
    /**
     * Validate specific SOB type with custom rules
     */
    ValidationResult validateWithSOBType(SOBData sobData, VendorMatrixData vendorMatrixData, SOBType sobType);
    
    /**
     * Generate corrected Vendor Matrix data based on SOB
     */
    VendorMatrixData generateCorrectedVendorMatrix(SOBData sobData, VendorMatrixData originalVendorMatrix, ValidationResult validationResult);
} 
