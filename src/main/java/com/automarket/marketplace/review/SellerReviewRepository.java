package com.automarket.marketplace.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerReviewRepository extends JpaRepository<SellerReview, UUID> {
    boolean existsByBookingIdAndReviewerId(UUID bookingId, UUID reviewerId);
    Page<SellerReview> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
}

