package com.automarket.marketplace.ai;

import com.automarket.marketplace.ai.dto.AiRecommendRequest;
import com.automarket.marketplace.listing.CarListing;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class OpenAiRecommendationProvider implements AiRecommendationProvider {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public OpenAiRecommendationProvider(
        RestTemplateBuilder restTemplateBuilder,
        @Value("${openai.api-key:placeholder}") String apiKey,
        @Value("${openai.model:gpt-4o}") String model,
        ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public Map<UUID, String> buildRecommendationReasons(AiRecommendRequest request, List<AiScoredListing> scoredListings) {
        if (scoredListings.isEmpty()) return new LinkedHashMap<>();
        
        try {
            return generateReasonsWithRetry("Generate 1-sentence recommendation reason mapping exactly to the UUIDs for each car strictly requested: " + request, scoredListings);
        } catch (Exception e) {
            log.warn("AI recommendation failed, using fallback.", e);
            return fallbackReasons("Strong fit based on your preferences and popularity.", scoredListings);
        }
    }

    @Override
    public Map<UUID, String> buildSimilarReasons(CarListing baseListing, List<AiScoredListing> scoredListings) {
        if (scoredListings.isEmpty()) return new LinkedHashMap<>();
        
        try {
            return generateReasonsWithRetry("Generate 1-sentence reason mapping exactly to the UUIDs why each car is similar to " + baseListing.getTitle(), scoredListings);
        } catch (Exception e) {
            log.warn("AI similarity failed, using fallback.", e);
            return fallbackReasons("Similar variant matching your target car.", scoredListings);
        }
    }

    @Override
    public String buildSummary(AiRecommendRequest request, List<AiScoredListing> scoredListings) {
        if (scoredListings.isEmpty()) return "No strong matches found. Try relaxing budget or preference constraints.";
        
        try {
            String prompt = "Give a 1 sentence summary bridging these recommendations based on: " + request;
            return callOpenAi(prompt);
        } catch (Exception e) {
            log.warn("AI summary failed, using fallback.", e);
            return "Based on your inputs, these are the top matches ranked by budget, usage, and preference fit.";
        }
    }

    private Map<UUID, String> generateReasonsWithRetry(String systemContext, List<AiScoredListing> scored) {
        int maxRetries = 2;
        for (int i = 0; i < maxRetries; i++) {
            try {
                StringBuilder prompt = new StringBuilder(systemContext).append("\nCars:\n");
                for (AiScoredListing s : scored) {
                    prompt.append("- [ID: ").append(s.listing().getId()).append("] ").append(s.listing().getTitle()).append("\n");
                }
                prompt.append("Return STRICTLY valid JSON like: {\"reasons\": {\"UUID_1\": \"reason\", \"UUID_2\": \"reason\"}}");

                String response = callOpenAi(prompt.toString());
                JsonNode root = objectMapper.readTree(response);
                JsonNode reasonsNode = root.path("reasons");
                
                Map<UUID, String> mapped = new LinkedHashMap<>();
                for (AiScoredListing s : scored) {
                    UUID id = s.listing().getId();
                    String reason = reasonsNode.path(id.toString()).asText("Great choice matching your needs.");
                    mapped.put(id, reason);
                }
                return mapped;
            } catch (Exception e) {
                if (i == maxRetries - 1) throw new RuntimeException("AI retries exhausted", e);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to generate AI reasons");
    }

    private String callOpenAi(String prompt) {
        if ("placeholder".equals(apiKey)) {
            throw new RuntimeException("OpenAI API key missing");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", "You are an AI car dealer. Limit responses to concise answers. Strictly output requested formats."),
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", 500,
            "response_format", Map.of("type", prompt.contains("JSON") ? "json_object" : "text")
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String responseRaw = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", entity, String.class);
        
        try {
            JsonNode root = objectMapper.readTree(responseRaw);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }

    private Map<UUID, String> fallbackReasons(String fallbackMsg, List<AiScoredListing> scoredListings) {
        Map<UUID, String> reasons = new LinkedHashMap<>();
        for (AiScoredListing s : scoredListings) {
            reasons.put(s.listing().getId(), fallbackMsg);
        }
        return reasons;
    }
}

