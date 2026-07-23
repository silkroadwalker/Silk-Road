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

/**
 * Service class responsible for managing advertisement-related business logic.
 * 
 * <p>
 * This service handles all operations related to advertisements, including
 * creation, retrieval, updating, deletion, status management, and rating.
 * It also manages user favorites and provides search functionality with
 * filtering capabilities.
 * </p>
 * 
 * <p>
 * All transactional methods are annotated with {@code @Transactional} to
 * ensure data consistency. Note that image file operations are not covered
 * by database transactions, which may result in orphaned files if an error
 * occurs during image processing.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see Advertisement
 * @see AdvertisementRepository
 * @see AdvertisementStatus
 */
@Service
public class AdvertisementService {

        private final AdvertisementRepository advertisementRepository;
        private final CategoryRepository categoryRepository;
        private final CityRepository cityRepository;
        private final UserRepository userRepository;
        private final ImageStorageService imageStorageService;
        private final RatingRepository ratingRepository;

        /**
         * Constructs a new AdvertisementService with all required dependencies.
         * 
         * @param advertisementRepository repository for advertisement persistence
         * @param categoryRepository      repository for category lookups
         * @param cityRepository          repository for city lookups
         * @param userRepository          repository for user lookups
         * @param imageStorageService     service for handling image storage operations
         * @param ratingRepository        repository for rating operations
         */
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

        /**
         * Searches for approved advertisements based on various filter criteria.
         * 
         * <p>
         * This method uses JPA Specifications to dynamically build queries
         * based on the provided search parameters. Only advertisements with
         * an {@code APPROVED} status are returned.
         * </p>
         * 
         * <p>
         * Search criteria include:
         * <ul>
         * <li>Keyword search (matches title or description)</li>
         * <li>Category filtering (including subcategories)</li>
         * <li>City filtering</li>
         * <li>Minimum and maximum price range</li>
         * </ul>
         * </p>
         * 
         * @param request the search request containing filter parameters
         * @return a list of summary responses for matching advertisements
         */
        public List<AdvertisementSummaryResponse> searchAdvertisements(
                AdvertisementSearchRequest request) {

                Specification<Advertisement> specification = AdvertisementSpecifications.approved();

                if (request.getKeyword() != null &&
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

                return advertisementRepository.findAll(specification)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
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

        /**
         * Retrieves all advertisements with a specified status.
         * 
         * <p>
         * This method is primarily used by administrators to view pending
         * advertisements for moderation.
         * </p>
         * 
         * @param status the advertisement status to filter by
         * @return a list of summary responses for matching advertisements
         */
        public List<AdvertisementSummaryResponse> getAdvertisementsByStatus(
                AdvertisementStatus status) {

                return advertisementRepository.findByStatus(status)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }

        /**
         * Converts an Advertisement entity to a summary response DTO.
         * 
         * <p>
         * The summary includes the first image as a thumbnail for display
         * in listing views.
         * </p>
         * 
         * @param ad the advertisement entity to convert
         * @return a summary response containing key advertisement information
         */
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

        /**
         * Approves a pending advertisement.
         * 
         * <p>
         * This method transitions the advertisement from {@code PENDING}
         * to {@code APPROVED} status. The advertisement must currently be
         * pending to be approved.
         * </p>
         * 
         * @param id the advertisement ID to approve
         * @throws ApiException if the advertisement is not found or is not pending
         */
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

        /**
         * Rejects a pending advertisement with a reason.
         * 
         * <p>
         * This method transitions the advertisement from {@code PENDING}
         * to {@code REJECTED} status and stores the rejection reason for
         * the seller's reference.
         * </p>
         * 
         * <p>
         * <b>Todo:</b> Notify the user of the rejection or approval of
         * their advertisement.
         * </p>
         * 
         * @param id      the advertisement ID to reject
         * @param request the rejection request containing the reason
         * @throws ApiException if the advertisement is not found or is not pending
         */
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

        /**
         * Retrieves an advertisement owned by a specific user.
         * 
         * <p>
         * This helper method verifies that the specified user owns the
         * advertisement and throws a forbidden exception if they do not.
         * </p>
         * 
         * @param id       the advertisement ID
         * @param username the username of the requesting user
         * @return the advertisement if owned by the user
         * @throws ApiException if the advertisement is not found or the user does not
         *                      own it
         */
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

        /**
         * Updates an existing advertisement.
         * 
         * <p>
         * This method allows the owner of an advertisement to update its
         * fields. Only fields provided in the request are updated. The seller
         * must own the advertisement to update it.
         * </p>
         * 
         * <p>
         * <b>Todo:</b> Enable image editing functionality.
         * </p>
         * 
         * @param id       the advertisement ID to update
         * @param request  the update request containing updated field values
         * @param username the username of the requesting user
         * @throws ApiException if the advertisement is not found or the user does not
         *                      own it
         */
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

        /**
         * Marks an advertisement as sold.
         * 
         * <p>
         * This method transitions the advertisement to {@code SOLD} status.
         * The seller must own the advertisement to perform this operation.
         * </p>
         * 
         * @param id       the advertisement ID to mark as sold
         * @param username the username of the requesting user
         * @throws ApiException if the advertisement is not found or the user does not
         *                      own it
         */
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

        /**
         * Rates a seller for a specific advertisement.
         * 
         * <p>
         * This method allows a buyer to rate a seller after a transaction.
         * Users cannot rate themselves, and each user can only rate an
         * advertisement once.
         * </p>
         * 
         * <p>
         * <b>Note:</b> The implementation currently allows rating of any
         * advertisement, but the commented code suggests that rating should
         * ideally be restricted to sold advertisements.
         * </p>
         * 
         * @param advertisementId the ID of the advertisement being rated
         * @param request         the rating request containing score and optional
         *                        comment
         * @param username        the username of the buyer submitting the rating
         * @throws ApiException if the advertisement is not found, the user is not
         *                      found,
         *                      the buyer tries to rate themselves, or the buyer has
         *                      already rated this advertisement
         */
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

        /**
         * Adds an advertisement to a user's favorites.
         * 
         * <p>
         * Only approved advertisements can be added to favorites. The user
         * cannot favorite the same advertisement twice.
         * </p>
         * 
         * @param advertisementId the advertisement ID to add to favorites
         * @param username        the username of the user adding to favorites
         * @throws ApiException if the user is not found, the advertisement is not
         *                      found,
         *                      the advertisement is not approved, or the advertisement
         *                      is already favorited
         */
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

        /**
         * Removes an advertisement from a user's favorites.
         * 
         * @param advertisementId the advertisement ID to remove from favorites
         * @param username        the username of the user removing from favorites
         * @throws ApiException if the user is not found or the advertisement is not
         *                      found
         */
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

        /**
         * Retrieves all advertisements in a user's favorites.
         * 
         * @param username the username of the user whose favorites are being retrieved
         * @return a list of summary responses for favorited advertisements
         * @throws ApiException if the user is not found
         */
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

        /**
         * Retrieves all advertisements posted by a specific user.
         * 
         * @param username the username of the seller
         * @return a list of summary responses for the user's advertisements
         */
        public List<AdvertisementSummaryResponse> getMyAdvertisements(String username) {

                return advertisementRepository.findBySellerUsername(username)
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();
        }

        /**
         * Retrieves the total number of advertisements.
         * 
         * @return the total count of advertisements in the system
         */
        public long getTotalAdvertisements() {
                return advertisementRepository.count();
        }

        /**
         * Retrieves the number of advertisements with PENDING status.
         * 
         * @return the count of pending advertisements
         */
        public long getPendingAdvertisements() {
                return advertisementRepository.countByStatus(
                                AdvertisementStatus.PENDING);
        }

        /**
         * Retrieves the number of advertisements with APPROVED status.
         * 
         * @return the count of approved advertisements
         */
        public long getApprovedAdvertisements() {
                return advertisementRepository.countByStatus(
                                AdvertisementStatus.APPROVED);
        }

        /**
         * Retrieves the number of advertisements with REJECTED status.
         * 
         * @return the count of rejected advertisements
         */
        public long getRejectedAdvertisements() {
                return advertisementRepository.countByStatus(
                                AdvertisementStatus.REJECTED);
        }

        /**
         * Retrieves the number of advertisements with SOLD status.
         * 
         * @return the count of sold advertisements
         */
        public long getSoldAdvertisements() {
                return advertisementRepository.countByStatus(
                                AdvertisementStatus.SOLD);
        }
}