package com.automarket.marketplace.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, FirebaseProperties.class, CloudinaryProperties.class})
public class AppConfig {
}
