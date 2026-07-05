package com.silkroad.market.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementImage;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementImageRepository;
import com.silkroad.market.service.ImageStorageService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/ads/images")
public class ImageController {

    private final AdvertisementImageRepository imageRepository;
    private final ImageStorageService imageStorageService;

    public ImageController(
            AdvertisementImageRepository imageRepository,
            ImageStorageService imageStorageService) {

        this.imageRepository = imageRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/{imageId}")
    // optional: might want to change the id system to be unique per ad
    // thus not running out of ids
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> getImage(
            @PathVariable Long imageId,
            Authentication authentication) throws IOException {

        AdvertisementImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ApiException(
                        "Image not found",
                        HttpStatus.NOT_FOUND));

        Advertisement advertisement = image.getAdvertisement();

        boolean allowed = false;

        if (advertisement.getStatus() == AdvertisementStatus.APPROVED) {
            allowed = true;
        }

        if (authentication != null) {

            String username = authentication.getName();

            if (advertisement.getSeller().getUsername().equals(username)) {
                allowed = true;
            }

            if (authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

                allowed = true;
            }
        }

        if (!allowed) {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN);
        }

        Resource resource = imageStorageService.loadImage(image.getFileName());

        Path path = Paths.get("uploads").resolve(image.getFileName());

        String contentType = Files.probeContentType(path);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        contentType != null ? contentType : "application/octet-stream")
                .body(resource);
    }
}