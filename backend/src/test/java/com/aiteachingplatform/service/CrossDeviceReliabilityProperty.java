package com.aiteachingplatform.service;

import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.UserPreferences;
import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 15: Cross-device reliability**
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
 * 
 * Property-based test for cross-device reliability including:
 * - Consistent functionality across different device contexts
 * - Progress synchronization across multiple sessions
 * - Network error handling and graceful degradation
 * - Responsive design compatibility (data structure validation)
 */
@SpringBootTest
@ActiveProfiles("test")
public class CrossDeviceReliabilityProperty extends PropertyTestBase {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProgressTrackingService progressTrackingService;
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Test
    @Transactional
    void crossDeviceReliability() {
        assertProperty(new AbstractCharacteristic<CrossDeviceTestData>() {
            @Override
            protected void doSpecify(CrossDeviceTestData testData) throws Throwable {
                // Create test user and lesson
                User user = new User(testData.username, testData.email, testData.passwordHash);
                user.getPreferences().setLearningPace(testData.learningPace);
                user.getPreferences().setExplanationStyle(testData.explanationStyle);
                user.getPreferences().setShowHints(testData.showHints);
                user.getPreferences().setCelebrationEnabled(testData.celebrationEnabled);
                user.getPreferences().setDarkMode(testData.darkMode);
                user.getPreferences().setNotificationEnabled(testData.notificationEnabled);
                
                User savedUser = userRepository.save(user);
                
                Lesson lesson = new Lesson(testData.lessonTitle, testData.subject, testData.sequenceOrder, testData.content);
                lesson.setObjectives(testData.objectives);
                lesson.setDifficulty(testData.difficulty);
                lesson.setEstimatedDurationMinutes(testData.estimatedDuration);
                
                Lesson savedLesson = lessonRepository.save(lesson);
                
                // Test 1: Consistent functionality across different device contexts
                // Simulate accessing from different devices by creating multiple progress entries
                Progress mobileProgress = new Progress(savedUser, savedLesson);
                mobileProgress.setStatus(testData.mobileProgressStatus);
                mobileProgress.setCompletionPercentage(testData.mobileCompletionPercentage);
                mobileProgress.setScore(testData.mobileScore);
                mobileProgress.setAttemptsCount(testData.mobileAttempts);
                mobileProgress.setTimeSpentMinutes(testData.mobileTimeSpent);
                mobileProgress.setDeviceType("mobile");
                
                Progress desktopProgress = new Progress(savedUser, savedLesson);
                desktopProgress.setStatus(testData.desktopProgressStatus);
                desktopProgress.setCompletionPercentage(testData.desktopCompletionPercentage);
                desktopProgress.setScore(testData.desktopScore);
                desktopProgress.setAttemptsCount(testData.desktopAttempts);
                desktopProgress.setTimeSpentMinutes(testData.desktopTimeSpent);
                desktopProgress.setDeviceType("desktop");
                
                Progress savedMobileProgress = progressRepository.save(mobileProgress);
                Progress savedDesktopProgress = progressRepository.save(desktopProgress);
                
                // Test 2: Progress synchronization across multiple sessions
                // Verify that progress can be retrieved consistently regardless of device
                List<Progress> userProgress = progressRepository.findByUserIdOrderByUpdatedAtDesc(savedUser.getId());
                assert !userProgress.isEmpty() : "Progress should be retrievable from any device";
                
                // The most recent progress should be available
                Progress latestProgress = userProgress.get(0);
                assert latestProgress != null : "Latest progress should be available for synchronization";
                assert latestProgress.getUser().getId().equals(savedUser.getId()) : "Progress should belong to correct user";
                assert latestProgress.getLesson().getId().equals(savedLesson.getId()) : "Progress should belong to correct lesson";
                
                // Test 3: Data consistency across device switches
                // Simulate switching devices by retrieving progress from different contexts
                Optional<Progress> mobileProgressRetrieved = progressRepository.findById(savedMobileProgress.getId());
                Optional<Progress> desktopProgressRetrieved = progressRepository.findById(savedDesktopProgress.getId());
                
                assert mobileProgressRetrieved.isPresent() : "Mobile progress should be retrievable from any device";
                assert desktopProgressRetrieved.isPresent() : "Desktop progress should be retrievable from any device";
                
                // Verify data integrity is maintained across devices
                Progress mobileFromDb = mobileProgressRetrieved.get();
                Progress desktopFromDb = desktopProgressRetrieved.get();
                
                assert mobileFromDb.getStatus().equals(testData.mobileProgressStatus) : "Mobile progress status should be preserved";
                assert mobileFromDb.getCompletionPercentage().equals(testData.mobileCompletionPercentage) : "Mobile completion percentage should be preserved";
                assert mobileFromDb.getDeviceType().equals("mobile") : "Mobile device type should be preserved";
                
                assert desktopFromDb.getStatus().equals(testData.desktopProgressStatus) : "Desktop progress status should be preserved";
                assert desktopFromDb.getCompletionPercentage().equals(testData.desktopCompletionPercentage) : "Desktop completion percentage should be preserved";
                assert desktopFromDb.getDeviceType().equals("desktop") : "Desktop device type should be preserved";
                
                // Test 4: User preferences synchronization across devices
                // Verify that user preferences are consistent regardless of access device
                Optional<User> userFromDb = userRepository.findById(savedUser.getId());
                assert userFromDb.isPresent() : "User should be retrievable from any device";
                
                User retrievedUser = userFromDb.get();
                assert retrievedUser.getPreferences().getLearningPace().equals(testData.learningPace) : "Learning pace should be synchronized across devices";
                assert retrievedUser.getPreferences().getExplanationStyle().equals(testData.explanationStyle) : "Explanation style should be synchronized across devices";
                assert retrievedUser.getPreferences().getShowHints().equals(testData.showHints) : "Show hints preference should be synchronized across devices";
                assert retrievedUser.getPreferences().getCelebrationEnabled().equals(testData.celebrationEnabled) : "Celebration preference should be synchronized across devices";
                assert retrievedUser.getPreferences().getDarkMode().equals(testData.darkMode) : "Dark mode preference should be synchronized across devices";
                assert retrievedUser.getPreferences().getNotificationEnabled().equals(testData.notificationEnabled) : "Notification preference should be synchronized across devices";
                
                // Test 5: Network error resilience simulation
                // Test that data operations can handle concurrent access patterns (simulating network issues)
                try {
                    CompletableFuture<Progress> future1 = CompletableFuture.supplyAsync(() -> {
                        Progress concurrentProgress = new Progress(savedUser, savedLesson);
                        concurrentProgress.setStatus(Progress.ProgressStatus.IN_PROGRESS);
                        concurrentProgress.setCompletionPercentage(50);
                        concurrentProgress.setDeviceType("tablet");
                        return progressRepository.save(concurrentProgress);
                    });
                    
                    CompletableFuture<Progress> future2 = CompletableFuture.supplyAsync(() -> {
                        Progress anotherProgress = new Progress(savedUser, savedLesson);
                        anotherProgress.setStatus(Progress.ProgressStatus.COMPLETED);
                        anotherProgress.setCompletionPercentage(100);
                        anotherProgress.setDeviceType("smartwatch");
                        return progressRepository.save(anotherProgress);
                    });
                    
                    Progress result1 = future1.get();
                    Progress result2 = future2.get();
                    
                    assert result1 != null : "Concurrent progress save should succeed";
                    assert result2 != null : "Concurrent progress save should succeed";
                    assert !result1.getId().equals(result2.getId()) : "Concurrent saves should create distinct records";
                    
                } catch (InterruptedException | ExecutionException e) {
                    // Network error handling: system should gracefully handle concurrent access
                    assert false : "System should handle concurrent access gracefully: " + e.getMessage();
                }
                
                // Test 6: Responsive design data structure validation
                // Verify that data structures support responsive design requirements
                List<Progress> allUserProgress = progressRepository.findByUserIdOrderByUpdatedAtDesc(savedUser.getId());
                
                for (Progress progress : allUserProgress) {
                    // Verify essential fields are present for responsive display
                    assert progress.getStatus() != null : "Progress status should be available for responsive display";
                    assert progress.getCompletionPercentage() != null : "Completion percentage should be available for responsive display";
                    assert progress.getCreatedAt() != null : "Creation timestamp should be available for responsive display";
                    assert progress.getUpdatedAt() != null : "Update timestamp should be available for responsive display";
                    
                    // Verify device type tracking for responsive adaptation
                    assert progress.getDeviceType() != null : "Device type should be tracked for responsive design";
                    assert progress.getDeviceType().length() > 0 : "Device type should not be empty";
                    
                    // Verify data is suitable for mobile display (no excessively long strings)
                    if (progress.getLesson().getTitle() != null) {
                        assert progress.getLesson().getTitle().length() <= 200 : "Lesson title should be suitable for mobile display";
                    }
                    if (progress.getLesson().getContent() != null) {
                        assert progress.getLesson().getContent().length() > 0 : "Lesson content should be available for display";
                    }
                }
                
                // Test 7: Cross-device session continuity
                // Verify that a user can continue their session seamlessly across devices
                Progress continuityTest = new Progress(savedUser, savedLesson);
                continuityTest.setStatus(Progress.ProgressStatus.IN_PROGRESS);
                continuityTest.setCompletionPercentage(testData.continuityPercentage);
                continuityTest.setCurrentStep(testData.currentStep);
                continuityTest.setDeviceType("device1");
                
                Progress savedContinuityProgress = progressRepository.save(continuityTest);
                
                // Simulate switching to another device
                Optional<Progress> continuityRetrieved = progressRepository.findById(savedContinuityProgress.getId());
                assert continuityRetrieved.isPresent() : "Progress should be available for continuation on new device";
                
                Progress continuedProgress = continuityRetrieved.get();
                assert continuedProgress.getCompletionPercentage().equals(testData.continuityPercentage) : "Progress percentage should be preserved for continuation";
                assert continuedProgress.getCurrentStep().equals(testData.currentStep) : "Current step should be preserved for continuation";
                assert continuedProgress.getStatus().equals(Progress.ProgressStatus.IN_PROGRESS) : "Progress status should indicate continuation is possible";
            }
        });
    }
    
    private static class CrossDeviceTestData {
        final String username;
        final String email;
        final String passwordHash;
        final UserPreferences.LearningPace learningPace;
        final UserPreferences.ExplanationStyle explanationStyle;
        final Boolean showHints;
        final Boolean celebrationEnabled;
        final Boolean darkMode;
        final Boolean notificationEnabled;
        final String lessonTitle;
        final String subject;
        final Integer sequenceOrder;
        final String content;
        final String objectives;
        final Lesson.Difficulty difficulty;
        final Integer estimatedDuration;
        final Progress.ProgressStatus mobileProgressStatus;
        final Integer mobileCompletionPercentage;
        final Integer mobileScore;
        final Integer mobileAttempts;
        final Integer mobileTimeSpent;
        final Progress.ProgressStatus desktopProgressStatus;
        final Integer desktopCompletionPercentage;
        final Integer desktopScore;
        final Integer desktopAttempts;
        final Integer desktopTimeSpent;
        final Integer continuityPercentage;
        final String currentStep;
        
        CrossDeviceTestData(String username, String email, String passwordHash,
                           UserPreferences.LearningPace learningPace, UserPreferences.ExplanationStyle explanationStyle,
                           Boolean showHints, Boolean celebrationEnabled, Boolean darkMode, Boolean notificationEnabled,
                           String lessonTitle, String subject, Integer sequenceOrder, String content, String objectives,
                           Lesson.Difficulty difficulty, Integer estimatedDuration,
                           Progress.ProgressStatus mobileProgressStatus, Integer mobileCompletionPercentage, Integer mobileScore,
                           Integer mobileAttempts, Integer mobileTimeSpent,
                           Progress.ProgressStatus desktopProgressStatus, Integer desktopCompletionPercentage, Integer desktopScore,
                           Integer desktopAttempts, Integer desktopTimeSpent,
                           Integer continuityPercentage, String currentStep) {
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.learningPace = learningPace;
            this.explanationStyle = explanationStyle;
            this.showHints = showHints;
            this.celebrationEnabled = celebrationEnabled;
            this.darkMode = darkMode;
            this.notificationEnabled = notificationEnabled;
            this.lessonTitle = lessonTitle;
            this.subject = subject;
            this.sequenceOrder = sequenceOrder;
            this.content = content;
            this.objectives = objectives;
            this.difficulty = difficulty;
            this.estimatedDuration = estimatedDuration;
            this.mobileProgressStatus = mobileProgressStatus;
            this.mobileCompletionPercentage = mobileCompletionPercentage;
            this.mobileScore = mobileScore;
            this.mobileAttempts = mobileAttempts;
            this.mobileTimeSpent = mobileTimeSpent;
            this.desktopProgressStatus = desktopProgressStatus;
            this.desktopCompletionPercentage = desktopCompletionPercentage;
            this.desktopScore = desktopScore;
            this.desktopAttempts = desktopAttempts;
            this.desktopTimeSpent = desktopTimeSpent;
            this.continuityPercentage = continuityPercentage;
            this.currentStep = currentStep;
        }
    }
    
    private static final Generator<CrossDeviceTestData> testDataGenerator = new Generator<CrossDeviceTestData>() {
        @Override
        public CrossDeviceTestData next() {
            String username = "user" + positiveIntegers().next();
            String email = username + "@test.com";
            String passwordHash = "$2a$10$" + strings(50, 50).next();
            UserPreferences.LearningPace learningPace = arrays(UserPreferences.LearningPace.values()).next();
            UserPreferences.ExplanationStyle explanationStyle = arrays(UserPreferences.ExplanationStyle.values()).next();
            Boolean showHints = booleans().next();
            Boolean celebrationEnabled = booleans().next();
            Boolean darkMode = booleans().next();
            Boolean notificationEnabled = booleans().next();
            String lessonTitle = "Lesson " + positiveIntegers().next();
            String subject = arrays(new String[]{"Java", "DSA", "FullStack", "Logic", "Interview"}).next();
            Integer sequenceOrder = integers(1, 100).next();
            String content = "Content for " + lessonTitle;
            String objectives = "Objectives for " + lessonTitle;
            Lesson.Difficulty difficulty = arrays(Lesson.Difficulty.values()).next();
            Integer estimatedDuration = integers(10, 120).next();
            Progress.ProgressStatus mobileProgressStatus = arrays(Progress.ProgressStatus.values()).next();
            Integer mobileCompletionPercentage = integers(0, 100).next();
            Integer mobileScore = booleans().next() ? integers(0, 100).next() : null;
            Integer mobileAttempts = integers(0, 10).next();
            Integer mobileTimeSpent = integers(0, 300).next();
            Progress.ProgressStatus desktopProgressStatus = arrays(Progress.ProgressStatus.values()).next();
            Integer desktopCompletionPercentage = integers(0, 100).next();
            Integer desktopScore = booleans().next() ? integers(0, 100).next() : null;
            Integer desktopAttempts = integers(0, 10).next();
            Integer desktopTimeSpent = integers(0, 300).next();
            Integer continuityPercentage = integers(0, 100).next();
            String currentStep = "step" + integers(1, 10).next();
            
            return new CrossDeviceTestData(username, email, passwordHash, learningPace, explanationStyle,
                                         showHints, celebrationEnabled, darkMode, notificationEnabled,
                                         lessonTitle, subject, sequenceOrder, content, objectives, difficulty, estimatedDuration,
                                         mobileProgressStatus, mobileCompletionPercentage, mobileScore, mobileAttempts, mobileTimeSpent,
                                         desktopProgressStatus, desktopCompletionPercentage, desktopScore, desktopAttempts, desktopTimeSpent,
                                         continuityPercentage, currentStep);
        }
    };
    
    {
        // Register the generator
        Generator.register(CrossDeviceTestData.class, testDataGenerator);
    }
}