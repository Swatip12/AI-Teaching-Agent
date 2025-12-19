package com.aiteachingplatform.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for code execution results
 * Contains execution output, errors, and metadata
 */
public class CodeExecutionResponse {
    
    private boolean success;
    private String output;
    private String error;
    private String compilationError;
    private ExecutionStatus status;
    private long executionTimeMs;
    private int memoryUsageMB;
    private LocalDateTime executedAt;
    private String language;
    
    public enum ExecutionStatus {
        SUCCESS,
        COMPILATION_ERROR,
        RUNTIME_ERROR,
        TIMEOUT,
        MEMORY_LIMIT_EXCEEDED,
        SECURITY_VIOLATION,
        SYSTEM_ERROR
    }
    
    // Constructors
    public CodeExecutionResponse() {
        this.executedAt = LocalDateTime.now();
    }
    
    public CodeExecutionResponse(boolean success, String output, ExecutionStatus status) {
        this();
        this.success = success;
        this.output = output;
        this.status = status;
    }
    
    // Static factory methods for common responses
    public static CodeExecutionResponse success(String output, long executionTimeMs) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = true;
        response.output = output;
        response.status = ExecutionStatus.SUCCESS;
        response.executionTimeMs = executionTimeMs;
        return response;
    }
    
    public static CodeExecutionResponse compilationError(String error) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.compilationError = error;
        response.status = ExecutionStatus.COMPILATION_ERROR;
        return response;
    }
    
    public static CodeExecutionResponse runtimeError(String error) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.error = error;
        response.status = ExecutionStatus.RUNTIME_ERROR;
        return response;
    }
    
    public static CodeExecutionResponse timeout() {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.error = "Code execution timed out";
        response.status = ExecutionStatus.TIMEOUT;
        return response;
    }
    
    public static CodeExecutionResponse memoryLimitExceeded() {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.error = "Memory limit exceeded";
        response.status = ExecutionStatus.MEMORY_LIMIT_EXCEEDED;
        return response;
    }
    
    public static CodeExecutionResponse securityViolation(String details) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.error = "Security violation: " + details;
        response.status = ExecutionStatus.SECURITY_VIOLATION;
        return response;
    }
    
    public static CodeExecutionResponse systemError(String error) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.success = false;
        response.error = "System error: " + error;
        response.status = ExecutionStatus.SYSTEM_ERROR;
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getCompilationError() {
        return compilationError;
    }
    
    public void setCompilationError(String compilationError) {
        this.compilationError = compilationError;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public int getMemoryUsageMB() {
        return memoryUsageMB;
    }
    
    public void setMemoryUsageMB(int memoryUsageMB) {
        this.memoryUsageMB = memoryUsageMB;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}