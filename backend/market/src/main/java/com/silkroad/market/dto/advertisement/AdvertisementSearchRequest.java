package com.silkroad.market.dto.advertisement;

import java.math.BigDecimal;

public class AdvertisementSearchRequest {

    private String keyword;

    private Long categoryId;

    private Long cityId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    public AdvertisementSearchRequest() {
    }

    public AdvertisementSearchRequest(
            String keyword,
            Long categoryId,
            Long cityId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        this.keyword = keyword;
        this.categoryId = categoryId;
        this.cityId = cityId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
}