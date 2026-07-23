package com.silkroad.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.AdvertisementDetailedResponse;
import com.silkroad.market.dto.advertisement.AdvertisementSearchRequest;
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

    /**
     * search/browse advertisements of any status (or every status at once)
     * with the same filters as the public home search: keyword, category
     * (including subcategories), city, and min/max price. deleted ads are
     * hard-deleted from the database, so they never appear here.
     *
     * @param request the shared keyword/category/city/price filters
     * @param status  optional status to restrict to (PENDING, APPROVED,
     *                REJECTED, or SOLD); omit to get ads of every status
     */
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<AdvertisementSummaryResponse> searchAdvertisements(
            @ModelAttribute AdvertisementSearchRequest request,
            @RequestParam(required = false) AdvertisementStatus status) {

        return advertisementService.adminSearchAdvertisements(request, status);
    }

    @GetMapping("/pending")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdvertisementSummaryResponse> getPendingAdvertisements() {
        return advertisementService.getAdvertisementsByStatus(AdvertisementStatus.PENDING);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public AdvertisementDetailedResponse getAdvertisementDetails(
            @PathVariable Long id, Authentication authentication) {

        return advertisementService.getAdvertisementDetails(id, null, authentication);
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
