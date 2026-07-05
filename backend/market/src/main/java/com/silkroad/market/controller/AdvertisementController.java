package com.silkroad.market.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.advertisement.CreateAdvertisementRequest;
import com.silkroad.market.service.AdvertisementService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

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
}