package com.aiteachingplatform.dto;

import com.aiteachingplatform.model.AIConversation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for AI tutor interactions
 */
public class AITutorRequest {
    
    @NotBlank(message = "Student message cannot be blank")
    private String studentMessage;
    
    private Long lessonId;
    
    @NotNull(message = "Conversation type is required")
    private AIConversation.ConversationType conversationType;
    
    private String contextData;
    
    private String previousResponse;
    
    private Integer difficultyLevel;
    
    private String learningStyle;
    
    // Constructors
    public AITutorRequest() {}
    
    public AITutorRequest(String studentMessage, AIConversation.ConversationType conversationType) {
        this.studentMessage = studentMessage;
        this.conversationType = conversationType;
    }
    
    public AITutorRequest(String studentMessage, Long lessonId, AIConversation.ConversationType conversationType) {
        this.studentMessage = studentMessage;
        this.lessonId = lessonId;
        this.conversationType = conversationType;
    }
    
    // Getters and Setters
    public String getStudentMessage() {
        return studentMessage;
    }
    
    public void setStudentMessage(String studentMessage) {
        this.studentMessage = studentMessage;
    }
    
    public Long getLessonId() {
        return lessonId;
    }
    
    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }
    
    public AIConversation.ConversationType getConversationType() {
        return conversationType;
    }
    
    public void setConversationType(AIConversation.ConversationType conversationType) {
        this.conversationType = conversationType;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public String getPreviousResponse() {
        return previousResponse;
    }
    
    public void setPreviousResponse(String previousResponse) {
        this.previousResponse = previousResponse;
    }
    
    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getLearningStyle() {
        return learningStyle;
    }
    
    public void setLearningStyle(String learningStyle) {
        this.learningStyle = learningStyle;
    }
    
    @Override
    public String toString() {
        return "AITutorRequest{" +
                "studentMessage='" + studentMessage + '\'' +
                ", lessonId=" + lessonId +
                ", conversationType=" + conversationType +
                ", difficultyLevel=" + difficultyLevel +
                ", learningStyle='" + learningStyle + '\'' +
                '}';
    }
}