package com.silkroad.market.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.user.UserSummaryResponse;
import com.silkroad.market.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // @GetMapping
    // @SecurityRequirement(name = "bearerAuth")
    // public List<UserSummaryResponse> getAllUsers() {

    // return userService.getAllUsers();
    // }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<UserSummaryResponse> getAllUsers(Authentication authentication) {

        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());

        return userService.getAllUsers();
    }
}