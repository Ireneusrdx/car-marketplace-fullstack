package com.automarket.marketplace.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateInquiryRequest(
    @NotNull UUID listingId,
    @NotBlank @Size(max = 2000) String message
) {
}

