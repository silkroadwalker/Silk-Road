package com.silkroad.market.dto.advertisement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.silkroad.market.entity.AdvertisementStatus;

public class AdvertisementDetailedResponse {

    private Long id;

    private String title;
    private String description;
    private BigDecimal price;

    private String sellerUsername;
    private String sellerFullName;
    private String sellerPhone;

    private String category;
    private String city;

    private AdvertisementStatus status;
    private String rejectionReason;

    private LocalDateTime createdAt;

    private boolean isSubmitter;

    private List<String> imageUrls;

    public AdvertisementDetailedResponse() {
    }

    public AdvertisementDetailedResponse(
            Long id,
            String title,
            String description,
            BigDecimal price,
            String sellerUsername,
            String sellerFullName,
            String sellerPhone,
            String category,
            String city,
            AdvertisementStatus status,
            String rejectionReason,
            LocalDateTime createdAt,
            List<String> imageUrls) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.sellerUsername = sellerUsername;
        this.sellerFullName = sellerFullName;
        this.sellerPhone = sellerPhone;
        this.category = category;
        this.city = city;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.imageUrls = imageUrls;
    }

    public boolean isSubmitter() {
        return isSubmitter;
    }

    public void setSubmitter(boolean isSubmitter) {
        this.isSubmitter = isSubmitter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getSellerFullName() {
        return sellerFullName;
    }

    public void setSellerFullName(String sellerFullName) {
        this.sellerFullName = sellerFullName;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public AdvertisementStatus getStatus() {
        return status;
    }

    public void setStatus(AdvertisementStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}