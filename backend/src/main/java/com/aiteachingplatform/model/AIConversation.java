package com.aiteachingplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AIConversation entity representing interactions between students and the AI tutor
 */
@Entity
@Table(name = "ai_conversations", indexes = {
    @Index(name = "idx_conversation_user", columnList = "user_id"),
    @Index(name = "idx_conversation_lesson", columnList = "lesson_id"),
    @Index(name = "idx_conversation_timestamp", columnList = "timestamp"),
    @Index(name = "idx_conversation_user_lesson", columnList = "user_id, lesson_id")
})
public class AIConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    @NotBlank
    @Column(name = "student_message", columnDefinition = "TEXT", nullable = false)
    private String studentMessage;
    
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type")
    private ConversationType conversationType = ConversationType.QUESTION;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status")
    private ResponseStatus responseStatus = ResponseStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData;
    
    @Column(name = "feedback_rating")
    private Integer feedbackRating;
    
    @Column(name = "feedback_comment")
    private String feedbackComment;
    
    // Constructors
    public AIConversation() {}
    
    public AIConversation(User user, String studentMessage) {
        this.user = user;
        this.studentMessage = studentMessage;
        this.conversationType = ConversationType.QUESTION;
        this.responseStatus = ResponseStatus.PENDING;
    }
    
    public AIConversation(User user, Lesson lesson, String studentMessage, ConversationType type) {
        this.user = user;
        this.lesson = lesson;
        this.studentMessage = studentMessage;
        this.conversationType = type;
        this.responseStatus = ResponseStatus.PENDING;
    }
    
    // Business methods
    public void setAIResponse(String response, long responseTimeMs) {
        this.aiResponse = response;
        this.responseTimeMs = responseTimeMs;
        this.responseStatus = ResponseStatus.COMPLETED;
    }
    
    public void markAsFailed(String errorMessage) {
        this.aiResponse = errorMessage;
        this.responseStatus = ResponseStatus.FAILED;
    }
    
    public void addFeedback(Integer rating, String comment) {
        this.feedbackRating = rating;
        this.feedbackComment = comment;
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
    
    public String getStudentMessage() {
        return studentMessage;
    }
    
    public void setStudentMessage(String studentMessage) {
        this.studentMessage = studentMessage;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public ConversationType getConversationType() {
        return conversationType;
    }
    
    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }
    
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
    
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public Integer getFeedbackRating() {
        return feedbackRating;
    }
    
    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }
    
    public String getFeedbackComment() {
        return feedbackComment;
    }
    
    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }
    
    public enum ConversationType {
        QUESTION, HELP_REQUEST, CONFUSION, FEEDBACK, ERROR_EXPLANATION
    }
    
    public enum ResponseStatus {
        PENDING, COMPLETED, FAILED, FALLBACK
    }
    
    @Override
    public String toString() {
        return "AIConversation{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", lessonId=" + (lesson != null ? lesson.getId() : null) +
                ", conversationType=" + conversationType +
                ", responseStatus=" + responseStatus +
                ", timestamp=" + timestamp +
                '}';
    }
}