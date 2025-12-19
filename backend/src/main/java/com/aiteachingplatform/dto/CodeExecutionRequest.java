package com.aiteachingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for code execution
 * Contains code to execute and execution parameters
 */
public class CodeExecutionRequest {
    
    @NotBlank(message = "Code cannot be empty")
    @Size(max = 10000, message = "Code cannot exceed 10000 characters")
    private String code;
    
    @NotNull(message = "Language is required")
    private Language language;
    
    private String input; // Optional input for the program
    
    @Size(max = 1000, message = "Input cannot exceed 1000 characters")
    private String stdin;
    
    private Integer timeoutSeconds = 10; // Default 10 seconds timeout
    
    public enum Language {
        JAVA("java"),
        PYTHON("python"),
        JAVASCRIPT("javascript"),
        CPP("cpp");
        
        private final String value;
        
        Language(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // Constructors
    public CodeExecutionRequest() {}
    
    public CodeExecutionRequest(String code, Language language) {
        this.code = code;
        this.language = language;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Language getLanguage() {
        return language;
    }
    
    public void setLanguage(Language language) {
        this.language = language;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getStdin() {
        return stdin;
    }
    
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
    
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}