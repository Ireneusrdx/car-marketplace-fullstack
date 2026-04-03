package com.automarket.marketplace.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminListingDto(
    UUID id,
    String slug,
    String title,
    String sellerName,
    String status,
    BigDecimal price,
    boolean verified,
    boolean featured,
    int viewCount,
    int inquiryCount,
    LocalDateTime createdAt
) {
}

