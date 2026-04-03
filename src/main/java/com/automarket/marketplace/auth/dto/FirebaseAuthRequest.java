package com.automarket.marketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record FirebaseAuthRequest(@NotBlank String idToken) {
}

