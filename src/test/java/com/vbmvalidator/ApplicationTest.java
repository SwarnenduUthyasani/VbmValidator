package com.vbmvalidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vbmvalidator.controller.ValidationController;
import com.vbmvalidator.model.ErrorSeverity;
import com.vbmvalidator.model.ErrorType;
import com.vbmvalidator.model.SOBBenefit;
import com.vbmvalidator.model.SOBData;
import com.vbmvalidator.model.SOBType;
import com.vbmvalidator.model.ValidationError;
import com.vbmvalidator.model.ValidationResult;
import com.vbmvalidator.model.ValidationStatus;
import com.vbmvalidator.model.VendorMatrixData;
import com.vbmvalidator.service.ValidationService;
import com.vbmvalidator.service.impl.DocumentProcessorService;
import com.vbmvalidator.service.impl.ExcelProcessor;
import com.vbmvalidator.service.impl.ValidationServiceImpl;

import jakarta.servlet.http.HttpSession;

@DisplayName("VBM Validator Application Tests")
public class ApplicationTest {

    private MockMvc mockMvc;
    
    @Mock
    private DocumentProcessorService documentProcessorService;
    
    @Mock
    private ValidationService validationService;
    
    private ValidationController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ValidationController();
        // Inject mocks using reflection
        injectMock(controller, "documentProcessorService", documentProcessorService);
        injectMock(controller, "validationService", validationService);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    
    @Test
    @DisplayName("Test 1: File Upload - Both files provided")
    void testSuccessfulFileUpload() throws Exception {
        // Create mock files
        MockMultipartFile sobFile = new MockMultipartFile(
            "sobFile", "HIP_HMO_SOB.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "SOB content".getBytes()
        );
        
        MockMultipartFile vmFile = new MockMultipartFile(
            "vendorMatrixFile", "HIP_HMO_VM.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "VM content".getBytes()
        );
        
        // Mock SOB data
        SOBData mockSOBData = SOBData.builder()
            .planName("HIP HMO Plan")
            .sobType(SOBType.HIP_HMO)
            .productId("HIP-001")
            .benefits(createMockBenefits())
            .sourceFileName("HIP_HMO_SOB.xlsx")
            .uploadedAt("2025-01-01T10:00:00")
            .build();
            
        // Mock VM data
        VendorMatrixData mockVMData = createMockVendorMatrixData();
        
        // Mock validation result
        ValidationResult mockResult = createMockValidationResult();
        
        when(documentProcessorService.extractSOBData(any())).thenReturn(mockSOBData);
        when(documentProcessorService.extractVendorMatrixData(any())).thenReturn(mockVMData);
        when(validationService.validateWithSOBType(any(), any(), any())).thenReturn(mockResult);
        
        mockMvc.perform(multipart("/upload")
                .file(sobFile)
                .file(vmFile)
                .param("sobType", "HIP_HMO"))
            .andExpect(status().isOk())
            .andExpect(view().name("validation-results"));
    }
    
    @Test
    @DisplayName("Test 2: File Upload - Missing SOB file")
    void testMissingSOBFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "sobFile", "", "text/plain", new byte[0]
        );
        
        MockMultipartFile vmFile = new MockMultipartFile(
            "vendorMatrixFile", "VM.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "content".getBytes()
        );
        
        mockMvc.perform(multipart("/upload")
                .file(emptyFile)
                .file(vmFile))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("error", "Please select both SOB and Vendor Matrix files"));
    }
    
    @Test
    @DisplayName("Test 3: Validation - Cost Sharing Mismatch")
    void testCostSharingValidation() {
        // Create test data
        SOBBenefit sobBenefit = SOBBenefit.builder()
            .pbpCategory("1a - Inpatient Hospital")
            .benefitCategory("1a - Inpatient Hospital")
            .benefitName("Inpatient Hospital - Acute")
            .costSharing("$350 per day (days 1-5)")
            .notations("Prior authorization required")
            .paRequired(true)
            .paNotes("Prior authorization required for inpatient admission")
            .moopApplicable(true)
            .deductibleApplicable(false)
            .build();
            
        VendorMatrixData vmData = VendorMatrixData.builder()
            .benefitData(Map.of("1a - Inpatient Hospital", "$300 per day (days 1-5)"))
            .build();
            
        SOBData sobData = SOBData.builder()
            .benefits(List.of(sobBenefit))
            .sobType(SOBType.HIP_HMO)
            .build();
            
        // Test validation
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        ValidationResult result = validationService.validateWithSOBType(sobData, vmData, SOBType.HIP_HMO);
        
        assertNotNull(result);
        assertTrue(!result.getErrors().isEmpty(), "Should have validation errors");
        
        // Check for cost sharing mismatch
        boolean hasCostSharingError = result.getErrors().stream()
            .anyMatch(e -> e.getErrorType() == ErrorType.COST_SHARING_MISMATCH);
        assertTrue(hasCostSharingError, "Should detect cost sharing mismatch");
    }
    
    @Test
    @DisplayName("Test 4: Validation - Prior Authorization Check")
    void testPriorAuthorizationValidation() {
        SOBBenefit sobBenefit = SOBBenefit.builder()
            .pbpCategory("1a - Inpatient Hospital")
            .benefitCategory("1a - Inpatient Hospital")
            .costSharing("$350 per day")
            .paRequired(true)
            .paNotes("Prior authorization required for all admissions")
            .build();
            
        VendorMatrixData vmData = VendorMatrixData.builder()
            .benefitData(Map.of("1a - Inpatient Hospital", "$350 per day"))
            .build();
            
        SOBData sobData = SOBData.builder()
            .benefits(List.of(sobBenefit))
            .sobType(SOBType.HIP_HMO)
            .build();
            
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        ValidationResult result = validationService.validateWithSOBType(sobData, vmData, SOBType.HIP_HMO);
        
        // Check for PA mismatch
        boolean hasPAError = result.getErrors().stream()
            .anyMatch(e -> e.getErrorType() == ErrorType.PRIOR_AUTH_MISMATCH);
        assertTrue(hasPAError, "Should detect missing prior authorization in VM");
    }
    
    @Test
    @DisplayName("Test 5: Validation - MOOP Not Applicable Check")
    void testMOOPValidation() {
        SOBBenefit sobBenefit = SOBBenefit.builder()
            .pbpCategory("Preventive Care")
            .benefitCategory("Preventive Care")
            .costSharing("$0 copay")
            .moopApplicable(false)
            .build();
            
        VendorMatrixData vmData = VendorMatrixData.builder()
            .benefitData(Map.of("Preventive Care", "$0 copay"))
            .build();
            
        SOBData sobData = SOBData.builder()
            .benefits(List.of(sobBenefit))
            .sobType(SOBType.HIP_HMO)
            .build();
            
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        ValidationResult result = validationService.validateWithSOBType(sobData, vmData, SOBType.HIP_HMO);
        
        // Check for MOOP statement missing
        boolean hasMOOPError = result.getErrors().stream()
            .anyMatch(e -> e.getErrorType() == ErrorType.MOOP_STATEMENT_MISSING);
        assertTrue(hasMOOPError, "Should detect missing MOOP statement");
    }
    
    @Test
    @DisplayName("Test 6: SOB Type - Manual Selection Only")
    void testSOBTypeValues() {
        // Test available enum values - auto-detection removed
        assertEquals("HIP HMO", SOBType.HIP_HMO.getDisplayName());
        assertEquals("GHI", SOBType.GHI.getDisplayName());
        assertEquals("Health Insurance Plan - HMO", SOBType.HIP_HMO.getDescription());
        assertEquals("Group Health Incorporated", SOBType.GHI.getDescription());
    }
    
    @Test
    @DisplayName("Test 7: Excel Processor - File Format Check")
    void testExcelProcessorFileFormat() {
        ExcelProcessor processor = new ExcelProcessor();
        
        MockMultipartFile excelFile = new MockMultipartFile(
            "test", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "content".getBytes()
        );
        assertTrue(processor.canProcess(excelFile));
        
        MockMultipartFile pdfFile = new MockMultipartFile(
            "test", "test.pdf", "application/pdf", "content".getBytes()
        );
        assertFalse(processor.canProcess(pdfFile));
    }
    
    @Test
    @DisplayName("Test 8: VBM Guidelines Validation")
    void testVBMGuidelinesValidation() {
        // Test podiatry medicare 6 limit rule
        SOBBenefit sobBenefit = SOBBenefit.builder()
            .pbpCategory("Podiatry Medicare")
            .benefitCategory("Podiatry Medicare")
            .costSharing("$20 copay")
            .build();
            
        VendorMatrixData vmData = VendorMatrixData.builder()
            .benefitData(Map.of("Podiatry Medicare", "$20 copay (4 visits)"))
            .build();
            
        SOBData sobData = SOBData.builder()
            .benefits(List.of(sobBenefit))
            .sobType(SOBType.HIP_HMO)
            .build();
            
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        ValidationResult result = validationService.validateWithSOBType(sobData, vmData, SOBType.HIP_HMO);
        
        // Should have guideline violation for not having 6 limit
        boolean hasGuidelineError = result.getErrors().stream()
            .anyMatch(e -> e.getErrorType() == ErrorType.GUIDELINE_VIOLATION);
        assertTrue(hasGuidelineError, "Should detect VBM guideline violation");
    }
    
    @Test
    @DisplayName("Test 9: Complete Validation Flow")
    void testCompleteValidationFlow() throws Exception {
        // This tests the complete flow from upload to validation results
        Model model = mock(Model.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        HttpSession session = mock(HttpSession.class);
        
        MockMultipartFile sobFile = new MockMultipartFile(
            "sobFile", "SOB.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "content".getBytes()
        );
        
        MockMultipartFile vmFile = new MockMultipartFile(
            "vendorMatrixFile", "VM.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "content".getBytes()
        );
        
        // Setup complete mock data
        SOBData sobData = createCompleteMockSOBData();
        VendorMatrixData vmData = createMockVendorMatrixData();
        ValidationResult validationResult = createMockValidationResult();
        
        when(documentProcessorService.extractSOBData(any())).thenReturn(sobData);
        when(documentProcessorService.extractVendorMatrixData(any())).thenReturn(vmData);
        when(validationService.validateWithSOBType(any(), any(), any())).thenReturn(validationResult);

        String result = controller.uploadFiles(
            sobFile,
            vmFile,
            SOBType.HIP_HMO.name(), // Pass as String, not enum
            redirectAttributes,
            model,
            session
        );

        assertEquals("validation-results", result);
        verify(model).addAttribute(eq("validationResult"), any(ValidationResult.class));
    }
    
    // Helper methods
    private void injectMock(Object target, String fieldName, Object mock) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, mock);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }
    }
    
    private List<SOBBenefit> createMockBenefits() {
        return Arrays.asList(
            SOBBenefit.builder()
                .pbpCategory("1a - Inpatient Hospital")
                .benefitCategory("1a - Inpatient Hospital")
                .benefitName("Inpatient Hospital - Acute")
                .costSharing("$350 per day (days 1-5)")
                .paRequired(true)
                .moopApplicable(true)
                .build(),
            SOBBenefit.builder()
                .pbpCategory("7a - PCP")
                .benefitCategory("7a - PCP")
                .benefitName("Primary Care Physician")
                .costSharing("$0 copay")
                .paRequired(false)
                .moopApplicable(false)
                .build()
        );
    }
    
    private VendorMatrixData createMockVendorMatrixData() {
        Map<String, String> benefitData = new HashMap<>();
        benefitData.put("1a - Inpatient Hospital", "$350 per day (days 1-5)");
        benefitData.put("7a - PCP", "$0 copay");
        
        return VendorMatrixData.builder()
            .productName("HIP HMO Gold Plus")
            .productId("HIP-001")
            .effectiveDate("2025-01-01")
            .benefitData(benefitData)
            .sourceFileName("HIP_HMO_VM.xlsx")
            .uploadedAt("2025-01-01T10:00:00")
            .build();
    }
    
    private ValidationResult createMockValidationResult() {
        List<ValidationError> errors = Arrays.asList(
            ValidationError.builder()
                .errorId("ERR-001")
                .errorType(ErrorType.COST_SHARING_MISMATCH)
                .severity(ErrorSeverity.HIGH)
                .benefitCategory("2 - SNF")
                .fieldName("Cost Sharing")
                .sobValue("$0 per day (days 1-20)")
                .vendorMatrixValue("$50 per day (days 1-20)")
                .description("Cost sharing mismatch")
                .build()
        );
        
        return ValidationResult.builder()
            .validationId("VAL-001")
            .status(ValidationStatus.FAILED_WITH_ERRORS)
            .errors(errors)
            .totalErrors(1)
            .totalWarnings(0)
            .validatedAt(java.time.LocalDateTime.now())
            .build();
    }
    
    private SOBData createCompleteMockSOBData() {
        return SOBData.builder()
            .planName("HIP HMO Gold Plus")
            .productId("HIP-001")
            .effectiveDate("2025-01-01")
            .moop("$3,400")
            .sobType(SOBType.HIP_HMO)
            .benefits(createMockBenefits())
            .sourceFileName("SOB.xlsx")
            .uploadedAt("2025-01-01T10:00:00")
            .build();
    }
} 