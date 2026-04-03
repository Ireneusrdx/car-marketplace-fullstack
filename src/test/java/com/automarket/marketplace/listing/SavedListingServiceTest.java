package com.automarket.marketplace.listing;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedListingServiceTest {

    @Mock private SavedListingRepository savedListingRepository;
    @Mock private CarListingRepository carListingRepository;
    @Mock private CarImageRepository carImageRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SavedListingService savedListingService;

    private UserPrincipal principal;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("buyer@automarket.dev");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        principal = new UserPrincipal(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    void isSavedShouldReturnRepositoryState() {
        UUID listingId = UUID.randomUUID();
        when(savedListingRepository.existsByIdUserIdAndIdListingId(userId, listingId)).thenReturn(true);

        boolean result = savedListingService.isSaved(principal, listingId);

        assertThat(result).isTrue();
    }

    @Test
    void unsaveShouldDeleteRelation() {
        UUID listingId = UUID.randomUUID();

        savedListingService.unsaveListing(principal, listingId);

        verify(savedListingRepository).deleteByIdUserIdAndIdListingId(userId, listingId);
    }
}

