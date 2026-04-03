package com.automarket.marketplace.listing.dto;

import java.util.UUID;

public record ListingMutationResponse(
    UUID id,
    String slug,
    String status,
    String message
) {
}

