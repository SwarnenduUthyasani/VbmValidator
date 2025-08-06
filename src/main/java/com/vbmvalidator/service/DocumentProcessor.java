package com.vbmvalidator.service;

import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.VendorMatrixData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentProcessor {
    
    /**
     * Determine if this processor can handle the given file
     */
    boolean canProcess(MultipartFile file);
    
    /**
     * Extract SOB data from uploaded file
     */
    SOBData extractSOBData(MultipartFile file) throws IOException;
    
    /**
     * Extract Vendor Matrix data from uploaded file
     */
    VendorMatrixData extractVendorMatrixData(MultipartFile file) throws IOException;
    
    /**
     * Get supported file extensions
     */
    String[] getSupportedExtensions();
} 
