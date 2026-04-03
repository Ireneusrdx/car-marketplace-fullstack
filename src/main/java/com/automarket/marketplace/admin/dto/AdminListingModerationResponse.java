package com.automarket.marketplace.admin.dto;

import java.util.UUID;

public record AdminListingModerationResponse(
    UUID listingId,
    String status,
    String message
) {
}

