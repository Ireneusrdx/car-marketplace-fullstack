package com.automarket.marketplace.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSellerReviewRequest(
    @NotNull UUID bookingId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(max = 255) String title,
    @Size(max = 3000) String body
) {
}

