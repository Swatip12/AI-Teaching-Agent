package com.aiteachingplatform.exception;

/**
 * Exception for AI service related errors
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
public class AIServiceException extends RuntimeException {
    
    private final String serviceType;
    private final boolean isRetryable;
    
    public AIServiceException(String message) {
        super(message);
        this.serviceType = "OpenAI";
        this.isRetryable = true;
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.serviceType = "OpenAI";
        this.isRetryable = true;
    }
    
    public AIServiceException(String message, String serviceType, boolean isRetryable) {
        super(message);
        this.serviceType = serviceType;
        this.isRetryable = isRetryable;
    }
    
    public AIServiceException(String message, Throwable cause, String serviceType, boolean isRetryable) {
        super(message, cause);
        this.serviceType = serviceType;
        this.isRetryable = isRetryable;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public boolean isRetryable() {
        return isRetryable;
    }
}