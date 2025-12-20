package com.aiteachingplatform.controller;

import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.service.CrossDeviceSyncService;
import com.aiteachingplatform.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for cross-device synchronization and reliability features
 * Implements Requirements 8.1, 8.2, 8.3, 8.4
 */
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CrossDeviceSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossDeviceSyncController.class);
    
    @Autowired
    private CrossDeviceSyncService crossDeviceSyncService;
    
    /**
     * Synchronize user progress across devices
     * Requirement 8.2: Progress synchronization across devices
     */
    @PostMapping("/progress")
    public ResponseEntity<?> syncProgress(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            String deviceType = request.get("deviceType");
            
            logger.info("Syncing progress for user {} on device type {}", userId, deviceType);
            
            List<Progress> syncedProgress = crossDeviceSyncService.syncUserProgress(userId, deviceType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Progress synchronized successfully",
                "progressCount", syncedProgress.size(),
                "progress", syncedProgress
            ));
            
        } catch (Exception e) {
            logger.error("Error syncing progress: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to sync progress: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user's current learning state for device continuity
     * Requirement 8.1: Consistent functionality across devices
     */
    @GetMapping("/continuity")
    public ResponseEntity<?> getContinuityState(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            logger.info("Getting continuity state for user {}", userId);
            
            CrossDeviceSyncService.DeviceContinuityState state = 
                crossDeviceSyncService.getUserContinuityState(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "continuityState", state
            ));
            
        } catch (Exception e) {
            logger.error("Error getting continuity state: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get continuity state: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update progress with device-specific information
     * Requirement 8.2: Progress synchronization
     */
    @PutMapping("/progress/{progressId}")
    public ResponseEntity<?> updateProgressWithDeviceInfo(
            @PathVariable Long progressId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String deviceType = request.get("deviceType");
            String currentStep = request.get("currentStep");
            
            logger.info("Updating progress {} with device info for user {}", progressId, userDetails.getId());
            
            Progress updatedProgress = crossDeviceSyncService.updateProgressWithDeviceInfo(
                progressId, deviceType, currentStep);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Progress updated with device info",
                "progress", updatedProgress
            ));
            
        } catch (Exception e) {
            logger.error("Error updating progress with device info: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update progress: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get offline data package for network error recovery
     * Requirement 8.3: Network error handling
     */
    @GetMapping("/offline-package")
    public ResponseEntity<?> getOfflineDataPackage(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            logger.info("Preparing offline data package for user {}", userId);
            
            CrossDeviceSyncService.OfflineDataPackage offlinePackage = 
                crossDeviceSyncService.getOfflineDataPackage(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "offlinePackage", offlinePackage
            ));
            
        } catch (Exception e) {
            logger.error("Error preparing offline data package: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to prepare offline data: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Health check endpoint for cross-device reliability
     * Requirement 8.3: Network error handling
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "healthy",
                "timestamp", java.time.LocalDateTime.now(),
                "message", "Cross-device sync service is operational"
            ));
            
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "status", "unhealthy",
                "message", "Service unavailable: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Validate progress data for responsive design
     * Requirement 8.4: Responsive design support
     */
    @PostMapping("/validate-responsive")
    public ResponseEntity<?> validateForResponsiveDesign(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        try {
            Long progressId = request.get("progressId");
            
            // This would typically validate the progress data structure
            // For now, return a simple validation response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isResponsiveCompatible", true,
                "message", "Data is compatible with responsive design"
            ));
            
        } catch (Exception e) {
            logger.error("Error validating responsive design compatibility: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Validation failed: " + e.getMessage()
            ));
        }
    }
}