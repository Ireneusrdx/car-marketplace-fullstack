package com.automarket.marketplace.compare;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.compare.dto.ComparedListingDto;
import com.automarket.marketplace.compare.dto.CompareSessionResponse;
import com.automarket.marketplace.listing.CarImage;
import com.automarket.marketplace.listing.CarImageRepository;
import com.automarket.marketplace.listing.CarListing;
import com.automarket.marketplace.listing.CarListingRepository;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompareService {

    private final ComparisonSessionRepository comparisonSessionRepository;
    private final CarListingRepository carListingRepository;
    private final CarImageRepository carImageRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompareSessionResponse saveSession(List<UUID> listingIds, UserPrincipal principal) {
        User user = requireUser(principal);
        validateIds(listingIds);

        ComparisonSession session = new ComparisonSession();
        session.setUser(user);
        session.setListingIds(listingIds.toArray(new UUID[0]));
        ComparisonSession saved = comparisonSessionRepository.save(session);

        return new CompareSessionResponse(saved.getId(), loadComparedListings(listingIds));
    }

    @Transactional(readOnly = true)
    public CompareSessionResponse getSession(UUID sessionId, UserPrincipal principal) {
        User user = requireUser(principal);
        ComparisonSession session = comparisonSessionRepository.findByIdAndUserId(sessionId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Comparison session not found"));

        List<UUID> ids = Arrays.asList(session.getListingIds());
        return new CompareSessionResponse(session.getId(), loadComparedListings(ids));
    }

    @Transactional(readOnly = true)
    public CompareSessionResponse quickCompare(List<UUID> listingIds) {
        validateIds(listingIds);
        return new CompareSessionResponse(null, loadComparedListings(listingIds));
    }

    private List<ComparedListingDto> loadComparedListings(List<UUID> listingIds) {
        List<CarListing> listings = carListingRepository.findAllById(listingIds);
        if (listings.size() != listingIds.size()) {
            throw new ResourceNotFoundException("One or more listings were not found");
        }

        Map<UUID, CarListing> byId = new HashMap<>();
        for (CarListing listing : listings) {
            if (!"ACTIVE".equalsIgnoreCase(String.valueOf(listing.getStatus()))) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "Only active listings can be compared");
            }
            byId.put(listing.getId(), listing);
        }

        Map<UUID, String> primaryImageByListing = new HashMap<>();
        for (CarImage image : carImageRepository.findByListingIdInOrderByDisplayOrderAsc(listingIds)) {
            UUID listingId = image.getListing().getId();
            primaryImageByListing.putIfAbsent(listingId, image.getUrl());
            if (image.isPrimary()) {
                primaryImageByListing.put(listingId, image.getUrl());
            }
        }

        List<ComparedListingDto> result = new ArrayList<>();
        for (UUID id : listingIds) {
            CarListing l = byId.get(id);
            result.add(new ComparedListingDto(
                l.getId(),
                l.getSlug(),
                l.getTitle(),
                l.getYear(),
                l.getMake() == null ? null : l.getMake().getName(),
                l.getModel() == null ? null : l.getModel().getName(),
                l.getPrice(),
                l.getMileage(),
                l.getFuelType(),
                l.getTransmission(),
                l.getEngineCc(),
                l.getPowerBhp(),
                l.getTorqueNm(),
                l.getSeats(),
                l.getCondition(),
                l.getRegistrationState(),
                l.getSeller() == null ? null : l.getSeller().getFullName(),
                l.getSeller() == null ? null : l.getSeller().getSellerRating(),
                primaryImageByListing.get(l.getId())
            ));
        }
        return result;
    }

    private void validateIds(List<UUID> listingIds) {
        if (listingIds == null || listingIds.size() < 2 || listingIds.size() > 3) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Provide 2 to 3 listing IDs for comparison");
        }
        Set<UUID> unique = new HashSet<>(listingIds);
        if (unique.size() != listingIds.size()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Duplicate listing IDs are not allowed");
        }
    }

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

