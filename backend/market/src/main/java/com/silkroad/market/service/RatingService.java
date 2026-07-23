package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.rating.RatingResponse;
import com.silkroad.market.entity.Rating;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.RatingRepository;

/**
 * Service class responsible for retrieving rating information.
 * 
 * <p>
 * This service provides read-only access to ratings associated with
 * advertisements. Ratings are created through the AdvertisementService,
 * and this service handles the retrieval and formatting of rating data.
 * </p>
 * 
 * <p>
 * Ratings include the buyer's username, a score (typically 1-5), and
 * an optional comment. They are ordered by ID in descending order to
 * show the most recent ratings first.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see Rating
 * @see RatingRepository
 * @see AdvertisementService#rateAdvertisement
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * Constructs a new RatingService with all required dependencies.
     * 
     * @param ratingRepository        repository for rating data retrieval
     * @param advertisementRepository repository for advertisement existence
     *                                validation
     */
    public RatingService(
            RatingRepository ratingRepository,
            AdvertisementRepository advertisementRepository) {

        this.ratingRepository = ratingRepository;
        this.advertisementRepository = advertisementRepository;
    }

    /**
     * Retrieves all ratings for a specific advertisement.
     * 
     * <p>
     * This method validates that the advertisement exists and then returns
     * all associated ratings. Ratings are ordered by ID in descending order,
     * which typically corresponds to newest ratings first.
     * </p>
     * 
     * @param advertisementId the ID of the advertisement
     * @return a list of rating responses containing buyer, score, and comment
     * @throws ApiException with NOT_FOUND status if the advertisement does not
     *                      exist
     */
    public List<RatingResponse> getAdvertisementRatings(
            Long advertisementId) {

        advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ApiException(
                        "Advertisement not found",
                        HttpStatus.NOT_FOUND));

        return ratingRepository
                .findByAdvertisementIdOrderByIdDesc(advertisementId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Converts a Rating entity to a response DTO.
     * 
     * @param rating the rating entity to convert
     * @return a response DTO containing rating information
     */
    private RatingResponse toResponse(Rating rating) {

        return new RatingResponse(
                rating.getId(),
                rating.getBuyer().getUsername(),
                rating.getScore(),
                rating.getComment());
    }
}