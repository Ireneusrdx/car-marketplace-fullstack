package com.automarket.marketplace.inquiry;

import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.inquiry.dto.CreateInquiryRequest;
import com.automarket.marketplace.inquiry.dto.InquiryReplyDto;
import com.automarket.marketplace.inquiry.dto.InquirySummaryDto;
import com.automarket.marketplace.inquiry.dto.InquiryThreadDto;
import com.automarket.marketplace.inquiry.dto.ReadStatusResponse;
import com.automarket.marketplace.inquiry.dto.ReplyInquiryRequest;
import com.automarket.marketplace.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<InquirySummaryDto> create(
        @Valid @RequestBody CreateInquiryRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryService.create(request, principal));
    }

    @GetMapping("/received")
    public ResponseEntity<PagedResponse<InquirySummaryDto>> received(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(inquiryService.received(principal, page, size));
    }

    @GetMapping("/sent")
    public ResponseEntity<PagedResponse<InquirySummaryDto>> sent(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(inquiryService.sent(principal, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryThreadDto> thread(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inquiryService.thread(id, principal));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<InquiryReplyDto> reply(
        @PathVariable UUID id,
        @Valid @RequestBody ReplyInquiryRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryService.reply(id, request, principal));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ReadStatusResponse> markRead(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ReadStatusResponse(inquiryService.markRead(id, principal)));
    }
}

