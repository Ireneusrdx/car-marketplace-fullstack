package com.automarket.marketplace.ai;

import com.automarket.marketplace.ai.dto.AiRecommendRequest;
import com.automarket.marketplace.ai.dto.AiRecommendResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<AiRecommendResponse> recommend(@Valid @RequestBody AiRecommendRequest request) {
        return ResponseEntity.ok(aiRecommendationService.recommend(request));
    }

    @GetMapping("/similar/{listingId}")
    public ResponseEntity<AiRecommendResponse> similar(@PathVariable UUID listingId) {
        return ResponseEntity.ok(aiRecommendationService.similar(listingId));
    }
}

