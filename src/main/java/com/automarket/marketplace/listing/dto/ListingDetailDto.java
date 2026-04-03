package com.automarket.marketplace.listing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ListingDetailDto(
    UUID id,
    String slug,
    String title,
    Integer year,
    String make,
    String model,
    String variant,
    BigDecimal price,
    boolean negotiable,
    Integer mileage,
    String fuelType,
    String transmission,
    String driveType,
    Integer engineCc,
    Integer powerBhp,
    Integer torqueNm,
    Integer seats,
    String color,
    String condition,
    String bodyType,
    Integer ownershipCount,
    boolean insuranceValid,
    LocalDate insuranceExpiry,
    Integer registrationYear,
    String registrationState,
    String description,
    List<String> features,
    String locationCity,
    String locationState,
    BigDecimal locationLat,
    BigDecimal locationLng,
    boolean featured,
    boolean verified,
    Integer viewCount,
    LocalDateTime createdAt,
    List<String> images
) {
}

