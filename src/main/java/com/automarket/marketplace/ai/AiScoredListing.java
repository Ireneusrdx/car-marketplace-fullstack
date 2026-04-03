package com.automarket.marketplace.ai;

import com.automarket.marketplace.listing.CarListing;

public record AiScoredListing(CarListing listing, int score) {
}

