package com.automarket.marketplace.review;

import com.automarket.marketplace.booking.Booking;
import com.automarket.marketplace.booking.BookingRepository;
import com.automarket.marketplace.review.dto.CreateSellerReviewRequest;
import com.automarket.marketplace.review.dto.SellerReviewDto;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import com.automarket.marketplace.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerReviewServiceTest {

    @Mock private SellerReviewRepository sellerReviewRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SellerReviewService sellerReviewService;

    private User reviewer;
    private User seller;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        reviewer = new User();
        reviewer.setId(UUID.randomUUID());
        reviewer.setEmail("buyer@automarket.dev");
        reviewer.setRole(UserRole.BUYER);
        reviewer.setActive(true);
        reviewer.setFullName("Buyer");

        seller = new User();
        seller.setId(UUID.randomUUID());
        seller.setEmail("seller@automarket.dev");
        seller.setRole(UserRole.SELLER);
        seller.setActive(true);
        seller.setFullName("Seller");

        principal = new UserPrincipal(reviewer);

        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
    }

    @Test
    void createShouldSaveReviewForEligibleBooking() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBuyer(reviewer);
        booking.setSeller(seller);
        booking.setStatus("COMPLETED");

        when(bookingRepository.findByIdAndBuyerId(bookingId, reviewer.getId())).thenReturn(Optional.of(booking));
        when(sellerReviewRepository.existsByBookingIdAndReviewerId(bookingId, reviewer.getId())).thenReturn(false);
        when(sellerReviewRepository.save(any(SellerReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SellerReviewDto result = sellerReviewService.create(
            seller.getId(),
            new CreateSellerReviewRequest(bookingId, 5, "Great deal", "Smooth and transparent process."),
            principal
        );

        assertThat(result.sellerId()).isEqualTo(seller.getId());
        assertThat(result.reviewerId()).isEqualTo(reviewer.getId());
        assertThat(result.rating()).isEqualTo(5);
    }
}

