package com.sobvalidator.service.impl;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sobvalidator.model.SOBData;
import com.sobvalidator.model.VendorMatrixData;
import com.sobvalidator.service.DocumentProcessor;

@Service
public class DocumentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorService.class);

    private final List<DocumentProcessor> processors;

    @Autowired
    public DocumentProcessorService(List<DocumentProcessor> processors) {
        this.processors = processors;
        log.info("Initialized DocumentProcessorService with {} processors", processors.size());
    }

    public SOBData extractSOBData(MultipartFile file) throws IOException {
        validateFile(file);
        
        // SOB files can only be Excel or PDF
        if (!isExcelFile(file) && !isPDFFile(file)) {
            throw new UnsupportedOperationException(
                "SOB files must be in Excel (.xlsx) or PDF format. CSV is not supported for SOB files."
            );
        }
        
        DocumentProcessor processor = findProcessor(file);
        if (processor == null) {
            throw new UnsupportedOperationException(
                "Unsupported file format for SOB. Supported formats: Excel (.xlsx), PDF"
            );
        }
        
        log.info("Processing SOB file {} with processor {}", 
                file.getOriginalFilename(), processor.getClass().getSimpleName());
        
        return processor.extractSOBData(file);
    }

    public VendorMatrixData extractVendorMatrixData(MultipartFile file) throws IOException {
        validateFile(file);
        
        // Vendor Matrix files can only be Excel (temporarily allow CSV for testing)
        if (!isExcelFile(file) && !isCSVFile(file)) {
            throw new UnsupportedOperationException(
                "Vendor Matrix files must be in Excel format (.xlsx). PDF not supported for Vendor Matrix."
            );
        }
        
        DocumentProcessor processor = findProcessor(file);
        if (processor == null) {
            throw new UnsupportedOperationException(
                "Unsupported file format for Vendor Matrix. Supported formats: Excel (.xlsx)"
            );
        }
        
        log.info("Processing Vendor Matrix file {} with processor {}", 
                file.getOriginalFilename(), processor.getClass().getSimpleName());
        
        return processor.extractVendorMatrixData(file);
    }

    private DocumentProcessor findProcessor(MultipartFile file) {
        return processors.stream()
                .filter(processor -> processor.canProcess(file))
                .findFirst()
                .orElse(null);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }
        
        if (file.getOriginalFilename() == null) {
            throw new IOException("File name is missing");
        }
        
        // Check file size (50MB limit)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds maximum limit of 50MB");
        }
    }

    private boolean isExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && 
               (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"));
    }

    private boolean isPDFFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    private boolean isCSVFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    public boolean canProcessSOB(MultipartFile file) {
        if (file == null) {
            return false;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String filename = originalFilename.toLowerCase();
        return filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".pdf");
    }

    public boolean canProcessVendorMatrix(MultipartFile file) {
        return isExcelFile(file) || isCSVFile(file); // Temporarily allow CSV for testing
    }

    public String[] getSupportedSOBFormats() {
        return new String[]{".xlsx", ".xls", ".pdf"};
    }

    public String[] getSupportedVendorMatrixFormats() {
        return new String[]{".xlsx", ".xls"};
    }
} 