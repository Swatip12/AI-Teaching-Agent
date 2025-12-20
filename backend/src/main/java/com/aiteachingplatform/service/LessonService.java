package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.exception.BusinessException;
import com.aiteachingplatform.exception.ResourceNotFoundException;
import com.aiteachingplatform.repository.LessonRepository;
import org.springframework.http.HttpStatus;
import com.aiteachingplatform.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for lesson content management operations
 * Handles lesson CRUD operations, sequencing, and prerequisite logic
 */
@Service
@Transactional
public class LessonService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    /**
     * Create a new lesson
     */
    public Lesson createLesson(Lesson lesson) {
        validateLessonStructure(lesson);
        return lessonRepository.save(lesson);
    }
    
    /**
     * Update an existing lesson
     */
    public Lesson updateLesson(Long id, Lesson lessonDetails) {
        Optional<Lesson> existingLesson = lessonRepository.findById(id);
        if (existingLesson.isEmpty()) {
            throw new ResourceNotFoundException("Lesson", id.toString());
        }
        
        Lesson lesson = existingLesson.get();
        lesson.setTitle(lessonDetails.getTitle());
        lesson.setSubject(lessonDetails.getSubject());
        lesson.setContent(lessonDetails.getContent());
        lesson.setObjectives(lessonDetails.getObjectives());
        lesson.setDifficulty(lessonDetails.getDifficulty());
        lesson.setEstimatedDurationMinutes(lessonDetails.getEstimatedDurationMinutes());
        lesson.setPrerequisiteLessonIds(lessonDetails.getPrerequisiteLessonIds());
        
        validateLessonStructure(lesson);
        return lessonRepository.save(lesson);
    }
    
    /**
     * Get lesson by ID
     */
    @Transactional(readOnly = true)
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }
    
    /**
     * Find lesson by ID (alias for getLessonById)
     */
    @Transactional(readOnly = true)
    public Optional<Lesson> findById(Long id) {
        return lessonRepository.findById(id);
    }
    
    /**
     * Get all lessons with pagination
     */
    @Transactional(readOnly = true)
    public Page<Lesson> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable);
    }
    
    /**
     * Get lessons by subject ordered by sequence
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsBySubject(String subject) {
        return lessonRepository.findBySubjectOrderBySequenceOrder(subject);
    }
    
    /**
     * Get lessons by subject and difficulty
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsBySubjectAndDifficulty(String subject, Lesson.Difficulty difficulty) {
        return lessonRepository.findBySubjectAndDifficultyOrderBySequenceOrder(subject, difficulty);
    }
    
    /**
     * Get the first lesson in a subject (entry point)
     */
    @Transactional(readOnly = true)
    public Optional<Lesson> getFirstLessonInSubject(String subject) {
        return lessonRepository.findFirstBySubjectOrderBySequenceOrder(subject);
    }
    
    /**
     * Get the next lesson in sequence for a subject
     */
    @Transactional(readOnly = true)
    public Optional<Lesson> getNextLessonInSubject(String subject, Integer currentSequenceOrder) {
        return lessonRepository.findNextLessonInSubject(subject, currentSequenceOrder);
    }
    
    /**
     * Get all available subjects
     */
    @Transactional(readOnly = true)
    public List<String> getAllSubjects() {
        return lessonRepository.findAllSubjects();
    }
    
    /**
     * Get lessons without prerequisites (entry points)
     */
    @Transactional(readOnly = true)
    public List<Lesson> getEntryPointLessons() {
        return lessonRepository.findLessonsWithoutPrerequisites();
    }
    
    /**
     * Check if user can access a lesson based on prerequisites
     */
    @Transactional(readOnly = true)
    public boolean canUserAccessLesson(User user, Lesson lesson) {
        // If lesson has no prerequisites, user can access it
        if (lesson.getPrerequisiteLessonIds() == null || lesson.getPrerequisiteLessonIds().isEmpty()) {
            return true;
        }
        
        // Check if all prerequisites are completed
        for (Long prereqId : lesson.getPrerequisiteLessonIds()) {
            Optional<Lesson> prereqLesson = lessonRepository.findById(prereqId);
            if (prereqLesson.isEmpty()) {
                return false; // Prerequisite doesn't exist
            }
            
            Optional<Progress> progress = progressRepository.findByUserAndLesson(user, prereqLesson.get());
            if (progress.isEmpty() || progress.get().getStatus() != Progress.ProgressStatus.COMPLETED) {
                return false; // Prerequisite not completed
            }
        }
        
        return true; // All prerequisites completed
    }
    
    /**
     * Get accessible lessons for a user (considering prerequisites)
     */
    @Transactional(readOnly = true)
    public List<Lesson> getAccessibleLessonsForUser(User user, String subject) {
        List<Lesson> allLessons = getLessonsBySubject(subject);
        return allLessons.stream()
                .filter(lesson -> canUserAccessLesson(user, lesson))
                .toList();
    }
    
    /**
     * Delete a lesson
     */
    public void deleteLesson(Long id) {
        Optional<Lesson> lesson = lessonRepository.findById(id);
        if (lesson.isEmpty()) {
            throw new ResourceNotFoundException("Lesson", id.toString());
        }
        
        // Check if lesson is a prerequisite for other lessons
        List<Lesson> dependentLessons = lessonRepository.findLessonsWithPrerequisite(id);
        if (!dependentLessons.isEmpty()) {
            throw new BusinessException("LESSON_HAS_DEPENDENTS", 
                "Cannot delete lesson as it is a prerequisite for other lessons", 
                HttpStatus.CONFLICT);
        }
        
        lessonRepository.deleteById(id);
    }
    
    /**
     * Search lessons by title
     */
    @Transactional(readOnly = true)
    public List<Lesson> searchLessonsByTitle(String title) {
        return lessonRepository.findByTitleContainingIgnoreCase(title);
    }
    
    /**
     * Get lessons by duration range
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByDurationRange(Integer minDuration, Integer maxDuration) {
        return lessonRepository.findByEstimatedDurationMinutesBetween(minDuration, maxDuration);
    }
    
    /**
     * Count lessons by subject
     */
    @Transactional(readOnly = true)
    public long countLessonsBySubject(String subject) {
        return lessonRepository.countBySubject(subject);
    }
    
    /**
     * Validate lesson structure according to requirements
     * Requirements 2.1, 2.2, 2.3, 2.4, 2.5
     */
    private void validateLessonStructure(Lesson lesson) {
        if (lesson.getContent() == null || lesson.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson content cannot be empty");
        }
        
        String content = lesson.getContent().toLowerCase();
        
        // Requirement 2.1: Should explain concepts in simple words first
        if (!content.contains("simple explanation")) {
            throw new IllegalArgumentException("Lesson must contain a simple explanation section");
        }
        
        // Requirement 2.2: Should provide real-life examples before technical examples
        int realLifeIndex = content.indexOf("real-life example");
        int technicalIndex = content.indexOf("technical example");
        
        if (realLifeIndex < 0) {
            throw new IllegalArgumentException("Lesson must contain a real-life example section");
        }
        
        if (technicalIndex < 0) {
            throw new IllegalArgumentException("Lesson must contain a technical example section");
        }
        
        if (realLifeIndex >= technicalIndex) {
            throw new IllegalArgumentException("Real-life example must come before technical example");
        }
        
        // Requirement 2.3: Should include simple coding demonstrations
        if (!content.contains("coding demonstration")) {
            throw new IllegalArgumentException("Lesson must contain a coding demonstration section");
        }
        
        // Note: Requirements 2.4 and 2.5 (checkpoint and practice questions) are validated
        // through the relationship constraints in the entity model
    }
    
    /**
     * Validate lesson sequence order within subject
     */
    public void validateSequenceOrder(String subject, Integer sequenceOrder, Long excludeLessonId) {
        boolean exists = lessonRepository.existsBySubjectAndSequenceOrder(subject, sequenceOrder);
        if (exists) {
            // Check if it's the same lesson being updated
            Optional<Lesson> existingLesson = lessonRepository.findBySubjectOrderBySequenceOrder(subject)
                    .stream()
                    .filter(l -> l.getSequenceOrder().equals(sequenceOrder))
                    .findFirst();
            
            if (existingLesson.isPresent() && !existingLesson.get().getId().equals(excludeLessonId)) {
                throw new IllegalArgumentException("Sequence order " + sequenceOrder + " already exists for subject " + subject);
            }
        }
    }
}