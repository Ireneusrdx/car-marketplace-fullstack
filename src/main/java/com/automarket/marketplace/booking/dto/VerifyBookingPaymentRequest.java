package com.automarket.marketplace.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VerifyBookingPaymentRequest(
    @NotNull UUID bookingId,
    @NotBlank String paymentIntentId
) {
}

