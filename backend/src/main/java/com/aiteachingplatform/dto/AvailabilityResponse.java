package com.aiteachingplatform.dto;

/**
 * DTO for availability check responses
 */
public class AvailabilityResponse {
    
    private String field;
    private boolean available;
    
    public AvailabilityResponse() {}
    
    public AvailabilityResponse(String field, boolean available) {
        this.field = field;
        this.available = available;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
}