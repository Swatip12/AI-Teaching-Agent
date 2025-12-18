package com.aiteachingplatform.repository;

import com.aiteachingplatform.model.AIConversation;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AIConversation entity operations
 */
@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {
    
    /**
     * Find conversations by user ordered by timestamp
     */
    List<AIConversation> findByUserOrderByTimestampDesc(User user);
    
    /**
     * Find conversations by user and lesson
     */
    List<AIConversation> findByUserAndLessonOrderByTimestampAsc(User user, Lesson lesson);
    
    /**
     * Find recent conversations for a user (within last N hours)
     */
    @Query("SELECT c FROM AIConversation c WHERE c.user = :user AND c.timestamp >= :since ORDER BY c.timestamp DESC")
    List<AIConversation> findRecentConversationsByUser(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * Find conversations by type
     */
    List<AIConversation> findByConversationType(AIConversation.ConversationType type);
    
    /**
     * Find failed conversations that need retry
     */
    List<AIConversation> findByResponseStatus(AIConversation.ResponseStatus status);
    
    /**
     * Find conversations with feedback ratings
     */
    @Query("SELECT c FROM AIConversation c WHERE c.feedbackRating IS NOT NULL ORDER BY c.timestamp DESC")
    List<AIConversation> findConversationsWithFeedback();
    
    /**
     * Calculate average response time
     */
    @Query("SELECT AVG(c.responseTimeMs) FROM AIConversation c WHERE c.responseTimeMs IS NOT NULL")
    Double calculateAverageResponseTime();
    
    /**
     * Calculate average feedback rating
     */
    @Query("SELECT AVG(c.feedbackRating) FROM AIConversation c WHERE c.feedbackRating IS NOT NULL")
    Double calculateAverageFeedbackRating();
    
    /**
     * Find conversations by lesson with specific type
     */
    List<AIConversation> findByLessonAndConversationType(Lesson lesson, AIConversation.ConversationType type);
    
    /**
     * Count conversations by user in date range
     */
    @Query("SELECT COUNT(c) FROM AIConversation c WHERE c.user = :user AND c.timestamp BETWEEN :startDate AND :endDate")
    long countConversationsByUserInDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find most active users by conversation count
     */
    @Query("SELECT c.user, COUNT(c) as conversationCount FROM AIConversation c GROUP BY c.user ORDER BY conversationCount DESC")
    List<Object[]> findMostActiveUsersByConversationCount();
    
    /**
     * Find conversations that took longer than threshold to respond
     */
    @Query("SELECT c FROM AIConversation c WHERE c.responseTimeMs > :thresholdMs ORDER BY c.responseTimeMs DESC")
    List<AIConversation> findSlowResponses(@Param("thresholdMs") Long thresholdMs);
    
    /**
     * Delete old conversations (older than specified date)
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);
    
    /**
     * Find conversations needing context data cleanup
     */
    @Query("SELECT c FROM AIConversation c WHERE LENGTH(c.contextData) > :maxLength")
    List<AIConversation> findConversationsWithLargeContext(@Param("maxLength") int maxLength);
}