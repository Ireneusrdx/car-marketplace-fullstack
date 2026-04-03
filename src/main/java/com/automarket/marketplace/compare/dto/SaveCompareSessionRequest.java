package com.automarket.marketplace.compare.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SaveCompareSessionRequest(
    @NotEmpty @Size(min = 2, max = 3) List<@NotNull UUID> listingIds
) {
}

