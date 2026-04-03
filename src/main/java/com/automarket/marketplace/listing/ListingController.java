package com.automarket.marketplace.listing;

import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.listing.dto.CreateListingRequest;
import com.automarket.marketplace.listing.dto.ListingCardDto;
import com.automarket.marketplace.listing.dto.ListingDetailDto;
import com.automarket.marketplace.listing.dto.ListingImageDto;
import com.automarket.marketplace.listing.dto.ListingMutationResponse;
import com.automarket.marketplace.listing.dto.ReorderListingImagesRequest;
import com.automarket.marketplace.listing.dto.UpdateListingRequest;
import com.automarket.marketplace.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<ListingMutationResponse> create(
        @Valid @RequestBody CreateListingRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(request, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingMutationResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateListingRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        listingService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/mark-sold")
    public ResponseEntity<ListingMutationResponse> markSold(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.markSold(id, principal));
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<List<ListingImageDto>> addImages(
        @PathVariable UUID id,
        @RequestParam("files") List<MultipartFile> files,
        @RequestParam(value = "angles", required = false) List<String> angles,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.addImages(id, files, angles, principal));
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<ListingImageDto> setPrimaryImage(
        @PathVariable UUID id,
        @PathVariable UUID imageId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.setPrimaryImage(id, imageId, principal));
    }

    @PutMapping("/{id}/images/{imageId}/angle")
    public ResponseEntity<ListingImageDto> updateImageAngle(
        @PathVariable UUID id,
        @PathVariable UUID imageId,
        @RequestParam("angle") String angle,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.updateImageAngle(id, imageId, angle, principal));
    }

    @PutMapping("/{id}/images/reorder")
    public ResponseEntity<List<ListingImageDto>> reorderImages(
        @PathVariable UUID id,
        @Valid @RequestBody ReorderListingImagesRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(listingService.reorderImages(id, request.imageIds(), principal));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
        @PathVariable UUID id,
        @PathVariable UUID imageId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        listingService.deleteImage(id, imageId, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-listings")
    public ResponseEntity<PagedResponse<ListingCardDto>> myListings(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(listingService.myListings(principal, page, size));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ListingCardDto>> search(
        @RequestParam(required = false) UUID make,
        @RequestParam(required = false) UUID model,
        @RequestParam(required = false) Integer yearMin,
        @RequestParam(required = false) Integer yearMax,
        @RequestParam(required = false) BigDecimal priceMin,
        @RequestParam(required = false) BigDecimal priceMax,
        @RequestParam(required = false) Integer mileageMax,
        @RequestParam(required = false) String fuelType,
        @RequestParam(required = false) String transmission,
        @RequestParam(required = false) String bodyType,
        @RequestParam(required = false) String condition,
        @RequestParam(required = false) String city,
        @RequestParam(required = false, defaultValue = "newest") String sort,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "12") int size,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(listingService.search(
            make, model, yearMin, yearMax, priceMin, priceMax, mileageMax,
            fuelType, transmission, bodyType, condition, city, sort, page, size, q
        ));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ListingCardDto>> featured(
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(listingService.featured(size));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ListingCardDto>> recent(
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(listingService.recent(size));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ListingDetailDto> detail(@PathVariable String slug) {
        return ResponseEntity.ok(listingService.detailBySlug(slug));
    }
}

