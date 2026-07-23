package com.silkroad.market.service;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.admin.AdminDashboardResponse;

@Service
public class AdminDashboardService {

    private final UserService userService;
    private final AdvertisementService advertisementService;

    public AdminDashboardService(
            UserService userService,
            AdvertisementService advertisementService) {

        this.userService = userService;
        this.advertisementService = advertisementService;
    }

    public AdminDashboardResponse getDashboard() {

        return new AdminDashboardResponse(

                userService.getTotalUsers(),
                userService.getActiveUsers(),
                userService.getBlockedUsers(),

                advertisementService.getTotalAdvertisements(),
                advertisementService.getPendingAdvertisements(),
                advertisementService.getApprovedAdvertisements(),
                advertisementService.getRejectedAdvertisements(),
                advertisementService.getSoldAdvertisements());
    }
}