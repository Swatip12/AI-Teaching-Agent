package com.aiteachingplatform.controller;

import com.aiteachingplatform.dto.AITutorRequest;
import com.aiteachingplatform.dto.AITutorResponse;
import com.aiteachingplatform.model.AIConversation;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.service.AITutorService;
import com.aiteachingplatform.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for AI tutor interactions
 */
@RestController
@RequestMapping("/api/ai-tutor")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AITutorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AITutorController.class);
    
    @Autowired
    private AITutorService aiTutorService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Process AI tutor request
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askAITutor(@Valid @RequestBody AITutorRequest request, 
                                       Authentication authentication) {
        try {
            logger.info("Received AI tutor request from user: {}", authentication.getName());
            
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            AITutorResponse response = aiTutorService.processRequest(user, request);
            
            if (response.isSuccessful()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing AI tutor request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to process your request at this time"));
        }
    }
    
    /**
     * Get conversation history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getConversationHistory(@RequestParam(required = false) Long lessonId,
                                                   @RequestParam(defaultValue = "10") int limit,
                                                   Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<AIConversation> history = aiTutorService.getConversationHistory(user, lessonId, limit);
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Error retrieving conversation history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to retrieve conversation history"));
        }
    }
    
    /**
     * Add feedback to conversation
     */
    @PostMapping("/feedback/{conversationId}")
    public ResponseEntity<?> addFeedback(@PathVariable Long conversationId,
                                        @RequestBody Map<String, Object> feedbackData,
                                        Authentication authentication) {
        try {
            Integer rating = (Integer) feedbackData.get("rating");
            String comment = (String) feedbackData.get("comment");
            
            if (rating == null || rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rating must be between 1 and 5"));
            }
            
            aiTutorService.addConversationFeedback(conversationId, rating, comment);
            
            return ResponseEntity.ok(Map.of("message", "Feedback added successfully"));
            
        } catch (Exception e) {
            logger.error("Error adding conversation feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to add feedback"));
        }
    }
    
    /**
     * Quick help endpoint for common questions
     */
    @PostMapping("/quick-help")
    public ResponseEntity<?> getQuickHelp(@RequestBody Map<String, String> request,
                                         Authentication authentication) {
        try {
            String topic = request.get("topic");
            if (topic == null || topic.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Topic is required"));
            }
            
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create a help request
            AITutorRequest aiRequest = new AITutorRequest(
                "I need help with: " + topic, 
                AIConversation.ConversationType.HELP_REQUEST
            );
            
            AITutorResponse response = aiTutorService.processRequest(user, aiRequest);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing quick help request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to provide help at this time"));
        }
    }
    
    /**
     * Explain error endpoint
     */
    @PostMapping("/explain-error")
    public ResponseEntity<?> explainError(@RequestBody Map<String, String> request,
                                         Authentication authentication) {
        try {
            String errorMessage = request.get("error");
            String code = request.get("code");
            
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error message is required"));
            }
            
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Build error explanation request
            StringBuilder message = new StringBuilder("I got this error: ");
            message.append(errorMessage);
            if (code != null && !code.trim().isEmpty()) {
                message.append("\n\nMy code was:\n").append(code);
            }
            message.append("\n\nCan you explain what went wrong and how to fix it?");
            
            AITutorRequest aiRequest = new AITutorRequest(
                message.toString(), 
                AIConversation.ConversationType.ERROR_EXPLANATION
            );
            
            AITutorResponse response = aiTutorService.processRequest(user, aiRequest);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing error explanation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to explain error at this time"));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "AI Tutor Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}