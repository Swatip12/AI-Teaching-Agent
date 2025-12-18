package com.aiteachingplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * PracticeQuestion entity representing exercises for students to apply learned concepts
 */
@Entity
@Table(name = "practice_questions", indexes = {
    @Index(name = "idx_practice_lesson", columnList = "lesson_id"),
    @Index(name = "idx_practice_difficulty", columnList = "difficulty")
})
public class PracticeQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @Column(name = "expected_solution", columnDefinition = "TEXT")
    private String expectedSolution;
    
    @Column(columnDefinition = "TEXT")
    private String hints;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType = QuestionType.CODING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.BEGINNER;
    
    @Column(name = "sequence_order")
    private Integer sequenceOrder;
    
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;
    
    @Column(name = "test_cases", columnDefinition = "TEXT")
    private String testCases;
    
    // Constructors
    public PracticeQuestion() {}
    
    public PracticeQuestion(Lesson lesson, String question, String expectedSolution, QuestionType questionType) {
        this.lesson = lesson;
        this.question = question;
        this.expectedSolution = expectedSolution;
        this.questionType = questionType;
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
    
    public String getExpectedSolution() {
        return expectedSolution;
    }
    
    public void setExpectedSolution(String expectedSolution) {
        this.expectedSolution = expectedSolution;
    }
    
    public String getHints() {
        return hints;
    }
    
    public void setHints(String hints) {
        this.hints = hints;
    }
    
    public QuestionType getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
    
    public String getStarterCode() {
        return starterCode;
    }
    
    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
    
    public String getTestCases() {
        return testCases;
    }
    
    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }
    
    public enum QuestionType {
        CODING, ALGORITHM, DEBUGGING, DESIGN
    }
    
    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
    
    @Override
    public String toString() {
        return "PracticeQuestion{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", questionType=" + questionType +
                ", difficulty=" + difficulty +
                '}';
    }
}