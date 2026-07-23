package com.silkroad.market.specification;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * matches ads whose category id is any of the given ids. used so that
     * filtering by a top-level category (e.g. "Real Estate") also returns
     * ads posted under its subcategories (e.g. "House", "Apartment").
     */
    public static Specification<Advertisement> categoryIn(List<Long> categoryIds) {

        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    /**
     * matches ads with the given status exactly. used by the admin panel
     * to filter between pending / approved / rejected / sold ads.
     */
    public static Specification<Advertisement> statusIs(AdvertisementStatus status) {

        return (root, query, cb) -> cb.equal(root.get("status"), status);
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
