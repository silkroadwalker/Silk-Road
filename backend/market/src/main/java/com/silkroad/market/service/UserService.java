package com.silkroad.market.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.user.UserSummaryResponse;
import com.silkroad.market.entity.UserStatus;
import com.silkroad.market.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getActiveUsers() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    public long getBlockedUsers() {
        return userRepository.countByStatus(UserStatus.BLOCKED);
    }
}