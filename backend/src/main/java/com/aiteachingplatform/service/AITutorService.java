package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.AITutorRequest;
import com.aiteachingplatform.dto.AITutorResponse;
import com.aiteachingplatform.model.*;
import com.aiteachingplatform.repository.AIConversationRepository;
import com.aiteachingplatform.exception.AIServiceException;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.service.LoggingService;

import java.util.Map;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for AI-powered tutoring interactions using OpenAI GPT
 */
@Service
@Transactional
public class AITutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AITutorService.class);
    
    private static final String GPT_MODEL = "gpt-4";
    private static final int MAX_TOKENS = 1000;
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_CONTEXT_MESSAGES = 10;
    
    @Autowired
    private OpenAiService openAiService;
    
    @Autowired
    private AIConversationRepository conversationRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private LoggingService loggingService;
    
    /**
     * Process AI tutor request and generate response
     */
    public AITutorResponse processRequest(User user, AITutorRequest request) {
        logger.info("Processing AI tutor request for user: {} with type: {}", 
                   user.getUsername(), request.getConversationType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create conversation record
            AIConversation conversation = createConversation(user, request);
            
            // Build context and generate response
            List<ChatMessage> messages = buildConversationContext(user, request);
            String aiResponse = generateAIResponse(messages);
            
            // Validate and filter response
            String filteredResponse = validateAndFilterResponse(aiResponse, request.getConversationType());
            
            // Calculate response time
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Update conversation with response
            conversation.setAIResponse(filteredResponse, responseTime);
            conversationRepository.save(conversation);
            
            // Log successful AI service interaction
            loggingService.logAIServiceInteraction(
                "PROCESS_REQUEST", 
                "OpenAI", 
                true, 
                responseTime, 
                user.getId(), 
                null
            );
            
            // Create response DTO
            AITutorResponse response = AITutorResponse.success(filteredResponse, responseTime);
            response.setConversationId(conversation.getId());
            response.setConfidenceScore(calculateConfidenceScore(filteredResponse));
            response.setRequiresFollowUp(determineFollowUpNeed(request.getConversationType(), filteredResponse));
            
            logger.info("Successfully processed AI request in {}ms", responseTime);
            return response;
            
        } catch (AIServiceException e) {
            logger.error("AI service error for user: {}", user.getUsername(), e);
            
            // Log AI service interaction
            loggingService.logAIServiceInteraction(
                "PROCESS_REQUEST", 
                e.getServiceType(), 
                false, 
                System.currentTimeMillis() - startTime, 
                user.getId(), 
                e.getMessage()
            );
            
            // Create failed conversation record
            AIConversation failedConversation = createConversation(user, request);
            failedConversation.markAsFailed("AI Service Error: " + e.getMessage());
            conversationRepository.save(failedConversation);
            
            // Return fallback response based on conversation type
            String fallbackResponse = getFallbackResponse(request.getConversationType(), request.getStudentMessage());
            return AITutorResponse.fallback(fallbackResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error processing AI tutor request for user: {}", user.getUsername(), e);
            
            // Log error with context
            Map<String, Object> context = Map.of(
                "conversationType", request.getConversationType().toString(),
                "lessonId", request.getLessonId() != null ? request.getLessonId().toString() : "none",
                "messageLength", request.getStudentMessage().length()
            );
            
            loggingService.logError(
                "AI_TUTOR_REQUEST", 
                "Unexpected error processing AI tutor request", 
                e, 
                user.getId(), 
                context
            );
            
            // Create failed conversation record
            AIConversation failedConversation = createConversation(user, request);
            failedConversation.markAsFailed("System Error: " + e.getMessage());
            conversationRepository.save(failedConversation);
            
            return AITutorResponse.failure("I'm having trouble responding right now. Please try again in a moment.");
        }
    }
    
    /**
     * Get conversation history for context
     */
    public List<AIConversation> getConversationHistory(User user, Long lessonId, int limit) {
        if (lessonId != null) {
            Optional<Lesson> lesson = lessonRepository.findById(lessonId);
            if (lesson.isPresent()) {
                List<AIConversation> conversations = conversationRepository
                    .findByUserAndLessonOrderByTimestampAsc(user, lesson.get());
                return conversations.size() > limit ? 
                    conversations.subList(Math.max(0, conversations.size() - limit), conversations.size()) :
                    conversations;
            }
        }
        
        List<AIConversation> recentConversations = conversationRepository
            .findRecentConversationsByUser(user, LocalDateTime.now().minusHours(24));
        return recentConversations.size() > limit ?
            recentConversations.subList(0, limit) : recentConversations;
    }
    
    /**
     * Add feedback to conversation
     */
    public void addConversationFeedback(Long conversationId, Integer rating, String comment) {
        Optional<AIConversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isPresent()) {
            conversation.get().addFeedback(rating, comment);
            conversationRepository.save(conversation.get());
            logger.info("Added feedback to conversation {}: rating={}, comment={}", 
                       conversationId, rating, comment);
        }
    }
    
    private AIConversation createConversation(User user, AITutorRequest request) {
        Lesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId()).orElse(null);
        }
        
        AIConversation conversation = new AIConversation(user, lesson, 
            request.getStudentMessage(), request.getConversationType());
        conversation.setContextData(request.getContextData());
        
        return conversationRepository.save(conversation);
    }
    
    private List<ChatMessage> buildConversationContext(User user, AITutorRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // Add system prompt based on conversation type
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            buildSystemPrompt(request.getConversationType(), request)));
        
        // Add recent conversation history for context
        List<AIConversation> history = getConversationHistory(user, request.getLessonId(), MAX_CONTEXT_MESSAGES);
        for (AIConversation conv : history) {
            if (conv.getStudentMessage() != null) {
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), conv.getStudentMessage()));
            }
            if (conv.getAiResponse() != null) {
                messages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), conv.getAiResponse()));
            }
        }
        
        // Add current message
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), request.getStudentMessage()));
        
        return messages;
    }
    
    private String buildSystemPrompt(AIConversation.ConversationType type, AITutorRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // Base personality
        prompt.append("You are a calm, friendly, and patient programming mentor. ");
        prompt.append("Your goal is to build student confidence through step-by-step learning. ");
        prompt.append("Always use simple language and avoid technical jargon unless necessary. ");
        
        // Type-specific instructions
        switch (type) {
            case QUESTION:
                prompt.append("The student has a question. Provide a clear, encouraging explanation. ");
                prompt.append("Start with simple concepts and build up to more complex ideas. ");
                break;
                
            case HELP_REQUEST:
                prompt.append("The student needs help. Be extra patient and supportive. ");
                prompt.append("Break down the problem into smaller, manageable steps. ");
                break;
                
            case CONFUSION:
                prompt.append("The student is confused. Re-explain the concept in simpler terms. ");
                prompt.append("Use real-life analogies and examples before technical explanations. ");
                prompt.append("Be encouraging and reassure them that confusion is normal in learning. ");
                break;
                
            case ERROR_EXPLANATION:
                prompt.append("The student made an error. Explain common beginner mistakes gently. ");
                prompt.append("Focus on learning from the mistake rather than just correcting it. ");
                prompt.append("Provide specific guidance on how to avoid similar errors. ");
                break;
                
            case FEEDBACK:
                prompt.append("Provide constructive feedback. Be encouraging and specific. ");
                prompt.append("Highlight what they did well before suggesting improvements. ");
                break;
        }
        
        // Context-specific additions
        if (request.getLessonId() != null) {
            prompt.append("This is related to a specific lesson. Stay focused on that lesson's concepts. ");
        }
        
        if (request.getDifficultyLevel() != null) {
            prompt.append("Adjust your explanation complexity to difficulty level ").append(request.getDifficultyLevel()).append(". ");
        }
        
        prompt.append("Keep responses concise but thorough. End with encouragement or a follow-up question when appropriate.");
        
        return prompt.toString();
    }
    
    private String generateAIResponse(List<ChatMessage> messages) {
        try {
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(messages)
                .maxTokens(MAX_TOKENS)
                .temperature(TEMPERATURE)
                .build();
            
            ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
            
            if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                return result.getChoices().get(0).getMessage().getContent().trim();
            }
            
            throw new AIServiceException("No response generated from OpenAI");
            
        } catch (Exception e) {
            logger.error("Error calling OpenAI API: {}", e.getMessage(), e);
            
            // Determine if this is a retryable error
            boolean isRetryable = isRetryableError(e);
            
            if (e.getMessage().contains("rate limit") || e.getMessage().contains("quota")) {
                throw new AIServiceException("AI service rate limit exceeded. Please try again in a moment.", "OpenAI", true);
            } else if (e.getMessage().contains("timeout") || e.getMessage().contains("connection")) {
                throw new AIServiceException("AI service connection timeout. Please try again.", "OpenAI", true);
            } else if (e.getMessage().contains("authentication") || e.getMessage().contains("unauthorized")) {
                throw new AIServiceException("AI service authentication error.", "OpenAI", false);
            } else {
                throw new AIServiceException("AI service temporarily unavailable: " + e.getMessage(), "OpenAI", isRetryable);
            }
        }
    }
    
    private boolean isRetryableError(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("timeout") || 
               message.contains("connection") || 
               message.contains("rate limit") ||
               message.contains("server error") ||
               message.contains("503") ||
               message.contains("502") ||
               message.contains("500");
    }
    
    private String validateAndFilterResponse(String response, AIConversation.ConversationType type) {
        if (response == null || response.trim().isEmpty()) {
            return "I'm not sure how to respond to that. Could you please rephrase your question?";
        }
        
        // Basic content filtering
        String filtered = response.trim();
        
        // Ensure response is appropriate for educational context
        if (containsInappropriateContent(filtered)) {
            logger.warn("Inappropriate content detected in AI response, using fallback");
            return getFallbackResponse(type);
        }
        
        // Ensure response is not too long
        if (filtered.length() > 2000) {
            filtered = filtered.substring(0, 1997) + "...";
        }
        
        return filtered;
    }
    
    private boolean containsInappropriateContent(String content) {
        // Basic inappropriate content detection
        String lowerContent = content.toLowerCase();
        String[] inappropriateWords = {"inappropriate", "offensive", "harmful"};
        
        for (String word : inappropriateWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String getFallbackResponse(AIConversation.ConversationType type) {
        switch (type) {
            case QUESTION:
                return "That's a great question! Let me help you understand this concept step by step.";
            case HELP_REQUEST:
                return "I'm here to help! Let's work through this together, one step at a time.";
            case CONFUSION:
                return "It's completely normal to feel confused when learning something new. Let me explain this in a simpler way.";
            case ERROR_EXPLANATION:
                return "Don't worry about the error - mistakes are how we learn! Let me explain what happened and how to fix it.";
            case FEEDBACK:
                return "You're making good progress! Keep practicing and you'll continue to improve.";
            default:
                return "I'm here to help you learn. What specific aspect would you like me to explain?";
        }
    }
    
    /**
     * Get contextual fallback response when AI service is unavailable
     * Requirement 6.3: Error handling with fallback responses
     */
    private String getFallbackResponse(AIConversation.ConversationType type, String studentMessage) {
        // Try to provide contextual fallback based on student message
        if (studentMessage != null && !studentMessage.trim().isEmpty()) {
            String lowerMessage = studentMessage.toLowerCase();
            
            if (lowerMessage.contains("error") || lowerMessage.contains("bug") || lowerMessage.contains("wrong")) {
                return "I see you're encountering an issue. While I'm temporarily unavailable, here's a general tip: " +
                       "carefully check your syntax, variable names, and make sure all brackets are properly closed. " +
                       "Try running your code step by step to identify where the problem occurs.";
            }
            
            if (lowerMessage.contains("how") || lowerMessage.contains("what") || lowerMessage.contains("why")) {
                return "That's an excellent question! While I'm temporarily unavailable, I encourage you to: " +
                       "1) Review the lesson material, 2) Try breaking down the problem into smaller parts, " +
                       "3) Look for similar examples in the course content. I'll be back soon to provide detailed help!";
            }
            
            if (lowerMessage.contains("help") || lowerMessage.contains("stuck") || lowerMessage.contains("confused")) {
                return "I understand you need assistance, and I'm sorry I'm temporarily unavailable. " +
                       "In the meantime, try reviewing the previous examples in this lesson, " +
                       "and remember that making mistakes is a normal part of learning. Keep practicing!";
            }
        }
        
        // Fall back to type-based response
        return getFallbackResponse(type);
    }
    
    private Integer calculateConfidenceScore(String response) {
        // Simple confidence scoring based on response characteristics
        int score = 70; // Base score
        
        if (response.length() > 100) score += 10; // Detailed response
        if (response.contains("example")) score += 5; // Contains examples
        if (response.contains("step")) score += 5; // Step-by-step explanation
        if (response.contains("?")) score += 5; // Asks follow-up questions
        
        return Math.min(100, score);
    }
    
    private boolean determineFollowUpNeed(AIConversation.ConversationType type, String response) {
        // Determine if follow-up is needed based on type and response content
        switch (type) {
            case CONFUSION:
                return true; // Always follow up on confusion
            case ERROR_EXPLANATION:
                return response.contains("try") || response.contains("practice");
            case HELP_REQUEST:
                return response.contains("next") || response.contains("continue");
            default:
                return response.contains("?"); // Follow up if response asks a question
        }
    }
}