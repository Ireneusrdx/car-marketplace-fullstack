package com.automarket.marketplace.listing;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SavedListingId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "listing_id")
    private UUID listingId;
}

