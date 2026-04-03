package com.automarket.marketplace.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "search_analytics")
@Getter
@Setter
public class SearchAnalytics {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String term;

    @Column(name = "searched_at")
    private LocalDateTime searchedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
    }
}

