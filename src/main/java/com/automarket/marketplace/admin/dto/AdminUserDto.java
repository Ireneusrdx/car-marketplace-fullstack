package com.automarket.marketplace.admin.dto;

import com.automarket.marketplace.user.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDto(
    UUID id,
    String email,
    String fullName,
    UserRole role,
    boolean verifiedSeller,
    boolean active,
    LocalDateTime createdAt
) {
}

