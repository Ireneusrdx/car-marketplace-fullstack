package com.automarket.marketplace.auth.dto;

import java.util.UUID;

public record MeResponse(
    UUID id,
    String email,
    String fullName,
    String phone,
    String role,
    boolean emailVerified,
    boolean verifiedSeller
) {
}

