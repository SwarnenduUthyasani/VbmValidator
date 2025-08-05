package com.sobvalidator.entity;

import java.time.LocalDateTime;
import java.util.List;

// JPA imports temporarily disabled for testing
// import jakarta.persistence.*;

// @Entity
// @Table(name = "validations")
// Entity disabled for testing - no Lombok annotations
public class ValidationEntity {
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // @Column(unique = true, nullable = false)
    private String validationId;
    
    // @Column(nullable = false)
    private String sobType;
    
    // @Column(nullable = false)
    private String sobFileName;
    
    // @Column(nullable = false)
    private String vendorMatrixFileName;
    
    // @Column(nullable = false)
    private LocalDateTime validatedAt;
    
    // @Column(nullable = false)
    private String status;
    
    private int totalErrors;
    private int totalWarnings;
    
    // @Lob
    private String validationSummary;
    
    // @Lob
    private String rawResults;
    
    // Bidirectional relationship - mapped by validation field in ValidationErrorEntity
    // @OneToMany(mappedBy = "validation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ValidationErrorEntity> errors;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Lifecycle callbacks
    // @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    // @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Basic getters and setters (minimal implementation for testing)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getValidationId() { return validationId; }
    public void setValidationId(String validationId) { this.validationId = validationId; }
    
    public String getSobType() { return sobType; }
    public void setSobType(String sobType) { this.sobType = sobType; }
    
    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
    
    public int getTotalWarnings() { return totalWarnings; }
    public void setTotalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; }
} 