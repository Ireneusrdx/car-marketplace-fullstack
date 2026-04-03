package com.automarket.marketplace.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
    void deleteAllByExpiresAtBefore(LocalDateTime cutoff);
}

