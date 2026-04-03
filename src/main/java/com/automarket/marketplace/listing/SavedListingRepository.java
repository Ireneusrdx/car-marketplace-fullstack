package com.automarket.marketplace.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SavedListingRepository extends JpaRepository<SavedListing, SavedListingId> {
    Page<SavedListing> findByIdUserIdOrderBySavedAtDesc(UUID userId, Pageable pageable);
    boolean existsByIdUserIdAndIdListingId(UUID userId, UUID listingId);
    void deleteByIdUserIdAndIdListingId(UUID userId, UUID listingId);
}

