package com.aiteachingplatform.dto;

import java.time.LocalDateTime;

/**
 * DTO for question feedback responses
 * Provides immediate feedback and explanations for student answers
 */
public class QuestionFeedbackResponse {
    
    private Long questionId;
    private String userAnswer;
    private boolean isCorrect;
    private String feedback;
    private String explanation;
    private LocalDateTime timestamp;
    private String encouragement;
    private String nextSteps;
    
    // Constructors
    public QuestionFeedbackResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public QuestionFeedbackResponse(Long questionId, String userAnswer, boolean isCorrect, 
                                  String feedback, String explanation) {
        this();
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.feedback = feedback;
        this.explanation = explanation;
    }
    
    // Static factory methods for common feedback scenarios
    public static QuestionFeedbackResponse correct(Long questionId, String userAnswer, 
                                                 String explanation) {
        QuestionFeedbackResponse response = new QuestionFeedbackResponse(
            questionId, userAnswer, true, 
            "Excellent! That's absolutely correct.", explanation
        );
        response.setEncouragement("Great job! You're really getting the hang of this.");
        return response;
    }
    
    public static QuestionFeedbackResponse incorrect(Long questionId, String userAnswer, 
                                                   String explanation, String correctAnswer) {
        String feedback = "That's not quite right, but don't worry - learning takes practice!";
        if (correctAnswer != null && !correctAnswer.isEmpty()) {
            feedback += " The correct answer is: " + correctAnswer;
        }
        
        QuestionFeedbackResponse response = new QuestionFeedbackResponse(
            questionId, userAnswer, false, feedback, explanation
        );
        response.setEncouragement("Keep going! Every mistake is a step toward understanding.");
        response.setNextSteps("Take a moment to review the explanation, then try the next question.");
        return response;
    }
    
    public static QuestionFeedbackResponse partiallyCorrect(Long questionId, String userAnswer, 
                                                          String explanation) {
        QuestionFeedbackResponse response = new QuestionFeedbackResponse(
            questionId, userAnswer, false,
            "You're on the right track! Your answer shows good understanding, but there's room for improvement.",
            explanation
        );
        response.setEncouragement("Nice effort! You're thinking about this correctly.");
        response.setNextSteps("Review the explanation and see if you can spot what to adjust.");
        return response;
    }
    
    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public String getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getEncouragement() {
        return encouragement;
    }
    
    public void setEncouragement(String encouragement) {
        this.encouragement = encouragement;
    }
    
    public String getNextSteps() {
        return nextSteps;
    }
    
    public void setNextSteps(String nextSteps) {
        this.nextSteps = nextSteps;
    }
    
    @Override
    public String toString() {
        return "QuestionFeedbackResponse{" +
                "questionId=" + questionId +
                ", isCorrect=" + isCorrect +
                ", feedback='" + feedback + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}