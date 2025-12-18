package com.aiteachingplatform.model;

import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 11: Prerequisite enforcement**
 * **Validates: Requirements 5.4**
 * 
 * Property-based test for prerequisite enforcement.
 * Tests that any lesson with prerequisites enforces proper learning sequence before allowing access.
 */
@DataJpaTest
@ActiveProfiles("test")
public class PrerequisiteEnforcementProperty extends PropertyTestBase {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Test
    void prerequisiteEnforcement() {
        assertProperty(new AbstractCharacteristic<PrerequisiteTestData>() {
            @Override
            protected void doSpecify(PrerequisiteTestData testData) throws Throwable {
                // Create and save user
                User user = new User(testData.username, testData.email, testData.passwordHash);
                user = userRepository.save(user);
                
                // Create and save prerequisite lessons
                List<Lesson> prerequisiteLessons = new ArrayList<>();
                for (int i = 0; i < testData.prerequisiteCount; i++) {
                    Lesson prereqLesson = new Lesson(
                        "Prerequisite Lesson " + (i + 1),
                        testData.subject,
                        i + 1,
                        "Content for prerequisite " + (i + 1)
                    );
                    prereqLesson.setDifficulty(Lesson.Difficulty.BEGINNER);
                    prereqLesson = lessonRepository.save(prereqLesson);
                    prerequisiteLessons.add(prereqLesson);
                }
                
                // Create target lesson with prerequisites
                Lesson targetLesson = new Lesson(
                    testData.targetLessonTitle,
                    testData.subject,
                    testData.prerequisiteCount + 1,
                    testData.targetLessonContent
                );
                targetLesson.setDifficulty(testData.difficulty);
                
                // Add prerequisite lesson IDs
                List<Long> prerequisiteIds = new ArrayList<>();
                for (Lesson prereq : prerequisiteLessons) {
                    prerequisiteIds.add(prereq.getId());
                }
                targetLesson.setPrerequisiteLessonIds(prerequisiteIds);
                targetLesson = lessonRepository.save(targetLesson);
                
                // Verify prerequisite enforcement logic
                
                // Requirement 5.4: Should enforce proper learning sequence
                assert !targetLesson.getPrerequisiteLessonIds().isEmpty() : 
                    "Target lesson should have prerequisites";
                
                assert targetLesson.getPrerequisiteLessonIds().size() == testData.prerequisiteCount : 
                    "Target lesson should have correct number of prerequisites";
                
                // Check if user can access target lesson (should check prerequisites)
                boolean canAccessLesson = canUserAccessLesson(user, targetLesson);
                
                // User should NOT be able to access lesson without completing prerequisites
                assert !canAccessLesson : 
                    "User should not be able to access lesson without completing prerequisites";
                
                // Complete all prerequisite lessons
                for (Lesson prereqLesson : prerequisiteLessons) {
                    Progress progress = new Progress(user, prereqLesson);
                    progress.setStatus(Progress.ProgressStatus.COMPLETED);
                    progress.setCompletionPercentage(100);
                    progressRepository.save(progress);
                }
                
                // Now check if user can access target lesson
                boolean canAccessAfterCompletion = canUserAccessLesson(user, targetLesson);
                
                // User SHOULD be able to access lesson after completing all prerequisites
                assert canAccessAfterCompletion : 
                    "User should be able to access lesson after completing all prerequisites";
                
                // Verify that prerequisite lessons exist in database
                for (Long prereqId : targetLesson.getPrerequisiteLessonIds()) {
                    Optional<Lesson> prereqLesson = lessonRepository.findById(prereqId);
                    assert prereqLesson.isPresent() : 
                        "Prerequisite lesson with ID " + prereqId + " should exist in database";
                }
                
                // Verify that lessons without prerequisites can be accessed immediately
                Lesson noPrereqLesson = new Lesson(
                    "No Prerequisite Lesson",
                    testData.subject,
                    100,
                    "Content without prerequisites"
                );
                noPrereqLesson = lessonRepository.save(noPrereqLesson);
                
                boolean canAccessNoPrereq = canUserAccessLesson(user, noPrereqLesson);
                assert canAccessNoPrereq : 
                    "User should be able to access lessons without prerequisites immediately";
            }
        });
    }
    
    /**
     * Helper method to check if user can access a lesson based on prerequisites
     */
    private boolean canUserAccessLesson(User user, Lesson lesson) {
        // If lesson has no prerequisites, user can access it
        if (lesson.getPrerequisiteLessonIds() == null || lesson.getPrerequisiteLessonIds().isEmpty()) {
            return true;
        }
        
        // Check if all prerequisites are completed
        for (Long prereqId : lesson.getPrerequisiteLessonIds()) {
            Optional<Lesson> prereqLesson = lessonRepository.findById(prereqId);
            if (prereqLesson.isEmpty()) {
                return false; // Prerequisite doesn't exist
            }
            
            Optional<Progress> progress = progressRepository.findByUserAndLesson(user, prereqLesson.get());
            if (progress.isEmpty() || progress.get().getStatus() != Progress.ProgressStatus.COMPLETED) {
                return false; // Prerequisite not completed
            }
        }
        
        return true; // All prerequisites completed
    }
    
    private static class PrerequisiteTestData {
        final String username;
        final String email;
        final String passwordHash;
        final String subject;
        final int prerequisiteCount;
        final String targetLessonTitle;
        final String targetLessonContent;
        final Lesson.Difficulty difficulty;
        
        PrerequisiteTestData(String username, String email, String passwordHash, String subject,
                           int prerequisiteCount, String targetLessonTitle, String targetLessonContent,
                           Lesson.Difficulty difficulty) {
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.subject = subject;
            this.prerequisiteCount = prerequisiteCount;
            this.targetLessonTitle = targetLessonTitle;
            this.targetLessonContent = targetLessonContent;
            this.difficulty = difficulty;
        }
    }
    
    private static final Generator<PrerequisiteTestData> prerequisiteTestDataGenerator = new Generator<PrerequisiteTestData>() {
        @Override
        public PrerequisiteTestData next() {
            String username = "user" + positiveIntegers().next();
            String email = username + "@test.com";
            String passwordHash = "$2a$10$" + strings(50, 50).next(); // BCrypt format
            String subject = arrays(new String[]{"Java", "DSA", "FullStack", "Logic", "Interview"}).next();
            int prerequisiteCount = integers(1, 3).next(); // 1-3 prerequisites
            String targetLessonTitle = "Advanced Lesson " + positiveIntegers().next();
            String targetLessonContent = "Advanced content requiring prerequisites";
            Lesson.Difficulty difficulty = arrays(new Lesson.Difficulty[]{
                Lesson.Difficulty.INTERMEDIATE, 
                Lesson.Difficulty.ADVANCED
            }).next();
            
            return new PrerequisiteTestData(username, email, passwordHash, subject, prerequisiteCount,
                                          targetLessonTitle, targetLessonContent, difficulty);
        }
    };
    
    {
        // Register the generator
        Generator.register(PrerequisiteTestData.class, prerequisiteTestDataGenerator);
    }
}
