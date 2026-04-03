package com.automarket.marketplace.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @Bean
    @ConditionalOnProperty(name = "firebase.credentials-path")
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        String path = firebaseProperties.credentialsPath();
        if (path == null || path.trim().isEmpty() || path.contains("placeholder")) {
            log.warn("Firebase credentials path is empty or placeholder. Skipping Firebase initialization.");
            return null;
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists() || file.isDirectory()) {
            log.warn("Firebase credentials file not found or is a directory at: {}. Skipping Firebase initialization.", path);
            return null;
        }

        try (FileInputStream serviceAccount = new FileInputStream(file)) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            log.info("Firebase initialized successfully from: {}", path);
            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.error("Failed to initialize Firebase from {}: {}. Continuing without Firebase.", path, e.getMessage());
            return null;
        }
    }
}
