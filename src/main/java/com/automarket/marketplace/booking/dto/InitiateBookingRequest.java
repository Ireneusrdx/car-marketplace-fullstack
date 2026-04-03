package com.automarket.marketplace.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record InitiateBookingRequest(
    @NotNull UUID listingId,
    @NotBlank String type,
    LocalDateTime scheduledDate,
    @Size(max = 2000) String notes
) {
}

