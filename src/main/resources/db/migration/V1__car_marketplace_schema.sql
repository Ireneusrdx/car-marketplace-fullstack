CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- USERS
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  full_name VARCHAR(255),
  avatar_url TEXT,
  auth_provider VARCHAR(20) NOT NULL,
  provider_id VARCHAR(255),
  email_verified BOOLEAN DEFAULT false,
  phone_verified BOOLEAN DEFAULT false,
  role VARCHAR(20) DEFAULT 'BUYER',
  is_verified_seller BOOLEAN DEFAULT false,
  seller_rating DECIMAL(3,2) DEFAULT 0,
  total_listings INTEGER DEFAULT 0,
  total_sales INTEGER DEFAULT 0,
  bio TEXT,
  location VARCHAR(255),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);

-- CAR MAKES
CREATE TABLE car_makes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) UNIQUE NOT NULL,
  logo_url TEXT,
  country VARCHAR(100),
  is_active BOOLEAN DEFAULT true
);

-- CAR MODELS
CREATE TABLE car_models (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  make_id UUID REFERENCES car_makes(id),
  name VARCHAR(100) NOT NULL,
  UNIQUE (make_id, name)
);

-- CAR LISTINGS
CREATE TABLE car_listings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id UUID REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  make_id UUID REFERENCES car_makes(id),
  model_id UUID REFERENCES car_models(id),
  year INTEGER NOT NULL,
  variant VARCHAR(100),
  price DECIMAL(12,2) NOT NULL,
  is_negotiable BOOLEAN DEFAULT true,
  mileage INTEGER,
  fuel_type VARCHAR(30),
  transmission VARCHAR(20),
  drive_type VARCHAR(10),
  engine_cc INTEGER,
  power_bhp INTEGER,
  torque_nm INTEGER,
  seats INTEGER DEFAULT 5,
  color VARCHAR(50),
  condition VARCHAR(20),
  body_type VARCHAR(30),
  ownership_count INTEGER DEFAULT 1,
  insurance_valid BOOLEAN DEFAULT false,
  insurance_expiry DATE,
  registration_year INTEGER,
  registration_state VARCHAR(50),
  vin VARCHAR(17) UNIQUE,
  description TEXT,
  features TEXT[],
  location_city VARCHAR(100),
  location_state VARCHAR(100),
  location_lat DECIMAL(10,7),
  location_lng DECIMAL(10,7),
  status VARCHAR(20) DEFAULT 'ACTIVE',
  is_featured BOOLEAN DEFAULT false,
  is_verified BOOLEAN DEFAULT false,
  view_count INTEGER DEFAULT 0,
  inquiry_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now(),
  expires_at TIMESTAMP
);

CREATE INDEX idx_listings_search
  ON car_listings USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));
CREATE INDEX idx_listings_title_trgm
  ON car_listings USING GIN (title gin_trgm_ops);
CREATE INDEX idx_listings_price ON car_listings(price);
CREATE INDEX idx_listings_year ON car_listings(year);
CREATE INDEX idx_listings_status ON car_listings(status);
CREATE INDEX idx_listings_make_model ON car_listings(make_id, model_id);
CREATE INDEX idx_listings_city_state ON car_listings(location_city, location_state);

-- CAR IMAGES
CREATE TABLE car_images (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  listing_id UUID REFERENCES car_listings(id) ON DELETE CASCADE,
  url TEXT NOT NULL,
  thumbnail_url TEXT,
  is_primary BOOLEAN DEFAULT false,
  display_order INTEGER DEFAULT 0,
  angle VARCHAR(30)
);

-- SAVED / WISHLIST
CREATE TABLE saved_listings (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  listing_id UUID REFERENCES car_listings(id) ON DELETE CASCADE,
  saved_at TIMESTAMP DEFAULT now(),
  PRIMARY KEY (user_id, listing_id)
);

-- INQUIRIES / MESSAGES
CREATE TABLE inquiries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  listing_id UUID REFERENCES car_listings(id),
  buyer_id UUID REFERENCES users(id),
  seller_id UUID REFERENCES users(id),
  message TEXT NOT NULL,
  is_read BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE inquiry_replies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  inquiry_id UUID REFERENCES inquiries(id) ON DELETE CASCADE,
  sender_id UUID REFERENCES users(id),
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT now()
);

-- BOOKINGS / DEPOSITS
CREATE TABLE bookings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  booking_number VARCHAR(20) UNIQUE,
  listing_id UUID REFERENCES car_listings(id),
  buyer_id UUID REFERENCES users(id),
  seller_id UUID REFERENCES users(id),
  booking_type VARCHAR(20),
  deposit_amount DECIMAL(10,2),
  total_amount DECIMAL(12,2),
  stripe_payment_intent_id VARCHAR(255),
  stripe_client_secret TEXT,
  status VARCHAR(20) DEFAULT 'PENDING',
  scheduled_date TIMESTAMP,
  notes TEXT,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);

-- REVIEWS
CREATE TABLE seller_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id UUID REFERENCES users(id),
  reviewer_id UUID REFERENCES users(id),
  booking_id UUID REFERENCES bookings(id),
  rating INTEGER CHECK (rating BETWEEN 1 AND 5),
  title VARCHAR(255),
  body TEXT,
  created_at TIMESTAMP DEFAULT now(),
  UNIQUE (booking_id, reviewer_id)
);

-- AI RECOMMENDATIONS LOG
CREATE TABLE ai_recommendations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id),
  preferences JSONB,
  recommendations JSONB,
  created_at TIMESTAMP DEFAULT now()
);

-- LISTING ANALYTICS
CREATE TABLE listing_views (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  listing_id UUID REFERENCES car_listings(id),
  viewer_id UUID,
  ip_address VARCHAR(50),
  viewed_at TIMESTAMP DEFAULT now()
);

-- COMPARISON SESSIONS
CREATE TABLE comparison_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id),
  listing_ids UUID[],
  created_at TIMESTAMP DEFAULT now()
);

-- Useful audit indexes
CREATE INDEX idx_inquiries_listing_created_at ON inquiries(listing_id, created_at DESC);
CREATE INDEX idx_bookings_listing_status ON bookings(listing_id, status);
CREATE INDEX idx_listing_views_listing_viewed_at ON listing_views(listing_id, viewed_at DESC);

