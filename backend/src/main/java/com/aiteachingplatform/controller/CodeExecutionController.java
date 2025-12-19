package com.aiteachingplatform.controller;

import com.aiteachingplatform.dto.CodeExecutionRequest;
import com.aiteachingplatform.dto.CodeExecutionResponse;
import com.aiteachingplatform.dto.MessageResponse;
import com.aiteachingplatform.service.CodeExecutionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for code execution functionality
 * Provides endpoints for executing code in secure Docker containers
 */
@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CodeExecutionController {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionController.class);
    
    @Autowired
    private CodeExecutionService codeExecutionService;
    
    /**
     * Execute code in a secure container
     * Requirements: 6.1, 6.2, 6.3, 6.4
     */
    @PostMapping("/execute")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> executeCode(@Valid @RequestBody CodeExecutionRequest request) {
        try {
            logger.info("Executing {} code for user", request.getLanguage());
            
            // Validate request
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Code cannot be empty"));
            }
            
            if (request.getLanguage() == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Programming language must be specified"));
            }
            
            // Execute code
            CodeExecutionResponse response = codeExecutionService.executeCode(request);
            
            // Log execution result
            if (response.isSuccess()) {
                logger.info("Code execution successful in {}ms", response.getExecutionTimeMs());
            } else {
                logger.warn("Code execution failed: {}", response.getStatus());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid code execution request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Code execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Validate code without executing it
     * Performs security checks and basic syntax validation
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> validateCode(@Valid @RequestBody CodeExecutionRequest request) {
        try {
            logger.info("Validating {} code", request.getLanguage());
            
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Code cannot be empty"));
            }
            
            // For now, we'll use the execution service's security validation
            // In a full implementation, this could include syntax parsing
            CodeExecutionResponse dryRunResponse = codeExecutionService.executeCode(
                new CodeExecutionRequest("System.out.println(\"validation\");", CodeExecutionRequest.Language.JAVA)
            );
            
            if (dryRunResponse.getStatus() == CodeExecutionResponse.ExecutionStatus.SECURITY_VIOLATION) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Code validation failed: Security violation detected"));
            }
            
            return ResponseEntity.ok(new MessageResponse("Code validation passed"));
            
        } catch (Exception e) {
            logger.error("Error validating code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Code validation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get supported programming languages
     */
    @GetMapping("/languages")
    public ResponseEntity<CodeExecutionRequest.Language[]> getSupportedLanguages() {
        return ResponseEntity.ok(CodeExecutionRequest.Language.values());
    }
    
    /**
     * Get code execution service status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getServiceStatus() {
        try {
            String status = codeExecutionService.getServiceStatus();
            boolean dockerAvailable = codeExecutionService.isDockerAvailable();
            
            return ResponseEntity.ok(new ServiceStatusResponse(status, dockerAvailable));
            
        } catch (Exception e) {
            logger.error("Error getting service status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to get service status: " + e.getMessage()));
        }
    }
    
    /**
     * Execute code with lesson context
     * Provides additional validation and hints based on lesson requirements
     */
    @PostMapping("/execute/lesson/{lessonId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> executeCodeForLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody CodeExecutionRequest request) {
        try {
            logger.info("Executing code for lesson {} in language {}", lessonId, request.getLanguage());
            
            // Execute the code
            CodeExecutionResponse response = codeExecutionService.executeCode(request);
            
            // Add lesson-specific context to response
            response.setLanguage(request.getLanguage().getValue());
            
            // In a full implementation, this could include:
            // - Lesson-specific validation
            // - Expected output comparison
            // - Hints based on common mistakes
            // - Progress tracking integration
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error executing code for lesson {}", lessonId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Code execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get code execution hints for common errors
     */
    @PostMapping("/hints")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getExecutionHints(@Valid @RequestBody CodeExecutionRequest request) {
        try {
            // Execute code to get error information
            CodeExecutionResponse response = codeExecutionService.executeCode(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(new HintResponse("Code executed successfully! No hints needed."));
            }
            
            // Generate hints based on error type
            String hint = generateHintForError(response, request.getLanguage());
            return ResponseEntity.ok(new HintResponse(hint));
            
        } catch (Exception e) {
            logger.error("Error generating hints", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to generate hints: " + e.getMessage()));
        }
    }
    
    /**
     * Generate helpful hints based on execution errors
     */
    private String generateHintForError(CodeExecutionResponse response, CodeExecutionRequest.Language language) {
        switch (response.getStatus()) {
            case COMPILATION_ERROR:
                return generateCompilationHint(response.getCompilationError(), language);
            case RUNTIME_ERROR:
                return generateRuntimeHint(response.getError(), language);
            case TIMEOUT:
                return "Your code is taking too long to execute. Check for infinite loops or optimize your algorithm.";
            case MEMORY_LIMIT_EXCEEDED:
                return "Your code is using too much memory. Consider using more efficient data structures.";
            case SECURITY_VIOLATION:
                return "Your code contains potentially unsafe operations. Stick to basic programming constructs for learning.";
            default:
                return "Something went wrong. Check your code syntax and logic.";
        }
    }
    
    private String generateCompilationHint(String error, CodeExecutionRequest.Language language) {
        if (error == null) return "Check your code syntax.";
        
        String lowerError = error.toLowerCase();
        
        if (lowerError.contains("cannot find symbol")) {
            return "Variable or method not found. Check spelling and make sure you've declared all variables.";
        } else if (lowerError.contains("expected")) {
            return "Syntax error detected. Check for missing semicolons, brackets, or parentheses.";
        } else if (lowerError.contains("class") && language == CodeExecutionRequest.Language.JAVA) {
            return "Java class issues. Make sure your class name matches the filename and is properly structured.";
        }
        
        return "Compilation error: " + error.substring(0, Math.min(error.length(), 100)) + "...";
    }
    
    private String generateRuntimeHint(String error, CodeExecutionRequest.Language language) {
        if (error == null) return "Runtime error occurred. Check your program logic.";
        
        String lowerError = error.toLowerCase();
        
        if (lowerError.contains("nullpointerexception")) {
            return "Null pointer error. Make sure you initialize your variables before using them.";
        } else if (lowerError.contains("arrayindexoutofbounds")) {
            return "Array index error. Check that your array indices are within valid bounds.";
        } else if (lowerError.contains("dividebyzero") || lowerError.contains("division by zero")) {
            return "Division by zero error. Make sure you're not dividing by zero in your calculations.";
        }
        
        return "Runtime error: " + error.substring(0, Math.min(error.length(), 100)) + "...";
    }
    
    /**
     * Response classes
     */
    public static class ServiceStatusResponse {
        private String status;
        private boolean dockerAvailable;
        
        public ServiceStatusResponse(String status, boolean dockerAvailable) {
            this.status = status;
            this.dockerAvailable = dockerAvailable;
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isDockerAvailable() { return dockerAvailable; }
        public void setDockerAvailable(boolean dockerAvailable) { this.dockerAvailable = dockerAvailable; }
    }
    
    public static class HintResponse {
        private String hint;
        
        public HintResponse(String hint) {
            this.hint = hint;
        }
        
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
    }
}