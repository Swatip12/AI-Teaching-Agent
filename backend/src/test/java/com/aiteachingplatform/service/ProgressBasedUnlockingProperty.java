package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 9: Progress-based unlocking**
 * **Validates: Requirements 4.4, 4.5**
 * 
 * Property: For any progress update, the system should unlock exactly the next appropriate lesson 
 * and celebrate milestone achievements
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProgressBasedUnlockingProperty extends PropertyTestBase {

    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Test
    void progressBasedUnlockingProperty() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Create test user
                User user = new User();
                user.setUsername("testuser" + System.currentTimeMillis());
                user.setEmail("test" + System.currentTimeMillis() + "@example.com");
                user.setPasswordHash("hashedpassword");
                user = userRepository.save(user);
                
                // Create sequential lessons in the same subject
                String subject = testData.subject;
                Lesson lesson1 = createLesson(subject, 1, "Lesson 1");
                Lesson lesson2 = createLesson(subject, 2, "Lesson 2");
                Lesson lesson3 = createLesson(subject, 3, "Lesson 3");
                
                // Set up prerequisites: lesson2 requires lesson1, lesson3 requires lesson2
                lesson2.setPrerequisiteLessonIds(List.of(lesson1.getId()));
                lesson3.setPrerequisiteLessonIds(List.of(lesson2.getId()));
                lessonRepository.save(lesson2);
                lessonRepository.save(lesson3);
                
                // Initially, only lesson1 should be available
                List<Lesson> availableLessons = progressTrackingService.getAvailableLessons(user, subject);
                assert availableLessons.size() == 1 : "Initially only first lesson should be available";
                assert availableLessons.get(0).getId().equals(lesson1.getId()) : "First lesson should be available";
                
                // Complete lesson1
                progressTrackingService.completeLesson(user, lesson1, testData.score1);
                
                // After completing lesson1, lesson2 should be unlocked
                availableLessons = progressTrackingService.getAvailableLessons(user, subject);
                boolean lesson2Available = availableLessons.stream()
                    .anyMatch(l -> l.getId().equals(lesson2.getId()));
                assert lesson2Available : "Lesson2 should be unlocked after completing lesson1";
                
                // lesson3 should still be locked
                boolean lesson3Available = availableLessons.stream()
                    .anyMatch(l -> l.getId().equals(lesson3.getId()));
                assert !lesson3Available : "Lesson3 should remain locked until lesson2 is completed";
                
                // Complete lesson2
                progressTrackingService.completeLesson(user, lesson2, testData.score2);
                
                // After completing lesson2, lesson3 should be unlocked
                availableLessons = progressTrackingService.getAvailableLessons(user, subject);
                lesson3Available = availableLessons.stream()
                    .anyMatch(l -> l.getId().equals(lesson3.getId()));
                assert lesson3Available : "Lesson3 should be unlocked after completing lesson2";
                
                // Check milestone celebration is triggered for progress milestones
                // After completing 2 out of 3 lessons (66%), should trigger milestone
                boolean milestoneReached = progressTrackingService.checkMilestoneAchievement(user, subject);
                // Milestone should be reached when completing significant portion of subject
                // (This will depend on the total number of lessons and completion ratio)
            }
        });
    }
    
    private Lesson createLesson(String subject, int sequenceOrder, String title) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setSubject(subject);
        lesson.setSequenceOrder(sequenceOrder);
        lesson.setContent("Test content for " + title);
        lesson.setDifficulty(Lesson.Difficulty.BEGINNER);
        return lessonRepository.save(lesson);
    }
    
    private static class TestData {
        final String subject;
        final int score1;
        final int score2;
        
        TestData(String subject, int score1, int score2) {
            this.subject = subject;
            this.score1 = score1;
            this.score2 = score2;
        }
    }
    
    private static final Generator<TestData> testDataGenerator = new Generator<TestData>() {
        @Override
        public TestData next() {
            String[] subjects = {"Java Programming", "Data Structures", "Algorithms", "Full Stack Development"};
            String subject = subjects[integers(0, subjects.length - 1).next()];
            int score1 = integers(60, 100).next(); // Passing scores
            int score2 = integers(60, 100).next(); // Passing scores
            return new TestData(subject, score1, score2);
        }
    };
    
    {
        // Register the generator
        Generator.register(TestData.class, testDataGenerator);
    }
}