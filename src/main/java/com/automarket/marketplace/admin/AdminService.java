package com.automarket.marketplace.admin;

import com.automarket.marketplace.admin.dto.AdminDashboardDto;
import com.automarket.marketplace.admin.dto.AdminBookingRowDto;
import com.automarket.marketplace.admin.dto.AdminBookingsResponseDto;
import com.automarket.marketplace.admin.dto.AdminListingDto;
import com.automarket.marketplace.admin.dto.AdminListingModerationResponse;
import com.automarket.marketplace.admin.dto.AdminUserDto;
import com.automarket.marketplace.admin.dto.PopularListingAnalyticsDto;
import com.automarket.marketplace.admin.dto.SearchTermAnalyticsDto;
import com.automarket.marketplace.analytics.SearchAnalyticsRepository;
import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.booking.Booking;
import com.automarket.marketplace.booking.BookingRepository;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.listing.CarListing;
import com.automarket.marketplace.listing.CarListingRepository;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import com.automarket.marketplace.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SearchAnalyticsRepository searchAnalyticsRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDto dashboard(UserPrincipal principal) {
        assertAdmin(principal);

        long totalListings = carListingRepository.count();
        long activeListings = carListingRepository.countByStatus("ACTIVE");
        long pendingListings = carListingRepository.countByStatus("PENDING");
        long soldListings = carListingRepository.countByStatus("SOLD");
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();

        BigDecimal totalRevenue = bookingRepository.findAll().stream()
            .filter(b -> "COMPLETED".equalsIgnoreCase(String.valueOf(b.getStatus())) || "CONFIRMED".equalsIgnoreCase(String.valueOf(b.getStatus())))
            .map(Booking::getTotalAmount)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminDashboardDto(totalListings, activeListings, pendingListings, soldListings, totalUsers, totalBookings, totalRevenue);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminListingDto> listings(UserPrincipal principal, int page, int size) {
        assertAdmin(principal);

        Page<CarListing> result = carListingRepository.findAll(PageRequest.of(page, size));
        List<AdminListingDto> items = result.getContent().stream().map(this::toAdminListing).toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Transactional
    public AdminListingModerationResponse verifyListing(UserPrincipal principal, UUID listingId) {
        assertAdmin(principal);
        CarListing listing = findListing(listingId);
        listing.setVerified(true);
        carListingRepository.save(listing);
        return new AdminListingModerationResponse(listing.getId(), listing.getStatus(), "Listing verified");
    }

    @Transactional
    public AdminListingModerationResponse featureListing(UserPrincipal principal, UUID listingId) {
        assertAdmin(principal);
        CarListing listing = findListing(listingId);
        listing.setFeatured(!listing.isFeatured());
        carListingRepository.save(listing);
        return new AdminListingModerationResponse(listing.getId(), listing.getStatus(), listing.isFeatured() ? "Listing featured" : "Listing unfeatured");
    }

    @Transactional
    public AdminListingModerationResponse rejectListing(UserPrincipal principal, UUID listingId, String reason) {
        assertAdmin(principal);
        CarListing listing = findListing(listingId);
        listing.setStatus("REJECTED");
        listing.setFeatured(false);
        listing.setVerified(false);
        carListingRepository.save(listing);
        String message = (reason == null || reason.isBlank()) ? "Listing rejected" : "Listing rejected: " + reason;
        return new AdminListingModerationResponse(listing.getId(), listing.getStatus(), message);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserDto> users(UserPrincipal principal, int page, int size) {
        assertAdmin(principal);

        Page<User> result = userRepository.findAll(PageRequest.of(page, size));
        List<AdminUserDto> items = result.getContent().stream().map(u -> new AdminUserDto(
            u.getId(),
            u.getEmail(),
            u.getFullName(),
            u.getRole(),
            u.isVerifiedSeller(),
            u.isActive(),
            u.getCreatedAt()
        )).toList();

        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Transactional
    public AdminListingModerationResponse verifySeller(UserPrincipal principal, UUID userId) {
        assertAdmin(principal);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setVerifiedSeller(true);
        userRepository.save(user);
        return new AdminListingModerationResponse(user.getId(), "VERIFIED", "Seller verified badge granted");
    }

    @Transactional(readOnly = true)
    public List<PopularListingAnalyticsDto> popularListings(UserPrincipal principal) {
        assertAdmin(principal);
        return carListingRepository.findTop10ByStatusOrderByViewCountDesc("ACTIVE").stream()
            .map(l -> new PopularListingAnalyticsDto(
                l.getId(),
                l.getTitle(),
                l.getViewCount() == null ? 0 : l.getViewCount(),
                l.getPrice()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchTermAnalyticsDto> topSearches(UserPrincipal principal) {
        assertAdmin(principal);
        return searchAnalyticsRepository.findTopSearchTerms(10).stream()
            .map(row -> new SearchTermAnalyticsDto(
                String.valueOf(row[0]),
                row[1] == null ? 0L : ((Number) row[1]).longValue()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminBookingsResponseDto bookings(UserPrincipal principal, int page, int size) {
        assertAdmin(principal);

        Page<Booking> result = bookingRepository.findAll(PageRequest.of(page, size));
        List<AdminBookingRowDto> items = result.getContent().stream().map(this::toAdminBooking).toList();
        BigDecimal totalRevenue = bookingRepository.totalRevenueForConfirmedAndCompleted();

        return new AdminBookingsResponseDto(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast(),
            totalRevenue == null ? BigDecimal.ZERO : totalRevenue
        );
    }

    private CarListing findListing(UUID listingId) {
        return carListingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private AdminListingDto toAdminListing(CarListing l) {
        return new AdminListingDto(
            l.getId(),
            l.getSlug(),
            l.getTitle(),
            l.getSeller() == null ? null : l.getSeller().getFullName(),
            l.getStatus(),
            l.getPrice(),
            l.isVerified(),
            l.isFeatured(),
            l.getViewCount() == null ? 0 : l.getViewCount(),
            l.getInquiryCount() == null ? 0 : l.getInquiryCount(),
            l.getCreatedAt()
        );
    }

    private AdminBookingRowDto toAdminBooking(Booking b) {
        return new AdminBookingRowDto(
            b.getId(),
            b.getBookingNumber(),
            b.getListing() == null ? null : b.getListing().getId(),
            b.getListing() == null ? null : b.getListing().getTitle(),
            b.getBuyer() == null ? null : b.getBuyer().getId(),
            b.getBuyer() == null ? null : b.getBuyer().getFullName(),
            b.getSeller() == null ? null : b.getSeller().getId(),
            b.getSeller() == null ? null : b.getSeller().getFullName(),
            b.getBookingType(),
            b.getTotalAmount(),
            b.getDepositAmount(),
            b.getStatus(),
            b.getCreatedAt()
        );
    }

    private void assertAdmin(UserPrincipal principal) {
        if (principal == null || principal.getRole() != UserRole.ADMIN) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}

