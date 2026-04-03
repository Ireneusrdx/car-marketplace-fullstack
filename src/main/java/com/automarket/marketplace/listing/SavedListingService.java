package com.automarket.marketplace.listing;

import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.listing.dto.ListingCardDto;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedListingService {

    private final SavedListingRepository savedListingRepository;
    private final CarListingRepository carListingRepository;
    private final CarImageRepository carImageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ListingCardDto> getSaved(UserPrincipal principal, int page, int size) {
        UUID userId = requireUser(principal).getId();

        Page<SavedListing> result = savedListingRepository.findByIdUserIdOrderBySavedAtDesc(userId, PageRequest.of(page, size));
        List<CarListing> listings = result.getContent().stream().map(SavedListing::getListing).toList();
        List<ListingCardDto> cards = toCardDtos(listings);

        return new PagedResponse<>(
            cards,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Transactional
    public void saveListing(UserPrincipal principal, UUID listingId) {
        User user = requireUser(principal);
        CarListing listing = carListingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (savedListingRepository.existsByIdUserIdAndIdListingId(user.getId(), listingId)) {
            return;
        }

        SavedListing saved = new SavedListing();
        saved.setId(new SavedListingId(user.getId(), listingId));
        saved.setUser(user);
        saved.setListing(listing);
        saved.setSavedAt(LocalDateTime.now());
        savedListingRepository.save(saved);
    }

    @Transactional
    public void unsaveListing(UserPrincipal principal, UUID listingId) {
        UUID userId = requireUser(principal).getId();
        savedListingRepository.deleteByIdUserIdAndIdListingId(userId, listingId);
    }

    @Transactional(readOnly = true)
    public boolean isSaved(UserPrincipal principal, UUID listingId) {
        UUID userId = requireUser(principal).getId();
        return savedListingRepository.existsByIdUserIdAndIdListingId(userId, listingId);
    }

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<ListingCardDto> toCardDtos(List<CarListing> listings) {
        if (listings.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> ids = listings.stream().map(CarListing::getId).toList();
        Map<UUID, String> imageByListingId = carImageRepository.findByListingIdInOrderByDisplayOrderAsc(ids).stream()
            .collect(Collectors.toMap(img -> img.getListing().getId(), CarImage::getUrl, (a, b) -> a));

        return listings.stream().map(l -> new ListingCardDto(
            l.getId(),
            l.getSlug(),
            l.getTitle(),
            l.getYear(),
            l.getMake() == null ? null : l.getMake().getName(),
            l.getModel() == null ? null : l.getModel().getName(),
            l.getVariant(),
            l.getPrice(),
            l.getMileage(),
            l.getFuelType(),
            l.getTransmission(),
            l.getBodyType(),
            l.getLocationCity(),
            l.getLocationState(),
            l.isNegotiable(),
            l.isFeatured(),
            l.isVerified(),
            imageByListingId.get(l.getId())
        )).toList();
    }
}

