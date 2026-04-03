package com.automarket.marketplace.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    @ConditionalOnProperty(name = "cloudinary.cloud-name")
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", properties.cloudName());
        config.put("api_key", properties.apiKey());
        config.put("api_secret", properties.apiSecret());
        return new Cloudinary(config);
    }
}

