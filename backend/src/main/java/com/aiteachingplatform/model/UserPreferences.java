package com.aiteachingplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Embeddable class for user preferences and personalization settings
 */
@Embeddable
public class UserPreferences {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "learning_pace")
    private LearningPace learningPace = LearningPace.NORMAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "explanation_style")
    private ExplanationStyle explanationStyle = ExplanationStyle.BALANCED;
    
    @Column(name = "show_hints")
    private Boolean showHints = true;
    
    @Column(name = "celebration_enabled")
    private Boolean celebrationEnabled = true;
    
    @Column(name = "dark_mode")
    private Boolean darkMode = false;
    
    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;
    
    // Constructors
    public UserPreferences() {}
    
    // Getters and Setters
    public LearningPace getLearningPace() {
        return learningPace;
    }
    
    public void setLearningPace(LearningPace learningPace) {
        this.learningPace = learningPace;
    }
    
    public ExplanationStyle getExplanationStyle() {
        return explanationStyle;
    }
    
    public void setExplanationStyle(ExplanationStyle explanationStyle) {
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
    
    public enum LearningPace {
        SLOW, NORMAL, FAST
    }
    
    public enum ExplanationStyle {
        SIMPLE, BALANCED, DETAILED
    }
}