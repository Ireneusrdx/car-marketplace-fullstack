package com.automarket.marketplace.inquiry.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InquiryThreadDto(
    UUID id,
    UUID listingId,
    String listingTitle,
    UUID buyerId,
    String buyerName,
    UUID sellerId,
    String sellerName,
    String message,
    boolean isRead,
    LocalDateTime createdAt,
    List<InquiryReplyDto> replies
) {
}

