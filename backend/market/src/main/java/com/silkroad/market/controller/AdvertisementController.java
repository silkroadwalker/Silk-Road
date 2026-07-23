package com.silkroad.market.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.AdvertisementDetailedResponse;
import com.silkroad.market.dto.advertisement.AdvertisementSearchRequest;
import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.dto.advertisement.CreateAdvertisementRequest;
import com.silkroad.market.dto.advertisement.UpdateAdvertisementRequest;
import com.silkroad.market.dto.rating.CreateRatingRequest;
import com.silkroad.market.dto.rating.RatingResponse;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.service.AdvertisementService;
import com.silkroad.market.service.RatingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Controller for advertisement-related REST endpoints.
 * <p>
 * Exposes public search operations and authenticated actions for creating,
 * updating, deleting, and rating advertisements.
 */
@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

        private final AdvertisementService advertisementService;
        private final RatingService ratingService;

        public AdvertisementController(
                        AdvertisementService advertisementService,
                        RatingService ratingService) {

                this.advertisementService = advertisementService;
                this.ratingService = ratingService;
        }

        /**
         * Creates a new advertisement with uploaded images.
         *
         * @param authentication authenticated user context
         * @param request        validated advertisement creation payload
         * @return success message on creation
         * @throws IOException if image persistence fails
         */
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<String> createAdvertisement(
                        Authentication authentication,
                        @Valid @ModelAttribute CreateAdvertisementRequest request)
                        throws IOException {

                advertisementService.createAdvertisement(
                                request,
                                authentication.getName());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body("Advertisement created successfully.");
        }

        @GetMapping
        public List<AdvertisementSummaryResponse> searchAdvertisements(
                        @ModelAttribute AdvertisementSearchRequest request) {

                return advertisementService.searchAdvertisements(request);
        }

        @GetMapping("/{id}")
        @SecurityRequirement(name = "bearerAuth")
        public AdvertisementDetailedResponse getAdvertisement(
                        @PathVariable Long id,
                        Authentication authentication) {

                return advertisementService.getAdvertisementDetails(
                                id,
                                AdvertisementStatus.APPROVED,
                                authentication);
        }

        @PatchMapping("/{id}")
        @SecurityRequirement(name = "bearerAuth")
        public void updateAdvertisement(
                        @PathVariable Long id,
                        @RequestBody UpdateAdvertisementRequest request,
                        Authentication authentication) {

                advertisementService.updateAdvertisement(
                                id,
                                request,
                                authentication.getName());
        }

        @PatchMapping("/{id}/markSold")
        @SecurityRequirement(name = "bearerAuth")
        public void updateAdvertisementStatusToSold(
                        @PathVariable Long id,
                        Authentication authentication) {

                advertisementService.updateAdvertisementStatusToSold(
                                id,
                                authentication.getName());
        }

        @DeleteMapping("/{id}")
        @SecurityRequirement(name = "bearerAuth")
        public void deleteAdvertisement(
                        @PathVariable Long id,
                        Authentication authentication) {

                advertisementService.deleteAdvertisement(
                                id,
                                authentication.getName());
        }

        @PostMapping("/{id}/rating")
        @SecurityRequirement(name = "bearerAuth")
        @ResponseStatus(HttpStatus.CREATED)
        public void rateAdvertisement(
                        @PathVariable Long id,
                        @Valid @RequestBody CreateRatingRequest request,
                        Authentication authentication) {

                advertisementService.rateAdvertisement(
                                id,
                                request,
                                authentication.getName());
        }

        @GetMapping("/my")
        @SecurityRequirement(name = "bearerAuth")
        public List<AdvertisementSummaryResponse> getMyAdvertisements(
                        Authentication authentication) {

                return advertisementService.getMyAdvertisements(
                                authentication.getName());
        }

        @GetMapping("/{advertisementId}/ratings")
        @SecurityRequirement(name = "bearerAuth")

        public List<RatingResponse> getAdvertisementRatings(
                        @PathVariable Long advertisementId) {

                return ratingService.getAdvertisementRatings(advertisementId);
        }

}