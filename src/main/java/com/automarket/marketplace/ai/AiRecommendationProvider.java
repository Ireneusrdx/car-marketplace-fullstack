package com.automarket.marketplace.ai;

import com.automarket.marketplace.ai.dto.AiRecommendRequest;
import com.automarket.marketplace.listing.CarListing;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AiRecommendationProvider {
    Map<UUID, String> buildRecommendationReasons(AiRecommendRequest request, List<AiScoredListing> scoredListings);
    Map<UUID, String> buildSimilarReasons(CarListing baseListing, List<AiScoredListing> scoredListings);
    String buildSummary(AiRecommendRequest request, List<AiScoredListing> scoredListings);
}

