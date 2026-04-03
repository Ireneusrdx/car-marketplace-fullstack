package com.automarket.marketplace.booking;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.booking.dto.BookingActionResponse;
import com.automarket.marketplace.booking.dto.BookingDto;
import com.automarket.marketplace.booking.dto.InitiateBookingRequest;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.listing.CarListing;
import com.automarket.marketplace.listing.CarListingRepository;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
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
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;
    private final BookingPaymentGateway bookingPaymentGateway;

    @Transactional
    public BookingDto initiate(InitiateBookingRequest request, UserPrincipal principal) {
        User buyer = requireUser(principal);
        CarListing listing = carListingRepository.findById(request.listingId())
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getSeller() == null) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Listing seller not found");
        }
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Cannot create booking for your own listing");
        }

        Booking booking = new Booking();
        booking.setBookingNumber("BK" + System.currentTimeMillis());
        booking.setListing(listing);
        booking.setBuyer(buyer);
        booking.setSeller(listing.getSeller());
        booking.setBookingType(request.type());
        booking.setScheduledDate(request.scheduledDate());
        booking.setNotes(request.notes());
        booking.setStatus("PENDING");

        BigDecimal total = listing.getPrice() == null ? BigDecimal.ZERO : listing.getPrice();
        booking.setTotalAmount(total);

        if (!"TEST_DRIVE".equalsIgnoreCase(request.type())) {
            BigDecimal deposit = total.compareTo(BigDecimal.valueOf(500)) < 0 ? total : BigDecimal.valueOf(500);
            booking.setDepositAmount(deposit);
            BookingPaymentGateway.PaymentIntentResult intent = bookingPaymentGateway.createPaymentIntent(
                deposit,
                "usd",
                booking.getBookingNumber(),
                "Booking deposit for " + listing.getTitle()
            );
            booking.setStripePaymentIntentId(intent.paymentIntentId());
            booking.setStripeClientSecret(intent.clientSecret());
        }

        Booking saved = bookingRepository.save(booking);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingDto> myBookings(UserPrincipal principal, int page, int size) {
        User user = requireUser(principal);
        Page<Booking> result = bookingRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
        return toPaged(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingDto> myReceived(UserPrincipal principal, int page, int size) {
        User user = requireUser(principal);
        Page<Booking> result = bookingRepository.findBySellerIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
        return toPaged(result);
    }

    @Transactional
    public BookingActionResponse confirm(UUID bookingId, UserPrincipal principal) {
        User user = requireUser(principal);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isSeller = booking.getSeller() != null && booking.getSeller().getId().equals(user.getId());
        boolean isAdmin = principal.getRole() == com.automarket.marketplace.user.UserRole.ADMIN;

        if (!isSeller && !isAdmin) {
            throw new com.automarket.marketplace.common.ForbiddenException("Only the seller or an admin can confirm this booking");
        }

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
        return new BookingActionResponse(booking.getId(), booking.getStatus(), "Booking confirmed");
    }

    @Transactional
    public BookingActionResponse cancel(UUID bookingId, UserPrincipal principal) {
        User user = requireUser(principal);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean participant = booking.getBuyer() != null && booking.getBuyer().getId().equals(user.getId()) || 
                              booking.getSeller() != null && booking.getSeller().getId().equals(user.getId());
        boolean isAdmin = principal.getRole() == com.automarket.marketplace.user.UserRole.ADMIN;

        if (!participant && !isAdmin) {
            throw new com.automarket.marketplace.common.ForbiddenException("You are not allowed to cancel this booking");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        return new BookingActionResponse(booking.getId(), booking.getStatus(), "Booking cancelled");
    }

    @Transactional
    public void processSuccessfulPayment(String bookingNumber, String paymentIntentId) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingNumber));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            booking.setStatus("CONFIRMED");
            booking.setStripePaymentIntentId(paymentIntentId);
            bookingRepository.save(booking);
        }
    }

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PagedResponse<BookingDto> toPaged(Page<Booking> result) {
        List<BookingDto> items = result.getContent().stream().map(this::toDto).toList();
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

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
            booking.getId(),
            booking.getBookingNumber(),
            booking.getListing() == null ? null : booking.getListing().getId(),
            booking.getListing() == null ? null : booking.getListing().getTitle(),
            booking.getBuyer() == null ? null : booking.getBuyer().getId(),
            booking.getSeller() == null ? null : booking.getSeller().getId(),
            booking.getBookingType(),
            booking.getDepositAmount(),
            booking.getTotalAmount(),
            booking.getStatus(),
            booking.getStripePaymentIntentId(),
            booking.getStripeClientSecret(),
            booking.getScheduledDate(),
            booking.getNotes(),
            booking.getCreatedAt()
        );
    }
}

