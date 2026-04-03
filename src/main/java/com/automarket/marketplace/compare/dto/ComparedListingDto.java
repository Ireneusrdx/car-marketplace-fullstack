package com.automarket.marketplace.compare.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ComparedListingDto(
    UUID id,
    String slug,
    String title,
    Integer year,
    String make,
    String model,
    BigDecimal price,
    Integer mileage,
    String fuelType,
    String transmission,
    Integer engineCc,
    Integer powerBhp,
    Integer torqueNm,
    Integer seats,
    String condition,
    String registrationState,
    String sellerName,
    BigDecimal sellerRating,
    String primaryImageUrl
) {
}

