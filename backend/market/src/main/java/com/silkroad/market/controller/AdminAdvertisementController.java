package com.silkroad.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.AdvertisementDetailedResponse;
import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.dto.advertisement.RejectAdvertisementRequest;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.service.AdvertisementService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

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
        return advertisementService.getAdvertisementsByStatus(AdvertisementStatus.PENDING);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public AdvertisementDetailedResponse getAdvertisementDetails(
            @PathVariable Long id) {

        return advertisementService.getAdvertisementDetails(id);
    }

    @PatchMapping("/{id}/approve")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> approveAdvertisement(
            @PathVariable Long id) {

        advertisementService.approveAdvertisement(id);

        return ResponseEntity.ok("Advertisement approved.");
    }

    @PatchMapping("/{id}/reject")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> rejectAdvertisement(
            @PathVariable Long id,
            @Valid @RequestBody RejectAdvertisementRequest request) {

        advertisementService.rejectAdvertisement(id, request);

        return ResponseEntity.ok("Advertisement rejected.");
    }

}
