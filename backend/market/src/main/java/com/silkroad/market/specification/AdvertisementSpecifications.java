package com.silkroad.market.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementStatus;

public class AdvertisementSpecifications {

    public static Specification<Advertisement> approved() {

        return (root, query, cb) -> cb.equal(root.get("status"), AdvertisementStatus.APPROVED);
    }

    public static Specification<Advertisement> titleOrDescriptionContains(
            String keyword) {

        return (root, query, cb) -> {

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("title")),
                            pattern),

                    cb.like(
                            cb.lower(root.get("description")),
                            pattern));
        };
    }

    public static Specification<Advertisement> categoryIs(Long categoryId) {

        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Advertisement> cityIs(Long cityId) {

        return (root, query, cb) -> cb.equal(root.get("city").get("id"), cityId);
    }

    public static Specification<Advertisement> minPrice(BigDecimal price) {

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), price);
    }

    public static Specification<Advertisement> maxPrice(BigDecimal price) {

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), price);
    }
}