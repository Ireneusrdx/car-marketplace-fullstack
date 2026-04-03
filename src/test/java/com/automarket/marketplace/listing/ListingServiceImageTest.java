package com.automarket.marketplace.listing;

import com.automarket.marketplace.car.CarMakeRepository;
import com.automarket.marketplace.car.CarModelRepository;
import com.automarket.marketplace.listing.dto.ListingImageDto;
import com.automarket.marketplace.listing.storage.ImageStorageService;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class ListingServiceImageTest {

    @Mock private CarListingRepository carListingRepository;
    @Mock private CarImageRepository carImageRepository;
    @Mock private CarMakeRepository carMakeRepository;
    @Mock private CarModelRepository carModelRepository;
    @Mock private UserRepository userRepository;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private ListingService listingService;

    private UUID listingId;
    private UUID userId;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        listingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("seller@automarket.dev");
        user.setRole(UserRole.SELLER);
        user.setActive(true);
        principal = new UserPrincipal(user);
    }

    @Test
    void addImagesShouldApplyProvidedAngles() {
        CarListing listing = new CarListing();
        listing.setId(listingId);

        MockMultipartFile file = new MockMultipartFile("files", "car.jpg", "image/jpeg", "img".getBytes());

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findTopByListingIdOrderByDisplayOrderDesc(listingId)).thenReturn(Optional.empty());
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of());
        when(imageStorageService.upload(any())).thenReturn(new ImageStorageService.UploadResult("https://img/1.jpg", "https://img/1_t.jpg"));
        when(carImageRepository.save(any(CarImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ListingImageDto> result = listingService.addImages(listingId, List.of(file), List.of("front"), principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).angle()).isEqualTo("FRONT");
    }

    @Test
    void setPrimaryImageShouldSwitchPrimaryFlag() {
        UUID imageAId = UUID.randomUUID();
        UUID imageBId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage imageA = new CarImage();
        imageA.setId(imageAId);
        imageA.setListing(listing);
        imageA.setPrimary(true);
        imageA.setUrl("https://img/a.jpg");

        CarImage imageB = new CarImage();
        imageB.setId(imageBId);
        imageB.setListing(listing);
        imageB.setPrimary(false);
        imageB.setUrl("https://img/b.jpg");

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByIdAndListingId(imageBId, listingId)).thenReturn(Optional.of(imageB));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of(imageA, imageB));
        when(carImageRepository.save(any(CarImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingImageDto dto = listingService.setPrimaryImage(listingId, imageBId, principal);

        assertThat(dto.id()).isEqualTo(imageBId);
        assertThat(imageA.isPrimary()).isFalse();
        assertThat(imageB.isPrimary()).isTrue();
    }

    @Test
    void deletePrimaryImageShouldPromoteNextImage() {
        UUID imagePrimaryId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage primary = new CarImage();
        primary.setId(imagePrimaryId);
        primary.setListing(listing);
        primary.setPrimary(true);
        primary.setUrl("https://img/p.jpg");

        CarImage next = new CarImage();
        next.setId(UUID.randomUUID());
        next.setListing(listing);
        next.setPrimary(false);
        next.setDisplayOrder(1);

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByIdAndListingId(imagePrimaryId, listingId)).thenReturn(Optional.of(primary));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of(next));

        listingService.deleteImage(listingId, imagePrimaryId, principal);

        verify(imageStorageService).deleteByUrl("https://img/p.jpg");
        assertThat(next.isPrimary()).isTrue();
        verify(carImageRepository).save(next);
    }

    @Test
    void reorderImagesShouldUpdateDisplayOrder() {
        UUID imageAId = UUID.randomUUID();
        UUID imageBId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage imageA = new CarImage();
        imageA.setId(imageAId);
        imageA.setListing(listing);
        imageA.setDisplayOrder(0);
        imageA.setUrl("https://img/a.jpg");
        imageA.setThumbnailUrl("https://img/a_t.jpg");

        CarImage imageB = new CarImage();
        imageB.setId(imageBId);
        imageB.setListing(listing);
        imageB.setDisplayOrder(1);
        imageB.setUrl("https://img/b.jpg");
        imageB.setThumbnailUrl("https://img/b_t.jpg");

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(new ArrayList<>(List.of(imageA, imageB)));
        when(carImageRepository.save(any(CarImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ListingImageDto> reordered = listingService.reorderImages(listingId, List.of(imageBId, imageAId), principal);

        assertThat(reordered).hasSize(2);
        assertThat(imageB.getDisplayOrder()).isEqualTo(0);
        assertThat(imageA.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void reorderImagesShouldRejectDuplicateIds() {
        UUID imageAId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage imageA = new CarImage();
        imageA.setId(imageAId);
        imageA.setListing(listing);

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of(imageA));

        assertThatThrownBy(() -> listingService.reorderImages(listingId, List.of(imageAId, imageAId), principal))
            .isInstanceOf(com.automarket.marketplace.auth.AuthException.class)
            .hasMessageContaining("Duplicate image IDs");
    }

    @Test
    void reorderImagesShouldRequireAllListingImageIds() {
        UUID imageAId = UUID.randomUUID();
        UUID imageBId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage imageA = new CarImage();
        imageA.setId(imageAId);
        imageA.setListing(listing);

        CarImage imageB = new CarImage();
        imageB.setId(imageBId);
        imageB.setListing(listing);

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of(imageA, imageB));

        assertThatThrownBy(() -> listingService.reorderImages(listingId, List.of(imageAId), principal))
            .isInstanceOf(com.automarket.marketplace.auth.AuthException.class)
            .hasMessageContaining("All listing image IDs must be provided");
    }

    @Test
    void reorderImagesShouldRejectNonOwnedImageId() {
        UUID imageAId = UUID.randomUUID();
        UUID foreignId = UUID.randomUUID();

        CarListing listing = new CarListing();
        listing.setId(listingId);

        CarImage imageA = new CarImage();
        imageA.setId(imageAId);
        imageA.setListing(listing);

        CarImage imageB = new CarImage();
        imageB.setId(UUID.randomUUID());
        imageB.setListing(listing);

        when(carListingRepository.findByIdAndSellerId(listingId, userId)).thenReturn(Optional.of(listing));
        when(carImageRepository.findByListingIdOrderByDisplayOrderAsc(listingId)).thenReturn(List.of(imageA, imageB));

        assertThatThrownBy(() -> listingService.reorderImages(listingId, List.of(imageAId, foreignId), principal))
            .isInstanceOf(com.automarket.marketplace.auth.AuthException.class)
            .hasMessageContaining("imageIds must match listing-owned images");
    }
}
