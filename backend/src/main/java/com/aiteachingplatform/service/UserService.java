package com.aiteachingplatform.service;

import com.aiteachingplatform.dto.*;
import com.aiteachingplatform.model.User;
import com.aiteachingplatform.model.UserPreferences;
import com.aiteachingplatform.repository.UserRepository;
import com.aiteachingplatform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service class for user management operations
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Register a new user
     */
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPreferences(new UserPreferences()); // Initialize with default preferences
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtil.generateToken(savedUser.getUsername());
        
        return new AuthResponse(jwt, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
    
    /**
     * Authenticate user and return JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));
            
            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            String jwt = jwtUtil.generateToken(user.getUsername());
            
            return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
            
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    /**
     * Get user profile by username
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getLastLoginAt(),
            user.getPreferences()
        );
    }
    
    /**
     * Update user preferences
     */
    public UserProfileResponse updateUserPreferences(String username, UpdatePreferencesRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            user.setPreferences(preferences);
        }
        
        // Update preferences if provided
        if (request.getLearningPace() != null) {
            preferences.setLearningPace(request.getLearningPace());
        }
        if (request.getExplanationStyle() != null) {
            preferences.setExplanationStyle(request.getExplanationStyle());
        }
        if (request.getShowHints() != null) {
            preferences.setShowHints(request.getShowHints());
        }
        if (request.getCelebrationEnabled() != null) {
            preferences.setCelebrationEnabled(request.getCelebrationEnabled());
        }
        if (request.getDarkMode() != null) {
            preferences.setDarkMode(request.getDarkMode());
        }
        if (request.getNotificationEnabled() != null) {
            preferences.setNotificationEnabled(request.getNotificationEnabled());
        }
        
        User savedUser = userRepository.save(user);
        
        return new UserProfileResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getCreatedAt(),
            savedUser.getLastLoginAt(),
            savedUser.getPreferences()
        );
    }
    
    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Get current user from authentication
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}