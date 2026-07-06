package com.silkroad.market.dto.advertisement;

import java.math.BigDecimal;

import com.silkroad.market.entity.AdvertisementStatus;

public class AdvertisementSummaryResponse {

    private Long id;
    private String title;
    private BigDecimal price;
    private String sellerUsername;
    private String category;
    private String city;
    private AdvertisementStatus status;
    private String thumbnailUrl;

    public AdvertisementSummaryResponse(Long id1, String title1, BigDecimal price1, String username, String name,
            AdvertisementStatus status1) {
    }

    public AdvertisementSummaryResponse(
            Long id,
            String title,
            BigDecimal price,
            String sellerUsername,
            String category,
            String city,
            AdvertisementStatus status,
            String thumbnailUrl) {

        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerUsername = sellerUsername;
        this.category = category;
        this.city = city;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getCategory() {
        return category;
    }

    public String getCity() {
        return city;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public AdvertisementStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setStatus(AdvertisementStatus status) {
        this.status = status;
    }
}