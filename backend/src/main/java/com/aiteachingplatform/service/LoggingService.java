package com.aiteachingplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized logging service for error tracking and monitoring
 * Requirement 6.3: Error handling for code execution
 * Requirement 8.3: Network error handling
 */
@Service
public class LoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");
    
    /**
     * Log error with context information
     */
    public void logError(String operation, String errorMessage, Throwable throwable, 
                        Long userId, Map<String, Object> context) {
        
        String correlationId = generateCorrelationId();
        
        try {
            // Set MDC context for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("operation", operation);
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            if (context != null) {
                context.forEach((key, value) -> 
                    MDC.put(key, value != null ? value.toString() : "null"));
            }
            
            errorLogger.error("Operation: {} | Error: {} | User: {} | Context: {}", 
                operation, errorMessage, userId, context, throwable);
                
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log warning with context
     */
    public void logWarning(String operation, String message, Long userId, 
                          Map<String, Object> context) {
        
        String correlationId = generateCorrelationId();
        
        try {
            MDC.put("correlationId", correlationId);
            MDC.put("operation", operation);
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            
            if (context != null) {
                context.forEach((key, value) -> 
                    MDC.put(key, value != null ? value.toString() : "null"));
            }
            
            logger.warn("Operation: {} | Warning: {} | User: {} | Context: {}", 
                operation, message, userId, context);
                
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log audit events for security and compliance
     */
    public void logAuditEvent(String eventType, String description, Long userId, 
                             String ipAddress, Map<String, Object> details) {
        
        String correlationId = generateCorrelationId();
        
        try {
            MDC.put("correlationId", correlationId);
            MDC.put("eventType", eventType);
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            MDC.put("ipAddress", ipAddress != null ? ipAddress : "unknown");
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            if (details != null) {
                details.forEach((key, value) -> 
                    MDC.put(key, value != null ? value.toString() : "null"));
            }
            
            auditLogger.info("Event: {} | Description: {} | User: {} | IP: {} | Details: {}", 
                eventType, description, userId, ipAddress, details);
                
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log performance metrics
     */
    public void logPerformanceMetric(String operation, long executionTimeMs, 
                                   Long userId, Map<String, Object> metrics) {
        
        try {
            MDC.put("operation", operation);
            MDC.put("executionTime", String.valueOf(executionTimeMs));
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            
            if (metrics != null) {
                metrics.forEach((key, value) -> 
                    MDC.put(key, value != null ? value.toString() : "null"));
            }
            
            if (executionTimeMs > 5000) { // Log slow operations
                logger.warn("SLOW_OPERATION: {} took {}ms | User: {} | Metrics: {}", 
                    operation, executionTimeMs, userId, metrics);
            } else {
                logger.info("PERFORMANCE: {} completed in {}ms | User: {} | Metrics: {}", 
                    operation, executionTimeMs, userId, metrics);
            }
                
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log AI service interactions
     */
    public void logAIServiceInteraction(String operation, String serviceType, 
                                      boolean success, long responseTimeMs, 
                                      Long userId, String errorMessage) {
        
        try {
            MDC.put("operation", operation);
            MDC.put("serviceType", serviceType);
            MDC.put("success", String.valueOf(success));
            MDC.put("responseTime", String.valueOf(responseTimeMs));
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            
            if (success) {
                logger.info("AI_SERVICE_SUCCESS: {} | Service: {} | Time: {}ms | User: {}", 
                    operation, serviceType, responseTimeMs, userId);
            } else {
                logger.error("AI_SERVICE_FAILURE: {} | Service: {} | Error: {} | User: {}", 
                    operation, serviceType, errorMessage, userId);
            }
                
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Log code execution events
     */
    public void logCodeExecution(String language, boolean success, long executionTimeMs, 
                                String status, Long userId, String errorMessage) {
        
        try {
            MDC.put("operation", "CODE_EXECUTION");
            MDC.put("language", language);
            MDC.put("success", String.valueOf(success));
            MDC.put("executionTime", String.valueOf(executionTimeMs));
            MDC.put("status", status);
            MDC.put("userId", userId != null ? userId.toString() : "anonymous");
            
            if (success) {
                logger.info("CODE_EXECUTION_SUCCESS: {} | Status: {} | Time: {}ms | User: {}", 
                    language, status, executionTimeMs, userId);
            } else {
                logger.warn("CODE_EXECUTION_FAILURE: {} | Status: {} | Error: {} | User: {}", 
                    language, status, errorMessage, userId);
            }
                
        } finally {
            MDC.clear();
        }
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}