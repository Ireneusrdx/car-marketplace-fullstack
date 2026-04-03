package com.automarket.marketplace.listing.storage;

import com.automarket.marketplace.auth.AuthException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageService implements ImageStorageService {

    private final ObjectProvider<Cloudinary> cloudinaryProvider;

    @Override
    @SuppressWarnings("unchecked")
    public UploadResult upload(MultipartFile file) {
        try {
            Cloudinary cloudinary = getCloudinary();
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) result.get("secure_url");
            String thumbnail = (String) result.getOrDefault("secure_url", url);
            return new UploadResult(url, thumbnail);
        } catch (AuthException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthException(HttpStatus.BAD_GATEWAY, "Image upload failed");
        }
    }

    @Override
    public void deleteByUrl(String url) {
        try {
            Cloudinary cloudinary = getCloudinary();
            String publicId = extractPublicId(url);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception ignored) {
            // Best-effort cleanup; DB delete remains source of truth.
        }
    }

    private Cloudinary getCloudinary() {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured");
        }
        return cloudinary;
    }

    private String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) {
            return null;
        }
        String[] parts = url.split("/upload/");
        if (parts.length < 2) {
            return null;
        }
        String path = parts[1];
        path = path.replaceFirst("^v\\d+/", "");
        int dot = path.lastIndexOf('.');
        return dot > 0 ? path.substring(0, dot) : path;
    }
}

