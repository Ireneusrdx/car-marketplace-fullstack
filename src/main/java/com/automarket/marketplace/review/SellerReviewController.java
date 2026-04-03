package com.automarket.marketplace.review;

import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.review.dto.CreateSellerReviewRequest;
import com.automarket.marketplace.review.dto.SellerReviewDto;
import com.automarket.marketplace.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class SellerReviewController {

    private final SellerReviewService sellerReviewService;

    @PostMapping("/seller/{sellerId}")
    public ResponseEntity<SellerReviewDto> create(
        @PathVariable UUID sellerId,
        @Valid @RequestBody CreateSellerReviewRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerReviewService.create(sellerId, request, principal));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<PagedResponse<SellerReviewDto>> getBySeller(
        @PathVariable UUID sellerId,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(sellerReviewService.getBySeller(sellerId, page, size));
    }
}

