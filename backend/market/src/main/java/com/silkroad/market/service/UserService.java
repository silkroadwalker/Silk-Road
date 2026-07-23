package com.silkroad.market.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.user.UserSummaryResponse;
import com.silkroad.market.entity.UserStatus;
import com.silkroad.market.repository.UserRepository;

/**
 * Service class responsible for user management and statistics.
 * 
 * <p>
 * This service provides administrative functionality for retrieving user
 * information and generating user-related statistics. It includes methods
 * for listing all users and counting users by status.
 * </p>
 * 
 * <p>
 * All methods in this service are read-only and do not perform any
 * write operations on user data.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see User
 * @see UserRepository
 * @see UserStatus
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a new UserService with the required dependency.
     * 
     * @param userRepository repository for user data retrieval and statistics
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all registered users.
     * 
     * <p>
     * This method returns a summary view of all users in the system,
     * typically used by administrators for user management.
     * </p>
     * 
     * @return a list of user summary responses containing ID, username, full name,
     *         and status
     */
    public List<UserSummaryResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getStatus()))
                .toList();
    }

    /**
     * Retrieves the total number of registered users.
     * 
     * @return the total count of users in the system
     */
    public long getTotalUsers() {
        return userRepository.count();
    }

    /**
     * Retrieves the number of users with ACTIVE status.
     * 
     * @return the count of active users
     */
    public long getActiveUsers() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    /**
     * Retrieves the number of users with BLOCKED status.
     * 
     * @return the count of blocked users
     */
    public long getBlockedUsers() {
        return userRepository.countByStatus(UserStatus.BLOCKED);
    }
}