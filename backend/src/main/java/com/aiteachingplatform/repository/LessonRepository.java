package com.aiteachingplatform.repository;

import com.aiteachingplatform.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Lesson entity operations
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    /**
     * Find lessons by subject ordered by sequence
     */
    List<Lesson> findBySubjectOrderBySequenceOrder(String subject);
    
    /**
     * Find lessons by subject and difficulty
     */
    List<Lesson> findBySubjectAndDifficultyOrderBySequenceOrder(String subject, Lesson.Difficulty difficulty);
    
    /**
     * Find the first lesson in a subject (lowest sequence order)
     */
    Optional<Lesson> findFirstBySubjectOrderBySequenceOrder(String subject);
    
    /**
     * Find the next lesson in sequence for a subject
     */
    @Query("SELECT l FROM Lesson l WHERE l.subject = :subject AND l.sequenceOrder > :currentOrder ORDER BY l.sequenceOrder ASC LIMIT 1")
    Optional<Lesson> findNextLessonInSubject(@Param("subject") String subject, @Param("currentOrder") Integer currentOrder);
    
    /**
     * Find lessons that have specific prerequisite
     */
    @Query("SELECT l FROM Lesson l WHERE :prerequisiteId MEMBER OF l.prerequisiteLessonIds")
    List<Lesson> findLessonsWithPrerequisite(@Param("prerequisiteId") Long prerequisiteId);
    
    /**
     * Find lessons without prerequisites (entry points)
     */
    @Query("SELECT l FROM Lesson l WHERE l.prerequisiteLessonIds IS EMPTY ORDER BY l.subject, l.sequenceOrder")
    List<Lesson> findLessonsWithoutPrerequisites();
    
    /**
     * Find all distinct subjects
     */
    @Query("SELECT DISTINCT l.subject FROM Lesson l ORDER BY l.subject")
    List<String> findAllSubjects();
    
    /**
     * Count lessons by subject
     */
    long countBySubject(String subject);
    
    /**
     * Find lessons by title containing text (case insensitive)
     */
    List<Lesson> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find lessons with estimated duration within range
     */
    List<Lesson> findByEstimatedDurationMinutesBetween(Integer minDuration, Integer maxDuration);
    
    /**
     * Check if lesson exists with specific sequence order in subject
     */
    boolean existsBySubjectAndSequenceOrder(String subject, Integer sequenceOrder);
}