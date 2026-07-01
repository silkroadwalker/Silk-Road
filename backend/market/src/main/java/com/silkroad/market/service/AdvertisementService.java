package com.silkroad.market.service;

import org.springframework.stereotype.Service;

import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.CategoryRepository;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;

    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            CategoryRepository categoryRepository) {

        this.advertisementRepository = advertisementRepository;
        this.categoryRepository = categoryRepository;
    }
}