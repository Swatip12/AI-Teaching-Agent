package com.aiteachingplatform.repository;

import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Progress entity operations
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    /**
     * Find progress by user and lesson
     */
    Optional<Progress> findByUserAndLesson(User user, Lesson lesson);
    
    /**
     * Find all progress for a user
     */
    List<Progress> findByUserOrderByUpdatedAtDesc(User user);
    
    /**
     * Find progress by user and subject
     */
    @Query("SELECT p FROM Progress p JOIN p.lesson l WHERE p.user = :user AND l.subject = :subject ORDER BY l.sequenceOrder")
    List<Progress> findByUserAndSubject(@Param("user") User user, @Param("subject") String subject);
    
    /**
     * Find completed lessons for a user
     */
    List<Progress> findByUserAndStatus(User user, Progress.ProgressStatus status);
    
    /**
     * Find in-progress lessons for a user
     */
    @Query("SELECT p FROM Progress p WHERE p.user = :user AND p.status = 'IN_PROGRESS' ORDER BY p.updatedAt DESC")
    List<Progress> findInProgressLessonsByUser(@Param("user") User user);
    
    /**
     * Calculate completion percentage for user in a subject
     */
    @Query("SELECT AVG(p.completionPercentage) FROM Progress p JOIN p.lesson l WHERE p.user = :user AND l.subject = :subject")
    Double calculateSubjectCompletionPercentage(@Param("user") User user, @Param("subject") String subject);
    
    /**
     * Count completed lessons for user in subject
     */
    @Query("SELECT COUNT(p) FROM Progress p JOIN p.lesson l WHERE p.user = :user AND l.subject = :subject AND p.status = 'COMPLETED'")
    long countCompletedLessonsInSubject(@Param("user") User user, @Param("subject") String subject);
    
    /**
     * Find user's last completed lesson in a subject
     */
    @Query("SELECT p FROM Progress p JOIN p.lesson l WHERE p.user = :user AND l.subject = :subject AND p.status = 'COMPLETED' ORDER BY l.sequenceOrder DESC LIMIT 1")
    Optional<Progress> findLastCompletedLessonInSubject(@Param("user") User user, @Param("subject") String subject);
    
    /**
     * Find progress updated after specific date
     */
    List<Progress> findByUpdatedAtAfter(LocalDateTime date);
    
    /**
     * Find users who completed a specific lesson
     */
    @Query("SELECT p.user FROM Progress p WHERE p.lesson = :lesson AND p.status = 'COMPLETED'")
    List<User> findUsersWhoCompletedLesson(@Param("lesson") Lesson lesson);
    
    /**
     * Calculate average score for a lesson
     */
    @Query("SELECT AVG(p.score) FROM Progress p WHERE p.lesson = :lesson AND p.score IS NOT NULL")
    Double calculateAverageScoreForLesson(@Param("lesson") Lesson lesson);
    
    /**
     * Find progress with low scores (below threshold)
     */
    @Query("SELECT p FROM Progress p WHERE p.score < :threshold AND p.score IS NOT NULL")
    List<Progress> findProgressWithLowScores(@Param("threshold") Integer threshold);
    
    /**
     * Check if user has completed prerequisites for a lesson
     */
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.user = :user AND p.lesson.id IN :prerequisiteIds AND p.status = 'COMPLETED'")
    long countCompletedPrerequisites(@Param("user") User user, @Param("prerequisiteIds") List<Long> prerequisiteIds);
    
    /**
     * Find user's current learning streak (consecutive days with progress)
     */
    @Query(value = "SELECT COUNT(*) FROM (SELECT DISTINCT DATE(updated_at) FROM progress WHERE user_id = :userId AND updated_at >= :startDate ORDER BY DATE(updated_at) DESC) AS daily_progress", nativeQuery = true)
    long calculateLearningStreak(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    /**
     * Find progress by user and lessons
     */
    List<Progress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}