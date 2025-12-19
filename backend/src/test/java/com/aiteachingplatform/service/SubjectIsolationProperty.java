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

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 10: Subject isolation and paths**
 * **Validates: Requirements 5.2, 5.3**
 * 
 * Property: For any subject switch, the system should maintain separate progress tracking 
 * and display appropriate learning paths
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SubjectIsolationProperty extends PropertyTestBase {

    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Test
    void subjectIsolationProperty() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Create test user
                User user = new User();
                user.setUsername("testuser" + System.currentTimeMillis());
                user.setEmail("test" + System.currentTimeMillis() + "@example.com");
                user.setPasswordHash("hashedpassword");
                user = userRepository.save(user);
                
                // Create lessons in different subjects
                String subject1 = testData.subject1;
                String subject2 = testData.subject2;
                
                Lesson lesson1Subject1 = createLesson(subject1, 1, "Subject1 Lesson1");
                Lesson lesson2Subject1 = createLesson(subject1, 2, "Subject1 Lesson2");
                Lesson lesson1Subject2 = createLesson(subject2, 1, "Subject2 Lesson1");
                Lesson lesson2Subject2 = createLesson(subject2, 2, "Subject2 Lesson2");
                
                // Make progress in subject1
                progressTrackingService.completeLesson(user, lesson1Subject1, testData.score1);
                progressTrackingService.startLesson(user, lesson2Subject1);
                
                // Make different progress in subject2
                progressTrackingService.completeLesson(user, lesson1Subject2, testData.score2);
                
                // Verify subject isolation: progress in subject1 should not affect subject2
                List<Progress> subject1Progress = progressRepository.findByUserAndSubject(user, subject1);
                List<Progress> subject2Progress = progressRepository.findByUserAndSubject(user, subject2);
                
                // Subject1 should have 2 progress records (1 completed, 1 in progress)
                assert subject1Progress.size() == 2 : "Subject1 should have exactly 2 progress records";
                long subject1Completed = subject1Progress.stream()
                    .mapToLong(p -> p.getStatus() == Progress.ProgressStatus.COMPLETED ? 1 : 0)
                    .sum();
                assert subject1Completed == 1 : "Subject1 should have exactly 1 completed lesson";
                
                // Subject2 should have 1 progress record (1 completed)
                assert subject2Progress.size() == 1 : "Subject2 should have exactly 1 progress record";
                assert subject2Progress.get(0).getStatus() == Progress.ProgressStatus.COMPLETED : 
                    "Subject2 progress should be completed";
                
                // Verify learning paths are separate
                List<Lesson> availableSubject1 = progressTrackingService.getAvailableLessons(user, subject1);
                List<Lesson> availableSubject2 = progressTrackingService.getAvailableLessons(user, subject2);
                
                // Available lessons should be subject-specific
                boolean subject1HasOnlySubject1Lessons = availableSubject1.stream()
                    .allMatch(l -> l.getSubject().equals(subject1));
                assert subject1HasOnlySubject1Lessons : "Subject1 available lessons should only contain subject1 lessons";
                
                boolean subject2HasOnlySubject2Lessons = availableSubject2.stream()
                    .allMatch(l -> l.getSubject().equals(subject2));
                assert subject2HasOnlySubject2Lessons : "Subject2 available lessons should only contain subject2 lessons";
                
                // Verify completion percentages are calculated separately
                Double subject1Completion = progressRepository.calculateSubjectCompletionPercentage(user, subject1);
                Double subject2Completion = progressRepository.calculateSubjectCompletionPercentage(user, subject2);
                
                // Subject1 has 1 completed out of 2 lessons (50% completion)
                assert subject1Completion != null && subject1Completion > 0 : 
                    "Subject1 should have positive completion percentage";
                
                // Subject2 has 1 completed out of 2 lessons (100% completion for started lessons)
                assert subject2Completion != null && subject2Completion > 0 : 
                    "Subject2 should have positive completion percentage";
                
                // Completion percentages should be independent
                assert !subject1Completion.equals(subject2Completion) || 
                       (subject1Completion == 100.0 && subject2Completion == 100.0) : 
                    "Subject completion percentages should be calculated independently";
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
        final String subject1;
        final String subject2;
        final int score1;
        final int score2;
        
        TestData(String subject1, String subject2, int score1, int score2) {
            this.subject1 = subject1;
            this.subject2 = subject2;
            this.score1 = score1;
            this.score2 = score2;
        }
    }
    
    private static final Generator<TestData> testDataGenerator = new Generator<TestData>() {
        @Override
        public TestData next() {
            String[] subjects = {"Java Programming", "Data Structures", "Algorithms", "Full Stack Development", "Interview Preparation"};
            String subject1 = subjects[integers(0, subjects.length - 1).next()];
            String subject2;
            do {
                subject2 = subjects[integers(0, subjects.length - 1).next()];
            } while (subject1.equals(subject2)); // Ensure different subjects
            
            int score1 = integers(60, 100).next(); // Passing scores
            int score2 = integers(60, 100).next(); // Passing scores
            return new TestData(subject1, subject2, score1, score2);
        }
    };
    
    {
        // Register the generator
        Generator.register(TestData.class, testDataGenerator);
    }
}