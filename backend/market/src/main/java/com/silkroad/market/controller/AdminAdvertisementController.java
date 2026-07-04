package com.silkroad.market.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.service.AdvertisementService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/ads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;
    // add verifyAdvertisement to service

    public AdminAdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("/pending")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdvertisementSummaryResponse> getPendingAdvertisements() {
        return advertisementService.getPendingAdvertisements();
    }

}
