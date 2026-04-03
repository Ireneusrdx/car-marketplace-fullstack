package com.automarket.marketplace.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {
    Page<Inquiry> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
    Page<Inquiry> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId, Pageable pageable);
}

