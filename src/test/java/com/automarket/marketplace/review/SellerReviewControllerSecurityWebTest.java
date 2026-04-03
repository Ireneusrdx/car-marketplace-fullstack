package com.automarket.marketplace.review;

import com.automarket.marketplace.review.dto.CreateSellerReviewRequest;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerReviewController.class)
@AutoConfigureMockMvc
class SellerReviewControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SellerReviewService sellerReviewService;

    @Test
    void createWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        UUID sellerId = UUID.randomUUID();
        CreateSellerReviewRequest request = new CreateSellerReviewRequest(UUID.randomUUID(), 5, "Nice", "Great seller");

        mockMvc.perform(post("/api/reviews/seller/{sellerId}", sellerId)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerReviewService);
    }

    @Test
    void createWithAuthenticationShouldPassPrincipal() throws Exception {
        UUID sellerId = UUID.randomUUID();
        CreateSellerReviewRequest request = new CreateSellerReviewRequest(UUID.randomUUID(), 5, "Nice", "Great seller");
        UserPrincipal principal = principal();

        mockMvc.perform(post("/api/reviews/seller/{sellerId}", sellerId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(sellerReviewService).create(eq(sellerId), eq(request), eq(principal));
    }

    @Test
    void getBySellerShouldBePublic() throws Exception {
        UUID sellerId = UUID.randomUUID();

        mockMvc.perform(get("/api/reviews/seller/{sellerId}", sellerId))
            .andExpect(status().isOk());

        verify(sellerReviewService).getBySeller(eq(sellerId), eq(0), eq(10));
    }

    private UserPrincipal principal() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("buyer@automarket.dev");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        return new UserPrincipal(user);
    }
}

