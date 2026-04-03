package com.automarket.marketplace.ai;

import com.automarket.marketplace.ai.dto.AiRecommendRequest;
import com.automarket.marketplace.ai.dto.AiRecommendResponse;
import com.automarket.marketplace.ai.dto.AiRecommendationItemDto;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.listing.CarListing;
import com.automarket.marketplace.listing.CarListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final CarListingRepository carListingRepository;
    private final AiRecommendationProvider aiRecommendationProvider;

    @Transactional(readOnly = true)
    public AiRecommendResponse recommend(AiRecommendRequest request) {
        List<CarListing> candidates = carListingRepository
            .findByStatusAndExpiresAtAfterOrderByCreatedAtDesc("ACTIVE", LocalDateTime.now(), PageRequest.of(0, 100))
            .getContent();

        List<AiScoredListing> scored = candidates.stream()
            .map(l -> new AiScoredListing(l, scoreForRequest(l, request)))
            .filter(s -> s.score() > 0)
            .sorted(Comparator.comparingInt(AiScoredListing::score).reversed())
            .limit(5)
            .toList();

        Map<java.util.UUID, String> reasons = aiRecommendationProvider.buildRecommendationReasons(request, scored);
        String summary = aiRecommendationProvider.buildSummary(request, scored);

        List<AiRecommendationItemDto> items = scored.stream().map(s -> toItem(s, reasons.get(s.listing().getId()))).toList();
        return new AiRecommendResponse(summary, items);
    }

    @Transactional(readOnly = true)
    public AiRecommendResponse similar(java.util.UUID listingId) {
        CarListing base = carListingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        List<CarListing> candidates = carListingRepository
            .findByStatusAndExpiresAtAfterOrderByCreatedAtDesc("ACTIVE", LocalDateTime.now(), PageRequest.of(0, 100))
            .getContent();

        List<AiScoredListing> scored = new ArrayList<>();
        for (CarListing c : candidates) {
            if (c.getId().equals(base.getId())) {
                continue;
            }
            int score = scoreSimilarity(base, c);
            if (score > 0) {
                scored.add(new AiScoredListing(c, score));
            }
        }

        scored = scored.stream()
            .sorted(Comparator.comparingInt(AiScoredListing::score).reversed())
            .limit(5)
            .toList();

        Map<java.util.UUID, String> reasons = aiRecommendationProvider.buildSimilarReasons(base, scored);
        List<AiRecommendationItemDto> items = scored.stream().map(s -> toItem(s, reasons.get(s.listing().getId()))).toList();
        return new AiRecommendResponse("These cars are similar in specs and price segment.", items);
    }

    private int scoreForRequest(CarListing listing, AiRecommendRequest request) {
        int score = 0;

        if (request.budget() != null) {
            BigDecimal price = listing.getPrice() == null ? BigDecimal.ZERO : listing.getPrice();
            if (request.budget().min() != null && price.compareTo(request.budget().min()) >= 0) score += 15;
            if (request.budget().max() != null && price.compareTo(request.budget().max()) <= 0) score += 15;
        }

        if (containsIgnoreCase(request.fuelType(), listing.getFuelType())) score += 15;
        if (containsIgnoreCase(request.bodyType(), listing.getBodyType())) score += 15;

        if (request.transmission() != null && !request.transmission().isBlank()) {
            String t = request.transmission().toUpperCase(Locale.ROOT);
            if ("ANY".equals(t) || t.equalsIgnoreCase(String.valueOf(listing.getTransmission()))) {
                score += 10;
            }
        }

        if (request.usage() != null) {
            String usage = request.usage().toLowerCase(Locale.ROOT);
            if ("family".equals(usage) && listing.getSeats() != null && listing.getSeats() >= 5) score += 10;
            if ("off_road".equals(usage) && inSet(listing.getDriveType(), Set.of("AWD", "4WD"))) score += 12;
            if ("daily_commute".equals(usage) && listing.getMileage() != null && listing.getMileage() <= 80000) score += 8;
            if ("luxury".equals(usage) && inSet(listing.getMake() == null ? null : listing.getMake().getName(), Set.of("BMW", "Mercedes-Benz", "Audi", "Tesla"))) score += 12;
        }

        if (request.location() != null && !request.location().isBlank()) {
            String loc = request.location().toLowerCase(Locale.ROOT);
            if ((listing.getLocationCity() != null && listing.getLocationCity().toLowerCase(Locale.ROOT).contains(loc))
                || (listing.getLocationState() != null && listing.getLocationState().toLowerCase(Locale.ROOT).contains(loc))) {
                score += 8;
            }
        }

        return Math.min(score, 100);
    }

    private int scoreSimilarity(CarListing base, CarListing other) {
        int score = 0;
        if (equalsIgnoreCase(name(base), name(other))) score += 30;
        if (equalsIgnoreCase(model(base), model(other))) score += 20;
        if (equalsIgnoreCase(base.getBodyType(), other.getBodyType())) score += 15;
        if (equalsIgnoreCase(base.getFuelType(), other.getFuelType())) score += 10;

        if (base.getYear() != null && other.getYear() != null) {
            int diff = Math.abs(base.getYear() - other.getYear());
            score += Math.max(0, 10 - diff * 2);
        }

        if (base.getPrice() != null && other.getPrice() != null && base.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal delta = base.getPrice().subtract(other.getPrice()).abs();
            BigDecimal pct = delta.divide(base.getPrice(), java.math.MathContext.DECIMAL64);
            if (pct.compareTo(BigDecimal.valueOf(0.10)) <= 0) score += 15;
            else if (pct.compareTo(BigDecimal.valueOf(0.20)) <= 0) score += 8;
        }

        return Math.min(score, 100);
    }

    private AiRecommendationItemDto toItem(AiScoredListing scored, String reason) {
        CarListing l = scored.listing();
        return new AiRecommendationItemDto(
            l.getId(),
            l.getSlug(),
            l.getTitle(),
            l.getPrice(),
            l.getYear(),
            l.getFuelType(),
            l.getBodyType(),
            scored.score(),
            reason
        );
    }

    private boolean containsIgnoreCase(List<String> options, String value) {
        if (options == null || options.isEmpty() || value == null) return false;
        return options.stream().anyMatch(o -> o != null && o.equalsIgnoreCase(value));
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private boolean inSet(String value, Set<String> options) {
        return value != null && options.stream().anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private String name(CarListing l) {
        return l.getMake() == null ? null : l.getMake().getName();
    }

    private String model(CarListing l) {
        return l.getModel() == null ? null : l.getModel().getName();
    }
}

