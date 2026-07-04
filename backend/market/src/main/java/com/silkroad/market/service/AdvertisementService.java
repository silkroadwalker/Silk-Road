package com.silkroad.market.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.silkroad.market.dto.advertisement.AdvertisementSummaryResponse;
import com.silkroad.market.dto.advertisement.CreateAdvertisementRequest;
import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementImage;
import com.silkroad.market.entity.AdvertisementStatus;
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
    private final ImageStorageService imageStorageService;

    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService) {

        this.advertisementRepository = advertisementRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
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

        Advertisement advertisement = new Advertisement();

        advertisement.setTitle(request.getTitle());
        advertisement.setDescription(request.getDescription());
        advertisement.setPrice(request.getPrice());

        advertisement.setSeller(seller);
        advertisement.setCategory(category);

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

    public List<AdvertisementSummaryResponse> getPendingAdvertisements() {

        return advertisementRepository.findByStatus(AdvertisementStatus.PENDING)
                .stream()
                .map(ad -> new AdvertisementSummaryResponse(
                        ad.getId(),
                        ad.getTitle(),
                        ad.getPrice(),
                        ad.getSeller().getUsername(),
                        ad.getCategory().getName(),
                        ad.getStatus()))
                .toList();
    }
}