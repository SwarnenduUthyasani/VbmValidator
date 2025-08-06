package com.vbmvalidator.controller;

import java.util.List;

// DTO for export requests
public class ExportRequest {
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
