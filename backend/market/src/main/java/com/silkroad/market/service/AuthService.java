package com.silkroad.market.service;

import com.silkroad.market.dto.AuthResponse;
import com.silkroad.market.dto.SignupRequest;
import com.silkroad.market.entity.Role;
import com.silkroad.market.entity.User;
import com.silkroad.market.entity.UserStatus;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException("Phone number already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse("Signup successful", user.getUsername(), user.getRole(), token);
    }
}