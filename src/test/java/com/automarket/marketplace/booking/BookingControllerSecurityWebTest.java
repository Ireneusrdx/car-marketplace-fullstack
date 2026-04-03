package com.automarket.marketplace.booking;

import com.automarket.marketplace.booking.dto.InitiateBookingRequest;
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

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void initiateWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        InitiateBookingRequest request = new InitiateBookingRequest(UUID.randomUUID(), "DEPOSIT", null, "Need test drive");

        mockMvc.perform(post("/api/bookings/initiate")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookingService);
    }

    @Test
    void initiateWithAuthenticationShouldPassPrincipal() throws Exception {
        InitiateBookingRequest request = new InitiateBookingRequest(UUID.randomUUID(), "DEPOSIT", null, "Need test drive");
        UserPrincipal principal = principal();

        mockMvc.perform(post("/api/bookings/initiate")
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(bookingService).initiate(eq(request), eq(principal));
    }

    @Test
    void myBookingsWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bookings/my-bookings"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookingService);
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

