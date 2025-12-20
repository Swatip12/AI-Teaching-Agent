package com.aiteachingplatform.controller;

import com.aiteachingplatform.dto.MessageResponse;
import com.aiteachingplatform.dto.QuestionFeedbackResponse;
import com.aiteachingplatform.dto.QuestionSubmissionRequest;
import com.aiteachingplatform.model.CheckpointQuestion;
import com.aiteachingplatform.model.Lesson;
import com.aiteachingplatform.model.PracticeQuestion;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.repository.LessonRepository;
import com.aiteachingplatform.security.UserDetailsImpl;
import com.aiteachingplatform.service.LessonService;
import com.aiteachingplatform.service.QuestionFeedbackService;
import com.aiteachingplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for lesson content management
 * Provides endpoints for lesson CRUD operations, sequencing, and access control
 */
@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LessonController {
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuestionFeedbackService questionFeedbackService;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    /**
     * Create a new lesson (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLesson(@Valid @RequestBody Lesson lesson) {
        try {
            // Validate sequence order
            lessonService.validateSequenceOrder(lesson.getSubject(), lesson.getSequenceOrder(), null);
            
            Lesson createdLesson = lessonService.createLesson(lesson);
            return ResponseEntity.ok(createdLesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Update an existing lesson (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lessonDetails) {
        try {
            // Validate sequence order
            lessonService.validateSequenceOrder(lessonDetails.getSubject(), lessonDetails.getSequenceOrder(), id);
            
            Lesson updatedLesson = lessonService.updateLesson(id, lessonDetails);
            return ResponseEntity.ok(updatedLesson);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Get lesson by ID (accessible to authenticated users if they can access it)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLessonById(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Lesson> lesson = lessonService.getLessonById(id);
            if (lesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user can access this lesson
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isPresent() && !lessonService.canUserAccessLesson(user.get(), lesson.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied: Prerequisites not completed"));
            }
            
            return ResponseEntity.ok(lesson.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Get all lessons with pagination (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Lesson>> getAllLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "subject") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Lesson> lessons = lessonService.getAllLessons(pageable);
        
        return ResponseEntity.ok(lessons);
    }
    
    /**
     * Get lessons by subject (accessible to authenticated users)
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<?> getLessonsBySubject(@PathVariable String subject, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            // Get only accessible lessons for the user
            List<Lesson> accessibleLessons = lessonService.getAccessibleLessonsForUser(user.get(), subject);
            return ResponseEntity.ok(accessibleLessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving lessons: " + e.getMessage()));
        }
    }
    
    /**
     * Get lessons by subject and difficulty
     */
    @GetMapping("/subject/{subject}/difficulty/{difficulty}")
    public ResponseEntity<?> getLessonsBySubjectAndDifficulty(
            @PathVariable String subject, 
            @PathVariable Lesson.Difficulty difficulty,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            List<Lesson> lessons = lessonService.getLessonsBySubjectAndDifficulty(subject, difficulty);
            
            // Filter by accessibility
            List<Lesson> accessibleLessons = lessons.stream()
                    .filter(lesson -> lessonService.canUserAccessLesson(user.get(), lesson))
                    .toList();
            
            return ResponseEntity.ok(accessibleLessons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving lessons: " + e.getMessage()));
        }
    }
    
    /**
     * Get the first lesson in a subject
     */
    @GetMapping("/subject/{subject}/first")
    public ResponseEntity<?> getFirstLessonInSubject(@PathVariable String subject) {
        try {
            Optional<Lesson> firstLesson = lessonService.getFirstLessonInSubject(subject);
            if (firstLesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(firstLesson.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving first lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Get the next lesson in sequence
     */
    @GetMapping("/subject/{subject}/next/{currentSequenceOrder}")
    public ResponseEntity<?> getNextLessonInSubject(
            @PathVariable String subject, 
            @PathVariable Integer currentSequenceOrder,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            Optional<Lesson> nextLesson = lessonService.getNextLessonInSubject(subject, currentSequenceOrder);
            if (nextLesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user can access the next lesson
            if (!lessonService.canUserAccessLesson(user.get(), nextLesson.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied: Prerequisites not completed"));
            }
            
            return ResponseEntity.ok(nextLesson.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving next lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Get all available subjects
     */
    @GetMapping("/subjects")
    public ResponseEntity<List<String>> getAllSubjects() {
        List<String> subjects = lessonService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }
    
    /**
     * Get entry point lessons (lessons without prerequisites)
     */
    @GetMapping("/entry-points")
    public ResponseEntity<List<Lesson>> getEntryPointLessons() {
        List<Lesson> entryPoints = lessonService.getEntryPointLessons();
        return ResponseEntity.ok(entryPoints);
    }
    
    /**
     * Check if user can access a specific lesson
     */
    @GetMapping("/{id}/access-check")
    public ResponseEntity<?> checkLessonAccess(@PathVariable Long id, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            Optional<Lesson> lesson = lessonService.getLessonById(id);
            
            if (user.isEmpty() || lesson.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            boolean canAccess = lessonService.canUserAccessLesson(user.get(), lesson.get());
            return ResponseEntity.ok(new AccessCheckResponse(canAccess));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error checking lesson access: " + e.getMessage()));
        }
    }
    
    /**
     * Delete a lesson (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        try {
            lessonService.deleteLesson(id);
            return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error deleting lesson: " + e.getMessage()));
        }
    }
    
    /**
     * Search lessons by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<Lesson>> searchLessonsByTitle(@RequestParam String title) {
        List<Lesson> lessons = lessonService.searchLessonsByTitle(title);
        return ResponseEntity.ok(lessons);
    }
    
    /**
     * Get lessons by duration range
     */
    @GetMapping("/duration")
    public ResponseEntity<List<Lesson>> getLessonsByDurationRange(
            @RequestParam Integer minDuration,
            @RequestParam Integer maxDuration) {
        List<Lesson> lessons = lessonService.getLessonsByDurationRange(minDuration, maxDuration);
        return ResponseEntity.ok(lessons);
    }
    
    /**
     * Get lesson count by subject
     */
    @GetMapping("/subject/{subject}/count")
    public ResponseEntity<Long> countLessonsBySubject(@PathVariable String subject) {
        long count = lessonService.countLessonsBySubject(subject);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Submit answer for checkpoint question and receive immediate feedback
     * Requirements 3.1: Immediate feedback on checkpoint questions
     */
    @PostMapping("/checkpoint-questions/{questionId}/submit")
    public ResponseEntity<?> submitCheckpointAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionSubmissionRequest request,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            // Find the checkpoint question
            CheckpointQuestion question = findCheckpointQuestionById(questionId);
            if (question == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user can access the lesson containing this question
            if (!lessonService.canUserAccessLesson(user.get(), question.getLesson())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied: Prerequisites not completed"));
            }
            
            // Evaluate answer and provide immediate feedback
            QuestionFeedbackResponse feedback = questionFeedbackService.evaluateCheckpointAnswer(
                question, request.getAnswer(), user
            );
            
            return ResponseEntity.ok(feedback);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error processing answer: " + e.getMessage()));
        }
    }
    
    /**
     * Submit answer for practice question and receive immediate feedback
     * Requirements 3.3: Feedback on practice questions with explanations
     */
    @PostMapping("/practice-questions/{questionId}/submit")
    public ResponseEntity<?> submitPracticeAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionSubmissionRequest request,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            // Find the practice question
            PracticeQuestion question = findPracticeQuestionById(questionId);
            if (question == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user can access the lesson containing this question
            if (!lessonService.canUserAccessLesson(user.get(), question.getLesson())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied: Prerequisites not completed"));
            }
            
            // Evaluate answer and provide immediate feedback
            QuestionFeedbackResponse feedback = questionFeedbackService.evaluatePracticeAnswer(
                question, request.getAnswer(), user
            );
            
            return ResponseEntity.ok(feedback);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error processing answer: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to find checkpoint question by ID
     */
    private CheckpointQuestion findCheckpointQuestionById(Long questionId) {
        return lessonRepository.findAll().stream()
                .flatMap(lesson -> lesson.getCheckpointQuestions().stream())
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Helper method to find practice question by ID
     */
    private PracticeQuestion findPracticeQuestionById(Long questionId) {
        return lessonRepository.findAll().stream()
                .flatMap(lesson -> lesson.getPracticeQuestions().stream())
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Response class for access check
     */
    public static class AccessCheckResponse {
        private boolean canAccess;
        
        public AccessCheckResponse(boolean canAccess) {
            this.canAccess = canAccess;
        }
        
        public boolean isCanAccess() {
            return canAccess;
        }
        
        public void setCanAccess(boolean canAccess) {
            this.canAccess = canAccess;
        }
    }
}