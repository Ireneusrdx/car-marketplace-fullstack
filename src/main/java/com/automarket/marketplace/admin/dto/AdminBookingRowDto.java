package com.automarket.marketplace.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminBookingRowDto(
    UUID id,
    String bookingNumber,
    UUID listingId,
    String listingTitle,
    UUID buyerId,
    String buyerName,
    UUID sellerId,
    String sellerName,
    String bookingType,
    BigDecimal totalAmount,
    BigDecimal depositAmount,
    String status,
    LocalDateTime createdAt
) {
}

