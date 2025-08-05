package com.sobvalidator.model;

public enum SOBType {
    HIP_HMO("HIP HMO", "Health Insurance Plan - HMO"),
    GHI("GHI", "Group Health Incorporated");

    private final String displayName;
    private final String description;

    SOBType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // Manual selection only - no auto-detection
    public static SOBType detectFromPlanName(String planName) {
        // Always return null to force manual selection
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 