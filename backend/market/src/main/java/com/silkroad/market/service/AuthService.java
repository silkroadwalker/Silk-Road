package com.silkroad.market.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.authentication.AuthResponse;
import com.silkroad.market.dto.authentication.LoginRequest;
import com.silkroad.market.dto.authentication.SignupRequest;
import com.silkroad.market.entity.Role;
import com.silkroad.market.entity.User;
import com.silkroad.market.entity.UserStatus;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.UserRepository;
import com.silkroad.market.security.JwtService;

/**
 * Service class responsible for authentication and user management operations.
 * 
 * <p>
 * This service handles user registration (signup) and authentication (login)
 * by validating credentials, managing user accounts, and generating JWT tokens
 * for authenticated sessions.
 * </p>
 * 
 * <p>
 * New users are registered with the {@code USER} role and {@code ACTIVE}
 * status by default. The service ensures that usernames and phone numbers
 * are unique to prevent duplicates.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see User
 * @see UserRepository
 * @see JwtService
 * @see Role
 * @see UserStatus
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Constructs a new AuthService with all required dependencies.
     * 
     * @param userRepository  repository for user persistence operations
     * @param passwordEncoder password encoder for secure password hashing
     * @param jwtService      service for JWT token generation and validation
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user account.
     * 
     * <p>
     * This method validates that the username and phone number are not
     * already in use, creates a new user with the provided information,
     * hashes the password using BCrypt, and generates a JWT token for
     * immediate use.
     * </p>
     * 
     * @param request the signup request containing user registration details
     * @return an authentication response containing the JWT token and user
     *         information
     * @throws ApiException with CONFLICT status if the username or phone number is
     *                      already registered
     */
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

    /**
     * Authenticates a user and generates a JWT token.
     * 
     * <p>
     * This method validates the provided username and password against
     * stored credentials. It checks that the user account is active
     * (not blocked) before generating a JWT token for the session.
     * </p>
     * 
     * @param request the login request containing username and password
     * @return an authentication response containing the JWT token and user
     *         information
     * @throws ApiException with UNAUTHORIZED status if credentials are invalid
     * @throws ApiException with FORBIDDEN status if the account is blocked
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException("Your account has been blocked", HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse("Login successful", user.getUsername(), user.getRole(), token);
    }
}