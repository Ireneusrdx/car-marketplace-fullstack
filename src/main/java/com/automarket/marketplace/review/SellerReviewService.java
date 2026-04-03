package com.automarket.marketplace.review;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.booking.Booking;
import com.automarket.marketplace.booking.BookingRepository;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.review.dto.CreateSellerReviewRequest;
import com.automarket.marketplace.review.dto.SellerReviewDto;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerReviewService {

    private static final Set<String> ALLOWED_BOOKING_STATUSES = Set.of("CONFIRMED", "COMPLETED");

    private final SellerReviewRepository sellerReviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public SellerReviewDto create(UUID sellerId, CreateSellerReviewRequest request, UserPrincipal principal) {
        User reviewer = requireUser(principal);
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Booking booking = bookingRepository.findByIdAndBuyerId(request.bookingId(), reviewer.getId())
            .orElseThrow(() -> new AuthException(HttpStatus.FORBIDDEN, "Booking is not eligible for review"));

        if (!booking.getSeller().getId().equals(sellerId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Booking does not belong to target seller");
        }

        if (!ALLOWED_BOOKING_STATUSES.contains(String.valueOf(booking.getStatus()).toUpperCase())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Booking status does not allow review yet");
        }

        if (sellerReviewRepository.existsByBookingIdAndReviewerId(request.bookingId(), reviewer.getId())) {
            throw new AuthException(HttpStatus.CONFLICT, "Review already submitted for this booking");
        }

        SellerReview review = new SellerReview();
        review.setSeller(seller);
        review.setReviewer(reviewer);
        review.setBooking(booking);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setBody(request.body());

        SellerReview saved = sellerReviewRepository.save(review);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SellerReviewDto> getBySeller(UUID sellerId, int page, int size) {
        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Seller not found");
        }

        Page<SellerReview> result = sellerReviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, PageRequest.of(page, size));
        List<SellerReviewDto> items = result.getContent().stream().map(this::toDto).toList();

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

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SellerReviewDto toDto(SellerReview review) {
        return new SellerReviewDto(
            review.getId(),
            review.getSeller().getId(),
            review.getReviewer().getId(),
            review.getReviewer().getFullName(),
            review.getRating(),
            review.getTitle(),
            review.getBody(),
            review.getCreatedAt()
        );
    }
}

