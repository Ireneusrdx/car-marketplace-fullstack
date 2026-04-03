package com.automarket.marketplace.listing;

import com.automarket.marketplace.listing.dto.ListingImageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListingControllerImageWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ListingService listingService;

    @Test
    void addImagesBindsFilesAndAngles() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile("files", "car.jpg", "image/jpeg", "image-bytes".getBytes());

        when(listingService.addImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.isNull()
        )).thenReturn(List.of(new ListingImageDto(imageId, "https://img/1.jpg", "https://img/1_t.jpg", true, 0, "FRONT")));

        mockMvc.perform(multipart("/api/listings/{id}/images", listingId)
                .file(file)
                .param("angles", "FRONT")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());

        verify(listingService).addImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.isNull()
        );
    }

    @Test
    void addImagesBindsMultipleAnglesInOrder() throws Exception {
        UUID listingId = UUID.randomUUID();

        MockMultipartFile fileA = new MockMultipartFile("files", "a.jpg", "image/jpeg", "a".getBytes());
        MockMultipartFile fileB = new MockMultipartFile("files", "b.jpg", "image/jpeg", "b".getBytes());

        when(listingService.addImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.anyList(),
            ArgumentMatchers.isNull()
        )).thenReturn(List.of());

        mockMvc.perform(multipart("/api/listings/{id}/images", listingId)
                .file(fileA)
                .file(fileB)
                .param("angles", "FRONT", "INTERIOR")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> anglesCaptor = (ArgumentCaptor<List<String>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(listingService).addImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.anyList(),
            anglesCaptor.capture(),
            ArgumentMatchers.isNull()
        );
        assertThat(anglesCaptor.getValue()).containsExactly("FRONT", "INTERIOR");
    }

    @Test
    void reorderImagesSuccess() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID imageA = UUID.randomUUID();
        UUID imageB = UUID.randomUUID();

        when(listingService.reorderImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.eq(List.of(imageB, imageA)),
            ArgumentMatchers.isNull()
        )).thenReturn(List.of());

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReorderRequest(List.of(imageB, imageA)))))
            .andExpect(status().isOk());

        verify(listingService).reorderImages(
            ArgumentMatchers.eq(listingId),
            ArgumentMatchers.eq(List.of(imageB, imageA)),
            ArgumentMatchers.isNull()
        );
    }

    @Test
    void reorderImagesRejectsEmptyList() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageIds\":[]}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(listingService);
    }

    @Test
    void reorderImagesRejectsNullElementInList() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageIds\":[null]}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(listingService);
    }

    @Test
    void reorderImagesRejectsMalformedUuid() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageIds\":[\"not-a-uuid\"]}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(listingService);
    }

    @Test
    void reorderImagesRejectsMissingImageIdsField() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(put("/api/listings/{id}/images/reorder", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(listingService);
    }

    private record ReorderRequest(List<UUID> imageIds) {
    }
}
