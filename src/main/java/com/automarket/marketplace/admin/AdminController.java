package com.automarket.marketplace.admin;

import com.automarket.marketplace.admin.dto.AdminDashboardDto;
import com.automarket.marketplace.admin.dto.AdminBookingsResponseDto;
import com.automarket.marketplace.admin.dto.AdminListingDto;
import com.automarket.marketplace.admin.dto.AdminListingModerationResponse;
import com.automarket.marketplace.admin.dto.AdminUserDto;
import com.automarket.marketplace.admin.dto.PopularListingAnalyticsDto;
import com.automarket.marketplace.admin.dto.SearchTermAnalyticsDto;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.dashboard(principal));
    }

    @GetMapping("/listings")
    public ResponseEntity<PagedResponse<AdminListingDto>> listings(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listings(principal, page, size));
    }

    @PutMapping("/listings/{id}/verify")
    public ResponseEntity<AdminListingModerationResponse> verify(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminService.verifyListing(principal, id));
    }

    @PutMapping("/listings/{id}/feature")
    public ResponseEntity<AdminListingModerationResponse> feature(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminService.featureListing(principal, id));
    }

    @PutMapping("/listings/{id}/reject")
    public ResponseEntity<AdminListingModerationResponse> reject(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id,
        @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(adminService.rejectListing(principal, id, reason));
    }

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserDto>> users(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.users(principal, page, size));
    }

    @PutMapping("/users/{id}/verify-seller")
    public ResponseEntity<AdminListingModerationResponse> verifySeller(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminService.verifySeller(principal, id));
    }

    @GetMapping("/analytics/popular")
    public ResponseEntity<List<PopularListingAnalyticsDto>> popular(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.popularListings(principal));
    }

    @GetMapping("/analytics/searches")
    public ResponseEntity<List<SearchTermAnalyticsDto>> searches(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.topSearches(principal));
    }

    @GetMapping("/bookings")
    public ResponseEntity<AdminBookingsResponseDto> bookings(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.bookings(principal, page, size));
    }
}
