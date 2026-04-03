package com.automarket.marketplace.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SellerReviewDto(
    UUID id,
    UUID sellerId,
    UUID reviewerId,
    String reviewerName,
    Integer rating,
    String title,
    String body,
    LocalDateTime createdAt
) {
}

