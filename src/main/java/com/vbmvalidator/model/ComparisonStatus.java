package com.vbmvalidator.model;

public enum ComparisonStatus {
    MATCH,           // SOB and Vendor Matrix match
    MISMATCH,        // Values don't match
    SOB_MISSING,     // Present in Vendor Matrix but not SOB
    VM_MISSING,      // Present in SOB but not Vendor Matrix
    PARTIAL_MATCH    // Some aspects match, others don't
} 
