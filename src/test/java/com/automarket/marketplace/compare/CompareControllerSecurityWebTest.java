package com.automarket.marketplace.compare;

import com.automarket.marketplace.compare.dto.CompareSessionResponse;
import com.automarket.marketplace.compare.dto.SaveCompareSessionRequest;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompareController.class)
@AutoConfigureMockMvc
class CompareControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompareService compareService;

    @Test
    void quickShouldBePublic() throws Exception {
        when(compareService.quickCompare(any())).thenReturn(new CompareSessionResponse(null, List.of()));

        mockMvc.perform(get("/api/compare/quick").param("ids", UUID.randomUUID() + "," + UUID.randomUUID()))
            .andExpect(status().isOk());

        verify(compareService).quickCompare(any());
    }

    @Test
    void saveWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        SaveCompareSessionRequest request = new SaveCompareSessionRequest(List.of(UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(post("/api/compare")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(compareService);
    }

    @Test
    void saveWithAuthenticationShouldPassPrincipal() throws Exception {
        SaveCompareSessionRequest request = new SaveCompareSessionRequest(List.of(UUID.randomUUID(), UUID.randomUUID()));
        UserPrincipal principal = principal();

        when(compareService.saveSession(eq(request.listingIds()), eq(principal))).thenReturn(new CompareSessionResponse(UUID.randomUUID(), List.of()));

        mockMvc.perform(post("/api/compare")
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        verify(compareService).saveSession(eq(request.listingIds()), eq(principal));
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

