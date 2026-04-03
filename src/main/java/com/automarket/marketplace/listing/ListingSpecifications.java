package com.automarket.marketplace.listing;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<CarListing> search(
        UUID makeId,
        UUID modelId,
        Integer yearMin,
        Integer yearMax,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Integer mileageMax,
        String fuelType,
        String transmission,
        String bodyType,
        String condition,
        String city,
        String q
    ) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), "ACTIVE"));
            predicates.add(cb.greaterThan(root.get("expiresAt"), LocalDateTime.now()));

            if (makeId != null) predicates.add(cb.equal(root.get("make").get("id"), makeId));
            if (modelId != null) predicates.add(cb.equal(root.get("model").get("id"), modelId));
            if (yearMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("year"), yearMin));
            if (yearMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("year"), yearMax));
            if (priceMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceMin));
            if (priceMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceMax));
            if (mileageMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("mileage"), mileageMax));
            if (fuelType != null && !fuelType.isBlank()) predicates.add(cb.equal(cb.upper(root.get("fuelType")), fuelType.toUpperCase()));
            if (transmission != null && !transmission.isBlank()) predicates.add(cb.equal(cb.upper(root.get("transmission")), transmission.toUpperCase()));
            if (bodyType != null && !bodyType.isBlank()) predicates.add(cb.equal(cb.upper(root.get("bodyType")), bodyType.toUpperCase()));
            if (condition != null && !condition.isBlank()) predicates.add(cb.equal(cb.upper(root.get("condition")), condition.toUpperCase()));
            if (city != null && !city.isBlank()) predicates.add(cb.equal(cb.lower(root.get("locationCity")), city.toLowerCase()));

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

