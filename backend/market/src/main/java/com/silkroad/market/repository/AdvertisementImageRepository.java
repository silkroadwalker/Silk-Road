package com.silkroad.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.AdvertisementImage;

public interface AdvertisementImageRepository
        extends JpaRepository<AdvertisementImage, Long> {

}