package com.automarket.marketplace.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByIdAndBuyerId(UUID id, UUID buyerId);
    Optional<Booking> findByBookingNumber(String bookingNumber);
    Optional<Booking> findByIdAndSellerId(UUID id, UUID sellerId);
    Page<Booking> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId, Pageable pageable);
    Page<Booking> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM Booking b
        WHERE UPPER(b.status) IN ('CONFIRMED', 'COMPLETED')
        """)
    BigDecimal totalRevenueForConfirmedAndCompleted();
}
