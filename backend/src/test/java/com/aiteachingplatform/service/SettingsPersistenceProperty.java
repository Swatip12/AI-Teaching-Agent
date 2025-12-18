package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.UpdatePreferencesRequest;
import com.aiteachingplatform.dto.UserProfileResponse;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.UserPreferences;
import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.util.PropertyTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * **Feature: ai-teaching-platform, Property 14: Settings persistence**
 * **Validates: Requirements 7.3**
 * 
 * Property test for settings persistence:
 * For any login session, the system should restore all personalized settings and preferences
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SettingsPersistenceProperty extends PropertyTestBase {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void settingsPersistenceProperty() {
        assertProperty(new AbstractCharacteristic<UserPreferences>() {
            @Override
            protected void doSpecify(UserPreferences preferences) throws Throwable {
                // Create a test user with random preferences
                String username = "testuser_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000);
                String email = username + "@test.com";
                String password = "testpassword123";
                
                User user = new User(username, email, passwordEncoder.encode(password));
                user.setPreferences(preferences);
                User savedUser = userRepository.save(user);
                
                // Create update request with the same preferences
                UpdatePreferencesRequest updateRequest = new UpdatePreferencesRequest();
                updateRequest.setLearningPace(preferences.getLearningPace());
                updateRequest.setExplanationStyle(preferences.getExplanationStyle());
                updateRequest.setShowHints(preferences.getShowHints());
                updateRequest.setCelebrationEnabled(preferences.getCelebrationEnabled());
                updateRequest.setDarkMode(preferences.getDarkMode());
                updateRequest.setNotificationEnabled(preferences.getNotificationEnabled());
                
                // Update preferences
                UserProfileResponse updatedProfile = userService.updateUserPreferences(username, updateRequest);
                
                // Retrieve user profile (simulating login session restoration)
                UserProfileResponse retrievedProfile = userService.getUserProfile(username);
                
                // Property: All preferences should be restored exactly as they were saved
                assert retrievedProfile.getPreferences().getLearningPace().equals(preferences.getLearningPace()) : 
                    "Learning pace not persisted correctly";
                assert retrievedProfile.getPreferences().getExplanationStyle().equals(preferences.getExplanationStyle()) : 
                    "Explanation style not persisted correctly";
                assert retrievedProfile.getPreferences().getShowHints().equals(preferences.getShowHints()) : 
                    "Show hints preference not persisted correctly";
                assert retrievedProfile.getPreferences().getCelebrationEnabled().equals(preferences.getCelebrationEnabled()) : 
                    "Celebration enabled preference not persisted correctly";
                assert retrievedProfile.getPreferences().getDarkMode().equals(preferences.getDarkMode()) : 
                    "Dark mode preference not persisted correctly";
                assert retrievedProfile.getPreferences().getNotificationEnabled().equals(preferences.getNotificationEnabled()) : 
                    "Notification enabled preference not persisted correctly";
                
                // Clean up
                userRepository.delete(savedUser);
            }
        });
    }
    
    /**
     * Generator for UserPreferences with random values
     */
    private static final Generator<UserPreferences> USER_PREFERENCES_GENERATOR = new Generator<UserPreferences>() {
        private final Random random = new Random();
        
        @Override
        public UserPreferences next() {
            UserPreferences preferences = new UserPreferences();
            
            // Generate random enum values
            UserPreferences.LearningPace[] paces = UserPreferences.LearningPace.values();
            preferences.setLearningPace(paces[random.nextInt(paces.length)]);
            
            UserPreferences.ExplanationStyle[] styles = UserPreferences.ExplanationStyle.values();
            preferences.setExplanationStyle(styles[random.nextInt(styles.length)]);
            
            // Generate random boolean values
            preferences.setShowHints(random.nextBoolean());
            preferences.setCelebrationEnabled(random.nextBoolean());
            preferences.setDarkMode(random.nextBoolean());
            preferences.setNotificationEnabled(random.nextBoolean());
            
            return preferences;
        }
    };
    
    @Test
    void settingsPersistencePropertyWithGenerator() {
        assertProperty(new AbstractCharacteristic<UserPreferences>(USER_PREFERENCES_GENERATOR) {
            @Override
            protected void doSpecify(UserPreferences preferences) throws Throwable {
                // Create a test user with random preferences
                String username = "testuser_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000);
                String email = username + "@test.com";
                String password = "testpassword123";
                
                User user = new User(username, email, passwordEncoder.encode(password));
                user.setPreferences(preferences);
                User savedUser = userRepository.save(user);
                
                // Simulate login session by retrieving user profile
                UserProfileResponse retrievedProfile = userService.getUserProfile(username);
                
                // Property: All preferences should be restored exactly as they were saved
                assert retrievedProfile.getPreferences().getLearningPace().equals(preferences.getLearningPace()) : 
                    "Learning pace not persisted correctly";
                assert retrievedProfile.getPreferences().getExplanationStyle().equals(preferences.getExplanationStyle()) : 
                    "Explanation style not persisted correctly";
                assert retrievedProfile.getPreferences().getShowHints().equals(preferences.getShowHints()) : 
                    "Show hints preference not persisted correctly";
                assert retrievedProfile.getPreferences().getCelebrationEnabled().equals(preferences.getCelebrationEnabled()) : 
                    "Celebration enabled preference not persisted correctly";
                assert retrievedProfile.getPreferences().getDarkMode().equals(preferences.getDarkMode()) : 
                    "Dark mode preference not persisted correctly";
                assert retrievedProfile.getPreferences().getNotificationEnabled().equals(preferences.getNotificationEnabled()) : 
                    "Notification enabled preference not persisted correctly";
                
                // Clean up
                userRepository.delete(savedUser);
            }
        });
    }
}