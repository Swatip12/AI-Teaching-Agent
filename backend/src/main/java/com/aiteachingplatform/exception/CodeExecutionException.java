package com.aiteachingplatform.exception;

/**
 * Exception for code execution related errors
 * Requirement 6.3: Error handling for code execution
 */
public class CodeExecutionException extends RuntimeException {
    
    private final String language;
    private final boolean timeoutOccurred;
    private final String executionOutput;
    
    public CodeExecutionException(String message, String language) {
        super(message);
        this.language = language;
        this.timeoutOccurred = false;
        this.executionOutput = null;
    }
    
    public CodeExecutionException(String message, String language, boolean timeoutOccurred) {
        super(message);
        this.language = language;
        this.timeoutOccurred = timeoutOccurred;
        this.executionOutput = null;
    }
    
    public CodeExecutionException(String message, String language, String executionOutput) {
        super(message);
        this.language = language;
        this.timeoutOccurred = false;
        this.executionOutput = executionOutput;
    }
    
    public CodeExecutionException(String message, Throwable cause, String language) {
        super(message, cause);
        this.language = language;
        this.timeoutOccurred = false;
        this.executionOutput = null;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public boolean isTimeoutOccurred() {
        return timeoutOccurred;
    }
    
    public String getExecutionOutput() {
        return executionOutput;
    }
}