package com.automarket.marketplace.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminBookingsResponseDto(
    List<AdminBookingRowDto> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    BigDecimal totalRevenue
) {
}

