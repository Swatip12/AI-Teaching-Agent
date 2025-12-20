package com.aiteachingplatform.controller;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.PracticeQuestion;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.service.LessonService;
import com.aiteachingplatform.service.PersonalizationService;
import com.aiteachingplatform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for personalization and adaptive learning features
 */
@RestController
@RequestMapping("/api/personalization")
@CrossOrigin(origins = "http://localhost:4200")
public class PersonalizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalizationController.class);
    
    @Autowired
    private PersonalizationService personalizationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LessonService lessonService;
    
    /**
     * Get learning pattern analysis for current user
     */
    @GetMapping("/learning-pattern")
    public ResponseEntity<PersonalizationService.LearningPattern> getLearningPattern(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            PersonalizationService.LearningPattern pattern = personalizationService.analyzeLearningPattern(user);
            
            logger.info("Retrieved learning pattern for user: {}", user.getUsername());
            return ResponseEntity.ok(pattern);
            
        } catch (Exception e) {
            logger.error("Error retrieving learning pattern", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get content adaptation recommendations for a specific lesson
     */
    @GetMapping("/content-adaptation/{lessonId}")
    public ResponseEntity<PersonalizationService.ContentAdaptation> getContentAdaptation(
            @PathVariable Long lessonId, Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            Optional<Lesson> lesson = lessonService.findById(lessonId);
            
            if (lesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PersonalizationService.ContentAdaptation adaptation = 
                personalizationService.adaptContentPacing(user, lesson.get());
            
            logger.info("Generated content adaptation for user {} and lesson {}", 
                       user.getUsername(), lessonId);
            return ResponseEntity.ok(adaptation);
            
        } catch (Exception e) {
            logger.error("Error generating content adaptation for lesson: {}", lessonId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get additional practice questions based on struggle areas
     */
    @GetMapping("/additional-practice/{subject}")
    public ResponseEntity<List<PracticeQuestion>> getAdditionalPractice(
            @PathVariable String subject, Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<PracticeQuestion> additionalQuestions = 
                personalizationService.generateAdditionalPractice(user, subject);
            
            logger.info("Generated {} additional practice questions for user {} in subject {}", 
                       additionalQuestions.size(), user.getUsername(), subject);
            return ResponseEntity.ok(additionalQuestions);
            
        } catch (Exception e) {
            logger.error("Error generating additional practice for subject: {}", subject, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get advanced challenges for a specific lesson
     */
    @GetMapping("/advanced-challenges/{lessonId}")
    public ResponseEntity<List<PracticeQuestion>> getAdvancedChallenges(
            @PathVariable Long lessonId, Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            Optional<Lesson> lesson = lessonService.findById(lessonId);
            
            if (lesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<PracticeQuestion> challenges = 
                personalizationService.generateAdvancedChallenges(user, lesson.get());
            
            logger.info("Generated {} advanced challenges for user {} and lesson {}", 
                       challenges.size(), user.getUsername(), lessonId);
            return ResponseEntity.ok(challenges);
            
        } catch (Exception e) {
            logger.error("Error generating advanced challenges for lesson: {}", lessonId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update user preferences based on detected learning patterns
     */
    @PostMapping("/update-preferences-from-pattern")
    public ResponseEntity<String> updatePreferencesFromPattern(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            PersonalizationService.LearningPattern pattern = personalizationService.analyzeLearningPattern(user);
            
            personalizationService.updatePreferencesFromPattern(user, pattern);
            
            logger.info("Updated preferences from pattern for user: {}", user.getUsername());
            return ResponseEntity.ok("Preferences updated successfully based on learning pattern");
            
        } catch (Exception e) {
            logger.error("Error updating preferences from pattern", e);
            return ResponseEntity.internalServerError().body("Failed to update preferences");
        }
    }
    
    /**
     * Get personalized learning recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<LearningRecommendations> getLearningRecommendations(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            PersonalizationService.LearningPattern pattern = personalizationService.analyzeLearningPattern(user);
            
            LearningRecommendations recommendations = generateRecommendations(user, pattern);
            
            logger.info("Generated learning recommendations for user: {}", user.getUsername());
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Error generating learning recommendations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private LearningRecommendations generateRecommendations(User user, PersonalizationService.LearningPattern pattern) {
        LearningRecommendations recommendations = new LearningRecommendations();
        
        // Pace recommendations
        if (pattern.getLearningPace() != user.getPreferences().getLearningPace()) {
            recommendations.addRecommendation("Consider adjusting your learning pace to " + 
                pattern.getLearningPace().toString().toLowerCase() + " based on your progress patterns.");
        }
        
        // Struggle area recommendations
        if (!pattern.getStruggleAreas().isEmpty()) {
            recommendations.addRecommendation("Focus on additional practice in: " + 
                String.join(", ", pattern.getStruggleAreas()));
        }
        
        // Engagement recommendations
        if (pattern.getEngagementLevel() < 0.6) {
            recommendations.addRecommendation("Try shorter, more frequent study sessions to improve engagement.");
        } else if (pattern.getEngagementLevel() > 0.8) {
            recommendations.addRecommendation("You're highly engaged! Consider taking on advanced challenges.");
        }
        
        // Consistency recommendations
        if (pattern.getConsistencyScore() < 0.5) {
            recommendations.addRecommendation("Try to maintain a more regular study schedule for better learning outcomes.");
        }
        
        return recommendations;
    }
    
    /**
     * Data class for learning recommendations
     */
    public static class LearningRecommendations {
        private List<String> recommendations = new java.util.ArrayList<>();
        
        public void addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
        }
        
        public List<String> getRecommendations() {
            return recommendations;
        }
        
        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
    }
}