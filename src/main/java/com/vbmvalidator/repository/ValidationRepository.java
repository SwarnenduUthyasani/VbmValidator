package com.vbmvalidator.repository;

import com.vbmvalidator.entity.ValidationEntity;
// JPA imports temporarily disabled for testing
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// @Repository
// Repository disabled for testing - no JPA functionality
public interface ValidationRepository /* extends JpaRepository<ValidationEntity, Long> */ {
    
    // JPA methods temporarily disabled for testing
    // Optional<ValidationEntity> findByValidationId(String validationId);
    
    // List<ValidationEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // List<ValidationEntity> findBySobTypeOrderByCreatedAtDesc(String sobType);
    
    // @Query("SELECT v FROM ValidationEntity v WHERE v.sobFileName = :fileName OR v.vendorMatrixFileName = :fileName")
    // List<ValidationEntity> findByFileName(@Param("fileName") String fileName);
    
    // @Query("SELECT COUNT(v) FROM ValidationEntity v WHERE v.status = :status")
    // long countByStatus(@Param("status") String status);
    
    // @Query("SELECT v FROM ValidationEntity v ORDER BY v.createdAt DESC")
    // List<ValidationEntity> findAllOrderByCreatedAtDesc();
} 
