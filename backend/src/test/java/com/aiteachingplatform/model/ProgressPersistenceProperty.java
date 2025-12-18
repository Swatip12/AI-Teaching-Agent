package com.aiteachingplatform.model;

import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 8: Progress persistence and restoration**
 * **Validates: Requirements 4.1, 4.3**
 * 
 * Property-based test for progress persistence and restoration functionality.
 * Tests that progress data is correctly saved and can be restored for any user-lesson combination.
 */
@DataJpaTest
@ActiveProfiles("test")
public class ProgressPersistenceProperty extends PropertyTestBase {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Test
    void progressPersistenceAndRestoration() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Create and save user
                User user = new User(testData.username, testData.email, testData.passwordHash);
                user = userRepository.save(user);
                
                // Create and save lesson
                Lesson lesson = new Lesson(testData.lessonTitle, testData.subject, testData.sequenceOrder, testData.content);
                lesson = lessonRepository.save(lesson);
                
                // Create progress with test data
                Progress originalProgress = new Progress(user, lesson);
                originalProgress.setStatus(testData.status);
                originalProgress.setCompletionPercentage(testData.completionPercentage);
                originalProgress.setScore(testData.score);
                originalProgress.setAttemptsCount(testData.attemptsCount);
                originalProgress.setTimeSpentMinutes(testData.timeSpentMinutes);
                originalProgress.setPracticeQuestionsCompleted(testData.practiceQuestionsCompleted);
                
                if (testData.status == Progress.ProgressStatus.IN_PROGRESS || testData.status == Progress.ProgressStatus.COMPLETED) {
                    originalProgress.setStartedAt(LocalDateTime.now().minusMinutes(testData.timeSpentMinutes));
                }
                
                if (testData.status == Progress.ProgressStatus.COMPLETED) {
                    originalProgress.setCompletedAt(LocalDateTime.now());
                }
                
                // Save progress
                Progress savedProgress = progressRepository.save(originalProgress);
                
                // Verify progress was saved with an ID
                assert savedProgress.getId() != null : "Progress should have an ID after saving";
                
                // Retrieve progress by user and lesson
                Optional<Progress> retrievedProgress = progressRepository.findByUserAndLesson(user, lesson);
                
                // Verify progress can be restored
                assert retrievedProgress.isPresent() : "Progress should be retrievable by user and lesson";
                
                Progress restored = retrievedProgress.get();
                
                // Verify all data is correctly restored
                assert restored.getUser().getId().equals(user.getId()) : "User should be correctly restored";
                assert restored.getLesson().getId().equals(lesson.getId()) : "Lesson should be correctly restored";
                assert restored.getStatus().equals(testData.status) : "Status should be correctly restored";
                assert restored.getCompletionPercentage().equals(testData.completionPercentage) : "Completion percentage should be correctly restored";
                assert restored.getScore() == null ? testData.score == null : restored.getScore().equals(testData.score) : "Score should be correctly restored";
                assert restored.getAttemptsCount().equals(testData.attemptsCount) : "Attempts count should be correctly restored";
                assert restored.getTimeSpentMinutes().equals(testData.timeSpentMinutes) : "Time spent should be correctly restored";
                assert restored.getPracticeQuestionsCompleted().equals(testData.practiceQuestionsCompleted) : "Practice questions completed should be correctly restored";
                
                // Verify timestamps are preserved
                if (originalProgress.getStartedAt() != null) {
                    assert restored.getStartedAt() != null : "Started at timestamp should be preserved";
                }
                
                if (originalProgress.getCompletedAt() != null) {
                    assert restored.getCompletedAt() != null : "Completed at timestamp should be preserved";
                }
                
                // Verify created and updated timestamps exist
                assert restored.getCreatedAt() != null : "Created at timestamp should exist";
                assert restored.getUpdatedAt() != null : "Updated at timestamp should exist";
            }
        });
    }
    
    private static class TestData {
        final String username;
        final String email;
        final String passwordHash;
        final String lessonTitle;
        final String subject;
        final Integer sequenceOrder;
        final String content;
        final Progress.ProgressStatus status;
        final Integer completionPercentage;
        final Integer score;
        final Integer attemptsCount;
        final Integer timeSpentMinutes;
        final Integer practiceQuestionsCompleted;
        
        TestData(String username, String email, String passwordHash, String lessonTitle, 
                String subject, Integer sequenceOrder, String content, Progress.ProgressStatus status,
                Integer completionPercentage, Integer score, Integer attemptsCount, 
                Integer timeSpentMinutes, Integer practiceQuestionsCompleted) {
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.lessonTitle = lessonTitle;
            this.subject = subject;
            this.sequenceOrder = sequenceOrder;
            this.content = content;
            this.status = status;
            this.completionPercentage = completionPercentage;
            this.score = score;
            this.attemptsCount = attemptsCount;
            this.timeSpentMinutes = timeSpentMinutes;
            this.practiceQuestionsCompleted = practiceQuestionsCompleted;
        }
    }
    
    private static final Generator<TestData> testDataGenerator = new Generator<TestData>() {
        @Override
        public TestData next() {
            String username = "user" + positiveIntegers().next();
            String email = username + "@test.com";
            String passwordHash = "$2a$10$" + strings(50, 50).next(); // BCrypt format
            String lessonTitle = "Lesson " + positiveIntegers().next();
            String subject = arrays(new String[]{"Java", "DSA", "FullStack", "Logic", "Interview"}).next();
            Integer sequenceOrder = integers(1, 100).next();
            String content = "Content for " + lessonTitle;
            Progress.ProgressStatus status = arrays(Progress.ProgressStatus.values()).next();
            Integer completionPercentage = integers(0, 100).next();
            Integer score = booleans().next() ? integers(0, 100).next() : null;
            Integer attemptsCount = integers(0, 10).next();
            Integer timeSpentMinutes = integers(0, 300).next();
            Integer practiceQuestionsCompleted = integers(0, 5).next();
            
            return new TestData(username, email, passwordHash, lessonTitle, subject, sequenceOrder, 
                              content, status, completionPercentage, score, attemptsCount, 
                              timeSpentMinutes, practiceQuestionsCompleted);
        }
    };
    
    {
        // Register the generator
        Generator.register(TestData.class, testDataGenerator);
    }
}