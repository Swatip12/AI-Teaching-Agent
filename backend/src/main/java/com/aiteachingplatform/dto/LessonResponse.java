package com.aiteachingplatform.dto;

import com.aiteachingplatform.model.CheckpointQuestion;
import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.PracticeQuestion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for lesson response data
 */
public class LessonResponse {
    
    private Long id;
    private String title;
    private String subject;
    private Integer sequenceOrder;
    private String content;
    private String objectives;
    private Lesson.Difficulty difficulty;
    private Integer estimatedDurationMinutes;
    private List<Long> prerequisiteLessonIds;
    private List<CheckpointQuestionResponse> checkpointQuestions;
    private List<PracticeQuestionResponse> practiceQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canAccess;
    
    // Constructors
    public LessonResponse() {}
    
    public LessonResponse(Lesson lesson) {
        this.id = lesson.getId();
        this.title = lesson.getTitle();
        this.subject = lesson.getSubject();
        this.sequenceOrder = lesson.getSequenceOrder();
        this.content = lesson.getContent();
        this.objectives = lesson.getObjectives();
        this.difficulty = lesson.getDifficulty();
        this.estimatedDurationMinutes = lesson.getEstimatedDurationMinutes();
        this.prerequisiteLessonIds = lesson.getPrerequisiteLessonIds();
        this.createdAt = lesson.getCreatedAt();
        this.updatedAt = lesson.getUpdatedAt();
        
        // Convert checkpoint questions
        if (lesson.getCheckpointQuestions() != null) {
            this.checkpointQuestions = lesson.getCheckpointQuestions().stream()
                    .map(CheckpointQuestionResponse::new)
                    .toList();
        }
        
        // Convert practice questions
        if (lesson.getPracticeQuestions() != null) {
            this.practiceQuestions = lesson.getPracticeQuestions().stream()
                    .map(PracticeQuestionResponse::new)
                    .toList();
        }
    }
    
    public LessonResponse(Lesson lesson, boolean canAccess) {
        this(lesson);
        this.canAccess = canAccess;
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
    
    public List<CheckpointQuestionResponse> getCheckpointQuestions() {
        return checkpointQuestions;
    }
    
    public void setCheckpointQuestions(List<CheckpointQuestionResponse> checkpointQuestions) {
        this.checkpointQuestions = checkpointQuestions;
    }
    
    public List<PracticeQuestionResponse> getPracticeQuestions() {
        return practiceQuestions;
    }
    
    public void setPracticeQuestions(List<PracticeQuestionResponse> practiceQuestions) {
        this.practiceQuestions = practiceQuestions;
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
    
    public boolean isCanAccess() {
        return canAccess;
    }
    
    public void setCanAccess(boolean canAccess) {
        this.canAccess = canAccess;
    }
    
    /**
     * Nested DTO for checkpoint questions
     */
    public static class CheckpointQuestionResponse {
        private Long id;
        private String question;
        private String correctAnswer;
        private String explanation;
        private CheckpointQuestion.QuestionType questionType;
        private Integer sequenceOrder;
        
        public CheckpointQuestionResponse() {}
        
        public CheckpointQuestionResponse(CheckpointQuestion checkpointQuestion) {
            this.id = checkpointQuestion.getId();
            this.question = checkpointQuestion.getQuestion();
            this.correctAnswer = checkpointQuestion.getCorrectAnswer();
            this.explanation = checkpointQuestion.getExplanation();
            this.questionType = checkpointQuestion.getQuestionType();
            this.sequenceOrder = checkpointQuestion.getSequenceOrder();
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public CheckpointQuestion.QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(CheckpointQuestion.QuestionType questionType) { this.questionType = questionType; }
        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    }
    
    /**
     * Nested DTO for practice questions
     */
    public static class PracticeQuestionResponse {
        private Long id;
        private String question;
        private String expectedSolution;
        private String hints;
        private PracticeQuestion.QuestionType questionType;
        private PracticeQuestion.Difficulty difficulty;
        private Integer sequenceOrder;
        private String starterCode;
        private String testCases;
        
        public PracticeQuestionResponse() {}
        
        public PracticeQuestionResponse(PracticeQuestion practiceQuestion) {
            this.id = practiceQuestion.getId();
            this.question = practiceQuestion.getQuestion();
            this.expectedSolution = practiceQuestion.getExpectedSolution();
            this.hints = practiceQuestion.getHints();
            this.questionType = practiceQuestion.getQuestionType();
            this.difficulty = practiceQuestion.getDifficulty();
            this.sequenceOrder = practiceQuestion.getSequenceOrder();
            this.starterCode = practiceQuestion.getStarterCode();
            this.testCases = practiceQuestion.getTestCases();
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getExpectedSolution() { return expectedSolution; }
        public void setExpectedSolution(String expectedSolution) { this.expectedSolution = expectedSolution; }
        public String getHints() { return hints; }
        public void setHints(String hints) { this.hints = hints; }
        public PracticeQuestion.QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(PracticeQuestion.QuestionType questionType) { this.questionType = questionType; }
        public PracticeQuestion.Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(PracticeQuestion.Difficulty difficulty) { this.difficulty = difficulty; }
        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
        public String getStarterCode() { return starterCode; }
        public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
        public String getTestCases() { return testCases; }
        public void setTestCases(String testCases) { this.testCases = testCases; }
    }
}