package com.automarket.marketplace.listing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingCardDto(
    UUID id,
    String slug,
    String title,
    Integer year,
    String make,
    String model,
    String variant,
    BigDecimal price,
    Integer mileage,
    String fuelType,
    String transmission,
    String bodyType,
    String locationCity,
    String locationState,
    boolean negotiable,
    boolean featured,
    boolean verified,
    String primaryImageUrl
) {
}

