package com.silkroad.market.dto.advertisement;

import java.math.BigDecimal;

import com.silkroad.market.entity.AdvertisementStatus;

public class AdvertisementSummaryResponse {

    private Long id;
    private String title;
    private BigDecimal price;
    private String sellerUsername;
    private String category;
    private AdvertisementStatus status;

    public AdvertisementSummaryResponse() {
    }

    public AdvertisementSummaryResponse(
            Long id,
            String title,
            BigDecimal price,
            String sellerUsername,
            String category,
            AdvertisementStatus status) {

        this.id = id;
        this.title = title;
        this.price = price;
        this.sellerUsername = sellerUsername;
        this.category = category;
        this.status = status;
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

    public void setStatus(AdvertisementStatus status) {
        this.status = status;
    }
}