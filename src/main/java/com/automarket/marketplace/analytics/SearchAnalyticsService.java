package com.automarket.marketplace.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAnalyticsService {

    private final SearchAnalyticsRepository searchAnalyticsRepository;

    @Transactional
    public void trackSearchTerm(String q) {
        if (q == null) {
            return;
        }
        String term = q.trim().toLowerCase();
        if (term.isBlank() || term.length() < 2) {
            return;
        }

        SearchAnalytics entry = new SearchAnalytics();
        entry.setTerm(term.length() > 255 ? term.substring(0, 255) : term);
        searchAnalyticsRepository.save(entry);
    }
}

