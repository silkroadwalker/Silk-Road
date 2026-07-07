package com.silkroad.market.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.silkroad.market.dto.advertisement.AdvertisementDetailedResponse;
import com.silkroad.market.dto.advertisement.AdvertisementSearchRequest;
import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.dto.advertisement.CreateAdvertisementRequest;
import com.silkroad.market.dto.advertisement.RejectAdvertisementRequest;
import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementImage;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.entity.Category;
import com.silkroad.market.entity.City;
import com.silkroad.market.entity.User;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.CategoryRepository;
import com.silkroad.market.repository.CityRepository;
import com.silkroad.market.repository.UserRepository;
import com.silkroad.market.specification.AdvertisementSpecifications;

@Service
public class AdvertisementService {

        private final AdvertisementRepository advertisementRepository;
        private final CategoryRepository categoryRepository;
        private final CityRepository cityRepository;
        private final UserRepository userRepository;
        private final ImageStorageService imageStorageService;

        public AdvertisementService(
                        AdvertisementRepository advertisementRepository,
                        CategoryRepository categoryRepository,
                        CityRepository cityRepository,
                        UserRepository userRepository,
                        ImageStorageService imageStorageService) {

                this.advertisementRepository = advertisementRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
                this.imageStorageService = imageStorageService;
                this.cityRepository = cityRepository;
        }

        // todo: Right now, if saving the third image fails:

        // image1 ✔
        // image2 ✔
        // image3 ❌

        // the database transaction rolls back because of @Transactional, but the first
        // two image files remain on disk.
        @Transactional
        public Advertisement createAdvertisement(
                        CreateAdvertisementRequest request,
                        String username) throws IOException {

                List<MultipartFile> images = request.getImages();

                User seller = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));

                City city = cityRepository.findById(request.getCityId())
                                .orElseThrow(() -> new ApiException(
                                                "City not found",
                                                HttpStatus.NOT_FOUND));

                Advertisement advertisement = new Advertisement();

                advertisement.setTitle(request.getTitle());
                advertisement.setDescription(request.getDescription());
                advertisement.setPrice(request.getPrice());

                advertisement.setSeller(seller);
                advertisement.setCategory(category);
                advertisement.setCity(city);

                if (images != null) {

                        for (MultipartFile file : images) {

                                if (file.isEmpty()) {
                                        continue;
                                }

                                String fileName = imageStorageService.saveImage(file);

                                AdvertisementImage image = new AdvertisementImage();

                                image.setFileName(fileName);
                                image.setAdvertisement(advertisement);

                                advertisement.getImages().add(image);
                        }
                }

                return advertisementRepository.save(advertisement);
        }

        public List<AdvertisementSummaryResponse> searchAdvertisements(
                        AdvertisementSearchRequest request) {

                Specification<Advertisement> specification = AdvertisementSpecifications.approved();

                if (request.getKeyword() != null &&
                                !request.getKeyword().isBlank()) {

                        specification = specification.and(
                                        AdvertisementSpecifications.titleContains(
                                                        request.getKeyword()));
                }

                if (request.getCategoryId() != null) {

                        specification = specification.and(
                                        AdvertisementSpecifications.categoryIs(
                                                        request.getCategoryId()));
                }

                if (request.getCityId() != null) {

                        specification = specification.and(
                                        AdvertisementSpecifications.cityIs(
                                                        request.getCityId()));
                }

                if (request.getMinPrice() != null) {

                        specification = specification.and(
                                        AdvertisementSpecifications.minPrice(
                                                        request.getMinPrice()));
                }

                if (request.getMaxPrice() != null) {

                        specification = specification.and(
                                        AdvertisementSpecifications.maxPrice(
                                                        request.getMaxPrice()));
                }

                return advertisementRepository.findAll(specification)
                                .stream()
                                .map(this::toSummaryResponse)
                                .toList();
        }

        // public AdvertisementDetailedResponse getAdvertisementDetails(Long id) {

        // Advertisement ad = advertisementRepository.findById(id)
        // .orElseThrow(() -> new ApiException(
        // "Advertisement not found",
        // HttpStatus.NOT_FOUND));

        // List<String> imageUrls = ad.getImages()
        // .stream()
        // .map(image -> "/api/ads/images/" + image.getId())
        // .toList();

        // return new AdvertisementDetailedResponse(
        // ad.getId(),
        // ad.getTitle(),
        // ad.getDescription(),
        // ad.getPrice(),
        // ad.getSeller().getUsername(),
        // ad.getSeller().getFullName(),
        // ad.getSeller().getPhone(),
        // ad.getCategory().getName(),
        // ad.getCity().getName(),
        // ad.getStatus(),
        // ad.getRejectionReason(),
        // ad.getCreatedAt(),
        // imageUrls);
        // }

        public AdvertisementDetailedResponse getAdvertisementDetails(
                        Long id,
                        AdvertisementStatus requiredStatus,
                        Authentication authentication) {

                Advertisement ad = advertisementRepository.findById(id)
                                .orElseThrow(() -> new ApiException(
                                                "Advertisement not found",
                                                HttpStatus.NOT_FOUND));

                boolean isSubmitter = false;

                if (authentication != null) {

                        isSubmitter = ad.getSeller()
                                        .getUsername()
                                        .equals(authentication.getName());
                }

                if (ad.getStatus() != requiredStatus && requiredStatus != null) {
                        throw new ApiException(
                                        "Advertisement not found",
                                        HttpStatus.NOT_FOUND);
                }

                List<String> imageUrls = ad.getImages()
                                .stream()
                                .map(image -> "/api/ads/images/" + image.getId())
                                .toList();

                AdvertisementDetailedResponse adDetailedResponse = new AdvertisementDetailedResponse(
                                ad.getId(),
                                ad.getTitle(),
                                ad.getDescription(),
                                ad.getPrice(),
                                ad.getSeller().getUsername(),
                                ad.getSeller().getFullName(),
                                ad.getSeller().getPhone(),
                                ad.getCategory().getName(),
                                ad.getCity().getName(),
                                ad.getStatus(),
                                ad.getRejectionReason(),
                                ad.getCreatedAt(),
                                imageUrls);
                adDetailedResponse.setSubmitter(isSubmitter);
                return adDetailedResponse;
        }

        public List<AdvertisementSummaryResponse> getAdvertisementsByStatus(
                        AdvertisementStatus status) {

                return advertisementRepository.findByStatus(status)
                                .stream()
                                .map(this::toSummaryResponse)
                                .toList();
        }

        private AdvertisementSummaryResponse toSummaryResponse(Advertisement ad) {

                String thumbnailUrl = null;

                if (!ad.getImages().isEmpty()) {
                        thumbnailUrl = "/api/ads/images/" + ad.getImages().get(0).getId();
                }

                return new AdvertisementSummaryResponse(
                                ad.getId(),
                                ad.getTitle(),
                                ad.getPrice(),
                                ad.getSeller().getUsername(),
                                ad.getCategory().getName(),
                                ad.getCity().getName(),
                                ad.getStatus(),
                                thumbnailUrl);
        }

        public void approveAdvertisement(Long id) {

                Advertisement advertisement = advertisementRepository.findById(id)
                                .orElseThrow(() -> new ApiException(
                                                "Advertisement not found",
                                                HttpStatus.NOT_FOUND));

                if (advertisement.getStatus() != AdvertisementStatus.PENDING) {
                        throw new ApiException(
                                        "Advertisement has already been reviewed",
                                        HttpStatus.BAD_REQUEST);
                }

                advertisement.setStatus(AdvertisementStatus.APPROVED);
                advertisement.setRejectionReason(null);

                advertisementRepository.save(advertisement);
        }

        public void rejectAdvertisement(
                        Long id,
                        RejectAdvertisementRequest request) {

                Advertisement advertisement = advertisementRepository.findById(id)
                                .orElseThrow(() -> new ApiException(
                                                "Advertisement not found",
                                                HttpStatus.NOT_FOUND));

                if (advertisement.getStatus() != AdvertisementStatus.PENDING) {
                        throw new ApiException(
                                        "Advertisement has already been reviewed",
                                        HttpStatus.BAD_REQUEST);
                }

                advertisement.setStatus(AdvertisementStatus.REJECTED);
                advertisement.setRejectionReason(request.getReason());

                // todo: user should be notified of the rejection or approval of their ad
                // or at least see their ads' status

                advertisementRepository.save(advertisement);
        }
}