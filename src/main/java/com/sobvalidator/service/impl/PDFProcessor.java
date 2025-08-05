package com.sobvalidator.service.impl;

import com.sobvalidator.model.*;
import com.sobvalidator.service.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PDFProcessor implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PDFProcessor.class);

    @Override
    public boolean canProcess(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public SOBData extractSOBData(MultipartFile file) throws IOException {
        log.info("Processing SOB PDF file: {}", file.getOriginalFilename());
        
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            
            return parseSOBFromPDFText(text, file.getOriginalFilename());
        }
    }

    @Override
    public VendorMatrixData extractVendorMatrixData(MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("PDF format is not supported for Vendor Matrix files. Please use Excel format.");
    }

    private SOBData parseSOBFromPDFText(String text, String fileName) {
        SOBData.SOBDataBuilder builder = SOBData.builder();
        List<SOBBenefit> benefits = new ArrayList<>();
        Map<String, String> rawData = new HashMap<>();
        
        // Split text into lines for processing
        String[] lines = text.split("\\r?\\n");
        
        // Parse plan metadata
        builder = parseMetadataFromPDF(lines, builder, rawData);
        
        // Parse benefits data
        benefits = parseBenefitsFromPDF(lines);
        
        // Detect SOB type from plan name
        String planName = builder.build().getPlanName();
        SOBType sobType = SOBType.detectFromPlanName(planName);
        
        return builder
                .sobType(sobType)
                .benefits(benefits)
                .rawData(rawData)
                .sourceFileName(fileName)
                .uploadedAt(LocalDateTime.now().toString())
                .build();
    }

    private SOBData.SOBDataBuilder parseMetadataFromPDF(String[] lines, SOBData.SOBDataBuilder builder, Map<String, String> rawData) {
        
        // Patterns for extracting metadata
        Pattern planNamePattern = Pattern.compile("Plan Name\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern effectiveDatePattern = Pattern.compile("Effective Date\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern productIdPattern = Pattern.compile("Product ID\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern contractPattern = Pattern.compile("(?:CMS )?Contract(?:\\s+Number)?(?:/PBP)?\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern serviceAreaPattern = Pattern.compile("Service Area\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern formularyPattern = Pattern.compile("Formulary\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern moopPattern = Pattern.compile("Maximum Out of Pocket\\s*(?:cost)?\\s*\\(MOOP\\)\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Pattern premiumPattern = Pattern.compile("(?:Monthly )?Premium\\s*:?\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        
        // Join all lines for pattern matching
        String fullText = String.join("\n", lines);
        
        // Extract metadata using patterns
        extractMetadataField(fullText, planNamePattern, "Plan Name", builder::planName, rawData);
        extractMetadataField(fullText, effectiveDatePattern, "Effective Date", builder::effectiveDate, rawData);
        extractMetadataField(fullText, productIdPattern, "Product ID", builder::productId, rawData);
        extractMetadataField(fullText, contractPattern, "Contract Number", builder::contractNumber, rawData);
        extractMetadataField(fullText, serviceAreaPattern, "Service Area", builder::serviceArea, rawData);
        extractMetadataField(fullText, formularyPattern, "Formulary", builder::formulary, rawData);
        extractMetadataField(fullText, moopPattern, "MOOP", builder::moop, rawData);
        extractMetadataField(fullText, premiumPattern, "Premium", builder::monthlyPremium, rawData);
        
        return builder;
    }

    private void extractMetadataField(String text, Pattern pattern, String fieldName, 
                                    java.util.function.Consumer<String> setter, Map<String, String> rawData) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            setter.accept(value);
            rawData.put(fieldName, value);
        }
    }

    private List<SOBBenefit> parseBenefitsFromPDF(String[] lines) {
        List<SOBBenefit> benefits = new ArrayList<>();
        
        // Find the start of benefits section
        int benefitsStartIndex = findBenefitsSection(lines);
        if (benefitsStartIndex == -1) {
            log.warn("Could not find benefits section in PDF");
            return benefits;
        }
        
        // Parse benefits starting from the found index
        for (int i = benefitsStartIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // Skip empty lines and headers
            if (StringUtils.isBlank(line) || isHeaderLine(line)) {
                continue;
            }
            
            // Try to parse benefit from this line
            SOBBenefit benefit = parseBenefitFromLine(line, lines, i);
            if (benefit != null) {
                benefits.add(benefit);
            }
        }
        
        return benefits;
    }

    private int findBenefitsSection(String[] lines) {
        // Look for common patterns that indicate the start of benefits
        Pattern benefitSectionPattern = Pattern.compile("(?:PBP\\s+Category|Benefit|Coverage|Service)", Pattern.CASE_INSENSITIVE);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (benefitSectionPattern.matcher(line).find() && 
                (line.contains("Cost") || line.contains("Sharing") || line.contains("Network"))) {
                return i + 1; // Return the line after the header
            }
        }
        
        // If not found, try to find by looking for PBP categories
        Pattern pbpPattern = Pattern.compile("^\\s*(\\d+[a-z]?)\\s+", Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < lines.length; i++) {
            if (pbpPattern.matcher(lines[i]).find()) {
                return i;
            }
        }
        
        return -1;
    }

    private boolean isHeaderLine(String line) {
        String upperLine = line.toUpperCase();
        return upperLine.contains("PBP CATEGORY") || 
               upperLine.contains("BENEFIT") || 
               upperLine.contains("COST SHARING") ||
               upperLine.contains("MEMBER COST") ||
               upperLine.contains("NETWORK") ||
               upperLine.contains("OUT OF NETWORK");
    }

    private SOBBenefit parseBenefitFromLine(String line, String[] allLines, int currentIndex) {
        // Pattern to match PBP category at the start of line
        Pattern pbpPattern = Pattern.compile("^\\s*(\\d+[a-z]?)\\s+(.+)");
        Matcher matcher = pbpPattern.matcher(line);
        
        if (!matcher.find()) {
            return null;
        }
        
        String pbpCategory = matcher.group(1).trim();
        String restOfLine = matcher.group(2).trim();
        
        // Extract benefit name and cost sharing
        String[] parts = restOfLine.split("\\s{2,}|\\t"); // Split on multiple spaces or tabs
        
        if (parts.length < 2) {
            return null;
        }
        
        String benefitName = parts[0].trim();
        String costSharing = parts.length > 1 ? parts[1].trim() : "";
        String notations = parts.length > 2 ? String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)) : "";
        
        // Look for additional information in following lines
        StringBuilder additionalInfo = new StringBuilder();
        for (int i = currentIndex + 1; i < Math.min(currentIndex + 3, allLines.length); i++) {
            String nextLine = allLines[i].trim();
            if (StringUtils.isBlank(nextLine) || nextLine.matches("^\\d+[a-z]?\\s+.*")) {
                break; // Next benefit found or empty line
            }
            additionalInfo.append(" ").append(nextLine);
        }
        
        if (additionalInfo.length() > 0) {
            notations += additionalInfo.toString();
        }
        
        // Parse boolean flags from notations
        Boolean supplementalBenefit = extractBooleanFlag(notations, "supplemental");
        Boolean paRequired = extractBooleanFlag(notations, "prior auth|authorization|PA required");
        Boolean referralRequired = extractBooleanFlag(notations, "referral");
        Boolean moopApplicable = extractBooleanFlag(notations, "MOOP");
        Boolean deductibleApplicable = extractBooleanFlag(notations, "deductible");
        
        return SOBBenefit.builder()
                .pbpCategory(pbpCategory)
                .benefitName(benefitName)
                .costSharing(costSharing)
                .notations(notations.trim())
                .supplementalBenefit(supplementalBenefit)
                .paRequired(paRequired)
                .referralRequired(referralRequired)
                .moopApplicable(moopApplicable)
                .deductibleApplicable(deductibleApplicable)
                .rawText(line)
                .build();
    }

    private Boolean extractBooleanFlag(String text, String pattern) {
        if (StringUtils.isBlank(text)) return null;
        
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        if (regex.matcher(text).find()) {
            // Check for negative indicators
            if (text.toLowerCase().contains("not required") || 
                text.toLowerCase().contains("no " + pattern.toLowerCase()) ||
                text.toLowerCase().contains("not applicable")) {
                return false;
            }
            return true;
        }
        return null;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".pdf"};
    }
} 