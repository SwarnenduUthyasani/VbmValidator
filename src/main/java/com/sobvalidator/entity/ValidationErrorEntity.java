package com.sobvalidator.entity;

import java.time.LocalDateTime;

// JPA imports temporarily disabled for testing
// import jakarta.persistence.*;

// @Entity
// @Table(name = "validation_errors")
// Entity disabled for testing - no Lombok annotations
public class ValidationErrorEntity {
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // @Column(unique = true, nullable = false)
    private String errorId;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "validation_id", nullable = false)
    private ValidationEntity validation;
    
    // @Column(nullable = false)
    private String errorType;
    
    // @Column(nullable = false)
    private String severity;
    
    private String benefitCategory;
    private String fieldName;
    
    // @Lob
    private String sobValue;
    
    // @Lob
    private String vendorMatrixValue;
    
    // @Lob
    private String expectedValue;
    
    // @Lob
    private String description;
    
    // @Lob
    private String recommendation;
    
    private boolean selected;
    
    private LocalDateTime createdAt;
    
    // @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Basic getters and setters (minimal implementation for testing)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getErrorId() { return errorId; }
    public void setErrorId(String errorId) { this.errorId = errorId; }
    
    public ValidationEntity getValidation() { return validation; }
    public void setValidation(ValidationEntity validation) { this.validation = validation; }
    
    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
} 