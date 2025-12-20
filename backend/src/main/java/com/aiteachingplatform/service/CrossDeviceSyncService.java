package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.UserPreferences;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling cross-device synchronization and reliability
 * Implements Requirements 8.1, 8.2, 8.3, 8.4
 */
@Service
public class CrossDeviceSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossDeviceSyncService.class);
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Synchronize user progress across devices
     * Requirement 8.2: Progress synchronization across devices
     */
    @Transactional
    public List<Progress> syncUserProgress(Long userId, String deviceType) {
        try {
            logger.info("Syncing progress for user {} on device type {}", userId, deviceType);
            
            // Get all user progress ordered by most recent
            List<Progress> userProgress = progressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
            
            // Update device type for current session if needed
            if (deviceType != null && !userProgress.isEmpty()) {
                Progress latestProgress = userProgress.get(0);
                if (latestProgress.getDeviceType() == null || !latestProgress.getDeviceType().equals(deviceType)) {
                    latestProgress.setDeviceType(deviceType);
                    progressRepository.save(latestProgress);
                }
            }
            
            logger.info("Successfully synced {} progress records for user {}", userProgress.size(), userId);
            return userProgress;
            
        } catch (Exception e) {
            logger.error("Error syncing progress for user {} on device {}: {}", userId, deviceType, e.getMessage());
            throw new RuntimeException("Failed to sync user progress", e);
        }
    }
    
    /**
     * Get user's current learning state for device continuity
     * Requirement 8.1: Consistent functionality across devices
     */
    @Transactional(readOnly = true)
    public DeviceContinuityState getUserContinuityState(Long userId) {
        try {
            logger.info("Getting continuity state for user {}", userId);
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }
            
            User user = userOpt.get();
            List<Progress> recentProgress = progressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
            
            DeviceContinuityState state = new DeviceContinuityState();
            state.setUserId(userId);
            state.setUserPreferences(user.getPreferences());
            state.setRecentProgress(recentProgress);
            state.setLastSyncTime(LocalDateTime.now());
            
            // Find current active lesson
            Optional<Progress> activeProgress = recentProgress.stream()
                .filter(p -> p.getStatus() == Progress.ProgressStatus.IN_PROGRESS)
                .findFirst();
            
            if (activeProgress.isPresent()) {
                state.setCurrentLessonId(activeProgress.get().getLesson().getId());
                state.setCurrentStep(activeProgress.get().getCurrentStep());
                state.setCompletionPercentage(activeProgress.get().getCompletionPercentage());
            }
            
            logger.info("Successfully retrieved continuity state for user {}", userId);
            return state;
            
        } catch (Exception e) {
            logger.error("Error getting continuity state for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user continuity state", e);
        }
    }
    
    /**
     * Update progress with device-specific information
     * Requirement 8.2: Progress synchronization
     */
    @Transactional
    public Progress updateProgressWithDeviceInfo(Long progressId, String deviceType, String currentStep) {
        try {
            logger.info("Updating progress {} with device info: type={}, step={}", progressId, deviceType, currentStep);
            
            Optional<Progress> progressOpt = progressRepository.findById(progressId);
            if (progressOpt.isEmpty()) {
                throw new RuntimeException("Progress not found: " + progressId);
            }
            
            Progress progress = progressOpt.get();
            progress.setDeviceType(deviceType);
            progress.setCurrentStep(currentStep);
            
            Progress savedProgress = progressRepository.save(progress);
            logger.info("Successfully updated progress {} with device info", progressId);
            
            return savedProgress;
            
        } catch (Exception e) {
            logger.error("Error updating progress {} with device info: {}", progressId, e.getMessage());
            throw new RuntimeException("Failed to update progress with device info", e);
        }
    }
    
    /**
     * Handle network error recovery by providing cached/offline data
     * Requirement 8.3: Network error handling
     */
    @Transactional(readOnly = true)
    public OfflineDataPackage getOfflineDataPackage(Long userId) {
        try {
            logger.info("Preparing offline data package for user {}", userId);
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }
            
            User user = userOpt.get();
            List<Progress> userProgress = progressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
            
            OfflineDataPackage offlinePackage = new OfflineDataPackage();
            offlinePackage.setUserId(userId);
            offlinePackage.setUserPreferences(user.getPreferences());
            offlinePackage.setProgressData(userProgress);
            offlinePackage.setPackageTimestamp(LocalDateTime.now());
            
            // Include essential lesson data for offline access
            userProgress.forEach(progress -> {
                if (progress.getLesson() != null) {
                    offlinePackage.addLessonSummary(
                        progress.getLesson().getId(),
                        progress.getLesson().getTitle(),
                        progress.getLesson().getSubject(),
                        progress.getCompletionPercentage()
                    );
                }
            });
            
            logger.info("Successfully prepared offline data package for user {} with {} progress records", 
                       userId, userProgress.size());
            return offlinePackage;
            
        } catch (Exception e) {
            logger.error("Error preparing offline data package for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to prepare offline data package", e);
        }
    }
    
    /**
     * Validate data for responsive design compatibility
     * Requirement 8.4: Responsive design support
     */
    public boolean validateDataForResponsiveDesign(Progress progress) {
        try {
            // Check if data is suitable for mobile display
            if (progress.getLesson() != null) {
                String title = progress.getLesson().getTitle();
                if (title != null && title.length() > 200) {
                    logger.warn("Lesson title too long for mobile display: {} characters", title.length());
                    return false;
                }
                
                String content = progress.getLesson().getContent();
                if (content != null && content.isEmpty()) {
                    logger.warn("Lesson content is empty, not suitable for display");
                    return false;
                }
            }
            
            // Validate essential fields are present
            if (progress.getStatus() == null || progress.getCompletionPercentage() == null) {
                logger.warn("Essential progress fields missing for responsive display");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating data for responsive design: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Data class for device continuity state
     */
    public static class DeviceContinuityState {
        private Long userId;
        private UserPreferences userPreferences;
        private List<Progress> recentProgress;
        private Long currentLessonId;
        private String currentStep;
        private Integer completionPercentage;
        private LocalDateTime lastSyncTime;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public UserPreferences getUserPreferences() { return userPreferences; }
        public void setUserPreferences(UserPreferences userPreferences) { this.userPreferences = userPreferences; }
        
        public List<Progress> getRecentProgress() { return recentProgress; }
        public void setRecentProgress(List<Progress> recentProgress) { this.recentProgress = recentProgress; }
        
        public Long getCurrentLessonId() { return currentLessonId; }
        public void setCurrentLessonId(Long currentLessonId) { this.currentLessonId = currentLessonId; }
        
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        
        public Integer getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
        
        public LocalDateTime getLastSyncTime() { return lastSyncTime; }
        public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    }
    
    /**
     * Data class for offline data package
     */
    public static class OfflineDataPackage {
        private Long userId;
        private UserPreferences userPreferences;
        private List<Progress> progressData;
        private LocalDateTime packageTimestamp;
        private java.util.Map<Long, LessonSummary> lessonSummaries = new java.util.HashMap<>();
        
        public void addLessonSummary(Long lessonId, String title, String subject, Integer completionPercentage) {
            lessonSummaries.put(lessonId, new LessonSummary(lessonId, title, subject, completionPercentage));
        }
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public UserPreferences getUserPreferences() { return userPreferences; }
        public void setUserPreferences(UserPreferences userPreferences) { this.userPreferences = userPreferences; }
        
        public List<Progress> getProgressData() { return progressData; }
        public void setProgressData(List<Progress> progressData) { this.progressData = progressData; }
        
        public LocalDateTime getPackageTimestamp() { return packageTimestamp; }
        public void setPackageTimestamp(LocalDateTime packageTimestamp) { this.packageTimestamp = packageTimestamp; }
        
        public java.util.Map<Long, LessonSummary> getLessonSummaries() { return lessonSummaries; }
        public void setLessonSummaries(java.util.Map<Long, LessonSummary> lessonSummaries) { this.lessonSummaries = lessonSummaries; }
        
        public static class LessonSummary {
            private Long lessonId;
            private String title;
            private String subject;
            private Integer completionPercentage;
            
            public LessonSummary(Long lessonId, String title, String subject, Integer completionPercentage) {
                this.lessonId = lessonId;
                this.title = title;
                this.subject = subject;
                this.completionPercentage = completionPercentage;
            }
            
            // Getters and setters
            public Long getLessonId() { return lessonId; }
            public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
            
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            
            public String getSubject() { return subject; }
            public void setSubject(String subject) { this.subject = subject; }
            
            public Integer getCompletionPercentage() { return completionPercentage; }
            public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
        }
    }
}