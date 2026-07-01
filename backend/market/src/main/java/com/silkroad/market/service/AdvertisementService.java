package com.silkroad.market.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.advertisement.CreateAdvertisementRequest;
import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.Category;
import com.silkroad.market.entity.User;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.CategoryRepository;
import com.silkroad.market.repository.UserRepository;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.advertisementRepository = advertisementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public void createAdvertisement(CreateAdvertisementRequest request,
            String username) {

        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(
                        "Category not found",
                        HttpStatus.NOT_FOUND));

        Advertisement advertisement = new Advertisement();

        advertisement.setTitle(request.getTitle());
        advertisement.setDescription(request.getDescription());
        advertisement.setPrice(request.getPrice());

        advertisement.setSeller(seller);
        advertisement.setCategory(category);

        advertisementRepository.save(advertisement);
    }
}