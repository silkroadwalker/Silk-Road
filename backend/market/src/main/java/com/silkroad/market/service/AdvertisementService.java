package com.silkroad.market.service;

import java.io.IOException;
import java.util.ArrayList;
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
import com.silkroad.market.dto.advertisement.UpdateAdvertisementRequest;
import com.silkroad.market.dto.rating.CreateRatingRequest;
import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementImage;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.entity.Category;
import com.silkroad.market.entity.City;
import com.silkroad.market.entity.Rating;
import com.silkroad.market.entity.User;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.CategoryRepository;
import com.silkroad.market.repository.CityRepository;
import com.silkroad.market.repository.RatingRepository;
import com.silkroad.market.repository.UserRepository;
import com.silkroad.market.specification.AdvertisementSpecifications;

@Service
public class AdvertisementService {

        private final AdvertisementRepository advertisementRepository;
        private final CategoryRepository categoryRepository;
        private final CityRepository cityRepository;
        private final UserRepository userRepository;
        private final ImageStorageService imageStorageService;
        private final RatingRepository ratingRepository;

        public AdvertisementService(
                AdvertisementRepository advertisementRepository,
                CategoryRepository categoryRepository,
                CityRepository cityRepository,
                UserRepository userRepository,
                ImageStorageService imageStorageService,
                RatingRepository ratingRepository) {

                this.advertisementRepository = advertisementRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
                this.imageStorageService = imageStorageService;
                this.cityRepository = cityRepository;
                this.ratingRepository = ratingRepository;
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

                Specification<Advertisement> specification = AdvertisementSpecifications.approved()
                        .and(buildFilterSpecification(request));

                return advertisementRepository.findAll(specification)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }

        /**
         * admin-only search across advertisements of any status (or a single
         * given status), using the same keyword/category/city/price filters
         * as the public search. deleted advertisements are hard-deleted from
         * the database, so they never show up here or anywhere else.
         *
         * @param request the shared keyword/category/city/price filters
         * @param status  restrict to a single status (PENDING/APPROVED/REJECTED/SOLD),
         *                or null to include ads of every status
         */
        public List<AdvertisementSummaryResponse> adminSearchAdvertisements(
                AdvertisementSearchRequest request,
                AdvertisementStatus status) {

                Specification<Advertisement> specification = buildFilterSpecification(request);

                if (status != null) {

                        specification = specification.and(
                                AdvertisementSpecifications.statusIs(status));
                }

                return advertisementRepository.findAll(specification)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }

        /**
         * builds the keyword/category/city/price portion of a search
         * specification, shared between the public and admin searches.
         * does not apply any status restriction.
         */
        private Specification<Advertisement> buildFilterSpecification(
                AdvertisementSearchRequest request) {

                Specification<Advertisement> specification = Specification.where((Specification<Advertisement>) null);                if (request.getKeyword() != null &&
                        !request.getKeyword().isBlank()) {

                        specification = specification.and(
                                AdvertisementSpecifications.titleOrDescriptionContains(
                                        request.getKeyword()));
                }

                if (request.getCategoryId() != null) {

                        specification = specification.and(
                                AdvertisementSpecifications.categoryIn(
                                        resolveCategoryIdsForFilter(request.getCategoryId())));
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

                return specification;
        }

        /**
         * expands a category filter into the ids to actually match against:
         * the category itself, plus its subcategory ids if it's a top-level
         * category with children. leaf/subcategory ids are returned unchanged.
         */
        private List<Long> resolveCategoryIdsForFilter(Long categoryId) {

                List<Long> ids = new ArrayList<>();
                ids.add(categoryId);

                categoryRepository.findByParentId(categoryId)
                        .forEach(subcategory -> ids.add(subcategory.getId()));

                return ids;
        }

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

                Double averageRating = ratingRepository.averageScoreBySeller(ad.getSeller());

                if (averageRating == null) {
                        averageRating = 0.0;
                }

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
                adDetailedResponse.setAverageRating(averageRating);

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

        private Advertisement getOwnedAdvertisement(
                Long id,
                String username) {

                Advertisement advertisement = advertisementRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND));

                if (!advertisement.getSeller()
                        .getUsername()
                        .equals(username)) {

                        throw new ApiException(
                                "Access denied",
                                HttpStatus.FORBIDDEN);
                }

                return advertisement;
        }

        @Transactional
        public void updateAdvertisement(
                // todo: enable image edit
                Long id,
                UpdateAdvertisementRequest request,
                String username) {

                Advertisement advertisement = getOwnedAdvertisement(id, username);

                if (request.getTitle() != null)
                        advertisement.setTitle(request.getTitle());

                if (request.getDescription() != null)
                        advertisement.setDescription(request.getDescription());

                if (request.getPrice() != null)
                        advertisement.setPrice(request.getPrice());

                if (request.getCategoryId() != null) {

                        Category category = categoryRepository.findById(
                                        request.getCategoryId())
                                .orElseThrow(() -> new ApiException(
                                        "Category not found",
                                        HttpStatus.NOT_FOUND));

                        advertisement.setCategory(category);
                }

                if (request.getCityId() != null) {

                        City city = cityRepository.findById(
                                        request.getCityId())
                                .orElseThrow(() -> new ApiException(
                                        "City not found",
                                        HttpStatus.NOT_FOUND));

                        advertisement.setCity(city);
                }

                advertisementRepository.save(advertisement);
        }

        @Transactional
        public void updateAdvertisementStatusToSold(
                Long id,
                String username) {

                Advertisement advertisement = getOwnedAdvertisement(id, username);

                advertisement.setStatus(AdvertisementStatus.SOLD);

                advertisementRepository.save(advertisement);
        }

        /**
         * deletes an advertisement. the ad's own seller can delete it
         * regardless of status; an admin can delete any advertisement
         * (including ones they don't own and ones already approved),
         * which is what powers the "Delete" button in the admin panel.
         *
         * @param id             the advertisement id
         * @param authentication the requester; used both for the username
         *                       (ownership check) and their granted roles
         *                       (admin override)
         */
        @Transactional
        public void deleteAdvertisement(
                Long id,
                Authentication authentication) {

                Advertisement advertisement = advertisementRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND));

                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                boolean isOwner = advertisement.getSeller()
                        .getUsername()
                        .equals(authentication.getName());

                if (!isAdmin && !isOwner) {
                        throw new ApiException(
                                "Access denied",
                                HttpStatus.FORBIDDEN);
                }

                advertisementRepository.delete(advertisement);
        }

        @Transactional
        public void rateAdvertisement(
                Long advertisementId,
                CreateRatingRequest request,
                String username) {

                Advertisement advertisement = advertisementRepository.findById(advertisementId)
                        .orElseThrow(() -> new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND));

                User buyer = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND));

                User seller = advertisement.getSeller();

                if (seller.getId().equals(buyer.getId())) {
                        throw new ApiException(
                                "You cannot rate yourself",
                                HttpStatus.BAD_REQUEST);
                }

                // optional: u might want Only sold advertisements can be rated
                // if (advertisement.getStatus() != AdvertisementStatus.SOLD) {
                // throw new ApiException(
                // "Only sold advertisements can be rated",
                // HttpStatus.BAD_REQUEST);
                // }

                if (ratingRepository.existsByAdvertisementAndBuyer(advertisement, buyer)) {
                        throw new ApiException(
                                "You have already rated this advertisement",
                                HttpStatus.BAD_REQUEST);
                }

                Rating rating = new Rating();

                rating.setAdvertisement(advertisement);
                rating.setBuyer(buyer);
                rating.setSeller(seller);
                rating.setScore(request.getScore());
                rating.setComment(request.getComment());

                ratingRepository.save(rating);
        }

        @Transactional
        public void addFavorite(Long advertisementId, String username) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND));

                Advertisement advertisement = advertisementRepository.findById(advertisementId)
                        .orElseThrow(() -> new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND));

                if (advertisement.getStatus() != AdvertisementStatus.APPROVED) {
                        throw new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND);
                }

                if (user.getFavoriteAdvertisements().contains(advertisement)) {
                        throw new ApiException(
                                "Advertisement is already in favorites",
                                HttpStatus.BAD_REQUEST);
                }

                user.getFavoriteAdvertisements().add(advertisement);

                userRepository.save(user);
        }

        @Transactional
        public void removeFavorite(Long advertisementId, String username) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND));

                Advertisement advertisement = advertisementRepository.findById(advertisementId)
                        .orElseThrow(() -> new ApiException(
                                "Advertisement not found",
                                HttpStatus.NOT_FOUND));

                user.getFavoriteAdvertisements().remove(advertisement);

                userRepository.save(user);
        }

        public List<AdvertisementSummaryResponse> getFavorites(String username) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND));

                return user.getFavoriteAdvertisements()
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }

        public List<AdvertisementSummaryResponse> getMyAdvertisements(String username) {

                return advertisementRepository.findBySellerUsername(username)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }
}