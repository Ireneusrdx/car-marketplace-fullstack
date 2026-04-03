package com.automarket.marketplace.inquiry;

import com.automarket.marketplace.inquiry.dto.CreateInquiryRequest;
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

@WebMvcTest(InquiryController.class)
@AutoConfigureMockMvc
class InquiryControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryService inquiryService;

    @Test
    void createWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        CreateInquiryRequest request = new CreateInquiryRequest(UUID.randomUUID(), "Hi, is this still available?");

        mockMvc.perform(post("/api/inquiries")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(inquiryService);
    }

    @Test
    void createWithAuthenticationShouldPassPrincipal() throws Exception {
        UUID listingId = UUID.randomUUID();
        CreateInquiryRequest request = new CreateInquiryRequest(listingId, "Interested in this car.");
        UserPrincipal principal = principal();

        mockMvc.perform(post("/api/inquiries")
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(inquiryService).create(eq(request), eq(principal));
    }

    @Test
    void receivedWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/inquiries/received"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(inquiryService);
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

