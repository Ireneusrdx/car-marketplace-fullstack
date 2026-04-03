package com.automarket.marketplace.compare.dto;

import java.util.List;
import java.util.UUID;

public record CompareSessionResponse(
    UUID sessionId,
    List<ComparedListingDto> listings
) {
}

