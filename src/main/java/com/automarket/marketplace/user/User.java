package com.automarket.marketplace.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    private UUID id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "auth_provider", nullable = false)
    private String authProvider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "phone_verified")
    private boolean phoneVerified;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.BUYER;

    @Column(name = "is_verified_seller")
    private boolean verifiedSeller;

    @Column(name = "seller_rating")
    private BigDecimal sellerRating;

    @Column(name = "total_listings")
    private Integer totalListings;

    @Column(name = "total_sales")
    private Integer totalSales;

    private String bio;

    private String location;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
        sellerRating = sellerRating == null ? BigDecimal.ZERO : sellerRating;
        totalListings = totalListings == null ? 0 : totalListings;
        totalSales = totalSales == null ? 0 : totalSales;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

