package com.aiteachingplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CheckpointQuestion entity representing verification questions within lessons
 */
@Entity
@Table(name = "checkpoint_questions", indexes = {
    @Index(name = "idx_checkpoint_lesson", columnList = "lesson_id")
})
public class CheckpointQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @NotBlank
    @Column(name = "correct_answer", columnDefinition = "TEXT", nullable = false)
    private String correctAnswer;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;
    
    @Column(name = "sequence_order")
    private Integer sequenceOrder;
    
    // Constructors
    public CheckpointQuestion() {}
    
    public CheckpointQuestion(Lesson lesson, String question, String correctAnswer, String explanation) {
        this.lesson = lesson;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Lesson getLesson() {
        return lesson;
    }
    
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public QuestionType getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    
    public Integer getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
    
    public enum QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, CODE_COMPLETION
    }
    
    @Override
    public String toString() {
        return "CheckpointQuestion{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", questionType=" + questionType +
                '}';
    }
}