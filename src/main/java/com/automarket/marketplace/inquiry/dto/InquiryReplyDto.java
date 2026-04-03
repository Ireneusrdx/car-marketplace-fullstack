package com.automarket.marketplace.inquiry.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InquiryReplyDto(
    UUID id,
    UUID senderId,
    String senderName,
    String message,
    LocalDateTime createdAt
) {
}

