package com.automarket.marketplace.admin;

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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
class AdminControllerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void dashboardWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminService);
    }

    @Test
    void dashboardWithBuyerRoleShouldReturnForbidden() throws Exception {
        UserPrincipal buyer = principal(UserRole.BUYER);

        mockMvc.perform(get("/api/admin/dashboard")
                .with(authentication(new UsernamePasswordAuthenticationToken(buyer, null, buyer.getAuthorities()))))
            .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    @Test
    void verifyListingWithAdminShouldPass() throws Exception {
        UserPrincipal admin = principal(UserRole.ADMIN);

        mockMvc.perform(put("/api/admin/listings/{id}/verify", java.util.UUID.randomUUID())
                .with(authentication(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isOk());

        verify(adminService).verifyListing(org.mockito.ArgumentMatchers.eq(admin), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void usersWithAdminShouldPass() throws Exception {
        UserPrincipal admin = principal(UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/users")
                .with(authentication(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()))))
            .andExpect(status().isOk());

        verify(adminService).users(org.mockito.ArgumentMatchers.eq(admin), org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20));
    }

    @Test
    void bookingsWithAdminShouldPass() throws Exception {
        UserPrincipal admin = principal(UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/bookings")
                .with(authentication(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()))))
            .andExpect(status().isOk());

        verify(adminService).bookings(org.mockito.ArgumentMatchers.eq(admin), org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20));
    }

    private UserPrincipal principal(UserRole role) {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setEmail("admin@automarket.dev");
        user.setRole(role);
        user.setActive(true);
        return new UserPrincipal(user);
    }
}
