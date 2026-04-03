package com.automarket.marketplace.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarListingRepository extends JpaRepository<CarListing, UUID>, JpaSpecificationExecutor<CarListing> {
    Page<CarListing> findByStatusAndExpiresAtAfterOrderByCreatedAtDesc(String status, LocalDateTime now, Pageable pageable);
    Page<CarListing> findByStatusAndFeaturedTrueAndExpiresAtAfterOrderByCreatedAtDesc(String status, LocalDateTime now, Pageable pageable);
    Optional<CarListing> findBySlugAndStatusAndExpiresAtAfter(String slug, String status, LocalDateTime now);
    Page<CarListing> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
    Optional<CarListing> findByIdAndSellerId(UUID id, UUID sellerId);
    long countByStatus(String status);
    long countByStatusAndVerifiedTrue(String status);
    long countByStatusAndFeaturedTrue(String status);
    List<CarListing> findTop10ByStatusOrderByViewCountDesc(String status);
}
