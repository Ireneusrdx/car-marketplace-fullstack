package com.automarket.marketplace.booking.dto;

import java.util.UUID;

public record BookingActionResponse(
    UUID bookingId,
    String status,
    String message
) {
}

