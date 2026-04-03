package com.automarket.marketplace.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    AuthUserDto user
) {
}

