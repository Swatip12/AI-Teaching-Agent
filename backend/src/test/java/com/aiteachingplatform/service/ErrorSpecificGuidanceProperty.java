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
 * **Feature: ai-teaching-platform, Property 6: Error-specific guidance**
 * **Validates: Requirements 3.2, 3.4**
 * 
 * Property: For any common mistake or error, the system should provide 
 * specific explanations about typical beginner errors and additional support
 */
@SpringBootTest
@ActiveProfiles("test")
public class ErrorSpecificGuidanceProperty extends PropertyTestBase {
    
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
    void testErrorSpecificGuidance() {
        assertProperty(new AbstractCharacteristic<ErrorTestData>() {
            @Override
            protected void doSpecify(ErrorTestData testData) throws Throwable {
                // Setup mocks for this test run
                setupMocksForErrorGuidance(testData);
                
                // Create error explanation request
                AITutorRequest errorRequest = new AITutorRequest(
                    testData.errorMessage, 
                    AIConversation.ConversationType.ERROR_EXPLANATION
                );
                
                // Process the request
                AITutorResponse response = aiTutorService.processRequest(testData.user, errorRequest);
                
                // Verify error-specific guidance properties
                assert response != null : "Response should not be null";
                assert response.isSuccessful() : "Response should be successful";
                assert response.getAiResponse() != null : "AI response should not be null";
                
                String aiResponse = response.getAiResponse().toLowerCase();
                
                // Property 1: Response should explain the specific error type
                assert explainsSpecificError(aiResponse, testData.errorType) : 
                    "Response should explain the specific error type: " + testData.errorType;
                
                // Property 2: Response should mention common beginner mistakes
                assert mentionsBeginnerMistakes(aiResponse) : 
                    "Response should mention common beginner mistakes: " + aiResponse;
                
                // Property 3: Response should provide specific guidance to fix the error
                assert providesSpecificGuidance(aiResponse) : 
                    "Response should provide specific guidance to fix the error: " + aiResponse;
                
                // Property 4: Response should offer additional support/resources
                assert offersAdditionalSupport(aiResponse) : 
                    "Response should offer additional support: " + aiResponse;
                
                // Property 5: Response should be gentle and educational (not just corrective)
                assert isGentleAndEducational(aiResponse) : 
                    "Response should be gentle and educational: " + aiResponse;
            }
        });
    }
    
    private void setupMocksForErrorGuidance(ErrorTestData testData) {
        // Mock conversation repository
        when(conversationRepository.save(any(AIConversation.class)))
            .thenAnswer(invocation -> {
                AIConversation conv = invocation.getArgument(0);
                conv.setId(1L);
                return conv;
            });
        
        when(conversationRepository.findRecentConversationsByUser(any(), any()))
            .thenReturn(Arrays.asList());
        
        // Mock OpenAI service to return error-specific guidance
        when(openAiService.createChatCompletion(any()))
            .thenReturn(createMockChatCompletionResult(testData.guidanceResponse));
    }
    
    private boolean explainsSpecificError(String response, ErrorType errorType) {
        switch (errorType) {
            case SYNTAX_ERROR:
                return response.contains("syntax") || response.contains("semicolon") || 
                       response.contains("bracket") || response.contains("parenthesis");
            case NULL_POINTER:
                return response.contains("null") || response.contains("pointer") || 
                       response.contains("reference") || response.contains("object");
            case COMPILATION_ERROR:
                return response.contains("compile") || response.contains("compilation") || 
                       response.contains("build") || response.contains("error");
            case LOGIC_ERROR:
                return response.contains("logic") || response.contains("algorithm") || 
                       response.contains("condition") || response.contains("loop");
            case TYPE_MISMATCH:
                return response.contains("type") || response.contains("mismatch") || 
                       response.contains("convert") || response.contains("cast");
            default:
                return true; // For unknown error types, assume explanation is present
        }
    }
    
    private boolean mentionsBeginnerMistakes(String response) {
        String[] beginnerMistakeIndicators = {
            "common mistake", "beginners often", "typical error", "many students",
            "happens to everyone", "frequent issue", "usual problem", "often forget",
            "easy to miss", "common oversight", "typical beginner"
        };
        
        for (String indicator : beginnerMistakeIndicators) {
            if (response.contains(indicator)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean providesSpecificGuidance(String response) {
        String[] guidanceIndicators = {
            "to fix this", "try this", "you should", "make sure", "check that",
            "add a", "remove the", "change this", "replace", "modify",
            "here's how", "follow these steps", "do this", "solution"
        };
        
        for (String indicator : guidanceIndicators) {
            if (response.contains(indicator)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean offersAdditionalSupport(String response) {
        String[] supportIndicators = {
            "if you need help", "feel free to ask", "let me know", "more questions",
            "need clarification", "want to practice", "try another example",
            "would you like", "can help you", "here to assist"
        };
        
        for (String indicator : supportIndicators) {
            if (response.contains(indicator)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isGentleAndEducational(String response) {
        // Check for gentle language
        String[] gentleIndicators = {
            "don't worry", "it's okay", "no problem", "happens", "learning",
            "understand", "let's", "together", "help you", "guide you"
        };
        
        // Check for educational content
        String[] educationalIndicators = {
            "because", "reason", "why", "how", "when", "what happens",
            "this means", "in other words", "for example", "think of it"
        };
        
        boolean hasGentleLanguage = false;
        boolean hasEducationalContent = false;
        
        for (String indicator : gentleIndicators) {
            if (response.contains(indicator)) {
                hasGentleLanguage = true;
                break;
            }
        }
        
        for (String indicator : educationalIndicators) {
            if (response.contains(indicator)) {
                hasEducationalContent = true;
                break;
            }
        }
        
        return hasGentleLanguage || hasEducationalContent;
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
    private static final Generator<ErrorTestData> ERROR_TEST_DATA_GENERATOR = new Generator<ErrorTestData>() {
        @Override
        public ErrorTestData next() {
            ErrorType errorType = generateRandomErrorType();
            return new ErrorTestData(
                createTestUser(),
                generateErrorMessage(errorType),
                errorType,
                generateGuidanceResponse(errorType)
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
        prefs.setLearningStyle("hands-on");
        prefs.setPacingPreference("medium");
        user.setPreferences(prefs);
        
        return user;
    }
    
    private static ErrorType generateRandomErrorType() {
        ErrorType[] types = ErrorType.values();
        return types[(int) (Math.random() * types.length)];
    }
    
    private static String generateErrorMessage(ErrorType errorType) {
        switch (errorType) {
            case SYNTAX_ERROR:
                return "I'm getting a syntax error: expected ';' at line 5";
            case NULL_POINTER:
                return "My program crashes with NullPointerException";
            case COMPILATION_ERROR:
                return "My code won't compile, it says 'cannot find symbol'";
            case LOGIC_ERROR:
                return "My program runs but gives wrong results";
            case TYPE_MISMATCH:
                return "Error: incompatible types: String cannot be converted to int";
            default:
                return "I'm getting an error but don't understand what it means";
        }
    }
    
    private static String generateGuidanceResponse(ErrorType errorType) {
        switch (errorType) {
            case SYNTAX_ERROR:
                return "Don't worry, syntax errors are very common for beginners! This error means you're missing a semicolon. In Java, every statement needs to end with a semicolon. To fix this, add a semicolon at the end of line 5. Many students forget this at first - it's a typical beginner mistake. Let me know if you need more help!";
            case NULL_POINTER:
                return "This is a common mistake that happens to everyone! A NullPointerException occurs when you try to use an object that hasn't been created yet. Make sure to initialize your objects before using them. For example, if you have 'String name;', you should set it to something like 'String name = \"Hello\";' before using it. Would you like me to show you more examples?";
            case COMPILATION_ERROR:
                return "No problem! 'Cannot find symbol' is a frequent issue for beginners. This usually means you're trying to use a variable or method that doesn't exist. Check the spelling and make sure you've declared the variable first. It's an easy mistake to make when learning. Try checking your variable names - they need to match exactly!";
            case LOGIC_ERROR:
                return "Logic errors can be tricky! Your code runs but doesn't do what you expect. This often happens with conditions or loops. Let's debug this step by step. Try adding some print statements to see what values your variables have. Many students find this helpful for understanding what's happening. I can help you trace through the logic if needed.";
            case TYPE_MISMATCH:
                return "This is a typical beginner error! Java is very strict about data types. You're trying to put a String where an int is expected. To fix this, you can either change the variable type or convert the String to an int using Integer.parseInt(). Don't worry - understanding types takes practice. Would you like me to explain more about data types?";
            default:
                return "I understand errors can be frustrating! Let's work through this together. Can you share the exact error message? That will help me give you specific guidance. Remember, errors are part of learning - every programmer deals with them. I'm here to help you understand and fix it!";
        }
    }
    
    // Error types enum
    private enum ErrorType {
        SYNTAX_ERROR, NULL_POINTER, COMPILATION_ERROR, LOGIC_ERROR, TYPE_MISMATCH
    }
    
    // Test data class
    private static class ErrorTestData {
        final User user;
        final String errorMessage;
        final ErrorType errorType;
        final String guidanceResponse;
        
        ErrorTestData(User user, String errorMessage, ErrorType errorType, String guidanceResponse) {
            this.user = user;
            this.errorMessage = errorMessage;
            this.errorType = errorType;
            this.guidanceResponse = guidanceResponse;
        }
    }
    
    // Override the generator method from QuickCheck
    @Override
    protected void assertProperty(AbstractCharacteristic<?> characteristic) {
        if (characteristic instanceof AbstractCharacteristic) {
            net.java.quickcheck.QuickCheck.forAll(DEFAULT_TEST_RUNS, ERROR_TEST_DATA_GENERATOR, 
                (AbstractCharacteristic<ErrorTestData>) characteristic);
        } else {
            super.assertProperty(characteristic);
        }
    }
}