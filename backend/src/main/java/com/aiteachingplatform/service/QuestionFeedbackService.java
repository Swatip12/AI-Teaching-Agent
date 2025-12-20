package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.QuestionFeedbackResponse;
import com.aiteachingplatform.model.CheckpointQuestion;
import com.aiteachingplatform.model.PracticeQuestion;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.repository.AIConversationRepository;
import com.aiteachingplatform.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for providing immediate feedback on student answers
 * Implements requirements 3.1 and 3.3 for immediate feedback provision
 */
@Service
public class QuestionFeedbackService {
    
    @Autowired
    private AITutorService aiTutorService;
    
    @Autowired
    private AIConversationRepository conversationRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    /**
     * Evaluate checkpoint question answer and provide immediate feedback
     * Requirements 3.1: Immediate feedback on checkpoint questions
     */
    public QuestionFeedbackResponse evaluateCheckpointAnswer(CheckpointQuestion question, 
                                                           String userAnswer, 
                                                           Optional<User> user) {
        if (question == null || userAnswer == null || userAnswer.trim().isEmpty()) {
            return createErrorFeedback(question != null ? question.getId() : null, userAnswer);
        }
        
        // Normalize answers for comparison
        String normalizedUserAnswer = normalizeAnswer(userAnswer);
        String normalizedCorrectAnswer = normalizeAnswer(question.getCorrectAnswer());
        
        boolean isCorrect = evaluateAnswer(normalizedUserAnswer, normalizedCorrectAnswer, question.getQuestionType());
        
        if (isCorrect) {
            return QuestionFeedbackResponse.correct(
                question.getId(), 
                userAnswer, 
                question.getExplanation() != null ? question.getExplanation() : 
                    "Perfect! You understood this concept correctly."
            );
        } else {
            // Check for partial correctness
            if (isPartiallyCorrect(normalizedUserAnswer, normalizedCorrectAnswer, question.getQuestionType())) {
                return QuestionFeedbackResponse.partiallyCorrect(
                    question.getId(),
                    userAnswer,
                    enhanceExplanationForPartialAnswer(question.getExplanation(), question.getCorrectAnswer())
                );
            } else {
                return QuestionFeedbackResponse.incorrect(
                    question.getId(),
                    userAnswer,
                    enhanceExplanationForIncorrectAnswer(question.getExplanation(), userAnswer, question.getCorrectAnswer()),
                    question.getCorrectAnswer()
                );
            }
        }
    }
    
    /**
     * Evaluate practice question answer and provide immediate feedback
     * Requirements 3.3: Feedback on practice questions with explanations
     */
    public QuestionFeedbackResponse evaluatePracticeAnswer(PracticeQuestion question, 
                                                         String userAnswer, 
                                                         Optional<User> user) {
        if (question == null || userAnswer == null || userAnswer.trim().isEmpty()) {
            return createErrorFeedback(question != null ? question.getId() : null, userAnswer);
        }
        
        // For practice questions, we use more sophisticated evaluation
        // This could involve code execution, pattern matching, or AI evaluation
        boolean isCorrect = evaluatePracticeAnswer(question, userAnswer);
        
        if (isCorrect) {
            return QuestionFeedbackResponse.correct(
                question.getId(),
                userAnswer,
                "Excellent solution! " + (question.getHints() != null ? 
                    "You applied the concepts correctly." : 
                    "Your approach demonstrates good understanding of the problem.")
            );
        } else {
            String enhancedExplanation = generatePracticeExplanation(question, userAnswer);
            return QuestionFeedbackResponse.incorrect(
                question.getId(),
                userAnswer,
                enhancedExplanation,
                question.getExpectedSolution()
            );
        }
    }
    
    /**
     * Normalize answer for comparison
     */
    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        return answer.toLowerCase().trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Evaluate answer based on question type
     */
    private boolean evaluateAnswer(String userAnswer, String correctAnswer, CheckpointQuestion.QuestionType questionType) {
        switch (questionType) {
            case TRUE_FALSE:
                return userAnswer.equals(correctAnswer) || 
                       (userAnswer.equals("true") && correctAnswer.equals("true")) ||
                       (userAnswer.equals("false") && correctAnswer.equals("false"));
                       
            case MULTIPLE_CHOICE:
                return userAnswer.equals(correctAnswer) || 
                       containsKeywords(userAnswer, correctAnswer);
                       
            case SHORT_ANSWER:
                return userAnswer.equals(correctAnswer) || 
                       calculateSimilarity(userAnswer, correctAnswer) > 0.8;
                       
            case CODE_COMPLETION:
                return evaluateCodeAnswer(userAnswer, correctAnswer);
                
            default:
                return userAnswer.equals(correctAnswer);
        }
    }
    
    /**
     * Check if answer contains key concepts even if not exactly correct
     */
    private boolean containsKeywords(String userAnswer, String correctAnswer) {
        String[] keywords = correctAnswer.split("\\s+");
        int matchCount = 0;
        
        for (String keyword : keywords) {
            if (keyword.length() > 2 && userAnswer.contains(keyword.toLowerCase())) {
                matchCount++;
            }
        }
        
        return matchCount >= keywords.length * 0.6; // 60% keyword match
    }
    
    /**
     * Calculate similarity between answers
     */
    private double calculateSimilarity(String answer1, String answer2) {
        if (answer1.equals(answer2)) return 1.0;
        
        // Simple Levenshtein distance-based similarity
        int maxLength = Math.max(answer1.length(), answer2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = levenshteinDistance(answer1, answer2);
        return 1.0 - (double) distance / maxLength;
    }
    
    /**
     * Simple Levenshtein distance calculation
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Evaluate code-based answers
     */
    private boolean evaluateCodeAnswer(String userCode, String expectedCode) {
        // Remove whitespace and normalize formatting
        String normalizedUser = userCode.replaceAll("\\s+", "").toLowerCase();
        String normalizedExpected = expectedCode.replaceAll("\\s+", "").toLowerCase();
        
        // Check for exact match or key structural elements
        return normalizedUser.equals(normalizedExpected) || 
               containsEssentialCodeElements(normalizedUser, normalizedExpected);
    }
    
    /**
     * Check if code contains essential elements
     */
    private boolean containsEssentialCodeElements(String userCode, String expectedCode) {
        // Extract key programming constructs
        String[] essentialElements = {"if", "for", "while", "return", "function", "class"};
        
        for (String element : essentialElements) {
            if (expectedCode.contains(element) && !userCode.contains(element)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check for partial correctness
     */
    private boolean isPartiallyCorrect(String userAnswer, String correctAnswer, CheckpointQuestion.QuestionType questionType) {
        if (questionType == CheckpointQuestion.QuestionType.TRUE_FALSE) {
            return false; // True/False is binary
        }
        
        double similarity = calculateSimilarity(userAnswer, correctAnswer);
        return similarity > 0.5 && similarity < 0.8;
    }
    
    /**
     * Evaluate practice question answer
     */
    private boolean evaluatePracticeAnswer(PracticeQuestion question, String userAnswer) {
        if (question.getExpectedSolution() == null) {
            return false; // Cannot evaluate without expected solution
        }
        
        switch (question.getQuestionType()) {
            case CODING:
                return evaluateCodeAnswer(userAnswer, question.getExpectedSolution());
            case ALGORITHM:
                return evaluateAlgorithmAnswer(userAnswer, question.getExpectedSolution());
            case DEBUGGING:
                return evaluateDebuggingAnswer(userAnswer, question.getExpectedSolution());
            case DESIGN:
                return evaluateDesignAnswer(userAnswer, question.getExpectedSolution());
            default:
                return calculateSimilarity(normalizeAnswer(userAnswer), 
                                         normalizeAnswer(question.getExpectedSolution())) > 0.7;
        }
    }
    
    private boolean evaluateAlgorithmAnswer(String userAnswer, String expectedSolution) {
        // Check for key algorithmic concepts
        return containsKeywords(normalizeAnswer(userAnswer), normalizeAnswer(expectedSolution));
    }
    
    private boolean evaluateDebuggingAnswer(String userAnswer, String expectedSolution) {
        // For debugging questions, check if the fix is identified
        return userAnswer.toLowerCase().contains("bug") || 
               userAnswer.toLowerCase().contains("error") ||
               containsKeywords(normalizeAnswer(userAnswer), normalizeAnswer(expectedSolution));
    }
    
    private boolean evaluateDesignAnswer(String userAnswer, String expectedSolution) {
        // For design questions, look for key design concepts
        String[] designKeywords = {"class", "interface", "method", "function", "pattern", "structure"};
        String normalizedAnswer = normalizeAnswer(userAnswer);
        
        int conceptCount = 0;
        for (String keyword : designKeywords) {
            if (normalizedAnswer.contains(keyword)) {
                conceptCount++;
            }
        }
        
        return conceptCount >= 2; // At least 2 design concepts mentioned
    }
    
    /**
     * Enhance explanation for partial answers
     */
    private String enhanceExplanationForPartialAnswer(String originalExplanation, String correctAnswer) {
        String enhanced = originalExplanation != null ? originalExplanation : "";
        enhanced += " You're on the right track! The complete answer is: " + correctAnswer;
        enhanced += " Take a moment to see what you might have missed.";
        return enhanced;
    }
    
    /**
     * Enhance explanation for incorrect answers
     */
    private String enhanceExplanationForIncorrectAnswer(String originalExplanation, String userAnswer, String correctAnswer) {
        String enhanced = originalExplanation != null ? originalExplanation : "";
        enhanced += " Don't worry - this is a common area where students need practice. ";
        enhanced += "The key difference between your answer and the correct one is in the approach to the problem.";
        return enhanced;
    }
    
    /**
     * Generate explanation for practice questions
     */
    private String generatePracticeExplanation(PracticeQuestion question, String userAnswer) {
        String explanation = question.getHints() != null ? question.getHints() : "";
        explanation += " Here's what to focus on: break down the problem into smaller steps, ";
        explanation += "think about the core concept being tested, and don't hesitate to try different approaches.";
        
        if (question.getExpectedSolution() != null) {
            explanation += " Compare your solution with the expected approach to see the differences.";
        }
        
        return explanation;
    }
    
    /**
     * Create error feedback for invalid submissions
     */
    private QuestionFeedbackResponse createErrorFeedback(Long questionId, String userAnswer) {
        return new QuestionFeedbackResponse(
            questionId,
            userAnswer,
            false,
            "Please provide a valid answer to continue.",
            "Make sure your answer is complete and addresses the question being asked."
        );
    }
}