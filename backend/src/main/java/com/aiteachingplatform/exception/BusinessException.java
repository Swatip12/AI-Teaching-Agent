package com.aiteachingplatform.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Custom exception for business logic errors
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> details;
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus, 
                           Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus, 
                           Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
}