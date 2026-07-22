package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.rating.RatingResponse;
import com.silkroad.market.entity.Rating;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.RatingRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final AdvertisementRepository advertisementRepository;

    public RatingService(
            RatingRepository ratingRepository,
            AdvertisementRepository advertisementRepository) {

        this.ratingRepository = ratingRepository;
        this.advertisementRepository = advertisementRepository;
    }

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

    private RatingResponse toResponse(Rating rating) {

        return new RatingResponse(
                rating.getId(),
                rating.getBuyer().getUsername(),
                rating.getScore(),
                rating.getComment());
    }
}