package com.vbmvalidator.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.ValidationError;
import com.vbmvalidator.model.ValidationResult;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.ExcelExportService;
import com.vbmvalidator.service.ValidationService;
import com.vbmvalidator.service.impl.DocumentProcessorService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class ValidationController {

    private static final Logger log = LoggerFactory.getLogger(ValidationController.class);

    @Autowired
    private DocumentProcessorService documentProcessorService;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("sobTypes", SOBType.values());
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam MultipartFile sobFile,
                            @RequestParam MultipartFile vendorMatrixFile,
                            @RequestParam("sobType") String sobTypeString,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpSession session) {
        
        log.info("=== UPLOAD REQUEST RECEIVED ===");
        log.info("SOB File: {}, Size: {}", sobFile.getOriginalFilename(), sobFile.getSize());
        log.info("VM File: {}, Size: {}", vendorMatrixFile.getOriginalFilename(), vendorMatrixFile.getSize());
        log.info("Manual SOB Type: {}", sobTypeString);
        
        SOBType sobType = null;
        try {
            sobType = SOBType.valueOf(sobTypeString);
        } catch (IllegalArgumentException e) {
            log.error("Invalid SOB Type provided: {}", sobTypeString);
            redirectAttributes.addFlashAttribute("error", "Invalid SOB Type selected. Please try again.");
            return "redirect:/";
        }
        
        try {
            // Validate file uploads
            if (sobFile.isEmpty() || vendorMatrixFile.isEmpty()) {
                log.error("Empty files detected - SOB empty: {}, VM empty: {}", sobFile.isEmpty(), vendorMatrixFile.isEmpty());
                redirectAttributes.addFlashAttribute("error", "Please select both SOB and Vendor Matrix files");
                return "redirect:/";
            }

            // Validate file types - only Excel files allowed
            String sobFilename = sobFile.getOriginalFilename();
            if (sobFilename == null || (!sobFilename.toLowerCase().endsWith(".xlsx") && !sobFilename.toLowerCase().endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "Invalid file type for Summary Of Benefits (SOB). Please upload an Excel file (.xlsx or .xls only).");
                return "redirect:/";
            }

            String vbmFilename = vendorMatrixFile.getOriginalFilename();
            if (vbmFilename == null || (!vbmFilename.toLowerCase().endsWith(".xlsx") && !vbmFilename.toLowerCase().endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "Invalid file type for Vendor Benefit Matrix (VBM). Please upload an Excel file (.xlsx or .xls only).");
                return "redirect:/";
            }

            // Process SOB file
            log.info("Starting SOB file processing...");
            SOBData sobData = documentProcessorService.extractSOBData(sobFile);
            log.info("Processed SOB file: {}", sobData.getSourceFileName());

            // Process Vendor Matrix file
            log.info("Starting Vendor Matrix file processing...");
            VendorMatrixData vendorMatrixData = documentProcessorService.extractVendorMatrixData(vendorMatrixFile);
            log.info("Processed Vendor Matrix file: {}", vendorMatrixData.getSourceFileName());

            // Perform validation
            log.info("Starting validation process with SOB Type: {}", sobType);
            ValidationResult validationResult = validationService.validateWithSOBType(sobData, vendorMatrixData, sobType);
            log.info("Validation completed with {} errors, {} warnings", validationResult.getTotalErrors(), validationResult.getTotalWarnings());

            // Store data in session for export functionality
            session.setAttribute("sobData", sobData);
            session.setAttribute("vendorMatrixData", vendorMatrixData);
            session.setAttribute("validationResult", validationResult);
            session.setAttribute("sobType", sobType);

            // Add to model for display
            log.info("Adding model attributes...");
            redirectAttributes.addFlashAttribute("sobData", sobData);
            redirectAttributes.addFlashAttribute("vendorMatrixData", vendorMatrixData);
            redirectAttributes.addFlashAttribute("validationResult", validationResult);
            redirectAttributes.addFlashAttribute("sobType", sobType);

            log.info("Redirecting to validation-results view");
            return "redirect:/validation-results";

        } catch (IOException | RuntimeException e) {
            log.error("Error during validation", e);
            String errorMessage = e instanceof IOException ? 
                "Error processing files: " + e.getMessage() : 
                "Unexpected error: " + e.getMessage();
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/";
        }
    }

    @GetMapping("/validation-results")
    public String showValidationResults(Model model) {
        if (!model.containsAttribute("validationResult")) {
            return "redirect:/";
        }
        
        ValidationResult validationResult = (ValidationResult) model.getAttribute("validationResult");
        SOBData sobData = (SOBData) model.getAttribute("sobData");
        
        if (validationResult != null && !validationResult.getErrors().isEmpty()) {
            int errorIndex = 0; // Default to first error
            ValidationError currentError = validationResult.getErrors().get(errorIndex);
            model.addAttribute("currentError", currentError);
            model.addAttribute("currentErrorIndex", errorIndex);
            
            if (sobData != null) {
                SOBBenefit currentSOBBenefit = sobData.getBenefit(currentError.getBenefitCategory());
                model.addAttribute("currentSOBBenefit", currentSOBBenefit);
            }
        }
        
        return "validation-results";
    }

    @GetMapping("/validation-results/details")
    public String showValidationErrorDetails(@RequestParam(defaultValue = "0") int errorIndex,
                                           Model model, HttpSession session) {
        ValidationResult validationResult = (ValidationResult) session.getAttribute("validationResult");
        SOBData sobData = (SOBData) session.getAttribute("sobData");
        
        if (validationResult == null || sobData == null) {
            return "redirect:/";
        }

        model.addAttribute("validationResult", validationResult);
        model.addAttribute("sobData", sobData);
        model.addAttribute("vendorMatrixData", session.getAttribute("vendorMatrixData"));
        model.addAttribute("sobType", session.getAttribute("sobType"));

        if (!validationResult.getErrors().isEmpty()) {
            if (errorIndex >= 0 && errorIndex < validationResult.getErrors().size()) {
                ValidationError currentError = validationResult.getErrors().get(errorIndex);
                SOBBenefit currentSOBBenefit = sobData.getBenefit(currentError.getBenefitCategory());
                
                model.addAttribute("currentError", currentError);
                model.addAttribute("currentErrorIndex", errorIndex);
                model.addAttribute("currentSOBBenefit", currentSOBBenefit);
            } else {
                model.addAttribute("currentError", validationResult.getErrors().get(0));
                model.addAttribute("currentErrorIndex", 0);
                model.addAttribute("currentSOBBenefit", sobData.getBenefit(validationResult.getErrors().get(0).getBenefitCategory()));
            }
        } else {
            model.addAttribute("currentError", null);
            model.addAttribute("currentErrorIndex", 0);
            model.addAttribute("currentSOBBenefit", null);
        }

        return "validation-results";
    }

    @GetMapping("/validation/{validationId}")
    public String getValidationResults(@PathVariable String validationId, Model model) {
        // Implementation for retrieving stored validation results
        // For now, redirect to home
        return "redirect:/";
    }

    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<ValidationResult> validateAPI(@RequestParam MultipartFile sobFile,
                                                      @RequestParam MultipartFile vendorMatrixFile,
                                                      @RequestParam(required = false) SOBType sobType) {
        try {
            SOBData sobData = documentProcessorService.extractSOBData(sobFile);
            VendorMatrixData vendorMatrixData = documentProcessorService.extractVendorMatrixData(vendorMatrixFile);
            
            SOBType finalSOBType = sobType != null ? sobType : sobData.getSobType();
            ValidationResult result = validationService.validateWithSOBType(sobData, vendorMatrixData, finalSOBType);
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("API validation error", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/export-corrected")
    @ResponseBody
    public ResponseEntity<byte[]> exportCorrectedMatrix(@RequestBody ExportRequest exportRequest, 
                                                       HttpSession session) {
        try {
            log.info("Export request received for {} selected errors", 
                exportRequest.getSelectedErrorIds() != null ? exportRequest.getSelectedErrorIds().size() : 0);
            
            // Retrieve data from session
            SOBData sobData = (SOBData) session.getAttribute("sobData");
            VendorMatrixData vendorMatrixData = (VendorMatrixData) session.getAttribute("vendorMatrixData");
            ValidationResult validationResult = (ValidationResult) session.getAttribute("validationResult");
            
            if (sobData == null || vendorMatrixData == null || validationResult == null) {
                log.error("Session data not found. User may need to re-upload files.");
                return ResponseEntity.badRequest()
                    .body("Session expired. Please upload files again.".getBytes());
            }
            
            // Get selected error IDs or default to all errors if none selected
            List<String> selectedErrorIds = exportRequest.getSelectedErrorIds();
            if (selectedErrorIds == null || selectedErrorIds.isEmpty()) {
                selectedErrorIds = validationResult.getErrors().stream()
                    .map(error -> error.getErrorId())
                    .collect(java.util.stream.Collectors.toList());
                log.info("No specific errors selected, using all {} errors", selectedErrorIds.size());
            }
            
            // Generate corrected Excel file
            byte[] excelData = excelExportService.generateCorrectedVendorMatrix(
                vendorMatrixData, 
                validationResult, 
                selectedErrorIds, 
                exportRequest.isHighlightChanges()
            );
            
            if (excelData == null || excelData.length == 0) {
                log.error("Generated Excel file is empty");
                return ResponseEntity.internalServerError()
                    .body("Failed to generate Excel file".getBytes());
            }
            
            log.info("Generated Excel file with {} bytes", excelData.length);
            
            // Create filename with timestamp
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            );
            String filename = "corrected_vendor_matrix_" + timestamp + ".xlsx";
            
            // Set proper headers for Excel download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            log.info("Returning Excel file: {} ({} bytes)", filename, excelData.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                    
        } catch (IOException | IllegalArgumentException e) {
            log.error("Export error", e);
            return ResponseEntity.internalServerError()
                .body(("Export failed: " + e.getMessage()).getBytes());
        } catch (RuntimeException e) {
            log.error("Runtime error during export", e);
            return ResponseEntity.internalServerError()
                .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/api/update-error-selection")
    @ResponseBody
    public ResponseEntity<String> updateErrorSelection(@RequestBody List<String> selectedErrorIds) {
        // Implementation for updating which errors user wants to fix
        log.info("User selected {} errors for correction", selectedErrorIds.size());
        return ResponseEntity.ok("Selection updated");
    }
} 
