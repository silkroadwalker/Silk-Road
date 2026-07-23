package com.silkroad.market.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.admin.AdminDashboardResponse;
import com.silkroad.market.service.AdminDashboardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService) {

        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public AdminDashboardResponse getDashboard() {

        return adminDashboardService.getDashboard();
    }
}