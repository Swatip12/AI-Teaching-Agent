package com.aiteachingplatform.service;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for curriculum and subject management
 * Handles subject organization, learning paths, and curriculum structure
 */
@Service
@Transactional
public class CurriculumService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private ProgressRepository progressRepository;
    
    /**
     * Get all available subjects with metadata
     */
    @Transactional(readOnly = true)
    public List<SubjectInfo> getAllSubjectsWithInfo() {
        List<String> subjects = lessonRepository.findAllSubjects();
        
        return subjects.stream()
                .map(this::getSubjectInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * Get detailed information about a specific subject
     */
    @Transactional(readOnly = true)
    public SubjectInfo getSubjectInfo(String subject) {
        List<Lesson> lessons = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
        
        long totalLessons = lessons.size();
        long beginnerLessons = lessons.stream()
                .filter(l -> l.getDifficulty() == Lesson.Difficulty.BEGINNER)
                .count();
        long intermediateLessons = lessons.stream()
                .filter(l -> l.getDifficulty() == Lesson.Difficulty.INTERMEDIATE)
                .count();
        long advancedLessons = lessons.stream()
                .filter(l -> l.getDifficulty() == Lesson.Difficulty.ADVANCED)
                .count();
        
        int totalEstimatedMinutes = lessons.stream()
                .mapToInt(l -> l.getEstimatedDurationMinutes() != null ? l.getEstimatedDurationMinutes() : 0)
                .sum();
        
        Optional<Lesson> firstLesson = lessons.isEmpty() ? Optional.empty() : Optional.of(lessons.get(0));
        
        return new SubjectInfo(subject, totalLessons, beginnerLessons, intermediateLessons, 
                             advancedLessons, totalEstimatedMinutes, firstLesson.orElse(null));
    }
    
    /**
     * Get learning path for a subject (lessons in proper sequence)
     */
    @Transactional(readOnly = true)
    public LearningPath getLearningPath(String subject) {
        List<Lesson> lessons = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
        
        // Group lessons by difficulty
        Map<Lesson.Difficulty, List<Lesson>> lessonsByDifficulty = lessons.stream()
                .collect(Collectors.groupingBy(Lesson::getDifficulty));
        
        return new LearningPath(subject, lessons, lessonsByDifficulty);
    }
    
    /**
     * Get user's progress in a subject
     */
    @Transactional(readOnly = true)
    public SubjectProgress getUserSubjectProgress(User user, String subject) {
        List<Lesson> allLessons = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
        List<Progress> userProgress = progressRepository.findByUserAndLessonIn(user, allLessons);
        
        Map<Long, Progress> progressMap = userProgress.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));
        
        long completedLessons = userProgress.stream()
                .filter(p -> p.getStatus() == Progress.ProgressStatus.COMPLETED)
                .count();
        
        long inProgressLessons = userProgress.stream()
                .filter(p -> p.getStatus() == Progress.ProgressStatus.IN_PROGRESS)
                .count();
        
        double completionPercentage = allLessons.isEmpty() ? 0.0 : 
                (double) completedLessons / allLessons.size() * 100.0;
        
        // Find next available lesson
        Lesson nextLesson = findNextAvailableLesson(user, allLessons, progressMap);
        
        return new SubjectProgress(subject, allLessons.size(), (int) completedLessons, 
                                 (int) inProgressLessons, completionPercentage, nextLesson);
    }
    
    /**
     * Find the next lesson a user can access
     */
    private Lesson findNextAvailableLesson(User user, List<Lesson> allLessons, Map<Long, Progress> progressMap) {
        for (Lesson lesson : allLessons) {
            Progress progress = progressMap.get(lesson.getId());
            
            // If lesson is not completed and user can access it
            if ((progress == null || progress.getStatus() != Progress.ProgressStatus.COMPLETED) &&
                canUserAccessLesson(user, lesson)) {
                return lesson;
            }
        }
        return null; // All lessons completed or none accessible
    }
    
    /**
     * Check if user can access a lesson (considering prerequisites)
     */
    private boolean canUserAccessLesson(User user, Lesson lesson) {
        if (lesson.getPrerequisiteLessonIds() == null || lesson.getPrerequisiteLessonIds().isEmpty()) {
            return true;
        }
        
        for (Long prereqId : lesson.getPrerequisiteLessonIds()) {
            Optional<Lesson> prereqLesson = lessonRepository.findById(prereqId);
            if (prereqLesson.isEmpty()) {
                return false;
            }
            
            Optional<Progress> progress = progressRepository.findByUserAndLesson(user, prereqLesson.get());
            if (progress.isEmpty() || progress.get().getStatus() != Progress.ProgressStatus.COMPLETED) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get recommended subjects for beginners
     */
    @Transactional(readOnly = true)
    public List<String> getRecommendedSubjectsForBeginners() {
        // Get subjects that have beginner-level entry points
        List<Lesson> entryPointLessons = lessonRepository.findLessonsWithoutPrerequisites();
        
        return entryPointLessons.stream()
                .filter(lesson -> lesson.getDifficulty() == Lesson.Difficulty.BEGINNER)
                .map(Lesson::getSubject)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Validate curriculum structure for a subject
     */
    public CurriculumValidationResult validateSubjectCurriculum(String subject) {
        List<Lesson> lessons = lessonRepository.findBySubjectOrderBySequenceOrder(subject);
        List<String> issues = new ArrayList<>();
        
        if (lessons.isEmpty()) {
            issues.add("Subject has no lessons");
            return new CurriculumValidationResult(false, issues);
        }
        
        // Check for entry point (lesson without prerequisites)
        boolean hasEntryPoint = lessons.stream()
                .anyMatch(l -> l.getPrerequisiteLessonIds() == null || l.getPrerequisiteLessonIds().isEmpty());
        
        if (!hasEntryPoint) {
            issues.add("Subject has no entry point lessons (lessons without prerequisites)");
        }
        
        // Check sequence order continuity
        Set<Integer> sequenceOrders = lessons.stream()
                .map(Lesson::getSequenceOrder)
                .collect(Collectors.toSet());
        
        for (int i = 1; i <= lessons.size(); i++) {
            if (!sequenceOrders.contains(i)) {
                issues.add("Missing sequence order: " + i);
            }
        }
        
        // Check prerequisite validity
        Set<Long> lessonIds = lessons.stream().map(Lesson::getId).collect(Collectors.toSet());
        
        for (Lesson lesson : lessons) {
            if (lesson.getPrerequisiteLessonIds() != null) {
                for (Long prereqId : lesson.getPrerequisiteLessonIds()) {
                    if (!lessonIds.contains(prereqId)) {
                        Optional<Lesson> prereqLesson = lessonRepository.findById(prereqId);
                        if (prereqLesson.isEmpty()) {
                            issues.add("Lesson '" + lesson.getTitle() + "' references non-existent prerequisite: " + prereqId);
                        } else if (!prereqLesson.get().getSubject().equals(subject)) {
                            issues.add("Lesson '" + lesson.getTitle() + "' references prerequisite from different subject");
                        }
                    }
                }
            }
        }
        
        return new CurriculumValidationResult(issues.isEmpty(), issues);
    }
    
    /**
     * Data class for subject information
     */
    public static class SubjectInfo {
        private final String subject;
        private final long totalLessons;
        private final long beginnerLessons;
        private final long intermediateLessons;
        private final long advancedLessons;
        private final int totalEstimatedMinutes;
        private final Lesson firstLesson;
        
        public SubjectInfo(String subject, long totalLessons, long beginnerLessons, 
                          long intermediateLessons, long advancedLessons, 
                          int totalEstimatedMinutes, Lesson firstLesson) {
            this.subject = subject;
            this.totalLessons = totalLessons;
            this.beginnerLessons = beginnerLessons;
            this.intermediateLessons = intermediateLessons;
            this.advancedLessons = advancedLessons;
            this.totalEstimatedMinutes = totalEstimatedMinutes;
            this.firstLesson = firstLesson;
        }
        
        // Getters
        public String getSubject() { return subject; }
        public long getTotalLessons() { return totalLessons; }
        public long getBeginnerLessons() { return beginnerLessons; }
        public long getIntermediateLessons() { return intermediateLessons; }
        public long getAdvancedLessons() { return advancedLessons; }
        public int getTotalEstimatedMinutes() { return totalEstimatedMinutes; }
        public Lesson getFirstLesson() { return firstLesson; }
    }
    
    /**
     * Data class for learning path
     */
    public static class LearningPath {
        private final String subject;
        private final List<Lesson> lessons;
        private final Map<Lesson.Difficulty, List<Lesson>> lessonsByDifficulty;
        
        public LearningPath(String subject, List<Lesson> lessons, 
                           Map<Lesson.Difficulty, List<Lesson>> lessonsByDifficulty) {
            this.subject = subject;
            this.lessons = lessons;
            this.lessonsByDifficulty = lessonsByDifficulty;
        }
        
        // Getters
        public String getSubject() { return subject; }
        public List<Lesson> getLessons() { return lessons; }
        public Map<Lesson.Difficulty, List<Lesson>> getLessonsByDifficulty() { return lessonsByDifficulty; }
    }
    
    /**
     * Data class for user's subject progress
     */
    public static class SubjectProgress {
        private final String subject;
        private final int totalLessons;
        private final int completedLessons;
        private final int inProgressLessons;
        private final double completionPercentage;
        private final Lesson nextLesson;
        
        public SubjectProgress(String subject, int totalLessons, int completedLessons, 
                             int inProgressLessons, double completionPercentage, Lesson nextLesson) {
            this.subject = subject;
            this.totalLessons = totalLessons;
            this.completedLessons = completedLessons;
            this.inProgressLessons = inProgressLessons;
            this.completionPercentage = completionPercentage;
            this.nextLesson = nextLesson;
        }
        
        // Getters
        public String getSubject() { return subject; }
        public int getTotalLessons() { return totalLessons; }
        public int getCompletedLessons() { return completedLessons; }
        public int getInProgressLessons() { return inProgressLessons; }
        public double getCompletionPercentage() { return completionPercentage; }
        public Lesson getNextLesson() { return nextLesson; }
    }
    
    /**
     * Data class for curriculum validation results
     */
    public static class CurriculumValidationResult {
        private final boolean valid;
        private final List<String> issues;
        
        public CurriculumValidationResult(boolean valid, List<String> issues) {
            this.valid = valid;
            this.issues = issues;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<String> getIssues() { return issues; }
    }
}