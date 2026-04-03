package com.automarket.marketplace.ai.dto;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public record AiRecommendRequest(
    @Valid BudgetPreference budget,
    List<String> fuelType,
    List<String> bodyType,
    String usage,
    String transmission,
    List<String> priorities,
    String location
) {
    public record BudgetPreference(BigDecimal min, BigDecimal max) {
    }
}

