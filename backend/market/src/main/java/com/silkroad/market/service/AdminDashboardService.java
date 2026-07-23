package com.silkroad.market.service;

import org.springframework.stereotype.Service;

import com.silkroad.market.dto.admin.AdminDashboardResponse;

/**
 * Service class responsible for aggregating administrative dashboard
 * statistics.
 * 
 * <p>
 * This service combines data from multiple domain services to provide
 * a comprehensive overview of the marketplace's current state, including
 * user statistics and advertisement statistics.
 * </p>
 * 
 * <p>
 * The dashboard data is intended for use by administrators to monitor
 * platform activity and content moderation status.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see AdminDashboardResponse
 * @see UserService
 * @see AdvertisementService
 */
@Service
public class AdminDashboardService {

    private final UserService userService;
    private final AdvertisementService advertisementService;

    /**
     * Constructs a new AdminDashboardService with the required dependencies.
     * 
     * @param userService          the service for retrieving user-related
     *                             statistics
     * @param advertisementService the service for retrieving advertisement-related
     *                             statistics
     */
    public AdminDashboardService(
            UserService userService,
            AdvertisementService advertisementService) {

        this.userService = userService;
        this.advertisementService = advertisementService;
    }

    /**
     * Retrieves a complete dashboard response containing user and advertisement
     * statistics.
     * 
     * <p>
     * The dashboard includes the following metrics:
     * <ul>
     * <li>Total number of registered users</li>
     * <li>Number of active users</li>
     * <li>Number of blocked users</li>
     * <li>Total number of advertisements</li>
     * <li>Number of pending advertisements</li>
     * <li>Number of approved advertisements</li>
     * <li>Number of rejected advertisements</li>
     * <li>Number of sold advertisements</li>
     * </ul>
     * </p>
     * 
     * @return an AdminDashboardResponse containing all aggregated statistics
     */
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