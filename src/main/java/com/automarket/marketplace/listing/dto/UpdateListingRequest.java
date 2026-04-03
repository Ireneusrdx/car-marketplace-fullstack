package com.automarket.marketplace.listing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateListingRequest(
    @Size(max = 255) String title,
    UUID makeId,
    UUID modelId,
    @Min(1990) @Max(2030) Integer year,
    @Size(max = 100) String variant,
    @DecimalMin("1.0") BigDecimal price,
    Boolean isNegotiable,
    @Min(0) Integer mileage,
    @Size(max = 30) String fuelType,
    @Size(max = 20) String transmission,
    @Size(max = 10) String driveType,
    Integer engineCc,
    Integer powerBhp,
    Integer torqueNm,
    Integer seats,
    @Size(max = 50) String color,
    @Size(max = 20) String condition,
    @Size(max = 30) String bodyType,
    Integer ownershipCount,
    Boolean insuranceValid,
    Integer registrationYear,
    @Size(max = 50) String registrationState,
    @Size(max = 17) String vin,
    @Size(max = 5000) String description,
    List<String> features,
    @Size(max = 100) String locationCity,
    @Size(max = 100) String locationState
) {
}

