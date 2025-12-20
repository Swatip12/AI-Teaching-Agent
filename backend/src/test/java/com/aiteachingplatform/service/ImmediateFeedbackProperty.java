package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.QuestionFeedbackResponse;
import com.aiteachingplatform.model.CheckpointQuestion;
import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.PracticeQuestion;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.java.quickcheck.generator.CombinedGenerators.oneOf;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 5: Immediate feedback provision**
 * **Validates: Requirements 3.1, 3.3**
 * 
 * Property-based test to verify that the system provides immediate, contextual feedback
 * for any student response to questions or exercises.
 */
public class ImmediateFeedbackProperty extends PropertyTestBase {
    
    @Mock
    private QuestionFeedbackService questionFeedbackService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        questionFeedbackService = new QuestionFeedbackService();
    }
    
    @Test
    void immediateFeedbackForCheckpointQuestions() {
        /**
         * Property: For any checkpoint question and any student answer,
         * the system should provide immediate feedback with explanations
         */
        assertProperty(new AbstractCharacteristic<CheckpointQuestionTestData>() {
            @Override
            protected void doSpecify(CheckpointQuestionTestData testData) throws Throwable {
                // Given: A checkpoint question and a student answer
                CheckpointQuestion question = testData.question;
                String studentAnswer = testData.studentAnswer;
                Optional<User> user = testData.user;
                
                // When: The student submits an answer
                QuestionFeedbackResponse feedback = questionFeedbackService.evaluateCheckpointAnswer(
                    question, studentAnswer, user
                );
                
                // Then: Immediate feedback should be provided
                assert feedback != null : "Feedback must not be null";
                assert feedback.getQuestionId() != null : "Question ID must be set";
                assert feedback.getUserAnswer() != null : "User answer must be recorded";
                assert feedback.getFeedback() != null && !feedback.getFeedback().trim().isEmpty() : 
                    "Feedback message must be provided";
                assert feedback.getExplanation() != null && !feedback.getExplanation().trim().isEmpty() : 
                    "Explanation must be provided";
                assert feedback.getTimestamp() != null : "Timestamp must be set";
                
                // Feedback should be contextual and immediate
                assert feedback.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(5)) : 
                    "Feedback should be immediate (within 5 seconds)";
                
                // Feedback should be appropriate for the correctness
                if (feedback.isCorrect()) {
                    assert feedback.getFeedback().toLowerCase().contains("correct") ||
                           feedback.getFeedback().toLowerCase().contains("excellent") ||
                           feedback.getFeedback().toLowerCase().contains("great") : 
                        "Positive feedback should be encouraging for correct answers";
                } else {
                    assert feedback.getFeedback().toLowerCase().contains("not quite") ||
                           feedback.getFeedback().toLowerCase().contains("try") ||
                           feedback.getFeedback().toLowerCase().contains("practice") : 
                        "Constructive feedback should be provided for incorrect answers";
                }
            }
        });
    }
    
    @Test
    void immediateFeedbackForPracticeQuestions() {
        /**
         * Property: For any practice question and any student solution,
         * the system should provide immediate feedback with explanations
         */
        assertProperty(new AbstractCharacteristic<PracticeQuestionTestData>() {
            @Override
            protected void doSpecify(PracticeQuestionTestData testData) throws Throwable {
                // Given: A practice question and a student solution
                PracticeQuestion question = testData.question;
                String studentSolution = testData.studentSolution;
                Optional<User> user = testData.user;
                
                // When: The student submits a solution
                QuestionFeedbackResponse feedback = questionFeedbackService.evaluatePracticeAnswer(
                    question, studentSolution, user
                );
                
                // Then: Immediate feedback should be provided
                assert feedback != null : "Feedback must not be null";
                assert feedback.getQuestionId() != null : "Question ID must be set";
                assert feedback.getUserAnswer() != null : "User solution must be recorded";
                assert feedback.getFeedback() != null && !feedback.getFeedback().trim().isEmpty() : 
                    "Feedback message must be provided";
                assert feedback.getExplanation() != null && !feedback.getExplanation().trim().isEmpty() : 
                    "Explanation must be provided";
                assert feedback.getTimestamp() != null : "Timestamp must be set";
                
                // Feedback should be immediate and contextual
                assert feedback.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(5)) : 
                    "Feedback should be immediate (within 5 seconds)";
                
                // Feedback should include helpful guidance
                assert feedback.getExplanation().length() > 10 : 
                    "Explanation should be substantial and helpful";
                
                // For practice questions, feedback should be educational
                if (!feedback.isCorrect()) {
                    assert feedback.getExplanation().toLowerCase().contains("step") ||
                           feedback.getExplanation().toLowerCase().contains("approach") ||
                           feedback.getExplanation().toLowerCase().contains("concept") ||
                           feedback.getExplanation().toLowerCase().contains("try") : 
                        "Educational guidance should be provided for practice questions";
                }
            }
        });
    }
    
    @Test
    void feedbackConsistencyAcrossQuestionTypes() {
        /**
         * Property: Feedback structure should be consistent regardless of question type
         */
        assertProperty(new AbstractCharacteristic<MixedQuestionTestData>() {
            @Override
            protected void doSpecify(MixedQuestionTestData testData) throws Throwable {
                QuestionFeedbackResponse checkpointFeedback = questionFeedbackService.evaluateCheckpointAnswer(
                    testData.checkpointQuestion, testData.answer, testData.user
                );
                
                QuestionFeedbackResponse practiceFeedback = questionFeedbackService.evaluatePracticeAnswer(
                    testData.practiceQuestion, testData.answer, testData.user
                );
                
                // Both feedback responses should have consistent structure
                assert checkpointFeedback.getTimestamp() != null && practiceFeedback.getTimestamp() != null : 
                    "Both feedback types should have timestamps";
                assert checkpointFeedback.getFeedback() != null && practiceFeedback.getFeedback() != null : 
                    "Both feedback types should have feedback messages";
                assert checkpointFeedback.getExplanation() != null && practiceFeedback.getExplanation() != null : 
                    "Both feedback types should have explanations";
                
                // Response time should be similar (within reasonable bounds)
                long timeDifference = Math.abs(
                    checkpointFeedback.getTimestamp().toLocalTime().toSecondOfDay() - 
                    practiceFeedback.getTimestamp().toLocalTime().toSecondOfDay()
                );
                assert timeDifference < 10 : "Response times should be consistently fast";
            }
        });
    }
    
    // Test data generators
    private static final Generator<CheckpointQuestionTestData> checkpointQuestionGenerator = 
        new Generator<CheckpointQuestionTestData>() {
            @Override
            public CheckpointQuestionTestData next() {
                Lesson lesson = createTestLesson();
                CheckpointQuestion question = createCheckpointQuestion(lesson);
                String answer = oneOf(
                    strings(1, 100),
                    fixedValues("true", "false", "yes", "no", "correct answer", "wrong answer")
                ).next();
                
                return new CheckpointQuestionTestData(question, answer, Optional.of(createTestUser()));
            }
        };
    
    private static final Generator<PracticeQuestionTestData> practiceQuestionGenerator = 
        new Generator<PracticeQuestionTestData>() {
            @Override
            public PracticeQuestionTestData next() {
                Lesson lesson = createTestLesson();
                PracticeQuestion question = createPracticeQuestion(lesson);
                String solution = oneOf(
                    strings(10, 500),
                    fixedValues("public void method() {}", "for(int i=0; i<10; i++)", "if (condition) { return true; }")
                ).next();
                
                return new PracticeQuestionTestData(question, solution, Optional.of(createTestUser()));
            }
        };
    
    private static final Generator<MixedQuestionTestData> mixedQuestionGenerator = 
        new Generator<MixedQuestionTestData>() {
            @Override
            public MixedQuestionTestData next() {
                Lesson lesson = createTestLesson();
                CheckpointQuestion checkpoint = createCheckpointQuestion(lesson);
                PracticeQuestion practice = createPracticeQuestion(lesson);
                String answer = strings(1, 200).next();
                
                return new MixedQuestionTestData(checkpoint, practice, answer, Optional.of(createTestUser()));
            }
        };
    
    // Helper methods to create test data
    private static Lesson createTestLesson() {
        Lesson lesson = new Lesson();
        lesson.setId(positiveIntegers().next().longValue());
        lesson.setTitle("Test Lesson " + strings(5, 20).next());
        lesson.setSubject(oneOf(fixedValues("Java", "Python", "JavaScript", "Data Structures")).next());
        lesson.setContent("Test lesson content explaining concepts clearly.");
        lesson.setDifficulty(oneOf(Lesson.Difficulty.values()).next());
        return lesson;
    }
    
    private static CheckpointQuestion createCheckpointQuestion(Lesson lesson) {
        CheckpointQuestion question = new CheckpointQuestion();
        question.setId(positiveIntegers().next().longValue());
        question.setLesson(lesson);
        question.setQuestion("What is " + strings(5, 30).next() + "?");
        question.setCorrectAnswer(oneOf(fixedValues("true", "false", "correct answer", "42")).next());
        question.setExplanation("This concept is important because " + strings(10, 50).next());
        question.setQuestionType(oneOf(CheckpointQuestion.QuestionType.values()).next());
        return question;
    }
    
    private static PracticeQuestion createPracticeQuestion(Lesson lesson) {
        PracticeQuestion question = new PracticeQuestion();
        question.setId(positiveIntegers().next().longValue());
        question.setLesson(lesson);
        question.setQuestion("Implement " + strings(5, 30).next());
        question.setExpectedSolution("public void solution() { /* implementation */ }");
        question.setHints("Think about " + strings(10, 40).next());
        question.setQuestionType(oneOf(PracticeQuestion.QuestionType.values()).next());
        question.setDifficulty(oneOf(PracticeQuestion.Difficulty.values()).next());
        return question;
    }
    
    private static User createTestUser() {
        User user = new User();
        user.setId(positiveIntegers().next().longValue());
        user.setUsername("testuser" + positiveIntegers().next());
        user.setEmail("test" + positiveIntegers().next() + "@example.com");
        return user;
    }
    
    // Test data classes
    private static class CheckpointQuestionTestData {
        final CheckpointQuestion question;
        final String studentAnswer;
        final Optional<User> user;
        
        CheckpointQuestionTestData(CheckpointQuestion question, String studentAnswer, Optional<User> user) {
            this.question = question;
            this.studentAnswer = studentAnswer;
            this.user = user;
        }
    }
    
    private static class PracticeQuestionTestData {
        final PracticeQuestion question;
        final String studentSolution;
        final Optional<User> user;
        
        PracticeQuestionTestData(PracticeQuestion question, String studentSolution, Optional<User> user) {
            this.question = question;
            this.studentSolution = studentSolution;
            this.user = user;
        }
    }
    
    private static class MixedQuestionTestData {
        final CheckpointQuestion checkpointQuestion;
        final PracticeQuestion practiceQuestion;
        final String answer;
        final Optional<User> user;
        
        MixedQuestionTestData(CheckpointQuestion checkpointQuestion, PracticeQuestion practiceQuestion, 
                            String answer, Optional<User> user) {
            this.checkpointQuestion = checkpointQuestion;
            this.practiceQuestion = practiceQuestion;
            this.answer = answer;
            this.user = user;
        }
    }
}