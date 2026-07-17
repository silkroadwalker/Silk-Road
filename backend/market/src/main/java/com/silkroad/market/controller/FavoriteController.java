package com.silkroad.market.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.service.AdvertisementService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/favorites")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final AdvertisementService advertisementService;

    public FavoriteController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping("/{advertisementId}")
    public void addFavorite(
            @PathVariable Long advertisementId,
            Authentication authentication) {

        advertisementService.addFavorite(
                advertisementId,
                authentication.getName());
    }

    @DeleteMapping("/{advertisementId}")
    public void removeFavorite(
            @PathVariable Long advertisementId,
            Authentication authentication) {

        advertisementService.removeFavorite(
                advertisementId,
                authentication.getName());
    }

    @GetMapping
    public List<AdvertisementSummaryResponse> getFavorites(
            Authentication authentication) {

        return advertisementService.getFavorites(
                authentication.getName());
    }
}