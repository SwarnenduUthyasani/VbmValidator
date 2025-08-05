package com.sobvalidator.controller;

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

import com.sobvalidator.model.SOBBenefit;
import com.sobvalidator.model.SOBData;
import com.sobvalidator.model.SOBType;
import com.sobvalidator.model.ValidationError;
import com.sobvalidator.model.ValidationResult;
import com.sobvalidator.model.VendorMatrixData;
import com.sobvalidator.service.ExcelExportService;
import com.sobvalidator.service.ValidationService;
import com.sobvalidator.service.impl.DocumentProcessorService;

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
    public String uploadFiles(@RequestParam("sobFile") MultipartFile sobFile,
                            @RequestParam("vendorMatrixFile") MultipartFile vendorMatrixFile,
                            @RequestParam(value = "sobType", required = true) SOBType sobType,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpSession session) {
        
        log.info("=== UPLOAD REQUEST RECEIVED ===");
        log.info("SOB File: {}, Size: {}", sobFile.getOriginalFilename(), sobFile.getSize());
        log.info("VM File: {}, Size: {}", vendorMatrixFile.getOriginalFilename(), vendorMatrixFile.getSize());
        log.info("SOB Type: {}", sobType);
        
        try {
            // Validate file uploads
            if (sobFile.isEmpty() || vendorMatrixFile.isEmpty()) {
                log.error("Empty files detected - SOB empty: {}, VM empty: {}", sobFile.isEmpty(), vendorMatrixFile.isEmpty());
                redirectAttributes.addFlashAttribute("error", "Please select both SOB and Vendor Matrix files");
                return "redirect:/";
            }

            // Validate SOB type is provided
            if (sobType == null) {
                log.error("SOB type not provided");
                redirectAttributes.addFlashAttribute("error", "Please select an SOB type");
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

            // Use the manually selected SOB type
            log.info("Using manually selected SOB Type: {}", sobType);

            // Perform validation
            log.info("Starting validation process...");
            ValidationResult validationResult = validationService.validateWithSOBType(sobData, vendorMatrixData, sobType);
            log.info("Validation completed with {} errors", validationResult.getErrors().size());

            // Store data in session for export functionality
            session.setAttribute("sobData", sobData);
            session.setAttribute("vendorMatrixData", vendorMatrixData);
            session.setAttribute("validationResult", validationResult);

            // Redirect to validation results with first error if any exist
            if (validationResult.getErrors().size() > 0) {
                return "redirect:/validation-results?errorIndex=0";
            } else {
                return "redirect:/validation-results";
            }

        } catch (Exception e) {
            log.error("Unexpected error during validation", e);
            redirectAttributes.addFlashAttribute("error", "Error processing files: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/validation-results")
    public String showValidationResults(@RequestParam(value = "errorIndex", required = false, defaultValue = "0") int errorIndex,
                                       Model model, HttpSession session) {
        log.info("=== VALIDATION RESULTS REQUEST ===");
        log.info("Error Index: {}", errorIndex);

        // Retrieve data from session
        SOBData sobData = (SOBData) session.getAttribute("sobData");
        VendorMatrixData vendorMatrixData = (VendorMatrixData) session.getAttribute("vendorMatrixData");
        ValidationResult validationResult = (ValidationResult) session.getAttribute("validationResult");

        if (sobData == null || vendorMatrixData == null || validationResult == null) {
            log.error("Session data not found. Redirecting to upload page.");
            model.addAttribute("error", "Session expired. Please upload files again.");
            return "redirect:/";
        }

        // Add basic data to model
        model.addAttribute("sobData", sobData);
        model.addAttribute("vendorMatrixData", vendorMatrixData);
        model.addAttribute("validationResult", validationResult);

        // Handle error navigation
        if (validationResult.getErrors().size() > 0) {
            // Validate error index
            if (errorIndex < 0) {
                errorIndex = 0;
            } else if (errorIndex >= validationResult.getErrors().size()) {
                errorIndex = validationResult.getErrors().size() - 1;
            }

            // Get current error
            ValidationError currentError = validationResult.getErrors().get(errorIndex);
            model.addAttribute("currentError", currentError);
            model.addAttribute("currentErrorIndex", errorIndex);

            // Find the corresponding SOB benefit for additional details
            SOBBenefit currentSOBBenefit = findSOBBenefitForError(sobData, currentError);
            model.addAttribute("currentSOBBenefit", currentSOBBenefit);

            log.info("Displaying error {} of {}: {}", errorIndex + 1, validationResult.getErrors().size(), 
                    currentError.getBenefitCategory());
        } else {
            model.addAttribute("currentError", null);
            model.addAttribute("currentErrorIndex", 0);
            model.addAttribute("currentSOBBenefit", null);
            log.info("No errors found. Displaying success state.");
        }

        return "validation-results";
    }

    private SOBBenefit findSOBBenefitForError(SOBData sobData, ValidationError error) {
        if (sobData.getBenefits() == null || error == null) {
            return null;
        }

        String benefitCategory = error.getBenefitCategory();
        log.debug("Looking for SOB benefit matching category: {}", benefitCategory);

        return sobData.getBenefits().stream()
                .filter(benefit -> {
                    // Try to match by benefit name first
                    if (benefit.getBenefitName() != null && 
                        benefit.getBenefitName().toLowerCase().contains(benefitCategory.toLowerCase())) {
                        return true;
                    }
                    // Fallback to PBP category match
                    if (benefit.getPbpCategory() != null && 
                        benefitCategory.toLowerCase().contains(benefit.getPbpCategory().toLowerCase())) {
                        return true;
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/validation/{validationId}")
    public String getValidationResults(@PathVariable String validationId, Model model) {
        // Implementation for retrieving stored validation results
        // For now, redirect to home
        return "redirect:/";
    }

    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<ValidationResult> validateAPI(@RequestParam("sobFile") MultipartFile sobFile,
                                                      @RequestParam("vendorMatrixFile") MultipartFile vendorMatrixFile,
                                                      @RequestParam(value = "sobType", required = false) SOBType sobType) {
        try {
            SOBData sobData = documentProcessorService.extractSOBData(sobFile);
            VendorMatrixData vendorMatrixData = documentProcessorService.extractVendorMatrixData(vendorMatrixFile);
            
            SOBType finalSOBType = sobType != null ? sobType : sobData.getSobType();
            ValidationResult result = validationService.validateWithSOBType(sobData, vendorMatrixData, finalSOBType);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
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
                    
        } catch (Exception e) {
            log.error("Export error", e);
            return ResponseEntity.internalServerError()
                .body(("Export failed: " + e.getMessage()).getBytes());
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

// DTO for export requests
class ExportRequest {
    private String validationId;
    private List<String> selectedErrorIds;
    private boolean highlightChanges;
    
    // Getters and setters
    public String getValidationId() { return validationId; }
    public void setValidationId(String validationId) { this.validationId = validationId; }
    public List<String> getSelectedErrorIds() { return selectedErrorIds; }
    public void setSelectedErrorIds(List<String> selectedErrorIds) { this.selectedErrorIds = selectedErrorIds; }
    public boolean isHighlightChanges() { return highlightChanges; }
    public void setHighlightChanges(boolean highlightChanges) { this.highlightChanges = highlightChanges; }
} 