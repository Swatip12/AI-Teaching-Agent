package com.aiteachingplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Progress entity representing student progress through lessons
 */
@Entity
@Table(name = "progress", 
    indexes = {
        @Index(name = "idx_progress_user", columnList = "user_id"),
        @Index(name = "idx_progress_lesson", columnList = "lesson_id"),
        @Index(name = "idx_progress_user_lesson", columnList = "user_id, lesson_id"),
        @Index(name = "idx_progress_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_lesson", columnNames = {"user_id", "lesson_id"})
    }
)
public class Progress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @NotNull
    private Lesson lesson;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    
    @Min(0)
    @Max(100)
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;
    
    @Min(0)
    @Max(100)
    @Column(name = "score")
    private Integer score;
    
    @Column(name = "attempts_count")
    private Integer attemptsCount = 0;
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_checkpoint_passed")
    private Integer lastCheckpointPassed;
    
    @Column(name = "practice_questions_completed")
    private Integer practiceQuestionsCompleted = 0;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "current_step")
    private String currentStep;
    
    // Constructors
    public Progress() {}
    
    public Progress(User user, Lesson lesson) {
        this.user = user;
        this.lesson = lesson;
        this.status = ProgressStatus.NOT_STARTED;
        this.completionPercentage = 0;
        this.attemptsCount = 0;
        this.timeSpentMinutes = 0;
        this.practiceQuestionsCompleted = 0;
    }
    
    // Business methods
    public void startLesson() {
        if (this.status == ProgressStatus.NOT_STARTED) {
            this.status = ProgressStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
            this.attemptsCount = 1;
        }
    }
    
    public void completeLesson(Integer finalScore) {
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.completionPercentage = 100;
        this.score = finalScore;
    }
    
    public void updateProgress(Integer percentage) {
        this.completionPercentage = Math.min(100, Math.max(0, percentage));
        if (this.status == ProgressStatus.NOT_STARTED) {
            startLesson();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Lesson getLesson() {
        return lesson;
    }
    
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
    
    public ProgressStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProgressStatus status) {
        this.status = status;
    }
    
    public Integer getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public Integer getAttemptsCount() {
        return attemptsCount;
    }
    
    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }
    
    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }
    
    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getLastCheckpointPassed() {
        return lastCheckpointPassed;
    }
    
    public void setLastCheckpointPassed(Integer lastCheckpointPassed) {
        this.lastCheckpointPassed = lastCheckpointPassed;
    }
    
    public Integer getPracticeQuestionsCompleted() {
        return practiceQuestionsCompleted;
    }
    
    public void setPracticeQuestionsCompleted(Integer practiceQuestionsCompleted) {
        this.practiceQuestionsCompleted = practiceQuestionsCompleted;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }
    
    public enum ProgressStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED
    }
    
    @Override
    public String toString() {
        return "Progress{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", lessonId=" + (lesson != null ? lesson.getId() : null) +
                ", status=" + status +
                ", completionPercentage=" + completionPercentage +
                ", score=" + score +
                '}';
    }
}