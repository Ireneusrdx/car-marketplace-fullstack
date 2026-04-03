package com.automarket.marketplace.listing;

import com.automarket.marketplace.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_listings")
@Getter
@Setter
public class SavedListing {

    @EmbeddedId
    private SavedListingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("listingId")
    @JoinColumn(name = "listing_id", nullable = false)
    private CarListing listing;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;
}

