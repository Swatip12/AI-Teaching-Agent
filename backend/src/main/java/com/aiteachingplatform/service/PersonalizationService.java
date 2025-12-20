package com.aiteachingplatform.service;

import com.aiteachingplatform.model.*;
import com.aiteachingplatform.repository.ProgressRepository;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.AIConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for personalization and adaptive learning features
 */
@Service
@Transactional
public class PersonalizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalizationService.class);
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private AIConversationRepository conversationRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Analyze user's learning patterns and detect preferences
     */
    public LearningPattern analyzeLearningPattern(User user) {
        List<Progress> userProgress = progressRepository.findByUserOrderByUpdatedAtDesc(user);
        List<AIConversation> conversations = conversationRepository.findRecentConversationsByUser(
            user, LocalDateTime.now().minusDays(30));
        
        LearningPattern pattern = new LearningPattern();
        
        // Analyze completion speed
        pattern.setAverageCompletionTime(calculateAverageCompletionTime(userProgress));
        pattern.setLearningPace(determineLearningPace(userProgress));
        
        // Analyze struggle areas
        pattern.setStruggleAreas(identifyStruggleAreas(userProgress, conversations));
        
        // Analyze preferred explanation style
        pattern.setPreferredExplanationStyle(analyzeExplanationPreference(conversations));
        
        // Analyze engagement patterns
        pattern.setEngagementLevel(calculateEngagementLevel(userProgress, conversations));
        
        // Analyze learning consistency
        pattern.setConsistencyScore(calculateConsistencyScore(userProgress));
        
        logger.info("Analyzed learning pattern for user {}: pace={}, engagement={}, consistency={}", 
                   user.getId(), pattern.getLearningPace(), pattern.getEngagementLevel(), pattern.getConsistencyScore());
        
        return pattern;
    }
    
    /**
     * Adapt content pacing based on user's learning pattern
     */
    public ContentAdaptation adaptContentPacing(User user, Lesson lesson) {
        LearningPattern pattern = analyzeLearningPattern(user);
        UserPreferences preferences = user.getPreferences();
        
        ContentAdaptation adaptation = new ContentAdaptation();
        
        // Adjust pacing based on learning pattern and preferences
        UserPreferences.LearningPace recommendedPace = determineOptimalPace(pattern, preferences);
        adaptation.setRecommendedPace(recommendedPace);
        
        // Adjust content complexity
        adaptation.setContentComplexity(determineContentComplexity(pattern, lesson));
        
        // Determine if additional practice is needed
        adaptation.setNeedsAdditionalPractice(shouldProvideAdditionalPractice(pattern, lesson));
        
        // Determine if advanced challenges should be offered
        adaptation.setOfferAdvancedChallenges(shouldOfferAdvancedChallenges(pattern, lesson));
        
        // Adjust explanation style
        adaptation.setExplanationStyle(determineOptimalExplanationStyle(pattern, preferences));
        
        logger.info("Adapted content for user {} and lesson {}: pace={}, complexity={}, additionalPractice={}", 
                   user.getId(), lesson.getId(), recommendedPace, adaptation.getContentComplexity(), 
                   adaptation.isNeedsAdditionalPractice());
        
        return adaptation;
    }
    
    /**
     * Generate additional practice questions based on struggle areas
     */
    public List<PracticeQuestion> generateAdditionalPractice(User user, String subject) {
        LearningPattern pattern = analyzeLearningPattern(user);
        List<String> struggleAreas = pattern.getStruggleAreas();
        
        List<PracticeQuestion> additionalQuestions = new ArrayList<>();
        
        for (String area : struggleAreas) {
            List<Lesson> relatedLessons = lessonRepository.findBySubjectAndContentContaining(subject, area);
            
            for (Lesson lesson : relatedLessons) {
                // Generate practice questions focused on struggle areas
                List<PracticeQuestion> focusedQuestions = createFocusedPracticeQuestions(lesson, area);
                additionalQuestions.addAll(focusedQuestions);
            }
        }
        
        // Limit to reasonable number
        return additionalQuestions.stream().limit(5).collect(Collectors.toList());
    }
    
    /**
     * Generate advanced challenges for excelling students
     */
    public List<PracticeQuestion> generateAdvancedChallenges(User user, Lesson lesson) {
        LearningPattern pattern = analyzeLearningPattern(user);
        
        if (pattern.getEngagementLevel() < 0.8 || pattern.getConsistencyScore() < 0.7) {
            return Collections.emptyList(); // Not ready for advanced challenges
        }
        
        List<PracticeQuestion> challenges = new ArrayList<>();
        
        // Create more complex variations of lesson concepts
        challenges.addAll(createAdvancedVariations(lesson));
        
        // Add integration challenges that combine multiple concepts
        challenges.addAll(createIntegrationChallenges(lesson));
        
        return challenges.stream().limit(3).collect(Collectors.toList());
    }
    
    /**
     * Update user preferences based on detected patterns
     */
    public void updatePreferencesFromPattern(User user, LearningPattern pattern) {
        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            user.setPreferences(preferences);
        }
        
        boolean updated = false;
        
        // Update learning pace if significantly different from current preference
        UserPreferences.LearningPace detectedPace = pattern.getLearningPace();
        if (detectedPace != null && !detectedPace.equals(preferences.getLearningPace())) {
            preferences.setLearningPace(detectedPace);
            updated = true;
        }
        
        // Update explanation style if pattern shows clear preference
        UserPreferences.ExplanationStyle detectedStyle = pattern.getPreferredExplanationStyle();
        if (detectedStyle != null && !detectedStyle.equals(preferences.getExplanationStyle())) {
            preferences.setExplanationStyle(detectedStyle);
            updated = true;
        }
        
        if (updated) {
            userService.updateUserPreferences(user.getUsername(), 
                createUpdateRequest(preferences));
            logger.info("Updated preferences for user {} based on learning pattern", user.getId());
        }
    }
    
    // Helper methods
    
    private double calculateAverageCompletionTime(List<Progress> progressList) {
        return progressList.stream()
            .filter(p -> p.getTimeSpentMinutes() != null && p.getTimeSpentMinutes() > 0)
            .mapToInt(Progress::getTimeSpentMinutes)
            .average()
            .orElse(30.0); // Default 30 minutes
    }
    
    private UserPreferences.LearningPace determineLearningPace(List<Progress> progressList) {
        double avgTime = calculateAverageCompletionTime(progressList);
        
        if (avgTime < 20) return UserPreferences.LearningPace.FAST;
        if (avgTime > 45) return UserPreferences.LearningPace.SLOW;
        return UserPreferences.LearningPace.NORMAL;
    }
    
    private List<String> identifyStruggleAreas(List<Progress> progressList, List<AIConversation> conversations) {
        Set<String> struggleAreas = new HashSet<>();
        
        // Identify from low scores
        progressList.stream()
            .filter(p -> p.getScore() != null && p.getScore() < 70)
            .forEach(p -> {
                if (p.getLesson() != null && p.getLesson().getSubject() != null) {
                    struggleAreas.add(p.getLesson().getSubject());
                }
            });
        
        // Identify from confusion conversations
        conversations.stream()
            .filter(c -> c.getConversationType() == AIConversation.ConversationType.CONFUSION ||
                        c.getConversationType() == AIConversation.ConversationType.ERROR_EXPLANATION)
            .forEach(c -> {
                if (c.getLesson() != null && c.getLesson().getSubject() != null) {
                    struggleAreas.add(c.getLesson().getSubject());
                }
            });
        
        return new ArrayList<>(struggleAreas);
    }
    
    private UserPreferences.ExplanationStyle analyzeExplanationPreference(List<AIConversation> conversations) {
        long simpleRequests = conversations.stream()
            .filter(c -> c.getStudentMessage() != null)
            .filter(c -> c.getStudentMessage().toLowerCase().contains("simple") ||
                        c.getStudentMessage().toLowerCase().contains("basic") ||
                        c.getStudentMessage().toLowerCase().contains("easy"))
            .count();
        
        long detailedRequests = conversations.stream()
            .filter(c -> c.getStudentMessage() != null)
            .filter(c -> c.getStudentMessage().toLowerCase().contains("detail") ||
                        c.getStudentMessage().toLowerCase().contains("explain more") ||
                        c.getStudentMessage().toLowerCase().contains("complex"))
            .count();
        
        if (simpleRequests > detailedRequests * 2) return UserPreferences.ExplanationStyle.SIMPLE;
        if (detailedRequests > simpleRequests * 2) return UserPreferences.ExplanationStyle.DETAILED;
        return UserPreferences.ExplanationStyle.BALANCED;
    }
    
    private double calculateEngagementLevel(List<Progress> progressList, List<AIConversation> conversations) {
        if (progressList.isEmpty()) return 0.5;
        
        // Calculate based on completion rate and interaction frequency
        long completedLessons = progressList.stream()
            .filter(p -> p.getStatus() == Progress.ProgressStatus.COMPLETED)
            .count();
        
        double completionRate = (double) completedLessons / progressList.size();
        
        // Factor in conversation frequency (more questions = higher engagement)
        double conversationFactor = Math.min(1.0, conversations.size() / 10.0);
        
        return (completionRate * 0.7) + (conversationFactor * 0.3);
    }
    
    private double calculateConsistencyScore(List<Progress> progressList) {
        if (progressList.size() < 3) return 0.5;
        
        // Calculate consistency based on regular progress updates
        List<LocalDateTime> updateTimes = progressList.stream()
            .map(Progress::getUpdatedAt)
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
        
        if (updateTimes.size() < 2) return 0.5;
        
        // Calculate average gap between updates
        long totalGapHours = 0;
        for (int i = 1; i < updateTimes.size(); i++) {
            totalGapHours += java.time.Duration.between(updateTimes.get(i-1), updateTimes.get(i)).toHours();
        }
        
        double avgGapHours = (double) totalGapHours / (updateTimes.size() - 1);
        
        // More consistent (smaller gaps) = higher score
        return Math.max(0.1, Math.min(1.0, 168.0 / avgGapHours)); // 168 hours = 1 week
    }
    
    private UserPreferences.LearningPace determineOptimalPace(LearningPattern pattern, UserPreferences preferences) {
        // Combine detected pattern with user preference
        UserPreferences.LearningPace detectedPace = pattern.getLearningPace();
        UserPreferences.LearningPace preferredPace = preferences != null ? preferences.getLearningPace() : null;
        
        if (preferredPace == null) return detectedPace;
        
        // If engagement is low, suggest slower pace
        if (pattern.getEngagementLevel() < 0.5) {
            return UserPreferences.LearningPace.SLOW;
        }
        
        // If consistency is high and engagement is high, can handle faster pace
        if (pattern.getConsistencyScore() > 0.8 && pattern.getEngagementLevel() > 0.8) {
            return UserPreferences.LearningPace.FAST;
        }
        
        return preferredPace;
    }
    
    private String determineContentComplexity(LearningPattern pattern, Lesson lesson) {
        if (pattern.getStruggleAreas().contains(lesson.getSubject())) {
            return "SIMPLIFIED";
        }
        
        if (pattern.getEngagementLevel() > 0.8 && pattern.getConsistencyScore() > 0.7) {
            return "ENHANCED";
        }
        
        return "STANDARD";
    }
    
    private boolean shouldProvideAdditionalPractice(LearningPattern pattern, Lesson lesson) {
        return pattern.getStruggleAreas().contains(lesson.getSubject()) ||
               pattern.getEngagementLevel() < 0.6;
    }
    
    private boolean shouldOfferAdvancedChallenges(LearningPattern pattern, Lesson lesson) {
        return pattern.getEngagementLevel() > 0.8 && 
               pattern.getConsistencyScore() > 0.7 &&
               !pattern.getStruggleAreas().contains(lesson.getSubject());
    }
    
    private UserPreferences.ExplanationStyle determineOptimalExplanationStyle(LearningPattern pattern, UserPreferences preferences) {
        UserPreferences.ExplanationStyle detected = pattern.getPreferredExplanationStyle();
        UserPreferences.ExplanationStyle preferred = preferences != null ? preferences.getExplanationStyle() : null;
        
        if (preferred == null) return detected;
        
        // If struggling, prefer simpler explanations
        if (!pattern.getStruggleAreas().isEmpty()) {
            return UserPreferences.ExplanationStyle.SIMPLE;
        }
        
        return preferred;
    }
    
    private List<PracticeQuestion> createFocusedPracticeQuestions(Lesson lesson, String focusArea) {
        // This would generate practice questions focused on specific struggle areas
        // For now, return empty list as this would require complex question generation
        return Collections.emptyList();
    }
    
    private List<PracticeQuestion> createAdvancedVariations(Lesson lesson) {
        // This would create more complex versions of existing practice questions
        // For now, return empty list as this would require complex question generation
        return Collections.emptyList();
    }
    
    private List<PracticeQuestion> createIntegrationChallenges(Lesson lesson) {
        // This would create questions that combine concepts from multiple lessons
        // For now, return empty list as this would require complex question generation
        return Collections.emptyList();
    }
    
    private com.aiteachingplatform.dto.UpdatePreferencesRequest createUpdateRequest(UserPreferences preferences) {
        com.aiteachingplatform.dto.UpdatePreferencesRequest request = new com.aiteachingplatform.dto.UpdatePreferencesRequest();
        request.setLearningPace(preferences.getLearningPace());
        request.setExplanationStyle(preferences.getExplanationStyle());
        request.setShowHints(preferences.getShowHints());
        request.setCelebrationEnabled(preferences.getCelebrationEnabled());
        request.setDarkMode(preferences.getDarkMode());
        request.setNotificationEnabled(preferences.getNotificationEnabled());
        return request;
    }
    
    /**
     * Data class for learning pattern analysis
     */
    public static class LearningPattern {
        private double averageCompletionTime;
        private UserPreferences.LearningPace learningPace;
        private List<String> struggleAreas = new ArrayList<>();
        private UserPreferences.ExplanationStyle preferredExplanationStyle;
        private double engagementLevel;
        private double consistencyScore;
        
        // Getters and setters
        public double getAverageCompletionTime() { return averageCompletionTime; }
        public void setAverageCompletionTime(double averageCompletionTime) { this.averageCompletionTime = averageCompletionTime; }
        
        public UserPreferences.LearningPace getLearningPace() { return learningPace; }
        public void setLearningPace(UserPreferences.LearningPace learningPace) { this.learningPace = learningPace; }
        
        public List<String> getStruggleAreas() { return struggleAreas; }
        public void setStruggleAreas(List<String> struggleAreas) { this.struggleAreas = struggleAreas; }
        
        public UserPreferences.ExplanationStyle getPreferredExplanationStyle() { return preferredExplanationStyle; }
        public void setPreferredExplanationStyle(UserPreferences.ExplanationStyle preferredExplanationStyle) { this.preferredExplanationStyle = preferredExplanationStyle; }
        
        public double getEngagementLevel() { return engagementLevel; }
        public void setEngagementLevel(double engagementLevel) { this.engagementLevel = engagementLevel; }
        
        public double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }
    }
    
    /**
     * Data class for content adaptation recommendations
     */
    public static class ContentAdaptation {
        private UserPreferences.LearningPace recommendedPace;
        private String contentComplexity;
        private boolean needsAdditionalPractice;
        private boolean offerAdvancedChallenges;
        private UserPreferences.ExplanationStyle explanationStyle;
        
        // Getters and setters
        public UserPreferences.LearningPace getRecommendedPace() { return recommendedPace; }
        public void setRecommendedPace(UserPreferences.LearningPace recommendedPace) { this.recommendedPace = recommendedPace; }
        
        public String getContentComplexity() { return contentComplexity; }
        public void setContentComplexity(String contentComplexity) { this.contentComplexity = contentComplexity; }
        
        public boolean isNeedsAdditionalPractice() { return needsAdditionalPractice; }
        public void setNeedsAdditionalPractice(boolean needsAdditionalPractice) { this.needsAdditionalPractice = needsAdditionalPractice; }
        
        public boolean isOfferAdvancedChallenges() { return offerAdvancedChallenges; }
        public void setOfferAdvancedChallenges(boolean offerAdvancedChallenges) { this.offerAdvancedChallenges = offerAdvancedChallenges; }
        
        public UserPreferences.ExplanationStyle getExplanationStyle() { return explanationStyle; }
        public void setExplanationStyle(UserPreferences.ExplanationStyle explanationStyle) { this.explanationStyle = explanationStyle; }
    }
}