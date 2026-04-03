package com.automarket.marketplace.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplyInquiryRequest(
    @NotBlank @Size(max = 2000) String message
) {
}

