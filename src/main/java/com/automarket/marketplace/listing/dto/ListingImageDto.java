package com.automarket.marketplace.listing.dto;

import java.util.UUID;

public record ListingImageDto(
    UUID id,
    String url,
    String thumbnailUrl,
    boolean isPrimary,
    Integer displayOrder,
    String angle
) {
}

