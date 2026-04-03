package com.automarket.marketplace.inquiry.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InquirySummaryDto(
    UUID id,
    UUID listingId,
    String listingTitle,
    UUID buyerId,
    String buyerName,
    UUID sellerId,
    String sellerName,
    String message,
    boolean isRead,
    LocalDateTime createdAt
) {
}

