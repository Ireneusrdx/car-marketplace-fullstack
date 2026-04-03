package com.automarket.marketplace.ai;

import com.automarket.marketplace.ai.dto.AiRecommendRequest;
import com.automarket.marketplace.ai.dto.AiRecommendResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@AutoConfigureMockMvc
class AiControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRecommendationService aiRecommendationService;

    @Test
    void recommendShouldBePublic() throws Exception {
        AiRecommendRequest request = new AiRecommendRequest(
            new AiRecommendRequest.BudgetPreference(java.math.BigDecimal.valueOf(10000), java.math.BigDecimal.valueOf(30000)),
            List.of("PETROL"),
            List.of("SUV"),
            "family",
            "automatic",
            List.of("reliability"),
            "new york"
        );

        when(aiRecommendationService.recommend(any(AiRecommendRequest.class)))
            .thenReturn(new AiRecommendResponse("ok", List.of()));

        mockMvc.perform(post("/api/ai/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(aiRecommendationService).recommend(any(AiRecommendRequest.class));
    }

    @Test
    void similarShouldBePublic() throws Exception {
        UUID listingId = UUID.randomUUID();

        when(aiRecommendationService.similar(eq(listingId))).thenReturn(new AiRecommendResponse("ok", List.of()));

        mockMvc.perform(get("/api/ai/similar/{listingId}", listingId))
            .andExpect(status().isOk());

        verify(aiRecommendationService).similar(eq(listingId));
    }
}

