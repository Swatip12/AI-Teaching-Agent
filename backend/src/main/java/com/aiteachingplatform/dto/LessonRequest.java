package com.aiteachingplatform.dto;

import com.aiteachingplatform.model.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for lesson creation and update requests
 */
public class LessonRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;
    
    @NotNull(message = "Sequence order is required")
    @Positive(message = "Sequence order must be positive")
    private Integer sequenceOrder;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String objectives;
    
    @NotNull(message = "Difficulty is required")
    private Lesson.Difficulty difficulty;
    
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationMinutes;
    
    private List<Long> prerequisiteLessonIds;
    
    // Constructors
    public LessonRequest() {}
    
    public LessonRequest(String title, String subject, Integer sequenceOrder, String content, 
                        String objectives, Lesson.Difficulty difficulty, Integer estimatedDurationMinutes,
                        List<Long> prerequisiteLessonIds) {
        this.title = title;
        this.subject = subject;
        this.sequenceOrder = sequenceOrder;
        this.content = content;
        this.objectives = objectives;
        this.difficulty = difficulty;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.prerequisiteLessonIds = prerequisiteLessonIds;
    }
    
    // Getters and Setters
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
    
    public Lesson.Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Lesson.Difficulty difficulty) {
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
    
    /**
     * Convert DTO to Lesson entity
     */
    public Lesson toLesson() 
    {
        Lesson lesson = new Lesson(title, subject, sequenceOrder, content);
        lesson.setObjectives(objectives);
        lesson.setDifficulty(difficulty);
        lesson.setEstimatedDurationMinutes(estimatedDurationMinutes);
        lesson.setPrerequisiteLessonIds(prerequisiteLessonIds);
        return lesson;
    }
}