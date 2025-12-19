package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.AITutorRequest;
import com.aiteachingplatform.dto.AITutorResponse;
import com.aiteachingplatform.model.AIConversation;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.UserPreferences;
import com.aiteachingplatform.repository.AIConversationRepository;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import com.theokanning.openai.service.OpenAiService;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * **Feature: ai-teaching-platform, Property 7: Success reinforcement**
 * **Validates: Requirements 3.5**
 * 
 * Property: For any successful completion or correct answer, 
 * the system should provide encouraging feedback
 */
@SpringBootTest
@ActiveProfiles("test")
public class SuccessReinforcementProperty extends PropertyTestBase {
    
    @Mock
    private OpenAiService openAiService;
    
    @Mock
    private AIConversationRepository conversationRepository;
    
    @Mock
    private LessonRepository lessonRepository;
    
    private AITutorService aiTutorService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiTutorService = new AITutorService();
        // Use reflection to inject mocks
        try {
            var openAiField = AITutorService.class.getDeclaredField("openAiService");
            openAiField.setAccessible(true);
            openAiField.set(aiTutorService, openAiService);
            
            var conversationField = AITutorService.class.getDeclaredField("conversationRepository");
            conversationField.setAccessible(true);
            conversationField.set(aiTutorService, conversationRepository);
            
            var lessonField = AITutorService.class.getDeclaredField("lessonRepository");
            lessonField.setAccessible(true);
            lessonField.set(aiTutorService, lessonRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks", e);
        }
    }
    
    @Test
    void testSuccessReinforcement() {
        assertProperty(new AbstractCharacteristic<SuccessTestData>() {
            @Override
            protected void doSpecify(SuccessTestData testData) throws Throwable {
                // Setup mocks for this test run
                setupMocksForSuccessReinforcement(testData);
                
                // Create feedback request indicating success
                AITutorRequest successRequest = new AITutorRequest(
                    testData.successMessage, 
                    AIConversation.ConversationType.FEEDBACK
                );
                
                // Process the request
                AITutorResponse response = aiTutorService.processRequest(testData.user, successRequest);
                
                // Verify success reinforcement properties
                assert response != null : "Response should not be null";
                assert response.isSuccessful() : "Response should be successful";
                assert response.getAiResponse() != null : "AI response should not be null";
                
                String aiResponse = response.getAiResponse().toLowerCase();
                
                // Property 1: Response should contain positive reinforcement
                assert containsPositiveReinforcement(aiResponse) : 
                    "Response should contain positive reinforcement: " + aiResponse;
                
                // Property 2: Response should acknowledge the specific achievement
                assert acknowledgesAchievement(aiResponse, testData.achievementType) : 
                    "Response should acknowledge the specific achievement: " + testData.achievementType;
                
                // Property 3: Response should encourage continued learning
                assert encouragesContinuedLearning(aiResponse) : 
                    "Response should encourage continued learning: " + aiResponse;
                
                // Property 4: Response should be celebratory but not overwhelming
                assert isCelebratoryButAppropriate(aiResponse) : 
                    "Response should be celebratory but appropriate: " + aiResponse;
                
                // Property 5: Response should build confidence for future challenges
                assert buildsConfidence(aiResponse) : 
                    "Response should build confidence for future challenges: " + aiResponse;
            }
        });
    }
    
    private void setupMocksForSuccessReinforcement(SuccessTestData testData) {
        // Mock conversation repository
        when(conversationRepository.save(any(AIConversation.class)))
            .thenAnswer(invocation -> {
                AIConversation conv = invocation.getArgument(0);
                conv.setId(1L);
                return conv;
            });
        
        when(conversationRepository.findRecentConversationsByUser(any(), any()))
            .thenReturn(Arrays.asList());
        
        // Mock OpenAI service to return success reinforcement
        when(openAiService.createChatCompletion(any()))
            .thenReturn(createMockChatCompletionResult(testData.reinforcementResponse));
    }
    
    private boolean containsPositiveReinforcement(String response) {
        String[] positiveWords = {
            "great", "excellent", "wonderful", "fantastic", "awesome", "amazing",
            "well done", "good job", "nice work", "perfect", "brilliant", "outstanding",
            "impressive", "superb", "terrific", "congratulations", "bravo", "kudos"
        };
        
        for (String word : positiveWords) {
            if (response.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean acknowledgesAchievement(String response, AchievementType achievementType) {
        switch (achievementType) {
            case CORRECT_ANSWER:
                return response.contains("correct") || response.contains("right") || 
                       response.contains("answer") || response.contains("got it");
            case COMPLETED_EXERCISE:
                return response.contains("completed") || response.contains("finished") || 
                       response.contains("exercise") || response.contains("done");
            case UNDERSTOOD_CONCEPT:
                return response.contains("understand") || response.contains("grasp") || 
                       response.contains("concept") || response.contains("get it");
            case SOLVED_PROBLEM:
                return response.contains("solved") || response.contains("solution") || 
                       response.contains("problem") || response.contains("figured");
            case IMPROVED_SKILL:
                return response.contains("improved") || response.contains("better") || 
                       response.contains("progress") || response.contains("skill");
            default:
                return true; // For unknown achievement types, assume acknowledgment is present
        }
    }
    
    private boolean encouragesContinuedLearning(String response) {
        String[] encouragementIndicators = {
            "keep going", "continue", "next", "more", "further", "advance",
            "ready for", "try", "practice", "explore", "learn", "challenge",
            "build on", "expand", "develop", "grow"
        };
        
        for (String indicator : encouragementIndicators) {
            if (response.contains(indicator)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCelebratoryButAppropriate(String response) {
        // Check for celebratory language
        String[] celebratoryWords = {
            "celebrate", "proud", "achievement", "success", "victory", "win",
            "accomplishment", "milestone", "breakthrough"
        };
        
        // Check that it's not overly dramatic
        String[] overly_dramatic = {
            "!!!!", "amazing!!!!", "incredible!!!!", "unbelievable!!!!"
        };
        
        boolean hasCelebration = false;
        for (String word : celebratoryWords) {
            if (response.contains(word)) {
                hasCelebration = true;
                break;
            }
        }
        
        // If no explicit celebration words, positive reinforcement counts as appropriate celebration
        if (!hasCelebration) {
            hasCelebration = containsPositiveReinforcement(response);
        }
        
        // Check it's not overly dramatic
        boolean isOverlyDramatic = false;
        for (String dramatic : overly_dramatic) {
            if (response.contains(dramatic)) {
                isOverlyDramatic = true;
                break;
            }
        }
        
        return hasCelebration && !isOverlyDramatic;
    }
    
    private boolean buildsConfidence(String response) {
        String[] confidenceBuilders = {
            "you can", "you're able", "you have", "you've shown", "you're ready",
            "confident", "capable", "skilled", "talented", "prepared",
            "trust yourself", "believe in", "you've got this", "you're learning",
            "you're improving", "you're getting", "you understand"
        };
        
        for (String builder : confidenceBuilders) {
            if (response.contains(builder)) {
                return true;
            }
        }
        return false;
    }
    
    private com.theokanning.openai.completion.chat.ChatCompletionResult createMockChatCompletionResult(String content) {
        var choice = new com.theokanning.openai.completion.chat.ChatCompletionChoice();
        var message = new com.theokanning.openai.completion.chat.ChatMessage();
        message.setContent(content);
        choice.setMessage(message);
        
        var result = new com.theokanning.openai.completion.chat.ChatCompletionResult();
        result.setChoices(Arrays.asList(choice));
        
        return result;
    }
    
    // Generator for test data
    private static final Generator<SuccessTestData> SUCCESS_TEST_DATA_GENERATOR = new Generator<SuccessTestData>() {
        @Override
        public SuccessTestData next() {
            AchievementType achievementType = generateRandomAchievementType();
            return new SuccessTestData(
                createTestUser(),
                generateSuccessMessage(achievementType),
                achievementType,
                generateReinforcementResponse(achievementType)
            );
        }
    };
    
    private static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        
        UserPreferences prefs = new UserPreferences();
        prefs.setLearningStyle("interactive");
        prefs.setPacingPreference("fast");
        user.setPreferences(prefs);
        
        return user;
    }
    
    private static AchievementType generateRandomAchievementType() {
        AchievementType[] types = AchievementType.values();
        return types[(int) (Math.random() * types.length)];
    }
    
    private static String generateSuccessMessage(AchievementType achievementType) {
        switch (achievementType) {
            case CORRECT_ANSWER:
                return "I got the right answer! The output is 42.";
            case COMPLETED_EXERCISE:
                return "I finished the coding exercise successfully!";
            case UNDERSTOOD_CONCEPT:
                return "I think I finally understand how loops work!";
            case SOLVED_PROBLEM:
                return "I solved the problem! My algorithm works correctly.";
            case IMPROVED_SKILL:
                return "I'm getting much better at debugging my code.";
            default:
                return "I did it! I completed the task successfully.";
        }
    }
    
    private static String generateReinforcementResponse(AchievementType achievementType) {
        switch (achievementType) {
            case CORRECT_ANSWER:
                return "Excellent work! You got the correct answer. This shows you really understand the concept. You're ready to tackle more challenging problems. Keep up the great progress!";
            case COMPLETED_EXERCISE:
                return "Fantastic! You completed the exercise successfully. That's a real achievement. You're building strong programming skills. Ready to try the next challenge?";
            case UNDERSTOOD_CONCEPT:
                return "Wonderful! Understanding loops is a major milestone in programming. You should be proud of this breakthrough. Now you can use this knowledge to solve more complex problems.";
            case SOLVED_PROBLEM:
                return "Outstanding! Solving problems with your own algorithm shows real programming thinking. You're developing the skills of a true programmer. Keep practicing and you'll continue to improve.";
            case IMPROVED_SKILL:
                return "Great progress! Improving your debugging skills is so important. You're becoming more confident and capable. This improvement will help you with all your future coding projects.";
            default:
                return "Well done! Your success shows that you're learning and growing as a programmer. You should feel proud of your accomplishment. Keep up the excellent work!";
        }
    }
    
    // Achievement types enum
    private enum AchievementType {
        CORRECT_ANSWER, COMPLETED_EXERCISE, UNDERSTOOD_CONCEPT, SOLVED_PROBLEM, IMPROVED_SKILL
    }
    
    // Test data class
    private static class SuccessTestData {
        final User user;
        final String successMessage;
        final AchievementType achievementType;
        final String reinforcementResponse;
        
        SuccessTestData(User user, String successMessage, AchievementType achievementType, String reinforcementResponse) {
            this.user = user;
            this.successMessage = successMessage;
            this.achievementType = achievementType;
            this.reinforcementResponse = reinforcementResponse;
        }
    }
    
    // Override the generator method from QuickCheck
    @Override
    protected void assertProperty(AbstractCharacteristic<?> characteristic) {
        if (characteristic instanceof AbstractCharacteristic) {
            net.java.quickcheck.QuickCheck.forAll(DEFAULT_TEST_RUNS, SUCCESS_TEST_DATA_GENERATOR, 
                (AbstractCharacteristic<SuccessTestData>) characteristic);
        } else {
            super.assertProperty(characteristic);
        }
    }
}