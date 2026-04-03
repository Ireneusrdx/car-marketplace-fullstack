package com.automarket.marketplace.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingDto(
    UUID id,
    String bookingNumber,
    UUID listingId,
    String listingTitle,
    UUID buyerId,
    UUID sellerId,
    String bookingType,
    BigDecimal depositAmount,
    BigDecimal totalAmount,
    String status,
    String stripePaymentIntentId,
    String stripeClientSecret,
    LocalDateTime scheduledDate,
    String notes,
    LocalDateTime createdAt
) {
}

