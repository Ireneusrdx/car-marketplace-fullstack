package com.automarket.marketplace.listing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarImageRepository extends JpaRepository<CarImage, UUID> {
    List<CarImage> findByListingIdInOrderByDisplayOrderAsc(List<UUID> listingIds);
    List<CarImage> findByListingIdOrderByDisplayOrderAsc(UUID listingId);
    Optional<CarImage> findByIdAndListingId(UUID id, UUID listingId);
    Optional<CarImage> findTopByListingIdOrderByDisplayOrderDesc(UUID listingId);
    Optional<CarImage> findByListingIdAndPrimaryTrue(UUID listingId);
}
