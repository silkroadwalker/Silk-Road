package com.silkroad.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.Rating;
import com.silkroad.market.entity.User;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByAdvertisementAndBuyer(
            Advertisement advertisement,
            User buyer);

    @Query("""
            SELECT AVG(r.score)
            FROM Rating r
            WHERE r.seller = :seller
            """)
    Double averageScoreBySeller(User seller);
}