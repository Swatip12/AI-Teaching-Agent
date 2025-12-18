package com.aiteachingplatform.model;

import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.repository.AIConversationRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static net.java.quickcheck.generator.PrimitiveGenerators.*;

/**
 * **Feature: ai-teaching-platform, Property 16: Data persistence reliability**
 * **Validates: Requirements 8.5**
 * 
 * Property-based test for data persistence reliability across all entities.
 * Tests that user data is persisted reliably without data loss across all core entities.
 */
@DataJpaTest
@ActiveProfiles("test")
public class DataReliabilityProperty extends PropertyTestBase {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private AIConversationRepository aiConversationRepository;
    
    @Test
    @Transactional
    void dataPersistenceReliability() {
        assertProperty(new AbstractCharacteristic<ReliabilityTestData>() {
            @Override
            protected void doSpecify(ReliabilityTestData testData) throws Throwable {
                // Create and save user
                User user = new User(testData.username, testData.email, testData.passwordHash);
                user.getPreferences().setLearningPace(testData.learningPace);
                user.getPreferences().setExplanationStyle(testData.explanationStyle);
                user.getPreferences().setShowHints(testData.showHints);
                user.getPreferences().setCelebrationEnabled(testData.celebrationEnabled);
                user.getPreferences().setDarkMode(testData.darkMode);
                user.getPreferences().setNotificationEnabled(testData.notificationEnabled);
                
                User savedUser = userRepository.save(user);
                Long userId = savedUser.getId();
                
                // Create and save lesson
                Lesson lesson = new Lesson(testData.lessonTitle, testData.subject, testData.sequenceOrder, testData.content);
                lesson.setObjectives(testData.objectives);
                lesson.setDifficulty(testData.difficulty);
                lesson.setEstimatedDurationMinutes(testData.estimatedDuration);
                
                Lesson savedLesson = lessonRepository.save(lesson);
                Long lessonId = savedLesson.getId();
                
                // Create and save progress
                Progress progress = new Progress(savedUser, savedLesson);
                progress.setStatus(testData.progressStatus);
                progress.setCompletionPercentage(testData.completionPercentage);
                progress.setScore(testData.score);
                progress.setAttemptsCount(testData.attemptsCount);
                progress.setTimeSpentMinutes(testData.timeSpentMinutes);
                
                Progress savedProgress = progressRepository.save(progress);
                Long progressId = savedProgress.getId();
                
                // Create and save AI conversation
                AIConversation conversation = new AIConversation(savedUser, savedLesson, testData.studentMessage, testData.conversationType);
                conversation.setAiResponse(testData.aiResponse);
                conversation.setResponseStatus(testData.responseStatus);
                conversation.setResponseTimeMs(testData.responseTimeMs);
                conversation.setContextData(testData.contextData);
                
                AIConversation savedConversation = aiConversationRepository.save(conversation);
                Long conversationId = savedConversation.getId();
                
                // Clear persistence context to ensure data is actually persisted
                userRepository.flush();
                lessonRepository.flush();
                progressRepository.flush();
                aiConversationRepository.flush();
                
                // Verify all entities can be retrieved by ID (data persistence reliability)
                Optional<User> retrievedUser = userRepository.findById(userId);
                Optional<Lesson> retrievedLesson = lessonRepository.findById(lessonId);
                Optional<Progress> retrievedProgress = progressRepository.findById(progressId);
                Optional<AIConversation> retrievedConversation = aiConversationRepository.findById(conversationId);
                
                // Assert all entities are retrievable
                assert retrievedUser.isPresent() : "User should be retrievable after persistence";
                assert retrievedLesson.isPresent() : "Lesson should be retrievable after persistence";
                assert retrievedProgress.isPresent() : "Progress should be retrievable after persistence";
                assert retrievedConversation.isPresent() : "AI Conversation should be retrievable after persistence";
                
                // Verify user data integrity
                User userFromDb = retrievedUser.get();
                assert userFromDb.getUsername().equals(testData.username) : "Username should be preserved";
                assert userFromDb.getEmail().equals(testData.email) : "Email should be preserved";
                assert userFromDb.getPasswordHash().equals(testData.passwordHash) : "Password hash should be preserved";
                assert userFromDb.getPreferences().getLearningPace().equals(testData.learningPace) : "Learning pace preference should be preserved";
                assert userFromDb.getPreferences().getExplanationStyle().equals(testData.explanationStyle) : "Explanation style preference should be preserved";
                assert userFromDb.getPreferences().getShowHints().equals(testData.showHints) : "Show hints preference should be preserved";
                assert userFromDb.getPreferences().getCelebrationEnabled().equals(testData.celebrationEnabled) : "Celebration enabled preference should be preserved";
                assert userFromDb.getPreferences().getDarkMode().equals(testData.darkMode) : "Dark mode preference should be preserved";
                assert userFromDb.getPreferences().getNotificationEnabled().equals(testData.notificationEnabled) : "Notification enabled preference should be preserved";
                
                // Verify lesson data integrity
                Lesson lessonFromDb = retrievedLesson.get();
                assert lessonFromDb.getTitle().equals(testData.lessonTitle) : "Lesson title should be preserved";
                assert lessonFromDb.getSubject().equals(testData.subject) : "Lesson subject should be preserved";
                assert lessonFromDb.getSequenceOrder().equals(testData.sequenceOrder) : "Lesson sequence order should be preserved";
                assert lessonFromDb.getContent().equals(testData.content) : "Lesson content should be preserved";
                assert lessonFromDb.getObjectives().equals(testData.objectives) : "Lesson objectives should be preserved";
                assert lessonFromDb.getDifficulty().equals(testData.difficulty) : "Lesson difficulty should be preserved";
                assert lessonFromDb.getEstimatedDurationMinutes().equals(testData.estimatedDuration) : "Lesson estimated duration should be preserved";
                
                // Verify progress data integrity
                Progress progressFromDb = retrievedProgress.get();
                assert progressFromDb.getStatus().equals(testData.progressStatus) : "Progress status should be preserved";
                assert progressFromDb.getCompletionPercentage().equals(testData.completionPercentage) : "Progress completion percentage should be preserved";
                assert progressFromDb.getScore() == null ? testData.score == null : progressFromDb.getScore().equals(testData.score) : "Progress score should be preserved";
                assert progressFromDb.getAttemptsCount().equals(testData.attemptsCount) : "Progress attempts count should be preserved";
                assert progressFromDb.getTimeSpentMinutes().equals(testData.timeSpentMinutes) : "Progress time spent should be preserved";
                
                // Verify AI conversation data integrity
                AIConversation conversationFromDb = retrievedConversation.get();
                assert conversationFromDb.getStudentMessage().equals(testData.studentMessage) : "Student message should be preserved";
                assert conversationFromDb.getAiResponse().equals(testData.aiResponse) : "AI response should be preserved";
                assert conversationFromDb.getConversationType().equals(testData.conversationType) : "Conversation type should be preserved";
                assert conversationFromDb.getResponseStatus().equals(testData.responseStatus) : "Response status should be preserved";
                assert conversationFromDb.getResponseTimeMs().equals(testData.responseTimeMs) : "Response time should be preserved";
                assert conversationFromDb.getContextData().equals(testData.contextData) : "Context data should be preserved";
                
                // Verify relationships are maintained
                assert progressFromDb.getUser().getId().equals(userId) : "Progress-User relationship should be preserved";
                assert progressFromDb.getLesson().getId().equals(lessonId) : "Progress-Lesson relationship should be preserved";
                assert conversationFromDb.getUser().getId().equals(userId) : "Conversation-User relationship should be preserved";
                assert conversationFromDb.getLesson().getId().equals(lessonId) : "Conversation-Lesson relationship should be preserved";
                
                // Verify timestamps are automatically set
                assert userFromDb.getCreatedAt() != null : "User created timestamp should be set";
                assert lessonFromDb.getCreatedAt() != null : "Lesson created timestamp should be set";
                assert progressFromDb.getCreatedAt() != null : "Progress created timestamp should be set";
                assert conversationFromDb.getTimestamp() != null : "Conversation timestamp should be set";
            }
        });
    }
    
    private static class ReliabilityTestData {
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
        final Progress.ProgressStatus progressStatus;
        final Integer completionPercentage;
        final Integer score;
        final Integer attemptsCount;
        final Integer timeSpentMinutes;
        final String studentMessage;
        final String aiResponse;
        final AIConversation.ConversationType conversationType;
        final AIConversation.ResponseStatus responseStatus;
        final Long responseTimeMs;
        final String contextData;
        
        ReliabilityTestData(String username, String email, String passwordHash,
                           UserPreferences.LearningPace learningPace, UserPreferences.ExplanationStyle explanationStyle,
                           Boolean showHints, Boolean celebrationEnabled, Boolean darkMode, Boolean notificationEnabled,
                           String lessonTitle, String subject, Integer sequenceOrder, String content, String objectives,
                           Lesson.Difficulty difficulty, Integer estimatedDuration,
                           Progress.ProgressStatus progressStatus, Integer completionPercentage, Integer score,
                           Integer attemptsCount, Integer timeSpentMinutes,
                           String studentMessage, String aiResponse, AIConversation.ConversationType conversationType,
                           AIConversation.ResponseStatus responseStatus, Long responseTimeMs, String contextData) {
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
            this.progressStatus = progressStatus;
            this.completionPercentage = completionPercentage;
            this.score = score;
            this.attemptsCount = attemptsCount;
            this.timeSpentMinutes = timeSpentMinutes;
            this.studentMessage = studentMessage;
            this.aiResponse = aiResponse;
            this.conversationType = conversationType;
            this.responseStatus = responseStatus;
            this.responseTimeMs = responseTimeMs;
            this.contextData = contextData;
        }
    }
    
    private static final Generator<ReliabilityTestData> testDataGenerator = new Generator<ReliabilityTestData>() {
        @Override
        public ReliabilityTestData next() {
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
            Progress.ProgressStatus progressStatus = arrays(Progress.ProgressStatus.values()).next();
            Integer completionPercentage = integers(0, 100).next();
            Integer score = booleans().next() ? integers(0, 100).next() : null;
            Integer attemptsCount = integers(0, 10).next();
            Integer timeSpentMinutes = integers(0, 300).next();
            String studentMessage = "Student question " + positiveIntegers().next();
            String aiResponse = "AI response " + positiveIntegers().next();
            AIConversation.ConversationType conversationType = arrays(AIConversation.ConversationType.values()).next();
            AIConversation.ResponseStatus responseStatus = arrays(AIConversation.ResponseStatus.values()).next();
            Long responseTimeMs = longs(100L, 5000L).next();
            String contextData = "Context data " + positiveIntegers().next();
            
            return new ReliabilityTestData(username, email, passwordHash, learningPace, explanationStyle,
                                         showHints, celebrationEnabled, darkMode, notificationEnabled,
                                         lessonTitle, subject, sequenceOrder, content, objectives, difficulty, estimatedDuration,
                                         progressStatus, completionPercentage, score, attemptsCount, timeSpentMinutes,
                                         studentMessage, aiResponse, conversationType, responseStatus, responseTimeMs, contextData);
        }
    };
    
    {
        // Register the generator
        Generator.register(ReliabilityTestData.class, testDataGenerator);
    }
}