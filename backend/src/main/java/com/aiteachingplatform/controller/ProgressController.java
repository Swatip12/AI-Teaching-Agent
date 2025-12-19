package com.aiteachingplatform.controller;

import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.Progress;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.service.LessonService;
import com.aiteachingplatform.service.ProgressTrackingService;
import com.aiteachingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for progress tracking operations
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {
    
    @Autowired
    private ProgressTrackingService progressTrackingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LessonService lessonService;
    
    /**
     * Start a lesson for the authenticated user
     */
    @PostMapping("/lessons/{lessonId}/start")
    public ResponseEntity<Progress> startLesson(@PathVariable Long lessonId, Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Lesson lesson = lessonService.getLessonById(lessonId);
            
            Progress progress = progressTrackingService.startLesson(user, lesson);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Complete a lesson with a score
     */
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Progress> completeLesson(@PathVariable Long lessonId, 
                                                 @RequestParam Integer score, 
                                                 Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Lesson lesson = lessonService.getLessonById(lessonId);
            
            Progress progress = progressTrackingService.completeLesson(user, lesson, score);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update progress percentage for a lesson
     */
    @PutMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<Progress> updateProgress(@PathVariable Long lessonId, 
                                                 @RequestParam Integer percentage, 
                                                 Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Lesson lesson = lessonService.getLessonById(lessonId);
            
            Progress progress = progressTrackingService.updateProgress(user, lesson, percentage);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get available lessons for user in a subject
     */
    @GetMapping("/subjects/{subject}/available-lessons")
    public ResponseEntity<List<Lesson>> getAvailableLessons(@PathVariable String subject, 
                                                          Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            List<Lesson> lessons = progressTrackingService.getAvailableLessons(user, subject);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's progress in a specific subject
     */
    @GetMapping("/subjects/{subject}")
    public ResponseEntity<List<Progress>> getSubjectProgress(@PathVariable String subject, 
                                                           Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            List<Progress> progress = progressTrackingService.getUserProgressInSubject(user, subject);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get subject completion percentage
     */
    @GetMapping("/subjects/{subject}/completion")
    public ResponseEntity<Double> getSubjectCompletion(@PathVariable String subject, 
                                                     Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Double completion = progressTrackingService.calculateSubjectCompletionPercentage(user, subject);
            return ResponseEntity.ok(completion != null ? completion : 0.0);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's overall progress
     */
    @GetMapping("/overview")
    public ResponseEntity<List<Progress>> getOverallProgress(Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            List<Progress> progress = progressTrackingService.getUserOverallProgress(user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get next recommended lesson for a subject
     */
    @GetMapping("/subjects/{subject}/next-lesson")
    public ResponseEntity<Lesson> getNextRecommendedLesson(@PathVariable String subject, 
                                                         Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Optional<Lesson> nextLesson = progressTrackingService.getNextRecommendedLesson(user, subject);
            return nextLesson.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's last active lesson
     */
    @GetMapping("/last-active")
    public ResponseEntity<Progress> getLastActiveLesson(Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            Optional<Progress> lastActive = progressTrackingService.getLastActiveLesson(user);
            return lastActive.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get progress analytics for the user
     */
    @GetMapping("/analytics")
    public ResponseEntity<ProgressTrackingService.ProgressAnalytics> getProgressAnalytics(Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            ProgressTrackingService.ProgressAnalytics analytics = progressTrackingService.getProgressAnalytics(user);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user's learning streak
     */
    @GetMapping("/streak")
    public ResponseEntity<Long> getLearningStreak(Authentication auth) {
        try {
            User user = userService.getCurrentUser(auth);
            long streak = progressTrackingService.getUserLearningStreak(user);
            return ResponseEntity.ok(streak);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}