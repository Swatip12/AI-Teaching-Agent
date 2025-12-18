package com.aiteachingplatform.dto;

import com.aiteachingplatform.model.UserPreferences;

/**
 * DTO for updating user preferences
 */
public class UpdatePreferencesRequest {
    
    private UserPreferences.LearningPace learningPace;
    private UserPreferences.ExplanationStyle explanationStyle;
    private Boolean showHints;
    private Boolean celebrationEnabled;
    private Boolean darkMode;
    private Boolean notificationEnabled;
    
    // Constructors
    public UpdatePreferencesRequest() {}
    
    // Getters and Setters
    public UserPreferences.LearningPace getLearningPace() {
        return learningPace;
    }
    
    public void setLearningPace(UserPreferences.LearningPace learningPace) {
        this.learningPace = learningPace;
    }
    
    public UserPreferences.ExplanationStyle getExplanationStyle() {
        return explanationStyle;
    }
    
    public void setExplanationStyle(UserPreferences.ExplanationStyle explanationStyle) {
        this.explanationStyle = explanationStyle;
    }
    
    public Boolean getShowHints() {
        return showHints;
    }
    
    public void setShowHints(Boolean showHints) {
        this.showHints = showHints;
    }
    
    public Boolean getCelebrationEnabled() {
        return celebrationEnabled;
    }
    
    public void setCelebrationEnabled(Boolean celebrationEnabled) {
        this.celebrationEnabled = celebrationEnabled;
    }
    
    public Boolean getDarkMode() {
        return darkMode;
    }
    
    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }
    
    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }
    
    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}