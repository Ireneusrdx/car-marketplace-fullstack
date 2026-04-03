package com.automarket.marketplace.listing;

import com.automarket.marketplace.common.ForbiddenException;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.common.UnauthorizedException;
import com.automarket.marketplace.analytics.SearchAnalyticsService;
import com.automarket.marketplace.car.CarMake;
import com.automarket.marketplace.car.CarMakeRepository;
import com.automarket.marketplace.car.CarModel;
import com.automarket.marketplace.car.CarModelRepository;
import com.automarket.marketplace.listing.dto.CreateListingRequest;
import com.automarket.marketplace.listing.dto.ListingCardDto;
import com.automarket.marketplace.listing.dto.ListingDetailDto;
import com.automarket.marketplace.listing.dto.ListingImageDto;
import com.automarket.marketplace.listing.dto.ListingMutationResponse;
import com.automarket.marketplace.listing.dto.UpdateListingRequest;
import com.automarket.marketplace.listing.storage.ImageStorageService;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import com.automarket.marketplace.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final CarListingRepository carListingRepository;
    private final CarImageRepository carImageRepository;
    private final CarMakeRepository carMakeRepository;
    private final CarModelRepository carModelRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final SearchAnalyticsService searchAnalyticsService;

    private static final Set<String> ALLOWED_ANGLES = Set.of("FRONT", "REAR", "SIDE", "INTERIOR", "ENGINE", "OTHER");

    @Transactional
    public ListingMutationResponse create(CreateListingRequest request, UserPrincipal principal) {
        User seller = getCurrentUser(principal);

        // Auto-upgrade BUYER to SELLER on first listing
        if (seller.getRole() == UserRole.BUYER) {
            seller.setRole(UserRole.SELLER);
            userRepository.save(seller);
        }

        CarListing listing = new CarListing();
        listing.setId(UUID.randomUUID());
        listing.setSeller(seller);
        listing.setTitle(request.title());
        listing.setSlug(generateSlug(request.title()));
        listing.setMake(getMake(request.makeId()));
        listing.setModel(getModel(request.modelId()));
        listing.setYear(request.year());
        listing.setVariant(request.variant());
        listing.setPrice(request.price());
        listing.setNegotiable(Boolean.TRUE.equals(request.isNegotiable()));
        listing.setMileage(request.mileage());
        listing.setFuelType(request.fuelType());
        listing.setTransmission(request.transmission());
        listing.setDriveType(request.driveType());
        listing.setEngineCc(request.engineCc());
        listing.setPowerBhp(request.powerBhp());
        listing.setTorqueNm(request.torqueNm());
        listing.setSeats(request.seats());
        listing.setColor(request.color());
        listing.setCondition(request.condition());
        listing.setBodyType(request.bodyType());
        listing.setOwnershipCount(request.ownershipCount());
        listing.setInsuranceValid(Boolean.TRUE.equals(request.insuranceValid()));
        listing.setRegistrationYear(request.registrationYear());
        listing.setRegistrationState(request.registrationState());
        listing.setVin(request.vin());
        listing.setDescription(request.description());
        listing.setFeatures(request.features() == null ? null : request.features().toArray(new String[0]));
        listing.setLocationCity(request.locationCity());
        listing.setLocationState(request.locationState());
        listing.setStatus("ACTIVE");
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        listing.setExpiresAt(LocalDateTime.now().plusDays(60));
        listing.setViewCount(0);
        listing.setInquiryCount(0);

        carListingRepository.save(listing);
        return new ListingMutationResponse(listing.getId(), listing.getSlug(), listing.getStatus(), "Listing created");
    }

    @Transactional
    public ListingMutationResponse update(UUID id, UpdateListingRequest request, UserPrincipal principal) {
        CarListing listing = resolveOwnedOrAdmin(id, principal);

        if (request.title() != null) {
            listing.setTitle(request.title());
            listing.setSlug(generateSlug(request.title()));
        }
        if (request.makeId() != null) listing.setMake(getMake(request.makeId()));
        if (request.modelId() != null) listing.setModel(getModel(request.modelId()));
        if (request.year() != null) listing.setYear(request.year());
        if (request.variant() != null) listing.setVariant(request.variant());
        if (request.price() != null) listing.setPrice(request.price());
        if (request.isNegotiable() != null) listing.setNegotiable(request.isNegotiable());
        if (request.mileage() != null) listing.setMileage(request.mileage());
        if (request.fuelType() != null) listing.setFuelType(request.fuelType());
        if (request.transmission() != null) listing.setTransmission(request.transmission());
        if (request.driveType() != null) listing.setDriveType(request.driveType());
        if (request.engineCc() != null) listing.setEngineCc(request.engineCc());
        if (request.powerBhp() != null) listing.setPowerBhp(request.powerBhp());
        if (request.torqueNm() != null) listing.setTorqueNm(request.torqueNm());
        if (request.seats() != null) listing.setSeats(request.seats());
        if (request.color() != null) listing.setColor(request.color());
        if (request.condition() != null) listing.setCondition(request.condition());
        if (request.bodyType() != null) listing.setBodyType(request.bodyType());
        if (request.ownershipCount() != null) listing.setOwnershipCount(request.ownershipCount());
        if (request.insuranceValid() != null) listing.setInsuranceValid(request.insuranceValid());
        if (request.registrationYear() != null) listing.setRegistrationYear(request.registrationYear());
        if (request.registrationState() != null) listing.setRegistrationState(request.registrationState());
        if (request.vin() != null) listing.setVin(request.vin());
        if (request.description() != null) listing.setDescription(request.description());
        if (request.features() != null) listing.setFeatures(request.features().toArray(new String[0]));
        if (request.locationCity() != null) listing.setLocationCity(request.locationCity());
        if (request.locationState() != null) listing.setLocationState(request.locationState());
        listing.setUpdatedAt(LocalDateTime.now());

        carListingRepository.save(listing);
        return new ListingMutationResponse(listing.getId(), listing.getSlug(), listing.getStatus(), "Listing updated");
    }

    @Transactional
    public ListingMutationResponse markSold(UUID id, UserPrincipal principal) {
        CarListing listing = resolveOwnedOrAdmin(id, principal);
        listing.setStatus("SOLD");
        listing.setUpdatedAt(LocalDateTime.now());
        carListingRepository.save(listing);
        return new ListingMutationResponse(listing.getId(), listing.getSlug(), listing.getStatus(), "Listing marked as sold");
    }

    @Transactional
    public void delete(UUID id, UserPrincipal principal) {
        CarListing listing = resolveOwnedOrAdmin(id, principal);
        carListingRepository.delete(listing);
    }

    @Transactional
    public List<ListingImageDto> addImages(UUID id, List<MultipartFile> files, List<String> angles, UserPrincipal principal) {
        if (files == null || files.isEmpty()) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "At least one file is required");
        }
        if (angles != null && !angles.isEmpty() && angles.size() != files.size()) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "Angles count must match files count");
        }

        CarListing listing = resolveOwnedOrAdmin(id, principal);
        int order = carImageRepository.findTopByListingIdOrderByDisplayOrderDesc(id)
            .map(img -> img.getDisplayOrder() == null ? 0 : img.getDisplayOrder() + 1)
            .orElse(0);

        boolean hasPrimary = carImageRepository.findByListingIdOrderByDisplayOrderAsc(id)
            .stream()
            .anyMatch(CarImage::isPrimary);

        List<ListingImageDto> uploaded = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ImageStorageService.UploadResult stored = imageStorageService.upload(file);
            String angle = normalizeAngle(angles == null || angles.isEmpty() ? null : angles.get(i));

            CarImage image = new CarImage();
            image.setId(UUID.randomUUID());
            image.setListing(listing);
            image.setUrl(stored.url());
            image.setThumbnailUrl(stored.thumbnailUrl());
            image.setPrimary(!hasPrimary && order == 0);
            image.setDisplayOrder(order++);
            image.setAngle(angle);
            carImageRepository.save(image);

            uploaded.add(new ListingImageDto(
                image.getId(),
                image.getUrl(),
                image.getThumbnailUrl(),
                image.isPrimary(),
                image.getDisplayOrder(),
                image.getAngle()
            ));
        }

        return uploaded;
    }

    @Transactional
    public void deleteImage(UUID id, UUID imageId, UserPrincipal principal) {
        resolveOwnedOrAdmin(id, principal);

        CarImage image = carImageRepository.findByIdAndListingId(imageId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        boolean wasPrimary = image.isPrimary();

        imageStorageService.deleteByUrl(image.getUrl());
        carImageRepository.delete(image);

        if (wasPrimary) {
            carImageRepository.findByListingIdOrderByDisplayOrderAsc(id).stream().findFirst().ifPresent(next -> {
                next.setPrimary(true);
                carImageRepository.save(next);
            });
        }
    }

    @Transactional
    public ListingImageDto setPrimaryImage(UUID id, UUID imageId, UserPrincipal principal) {
        resolveOwnedOrAdmin(id, principal);
        CarImage target = carImageRepository.findByIdAndListingId(imageId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        List<CarImage> images = carImageRepository.findByListingIdOrderByDisplayOrderAsc(id);
        for (CarImage image : images) {
            image.setPrimary(image.getId().equals(target.getId()));
            carImageRepository.save(image);
        }

        return toImageDto(target);
    }

    @Transactional
    public ListingImageDto updateImageAngle(UUID id, UUID imageId, String angle, UserPrincipal principal) {
        resolveOwnedOrAdmin(id, principal);
        CarImage image = carImageRepository.findByIdAndListingId(imageId, id)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        image.setAngle(normalizeAngle(angle));
        carImageRepository.save(image);
        return toImageDto(image);
    }

    @Transactional
    public List<ListingImageDto> reorderImages(UUID id, List<UUID> imageIds, UserPrincipal principal) {
        resolveOwnedOrAdmin(id, principal);
        if (imageIds == null || imageIds.isEmpty()) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "imageIds cannot be empty");
        }

        List<CarImage> existing = carImageRepository.findByListingIdOrderByDisplayOrderAsc(id);
        if (existing.size() != imageIds.size()) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "All listing image IDs must be provided");
        }

        Set<UUID> requestedUnique = new HashSet<>(imageIds);
        if (requestedUnique.size() != imageIds.size()) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "Duplicate image IDs are not allowed");
        }

        Map<UUID, CarImage> byId = existing.stream().collect(Collectors.toMap(CarImage::getId, img -> img));
        if (!byId.keySet().equals(requestedUnique)) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "imageIds must match listing-owned images");
        }

        List<ListingImageDto> result = new ArrayList<>();
        for (int order = 0; order < imageIds.size(); order++) {
            CarImage image = byId.get(imageIds.get(order));
            image.setDisplayOrder(order);
            carImageRepository.save(image);
            result.add(toImageDto(image));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingCardDto> myListings(UserPrincipal principal, int page, int size) {
        assertSellerOrAdmin(principal);
        Page<CarListing> result = carListingRepository.findBySellerIdOrderByCreatedAtDesc(principal.getId(), PageRequest.of(page, size));
        List<ListingCardDto> cards = toCardDtos(result.getContent());
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

    @Transactional(readOnly = true)
    public PagedResponse<ListingCardDto> search(
        UUID makeId,
        UUID modelId,
        Integer yearMin,
        Integer yearMax,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Integer mileageMax,
        String fuelType,
        String transmission,
        String bodyType,
        String condition,
        String city,
        String sort,
        int page,
        int size,
        String q
    ) {
        searchAnalyticsService.trackSearchTerm(q);

        Pageable pageable = PageRequest.of(page, size, mapSort(sort));
        Page<CarListing> result = carListingRepository.findAll(
            ListingSpecifications.search(
                makeId, modelId, yearMin, yearMax, priceMin, priceMax, mileageMax,
                fuelType, transmission, bodyType, condition, city, q
            ),
            pageable
        );

        List<ListingCardDto> cards = toCardDtos(result.getContent());

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

    @Transactional(readOnly = true)
    public List<ListingCardDto> featured(int size) {
        Page<CarListing> result = carListingRepository
            .findByStatusAndFeaturedTrueAndExpiresAtAfterOrderByCreatedAtDesc(
                "ACTIVE",
                LocalDateTime.now(),
                PageRequest.of(0, size)
            );
        return toCardDtos(result.getContent());
    }

    @Transactional(readOnly = true)
    public List<ListingCardDto> recent(int size) {
        Page<CarListing> result = carListingRepository
            .findByStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                "ACTIVE",
                LocalDateTime.now(),
                PageRequest.of(0, size)
            );
        return toCardDtos(result.getContent());
    }

    @Transactional(readOnly = true)
    public ListingDetailDto detailBySlug(String slug) {
        CarListing listing = carListingRepository.findBySlugAndStatusAndExpiresAtAfter(slug, "ACTIVE", LocalDateTime.now())
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        listing.setViewCount((listing.getViewCount() == null ? 0 : listing.getViewCount()) + 1);
        carListingRepository.save(listing);

        List<String> images = carImageRepository.findByListingIdOrderByDisplayOrderAsc(listing.getId())
            .stream()
            .map(CarImage::getUrl)
            .toList();

        return new ListingDetailDto(
            listing.getId(),
            listing.getSlug(),
            listing.getTitle(),
            listing.getYear(),
            listing.getMake() == null ? null : listing.getMake().getName(),
            listing.getModel() == null ? null : listing.getModel().getName(),
            listing.getVariant(),
            listing.getPrice(),
            listing.isNegotiable(),
            listing.getMileage(),
            listing.getFuelType(),
            listing.getTransmission(),
            listing.getDriveType(),
            listing.getEngineCc(),
            listing.getPowerBhp(),
            listing.getTorqueNm(),
            listing.getSeats(),
            listing.getColor(),
            listing.getCondition(),
            listing.getBodyType(),
            listing.getOwnershipCount(),
            listing.isInsuranceValid(),
            listing.getInsuranceExpiry(),
            listing.getRegistrationYear(),
            listing.getRegistrationState(),
            listing.getDescription(),
            listing.getFeatures() == null ? Collections.emptyList() : Arrays.asList(listing.getFeatures()),
            listing.getLocationCity(),
            listing.getLocationState(),
            listing.getLocationLat(),
            listing.getLocationLng(),
            listing.isFeatured(),
            listing.isVerified(),
            listing.getViewCount(),
            listing.getCreatedAt(),
            images
        );
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

    private Sort mapSort(String sort) {
        if (sort == null || sort.isBlank() || sort.equals("newest")) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "mileage_asc" -> Sort.by(Sort.Direction.ASC, "mileage");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private CarListing resolveOwnedOrAdmin(UUID id, UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        CarListing listing = carListingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        boolean isOwner = listing.getSeller() != null && listing.getSeller().getId().equals(principal.getId());
        boolean isAdmin = principal.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You don't have permission to modify this listing");
        }
        
        return listing;
    }

    private void assertSellerOrAdmin(UserPrincipal principal) {
        if (principal == null || (principal.getRole() != UserRole.SELLER && principal.getRole() != UserRole.DEALER && principal.getRole() != UserRole.ADMIN)) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.FORBIDDEN, "Seller or dealer account required");
        }
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CarMake getMake(UUID makeId) {
        return carMakeRepository.findById(makeId)
            .orElseThrow(() -> new ResourceNotFoundException("Make not found"));
    }

    private CarModel getModel(UUID modelId) {
        return carModelRepository.findById(modelId)
            .orElseThrow(() -> new ResourceNotFoundException("Model not found"));
    }

    private String generateSlug(String title) {
        String base = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String normalizeAngle(String angle) {
        String resolved = (angle == null || angle.isBlank()) ? "OTHER" : angle.trim().toUpperCase();
        if (!ALLOWED_ANGLES.contains(resolved)) {
            throw new com.automarket.marketplace.auth.AuthException(HttpStatus.BAD_REQUEST, "Invalid angle. Use FRONT, REAR, SIDE, INTERIOR, ENGINE, or OTHER");
        }
        return resolved;
    }

    private ListingImageDto toImageDto(CarImage image) {
        return new ListingImageDto(
            image.getId(),
            image.getUrl(),
            image.getThumbnailUrl(),
            image.isPrimary(),
            image.getDisplayOrder(),
            image.getAngle()
        );
    }
}

