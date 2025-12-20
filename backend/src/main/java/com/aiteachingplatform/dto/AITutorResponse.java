package com.aiteachingplatform.dto;

import com.aiteachingplatform.model.AIConversation;

import java.time.LocalDateTime;

/**
 * Response DTO for AI tutor interactions
 */
public class AITutorResponse {
    
    private Long conversationId;
    
    private String aiResponse;
    
    private AIConversation.ResponseStatus responseStatus;
    
    private Long responseTimeMs;
    
    private LocalDateTime timestamp;
    
    private String errorMessage;
    
    private boolean requiresFollowUp;
    
    private String suggestedAction;
    
    private Integer confidenceScore;
    
    // Constructors
    public AITutorResponse() {}
    
    public AITutorResponse(String aiResponse) {
        this.aiResponse = aiResponse;
        this.responseStatus = AIConversation.ResponseStatus.COMPLETED;
        this.timestamp = LocalDateTime.now();
    }
    
    public AITutorResponse(String errorMessage, AIConversation.ResponseStatus status) {
        this.errorMessage = errorMessage;
        this.responseStatus = status;
        this.timestamp = LocalDateTime.now();
    }
    
    // Static factory methods
    public static AITutorResponse success(String response, Long responseTimeMs) {
        AITutorResponse tutorResponse = new AITutorResponse(response);
        tutorResponse.setResponseTimeMs(responseTimeMs);
        return tutorResponse;
    }
    
    public static AITutorResponse failure(String errorMessage) {
        return new AITutorResponse(errorMessage, AIConversation.ResponseStatus.FAILED);
    }
    
    public static AITutorResponse pending() {
        AITutorResponse response = new AITutorResponse();
        response.setResponseStatus(AIConversation.ResponseStatus.PENDING);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    public static AITutorResponse fallback(String fallbackResponse) {
        AITutorResponse response = new AITutorResponse(fallbackResponse);
        response.setResponseStatus(AIConversation.ResponseStatus.FALLBACK);
        response.setSuggestedAction("AI service temporarily unavailable - using fallback response");
        return response;
    }
    
    // Business methods
    public boolean isSuccessful() {
        return responseStatus == AIConversation.ResponseStatus.COMPLETED && aiResponse != null;
    }
    
    public boolean isFailed() {
        return responseStatus == AIConversation.ResponseStatus.FAILED;
    }
    
    public boolean isPending() {
        return responseStatus == AIConversation.ResponseStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public AIConversation.ResponseStatus getResponseStatus() {
        return responseStatus;
    }
    
    public void setResponseStatus(AIConversation.ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean isRequiresFollowUp() {
        return requiresFollowUp;
    }
    
    public void setRequiresFollowUp(boolean requiresFollowUp) {
        this.requiresFollowUp = requiresFollowUp;
    }
    
    public String getSuggestedAction() {
        return suggestedAction;
    }
    
    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }
    
    public Integer getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    @Override
    public String toString() {
        return "AITutorResponse{" +
                "conversationId=" + conversationId +
                ", responseStatus=" + responseStatus +
                ", responseTimeMs=" + responseTimeMs +
                ", timestamp=" + timestamp +
                ", requiresFollowUp=" + requiresFollowUp +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}