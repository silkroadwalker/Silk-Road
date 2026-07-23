package com.silkroad.market.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.entity.Category;
import com.silkroad.market.entity.User;

public interface AdvertisementRepository
                extends JpaRepository<Advertisement, Long>,
                JpaSpecificationExecutor<Advertisement> {

        List<Advertisement> findBySeller(User seller);

        List<Advertisement> findBySellerUsername(String username);

        List<Advertisement> findByCategory(Category category);

        List<Advertisement> findByStatus(AdvertisementStatus status);

        List<Advertisement> findByCategoryAndStatus(Category category,
                        AdvertisementStatus status);

        long countByStatus(AdvertisementStatus status);
}