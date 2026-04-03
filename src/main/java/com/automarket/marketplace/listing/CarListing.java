package com.automarket.marketplace.listing;

import com.automarket.marketplace.car.CarMake;
import com.automarket.marketplace.car.CarModel;
import com.automarket.marketplace.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "car_listings")
@Getter
@Setter
public class CarListing {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id")
    private CarMake make;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private CarModel model;

    private Integer year;
    private String variant;
    private BigDecimal price;

    @Column(name = "is_negotiable")
    private boolean negotiable;

    private Integer mileage;

    @Column(name = "fuel_type")
    private String fuelType;

    private String transmission;

    @Column(name = "drive_type")
    private String driveType;

    @Column(name = "engine_cc")
    private Integer engineCc;

    @Column(name = "power_bhp")
    private Integer powerBhp;

    @Column(name = "torque_nm")
    private Integer torqueNm;

    private Integer seats;
    private String color;
    private String condition;

    @Column(name = "body_type")
    private String bodyType;

    @Column(name = "ownership_count")
    private Integer ownershipCount;

    @Column(name = "insurance_valid")
    private boolean insuranceValid;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "registration_year")
    private Integer registrationYear;

    @Column(name = "registration_state")
    private String registrationState;

    private String vin;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "features", columnDefinition = "text[]")
    private String[] features;

    @Column(name = "location_city")
    private String locationCity;

    @Column(name = "location_state")
    private String locationState;

    @Column(name = "location_lat")
    private BigDecimal locationLat;

    @Column(name = "location_lng")
    private BigDecimal locationLng;

    private String status;

    @Column(name = "is_featured")
    private boolean featured;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "inquiry_count")
    private Integer inquiryCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}

