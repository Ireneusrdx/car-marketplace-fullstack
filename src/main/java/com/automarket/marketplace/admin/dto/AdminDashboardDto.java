package com.automarket.marketplace.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardDto(
    long totalListings,
    long activeListings,
    long pendingListings,
    long soldListings,
    long totalUsers,
    long totalBookings,
    BigDecimal totalRevenue
) {
}

