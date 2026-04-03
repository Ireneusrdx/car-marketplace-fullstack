package com.automarket.marketplace.listing;

import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.listing.dto.ListingCardDto;
import com.automarket.marketplace.listing.dto.SavedCheckResponse;
import com.automarket.marketplace.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/saved")
@RequiredArgsConstructor
public class SavedListingController {

    private final SavedListingService savedListingService;

    @GetMapping
    public ResponseEntity<PagedResponse<ListingCardDto>> getSaved(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(savedListingService.getSaved(principal, page, size));
    }

    @PostMapping("/{listingId}")
    public ResponseEntity<Void> save(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID listingId
    ) {
        savedListingService.saveListing(principal, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> unsave(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID listingId
    ) {
        savedListingService.unsaveListing(principal, listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{listingId}")
    public ResponseEntity<SavedCheckResponse> check(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID listingId
    ) {
        return ResponseEntity.ok(new SavedCheckResponse(savedListingService.isSaved(principal, listingId)));
    }
}

