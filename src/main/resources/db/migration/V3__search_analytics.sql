CREATE TABLE IF NOT EXISTS search_analytics (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  term        VARCHAR(255) NOT NULL,
  searched_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_search_analytics_term ON search_analytics(term);
CREATE INDEX IF NOT EXISTS idx_search_analytics_searched_at ON search_analytics(searched_at DESC);

