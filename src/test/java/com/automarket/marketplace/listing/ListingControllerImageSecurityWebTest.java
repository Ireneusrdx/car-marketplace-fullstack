package com.automarket.marketplace.listing;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc
class ListingControllerImageSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ListingService listingService;

    @Test
    void reorderImagesWithoutAuthenticationShouldBeRejected() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReorderRequest(List.of(imageId)))))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(listingService);
    }

    @Test
    void reorderImagesWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageA = UUID.randomUUID();
        UUID imageB = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReorderRequest(List.of(imageA, imageB)))))
            .andExpect(status().isOk());

        verify(listingService).reorderImages(eq(listingId), eq(List.of(imageA, imageB)), eq(principal));
    }

    @Test
    void setPrimaryWithoutAuthenticationShouldBeRejected() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/{imageId}/primary", listingId, imageId)
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(listingService);
    }

    @Test
    void setPrimaryWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(put("/api/listings/{id}/images/{imageId}/primary", listingId, imageId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isOk());

        verify(listingService).setPrimaryImage(eq(listingId), eq(imageId), eq(principal));
    }

    @Test
    void updateAngleWithoutAuthenticationShouldBeRejected() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/{imageId}/angle", listingId, imageId)
                .with(csrf())
                .param("angle", "FRONT"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(listingService);
    }

    @Test
    void updateAngleWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(put("/api/listings/{id}/images/{imageId}/angle", listingId, imageId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf())
                .param("angle", "FRONT"))
            .andExpect(status().isOk());

        verify(listingService).updateImageAngle(eq(listingId), eq(imageId), eq("FRONT"), eq(principal));
    }

    @Test
    void deleteImageWithoutAuthenticationShouldBeRejected() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/listings/{id}/images/{imageId}", listingId, imageId)
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(listingService);
    }

    @Test
    void deleteImageWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        UserPrincipal principal = principal();

        mockMvc.perform(delete("/api/listings/{id}/images/{imageId}", listingId, imageId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(listingService).deleteImage(eq(listingId), eq(imageId), eq(principal));
    }

    @Test
    void addImagesWithoutAuthenticationShouldBeRejected() throws Exception {
        UUID listingId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("files", "car.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart("/api/listings/{id}/images", listingId)
                .file(file)
                .param("angles", "FRONT")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(listingService);
    }

    @Test
    void addImagesWithAuthenticationShouldPassPrincipalToService() throws Exception {
        UUID listingId = UUID.randomUUID();
        UserPrincipal principal = principal();
        MockMultipartFile file = new MockMultipartFile("files", "car.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart("/api/listings/{id}/images", listingId)
                .file(file)
                .param("angles", "FRONT")
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isOk());

        verify(listingService).addImages(eq(listingId), org.mockito.ArgumentMatchers.anyList(), eq(List.of("FRONT")), eq(principal));
    }

    @Test
    void deleteImageWithAuthenticationShouldReturnForbiddenWhenServiceRejects() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        UserPrincipal principal = principal();

        doThrow(new AuthException(org.springframework.http.HttpStatus.FORBIDDEN, "Seller or dealer account required"))
            .when(listingService).deleteImage(eq(listingId), eq(imageId), eq(principal));

        mockMvc.perform(delete("/api/listings/{id}/images/{imageId}", listingId, imageId)
                .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    private UserPrincipal principal() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("seller@automarket.dev");
        user.setRole(UserRole.SELLER);
        user.setActive(true);
        return new UserPrincipal(user);
    }

    private record ReorderRequest(List<UUID> imageIds) {
    }
}
