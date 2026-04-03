package com.automarket.marketplace.auth.dto;

import java.util.UUID;

public record AuthUserDto(
    UUID id,
    String email,
    String fullName,
    String role,
    boolean verifiedSeller
) {
}

