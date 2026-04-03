package com.automarket.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(String credentialsPath) {
    public boolean hasCredentialsPath() {
        return credentialsPath != null && !credentialsPath.isBlank();
    }
}

