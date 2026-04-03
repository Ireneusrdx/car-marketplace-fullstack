package com.automarket.marketplace.listing;

import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedListingController.class)
@AutoConfigureMockMvc
class SavedListingControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavedListingService savedListingService;

    @Test
    void getSavedWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/saved"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(savedListingService);
    }

    @Test
    void saveWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(post("/api/saved/{listingId}", listingId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isCreated());

        verify(savedListingService).saveListing(eq(principal), eq(listingId));
    }

    @Test
    void unsaveWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(delete("/api/saved/{listingId}", listingId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(savedListingService).unsaveListing(eq(principal), eq(listingId));
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

