package com.aiteachingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for question submission requests
 */
public class QuestionSubmissionRequest {
    
    @NotBlank(message = "Answer is required")
    private String answer;
    
    // Optional fields for additional context
    private Long userId;
    private Long lessonId;
    private String sessionId;
    
    // Constructors
    public QuestionSubmissionRequest() {}
    
    public QuestionSubmissionRequest(String answer) {
        this.answer = answer;
    }
    
    // Getters and Setters
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getLessonId() {
        return lessonId;
    }
    
    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    @Override
    public String toString() {
        return "QuestionSubmissionRequest{" +
                "answer='" + answer + '\'' +
                ", userId=" + userId +
                ", lessonId=" + lessonId +
                '}';
    }
}