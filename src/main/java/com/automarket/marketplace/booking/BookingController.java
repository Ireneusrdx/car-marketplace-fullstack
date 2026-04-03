package com.automarket.marketplace.booking;

import com.automarket.marketplace.booking.dto.BookingActionResponse;
import com.automarket.marketplace.booking.dto.BookingDto;
import com.automarket.marketplace.booking.dto.InitiateBookingRequest;
import com.automarket.marketplace.common.PagedResponse;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/initiate")
    public ResponseEntity<BookingDto> initiate(
        @Valid @RequestBody InitiateBookingRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.initiate(request, principal));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<PagedResponse<BookingDto>> myBookings(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingService.myBookings(principal, page, size));
    }

    @GetMapping("/my-received")
    public ResponseEntity<PagedResponse<BookingDto>> myReceived(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingService.myReceived(principal, page, size));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingActionResponse> confirm(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(bookingService.confirm(id, principal));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingActionResponse> cancel(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(bookingService.cancel(id, principal));
    }
}

