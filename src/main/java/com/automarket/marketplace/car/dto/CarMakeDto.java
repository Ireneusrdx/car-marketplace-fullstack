package com.automarket.marketplace.car.dto;

import java.util.UUID;

public record CarMakeDto(
    UUID id,
    String name,
    String logoUrl,
    String country
) {
}

