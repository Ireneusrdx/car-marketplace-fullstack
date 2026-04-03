package com.automarket.marketplace.listing.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    UploadResult upload(MultipartFile file);
    void deleteByUrl(String url);

    record UploadResult(String url, String thumbnailUrl) {
    }
}

