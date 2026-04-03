package com.automarket.marketplace.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PopularListingAnalyticsDto(
    UUID listingId,
    String title,
    int viewCount,
    BigDecimal price
) {
}

