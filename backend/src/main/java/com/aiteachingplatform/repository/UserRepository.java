package com.aiteachingplatform.repository;

import com.aiteachingplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users who haven't logged in since a specific date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date OR u.lastLoginAt IS NULL")
    List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);
    
    /**
     * Find users created after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find users by learning pace preference
     */
    @Query("SELECT u FROM User u WHERE u.preferences.learningPace = :pace")
    List<User> findByLearningPace(@Param("pace") com.aiteachingplatform.model.UserPreferences.LearningPace pace);
    
    /**
     * Count total active users (logged in within last 30 days)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :thirtyDaysAgo")
    long countActiveUsers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}