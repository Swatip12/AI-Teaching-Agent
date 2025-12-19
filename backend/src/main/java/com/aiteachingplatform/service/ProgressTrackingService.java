package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for tracking student progress and managing lesson unlocking
 */
@Service
@Transactional
public class ProgressTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProgressTrackingService.class);
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    /**
     * Start a lesson for a user
     */
    public Progress startLesson(User user, Lesson lesson) {
        Optional<Progress> existingProgress = progressRepository.findByUserAndLesson(user, lesson);
        
        if (existingProgress.isPresent()) {
            Progress progress = existingProgress.get();
            if (progress.getStatus() == Progress.ProgressStatus.NOT_STARTED) {
                progress.startLesson();
                return progressRepository.save(progress);
            }
            return progress;
        } else {
            Progress newProgress = new Progress(user, lesson);
            newProgress.startLesson();
            return progressRepository.save(newProgress);
        }
    }
    
    /**
     * Complete a lesson for a user with a score
     */
    public Progress completeLesson(User user, Lesson lesson, Integer score) {
        Optional<Progress> existingProgress = progressRepository.findByUserAndLesson(user, lesson);
        
        Progress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        } else {
            progress = new Progress(user, lesson);
        }
        
        progress.completeLesson(score);
        progress = progressRepository.save(progress);
        
        logger.info("User {} completed lesson {} with score {}", user.getId(), lesson.getId(), score);
        
        // Check for milestone achievements
        checkMilestoneAchievement(user, lesson.getSubject());
        
        return progress;
    }
    
    /**
     * Update progress percentage for a lesson
     */
    public Progress updateProgress(User user, Lesson lesson, Integer percentage) {
        Optional<Progress> existingProgress = progressRepository.findByUserAndLesson(user, lesson);
        
        Progress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        } else {
            progress = new Progress(user, lesson);
        }
        
        progress.updateProgress(percentage);
        return progressRepository.save(progress);
    }
    
    /**
     * Get available lessons for a user in a subject (considering prerequisites)
     */
    public List<Lesson> getAvailableLessons(User user, String subject) {
        List<Lesson> allLessonsInSubject = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
        List<Progress> userProgress = progressRepository.findByUserAndSubject(user, subject);
        
        return allLessonsInSubject.stream()
            .filter(lesson -> isLessonAvailable(lesson, userProgress))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a lesson is available based on prerequisites
     */
    private boolean isLessonAvailable(Lesson lesson, List<Progress> userProgress) {
        if (lesson.getPrerequisiteLessonIds() == null || lesson.getPrerequisiteLessonIds().isEmpty()) {
            return true; // No prerequisites
        }
        
        // Check if all prerequisites are completed
        long completedPrerequisites = userProgress.stream()
            .filter(p -> lesson.getPrerequisiteLessonIds().contains(p.getLesson().getId()))
            .filter(p -> p.getStatus() == Progress.ProgressStatus.COMPLETED)
            .count();
        
        return completedPrerequisites == lesson.getPrerequisiteLessonIds().size();
    }
    
    /**
     * Get user's progress in a specific subject
     */
    public List<Progress> getUserProgressInSubject(User user, String subject) {
        return progressRepository.findByUserAndSubject(user, subject);
    }
    
    /**
     * Calculate completion percentage for a user in a subject
     */
    public Double calculateSubjectCompletionPercentage(User user, String subject) {
        return progressRepository.calculateSubjectCompletionPercentage(user, subject);
    }
    
    /**
     * Get user's overall progress across all subjects
     */
    public List<Progress> getUserOverallProgress(User user) {
        return progressRepository.findByUserOrderByUpdatedAtDesc(user);
    }
    
    /**
     * Check for milestone achievements and trigger celebrations
     */
    public boolean checkMilestoneAchievement(User user, String subject) {
        long completedLessons = progressRepository.countCompletedLessonsInSubject(user, subject);
        long totalLessons = lessonRepository.countBySubject(subject);
        
        double completionRatio = (double) completedLessons / totalLessons;
        
        // Milestone thresholds: 25%, 50%, 75%, 100%
        boolean isMilestone = completionRatio >= 0.25 && 
                             (completionRatio == 0.25 || completionRatio == 0.5 || 
                              completionRatio == 0.75 || completionRatio == 1.0);
        
        if (isMilestone) {
            logger.info("Milestone achieved! User {} completed {}% of {} subject", 
                       user.getId(), (int)(completionRatio * 100), subject);
            // Here you would trigger celebration logic (notifications, badges, etc.)
            triggerMilestoneCelebration(user, subject, completionRatio);
        }
        
        return isMilestone;
    }
    
    /**
     * Trigger milestone celebration (placeholder for future implementation)
     */
    private void triggerMilestoneCelebration(User user, String subject, double completionRatio) {
        // This would integrate with notification service, achievement system, etc.
        logger.info("🎉 Celebrating milestone: User {} achieved {}% completion in {}", 
                   user.getId(), (int)(completionRatio * 100), subject);
    }
    
    /**
     * Get user's learning streak (consecutive days with progress)
     */
    public long getUserLearningStreak(User user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return progressRepository.calculateLearningStreak(user.getId(), thirtyDaysAgo);
    }
    
    /**
     * Get user's next recommended lesson
     */
    public Optional<Lesson> getNextRecommendedLesson(User user, String subject) {
        List<Lesson> availableLessons = getAvailableLessons(user, subject);
        List<Progress> userProgress = progressRepository.findByUserAndSubject(user, subject);
        
        // Find the first available lesson that hasn't been started
        return availableLessons.stream()
            .filter(lesson -> userProgress.stream()
                .noneMatch(p -> p.getLesson().getId().equals(lesson.getId())))
            .findFirst();
    }
    
    /**
     * Resume user's last active lesson
     */
    public Optional<Progress> getLastActiveLesson(User user) {
        List<Progress> inProgressLessons = progressRepository.findInProgressLessonsByUser(user);
        return inProgressLessons.isEmpty() ? Optional.empty() : Optional.of(inProgressLessons.get(0));
    }
    
    /**
     * Get progress analytics for a user
     */
    public ProgressAnalytics getProgressAnalytics(User user) {
        List<Progress> allProgress = progressRepository.findByUserOrderByUpdatedAtDesc(user);
        
        long totalLessonsStarted = allProgress.size();
        long totalLessonsCompleted = allProgress.stream()
            .mapToLong(p -> p.getStatus() == Progress.ProgressStatus.COMPLETED ? 1 : 0)
            .sum();
        
        double averageScore = allProgress.stream()
            .filter(p -> p.getScore() != null)
            .mapToInt(Progress::getScore)
            .average()
            .orElse(0.0);
        
        int totalTimeSpent = allProgress.stream()
            .mapToInt(p -> p.getTimeSpentMinutes() != null ? p.getTimeSpentMinutes() : 0)
            .sum();
        
        long learningStreak = getUserLearningStreak(user);
        
        return new ProgressAnalytics(totalLessonsStarted, totalLessonsCompleted, 
                                   averageScore, totalTimeSpent, learningStreak);
    }
    
    /**
     * Data class for progress analytics
     */
    public static class ProgressAnalytics {
        private final long totalLessonsStarted;
        private final long totalLessonsCompleted;
        private final double averageScore;
        private final int totalTimeSpentMinutes;
        private final long learningStreak;
        
        public ProgressAnalytics(long totalLessonsStarted, long totalLessonsCompleted, 
                               double averageScore, int totalTimeSpentMinutes, long learningStreak) {
            this.totalLessonsStarted = totalLessonsStarted;
            this.totalLessonsCompleted = totalLessonsCompleted;
            this.averageScore = averageScore;
            this.totalTimeSpentMinutes = totalTimeSpentMinutes;
            this.learningStreak = learningStreak;
        }
        
        // Getters
        public long getTotalLessonsStarted() { return totalLessonsStarted; }
        public long getTotalLessonsCompleted() { return totalLessonsCompleted; }
        public double getAverageScore() { return averageScore; }
        public int getTotalTimeSpentMinutes() { return totalTimeSpentMinutes; }
        public long getLearningStreak() { return learningStreak; }
        
        public double getCompletionRate() {
            return totalLessonsStarted > 0 ? (double) totalLessonsCompleted / totalLessonsStarted : 0.0;
        }
    }
}