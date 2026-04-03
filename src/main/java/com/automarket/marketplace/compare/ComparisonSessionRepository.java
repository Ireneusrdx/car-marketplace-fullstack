package com.automarket.marketplace.compare;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComparisonSessionRepository extends JpaRepository<ComparisonSession, UUID> {
    Optional<ComparisonSession> findByIdAndUserId(UUID id, UUID userId);
}

