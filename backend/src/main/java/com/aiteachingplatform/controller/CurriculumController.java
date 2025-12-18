package com.aiteachingplatform.controller;

import com.aiteachingplatform.dto.MessageResponse;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.security.UserDetailsImpl;
import com.aiteachingplatform.service.CurriculumService;
import com.aiteachingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for curriculum and subject management
 * Provides endpoints for subject information, learning paths, and curriculum validation
 */
@RestController
@RequestMapping("/api/curriculum")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CurriculumController {
    
    @Autowired
    private CurriculumService curriculumService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all subjects with detailed information
     */
    @GetMapping("/subjects")
    public ResponseEntity<List<CurriculumService.SubjectInfo>> getAllSubjectsWithInfo() {
        try {
            List<CurriculumService.SubjectInfo> subjects = curriculumService.getAllSubjectsWithInfo();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get detailed information about a specific subject
     */
    @GetMapping("/subjects/{subject}")
    public ResponseEntity<?> getSubjectInfo(@PathVariable String subject) {
        try {
            CurriculumService.SubjectInfo subjectInfo = curriculumService.getSubjectInfo(subject);
            return ResponseEntity.ok(subjectInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving subject information: " + e.getMessage()));
        }
    }
    
    /**
     * Get learning path for a subject
     */
    @GetMapping("/subjects/{subject}/learning-path")
    public ResponseEntity<?> getLearningPath(@PathVariable String subject) {
        try {
            CurriculumService.LearningPath learningPath = curriculumService.getLearningPath(subject);
            return ResponseEntity.ok(learningPath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving learning path: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's progress in a specific subject
     */
    @GetMapping("/subjects/{subject}/progress")
    public ResponseEntity<?> getUserSubjectProgress(@PathVariable String subject, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            CurriculumService.SubjectProgress progress = curriculumService.getUserSubjectProgress(user.get(), subject);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving subject progress: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommended subjects for beginners
     */
    @GetMapping("/subjects/recommended/beginners")
    public ResponseEntity<List<String>> getRecommendedSubjectsForBeginners() {
        try {
            List<String> recommendedSubjects = curriculumService.getRecommendedSubjectsForBeginners();
            return ResponseEntity.ok(recommendedSubjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Validate curriculum structure for a subject (Admin only)
     */
    @GetMapping("/subjects/{subject}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateSubjectCurriculum(@PathVariable String subject) {
        try {
            CurriculumService.CurriculumValidationResult result = curriculumService.validateSubjectCurriculum(subject);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error validating curriculum: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's overall progress across all subjects
     */
    @GetMapping("/progress/overview")
    public ResponseEntity<?> getUserOverallProgress(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userService.findById(userDetails.getId());
            
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User not found"));
            }
            
            List<CurriculumService.SubjectInfo> allSubjects = curriculumService.getAllSubjectsWithInfo();
            List<OverallProgressResponse> overallProgress = allSubjects.stream()
                    .map(subjectInfo -> {
                        CurriculumService.SubjectProgress progress = 
                                curriculumService.getUserSubjectProgress(user.get(), subjectInfo.getSubject());
                        return new OverallProgressResponse(subjectInfo, progress);
                    })
                    .toList();
            
            return ResponseEntity.ok(overallProgress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving overall progress: " + e.getMessage()));
        }
    }
    
    /**
     * Response class for overall progress
     */
    public static class OverallProgressResponse {
        private final CurriculumService.SubjectInfo subjectInfo;
        private final CurriculumService.SubjectProgress progress;
        
        public OverallProgressResponse(CurriculumService.SubjectInfo subjectInfo, 
                                     CurriculumService.SubjectProgress progress) {
            this.subjectInfo = subjectInfo;
            this.progress = progress;
        }
        
        public CurriculumService.SubjectInfo getSubjectInfo() {
            return subjectInfo;
        }
        
        public CurriculumService.SubjectProgress getProgress() {
            return progress;
        }
    }
}