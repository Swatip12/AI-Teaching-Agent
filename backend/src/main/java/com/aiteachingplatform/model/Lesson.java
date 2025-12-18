package com.aiteachingplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lesson entity representing individual learning units
 */
@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_subject", columnList = "subject"),
    @Index(name = "idx_lesson_sequence", columnList = "subject, sequence_order"),
    @Index(name = "idx_lesson_difficulty", columnList = "difficulty")
})
public class Lesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String subject;
    
    @NotNull
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String objectives;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.BEGINNER;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @ElementCollection
    @CollectionTable(name = "lesson_prerequisites", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "prerequisite_lesson_id")
    private List<Long> prerequisiteLessonIds = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CheckpointQuestion> checkpointQuestions = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PracticeQuestion> practiceQuestions = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Progress> progressList = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AIConversation> conversations = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Lesson() {}
    
    public Lesson(String title, String subject, Integer sequenceOrder, String content) {
        this.title = title;
        this.subject = subject;
        this.sequenceOrder = sequenceOrder;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public Integer getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getObjectives() {
        return objectives;
    }
    
    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }
    
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }
    
    public List<Long> getPrerequisiteLessonIds() {
        return prerequisiteLessonIds;
    }
    
    public void setPrerequisiteLessonIds(List<Long> prerequisiteLessonIds) {
        this.prerequisiteLessonIds = prerequisiteLessonIds;
    }
    
    public List<CheckpointQuestion> getCheckpointQuestions() {
        return checkpointQuestions;
    }
    
    public void setCheckpointQuestions(List<CheckpointQuestion> checkpointQuestions) {
        this.checkpointQuestions = checkpointQuestions;
    }
    
    public List<PracticeQuestion> getPracticeQuestions() {
        return practiceQuestions;
    }
    
    public void setPracticeQuestions(List<PracticeQuestion> practiceQuestions) {
        this.practiceQuestions = practiceQuestions;
    }
    
    public List<Progress> getProgressList() {
        return progressList;
    }
    
    public void setProgressList(List<Progress> progressList) {
        this.progressList = progressList;
    }
    
    public List<AIConversation> getConversations() {
        return conversations;
    }
    
    public void setConversations(List<AIConversation> conversations) {
        this.conversations = conversations;
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
    
    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
    
    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                ", sequenceOrder=" + sequenceOrder +
                ", difficulty=" + difficulty +
                '}';
    }
}