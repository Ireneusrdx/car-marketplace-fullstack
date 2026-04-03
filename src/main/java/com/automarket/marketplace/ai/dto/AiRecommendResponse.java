package com.automarket.marketplace.ai.dto;

import java.util.List;

public record AiRecommendResponse(
    String summary,
    List<AiRecommendationItemDto> recommendations
) {
}

