package com.automarket.marketplace.ai.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AiRecommendationItemDto(
    UUID listingId,
    String slug,
    String title,
    BigDecimal price,
    Integer year,
    String fuelType,
    String bodyType,
    int score,
    String reason
) {
}

