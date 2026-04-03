package com.automarket.marketplace.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SearchAnalyticsRepository extends JpaRepository<SearchAnalytics, UUID> {

    @Query(value = """
        SELECT term, COUNT(*) AS cnt
        FROM search_analytics
        GROUP BY term
        ORDER BY cnt DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopSearchTerms(int limit);
}

