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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * **Feature: ai-teaching-platform, Property 3: AI adaptive response behavior**
 * **Validates: Requirements 1.4, 1.5**
 * 
 * Property: For any student confusion signal or struggle indicator, 
 * the AI should provide different, simpler explanations and encouraging feedback
 */
@SpringBootTest
@ActiveProfiles("test")
public class AIAdaptiveResponseProperty extends PropertyTestBase {
    
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
        // Use reflection to inject mocks since we can't use @InjectMocks with property tests
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
    void testAIAdaptiveResponseBehavior() {
        assertProperty(new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Setup mocks for this test run
                setupMocksForAdaptiveResponse(testData);
                
                // Create confusion/struggle request
                AITutorRequest confusionRequest = new AITutorRequest(
                    testData.confusionMessage, 
                    AIConversation.ConversationType.CONFUSION
                );
                
                // Process the request
                AITutorResponse response = aiTutorService.processRequest(testData.user, confusionRequest);
                
                // Verify adaptive behavior properties
                assert response != null : "Response should not be null";
                assert response.isSuccessful() : "Response should be successful";
                assert response.getAiResponse() != null : "AI response should not be null";
                
                String aiResponse = response.getAiResponse().toLowerCase();
                
                // Property 1: Response should be encouraging
                assert containsEncouragingLanguage(aiResponse) : 
                    "Response should contain encouraging language for confusion: " + aiResponse;
                
                // Property 2: Response should be simpler (avoid technical jargon)
                assert !containsTechnicalJargon(aiResponse) : 
                    "Response should avoid technical jargon for confused students: " + aiResponse;
                
                // Property 3: Response should acknowledge the confusion
                assert acknowledgesConfusion(aiResponse) : 
                    "Response should acknowledge student confusion: " + aiResponse;
                
                // Property 4: Response should provide different explanation approach
                assert providesAlternativeExplanation(aiResponse) : 
                    "Response should provide alternative explanation approach: " + aiResponse;
            }
        });
    }
    
    private void setupMocksForAdaptiveResponse(TestData testData) {
        // Mock conversation repository
        when(conversationRepository.save(any(AIConversation.class)))
            .thenAnswer(invocation -> {
                AIConversation conv = invocation.getArgument(0);
                conv.setId(1L);
                return conv;
            });
        
        when(conversationRepository.findRecentConversationsByUser(any(), any()))
            .thenReturn(Arrays.asList());
        
        // Mock OpenAI service to return adaptive response
        when(openAiService.createChatCompletion(any()))
            .thenReturn(createMockChatCompletionResult(testData.adaptiveResponse));
    }
    
    private boolean containsEncouragingLanguage(String response) {
        String[] encouragingPhrases = {
            "don't worry", "it's okay", "that's normal", "you're doing great",
            "keep going", "you can do this", "let's try", "no problem",
            "perfectly normal", "happens to everyone", "you're learning"
        };
        
        for (String phrase : encouragingPhrases) {
            if (response.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsTechnicalJargon(String response) {
        String[] technicalTerms = {
            "polymorphism", "encapsulation", "inheritance", "abstraction",
            "instantiation", "serialization", "deserialization", "reflection",
            "annotation", "dependency injection", "aspect-oriented"
        };
        
        for (String term : technicalTerms) {
            if (response.contains(term)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean acknowledgesConfusion(String response) {
        String[] acknowledgmentPhrases = {
            "i understand", "i see", "confusion", "confusing", "unclear",
            "let me explain", "let me help", "makes sense", "understand"
        };
        
        for (String phrase : acknowledgmentPhrases) {
            if (response.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean providesAlternativeExplanation(String response) {
        String[] alternativeIndicators = {
            "another way", "different way", "think of it", "imagine",
            "like", "similar to", "for example", "let's try",
            "step by step", "break it down", "simpler terms"
        };
        
        for (String indicator : alternativeIndicators) {
            if (response.contains(indicator)) {
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
    private static final Generator<TestData> TEST_DATA_GENERATOR = new Generator<TestData>() {
        @Override
        public TestData next() {
            return new TestData(
                createTestUser(),
                generateConfusionMessage(),
                generateAdaptiveResponse()
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
        prefs.setLearningStyle("visual");
        prefs.setPacingPreference("slow");
        user.setPreferences(prefs);
        
        return user;
    }
    
    private static String generateConfusionMessage() {
        String[] confusionMessages = {
            "I don't understand this at all",
            "This is too confusing",
            "I'm completely lost",
            "This doesn't make sense to me",
            "I'm struggling with this concept",
            "Can you explain this differently?",
            "I'm having trouble following this",
            "This is over my head"
        };
        
        return confusionMessages[(int) (Math.random() * confusionMessages.length)];
    }
    
    private static String generateAdaptiveResponse() {
        String[] adaptiveResponses = {
            "Don't worry, this is perfectly normal! Let me explain this in a simpler way. Think of it like organizing your closet - you want to group similar things together.",
            "I understand this can be confusing at first. Let's break it down step by step. Imagine you're giving directions to a friend.",
            "That's a great question! Many students find this tricky. Let me try a different approach using a real-life example you might relate to.",
            "No problem at all! Confusion means you're learning. Let's think of this concept like cooking a recipe - you follow steps in order.",
            "I see where the confusion comes from. Let's try thinking about this differently. It's like having a toolbox where each tool has a specific purpose."
        };
        
        return adaptiveResponses[(int) (Math.random() * adaptiveResponses.length)];
    }
    
    // Test data class
    private static class TestData {
        final User user;
        final String confusionMessage;
        final String adaptiveResponse;
        
        TestData(User user, String confusionMessage, String adaptiveResponse) {
            this.user = user;
            this.confusionMessage = confusionMessage;
            this.adaptiveResponse = adaptiveResponse;
        }
    }
    
    // Override the generator method from QuickCheck
    @Override
    protected void assertProperty(AbstractCharacteristic<?> characteristic) {
        if (characteristic instanceof AbstractCharacteristic) {
            net.java.quickcheck.QuickCheck.forAll(DEFAULT_TEST_RUNS, TEST_DATA_GENERATOR, 
                (AbstractCharacteristic<TestData>) characteristic);
        } else {
            super.assertProperty(characteristic);
        }
    }
}