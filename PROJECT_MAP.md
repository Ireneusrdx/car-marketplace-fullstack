# AutoVault — Project Map

> **AutoVault** is a full-stack AI-powered car marketplace built with **Spring Boot 3.3 (Java 17)** on the backend and **React 18 + Vite** on the frontend.
> Key integrations: PostgreSQL 16, Redis, Kafka, Stripe payments, Firebase auth, Cloudinary image storage, OpenAI GPT-4o recommendations.

---

```
project/
│
├── pom.xml                              # Maven build config — Spring Boot, PostgreSQL, JWT, Firebase, Stripe, OpenAI, Cloudinary
├── start-local.sh                           # Local dev startup helper: checks PG/Redis/Kafka, starts backend
├── firebase-service-account.json        # Firebase Admin SDK credentials for social authentication
├── .env                                 # Environment variables: DB, API keys (Stripe, OpenAI, Cloudinary), SMTP, Firebase
├── .gitignore                           # Excludes build artifacts, IDE files, secrets, logs, node_modules
├── README.md                            # Project documentation: tech stack, architecture, local dev setup
├── QUICK_START.md                       # Step-by-step native local dev setup guide (PG/Redis/Kafka)
│
│
│ ═══════════════════════════════════════════════════════════════
│  BACKEND — Java / Spring Boot
│ ═══════════════════════════════════════════════════════════════
│
├── src/
│   ├── main/
│   │   ├── java/com/automarket/marketplace/
│   │   │   │
│   │   │   ├── AutomarketBackendApplication.java        # @SpringBootApplication main class — boots the entire backend
│   │   │   │
│   │   │   ├── admin/                                   # ── Admin Module ──
│   │   │   │   ├── AdminController.java                 # REST endpoints: admin dashboard, user/listing moderation, analytics (role-protected)
│   │   │   │   ├── AdminService.java                    # Business logic: dashboard stats, booking approvals, listing moderation
│   │   │   │   └── dto/
│   │   │   │       ├── AdminDashboardDto.java           # DTO: admin dashboard metrics (total users, listings, revenue)
│   │   │   │       ├── AdminBookingsResponseDto.java    # DTO: paginated booking data for admin panel
│   │   │   │       ├── AdminListingDto.java             # DTO: listing data for admin moderation view
│   │   │   │       ├── AdminListingModerationResponse.java  # DTO: moderation action results (approve/reject)
│   │   │   │       ├── AdminUserDto.java                # DTO: user data in admin dashboard
│   │   │   │       ├── PopularListingAnalyticsDto.java  # DTO: trending/most-viewed listings analytics
│   │   │   │       └── SearchTermAnalyticsDto.java      # DTO: search-query analytics and trending terms
│   │   │   │
│   │   │   ├── ai/                                      # ── AI Recommendation Module ──
│   │   │   │   ├── AiController.java                    # REST endpoints: AI car recommendations & similarity suggestions
│   │   │   │   ├── AiRecommendationService.java         # Core service: queries OpenAI GPT-4o to score/rank listings
│   │   │   │   ├── AiRecommendationProvider.java        # Interface for pluggable AI providers (strategy pattern)
│   │   │   │   ├── OpenAiRecommendationProvider.java    # OpenAI implementation: builds prompts, calls API, parses scores
│   │   │   │   ├── AiScoredListing.java                 # Data class: listing paired with AI-computed match score
│   │   │   │   └── dto/
│   │   │   │       ├── AiRecommendRequest.java          # Request DTO: budget, usage type, fuel preference, priorities
│   │   │   │       ├── AiRecommendResponse.java         # Response DTO: ranked list of AI-scored recommendations
│   │   │   │       └── AiRecommendationItemDto.java     # DTO: single AI-scored listing item
│   │   │   │
│   │   │   ├── analytics/                               # ── Analytics Module ──
│   │   │   │   ├── SearchAnalytics.java                 # JPA entity: tracks search terms, timestamps, user IDs
│   │   │   │   ├── SearchAnalyticsRepository.java       # Repository: aggregation queries for search analytics
│   │   │   │   └── SearchAnalyticsService.java          # Service: records searches, surfaces popular search terms
│   │   │   │
│   │   │   ├── auth/                                    # ── Authentication Module ──
│   │   │   │   ├── AuthController.java                  # REST endpoints: email/Firebase login, token refresh, logout
│   │   │   │   ├── AuthService.java                     # Core logic: registration, email login, token generation
│   │   │   │   ├── FirebaseAuthService.java             # Firebase token verification & user-linking for social logins
│   │   │   │   ├── AuthException.java                   # Custom exception with HTTP status for auth errors
│   │   │   │   ├── AuthExceptionHandler.java            # @ControllerAdvice: translates auth exceptions to JSON errors
│   │   │   │   ├── RefreshToken.java                    # JPA entity: hashed refresh tokens with expiry & revocation
│   │   │   │   ├── RefreshTokenRepository.java          # Repository: refresh token CRUD and lookup by hash
│   │   │   │   ├── TokenHasher.java                     # Utility: SHA-256 hashes refresh tokens before storage
│   │   │   │   └── dto/
│   │   │   │       ├── AuthResponse.java                # Response DTO: user profile + access token + refresh token
│   │   │   │       ├── EmailLoginRequest.java           # Request DTO: email + password login
│   │   │   │       ├── EmailRegisterRequest.java        # Request DTO: new email registration
│   │   │   │       ├── FirebaseAuthRequest.java         # Request DTO: Firebase ID token for social login exchange
│   │   │   │       ├── LogoutRequest.java               # Request DTO: refresh token to revoke on logout
│   │   │   │       ├── MeResponse.java                  # Response DTO: current user profile (/me endpoint)
│   │   │   │       └── RefreshRequest.java              # Request DTO: token refresh
│   │   │   │
│   │   │   ├── booking/                                 # ── Booking Module ──
│   │   │   │   ├── BookingController.java               # REST endpoints: test-drive bookings, approvals, payments
│   │   │   │   ├── BookingService.java                  # Business logic: initiation → approval → payment → completion
│   │   │   │   ├── Booking.java                         # JPA entity: test-drive booking with price, status, dates
│   │   │   │   ├── BookingRepository.java               # Repository: booking queries and persistence
│   │   │   │   ├── BookingPaymentGateway.java           # Interface for pluggable payment providers (strategy pattern)
│   │   │   │   ├── StripeBookingPaymentGateway.java     # Stripe implementation: creates checkout sessions
│   │   │   │   ├── StripeWebhookController.java         # Handles Stripe webhook events (payment success/failure)
│   │   │   │   ├── ProcessedEvent.java                  # JPA entity: tracks processed webhook events (idempotency)
│   │   │   │   ├── ProcessedEventRepository.java        # Repository: processed event lookup
│   │   │   │   └── dto/
│   │   │   │       ├── BookingDto.java                  # DTO: booking details (dates, price, status, participants)
│   │   │   │       ├── InitiateBookingRequest.java      # Request DTO: new booking with date/time
│   │   │   │       └── BookingActionResponse.java       # Response DTO: booking action results (approve, cancel)
│   │   │   │
│   │   │   ├── calculator/                              # ── EMI Calculator Module ──
│   │   │   │   ├── EmiCalculatorController.java         # REST endpoint: EMI (Equated Monthly Installment) calculator
│   │   │   │   ├── EmiCalculatorService.java            # Calculates EMI with compound interest & amortization schedule
│   │   │   │   └── dto/
│   │   │   │       ├── EmiCalculationRequest.java       # Request DTO: price, down payment, interest rate, tenure
│   │   │   │       ├── EmiCalculationResponse.java      # Response DTO: monthly EMI + full amortization table
│   │   │   │       └── AmortizationRowDto.java          # DTO: single month row in amortization schedule
│   │   │   │
│   │   │   ├── car/                                     # ── Car Make/Model Module ──
│   │   │   │   ├── CarsController.java                  # REST endpoints: browse car manufacturers and models
│   │   │   │   ├── CarsService.java                     # Service: manages the makes-and-models reference database
│   │   │   │   ├── CarMake.java                         # JPA entity: car manufacturer (name, logo URL, country)
│   │   │   │   ├── CarMakeRepository.java               # Repository: car makes
│   │   │   │   ├── CarModel.java                        # JPA entity: car model linked to its parent make
│   │   │   │   ├── CarModelRepository.java              # Repository: car models
│   │   │   │   └── dto/
│   │   │   │       ├── CarMakeDto.java                  # DTO: car make data
│   │   │   │       └── CarModelDto.java                 # DTO: car model data
│   │   │   │
│   │   │   ├── common/                                  # ── Common Utilities ──
│   │   │   │   ├── GlobalExceptionHandler.java          # Global @ControllerAdvice: uniform JSON error responses
│   │   │   │   ├── ForbiddenException.java              # Custom 403 exception for forbidden access
│   │   │   │   ├── ResourceNotFoundException.java       # Custom 404 exception for missing resources
│   │   │   │   ├── UnauthorizedException.java           # Custom 401 exception for unauthenticated requests
│   │   │   │   └── PagedResponse.java                   # Generic wrapper for paginated API responses
│   │   │   │
│   │   │   ├── compare/                                 # ── Compare Module ──
│   │   │   │   ├── CompareController.java               # REST endpoints: side-by-side comparison of car listings
│   │   │   │   ├── CompareService.java                  # Service: manages comparison sessions & fetches listing data
│   │   │   │   ├── ComparisonSession.java               # JPA entity: saved comparison session (list of listing IDs)
│   │   │   │   ├── ComparisonSessionRepository.java     # Repository: comparison sessions
│   │   │   │   └── dto/
│   │   │   │       ├── SaveCompareSessionRequest.java   # Request DTO: save a comparison session
│   │   │   │       ├── CompareSessionResponse.java      # Response DTO: comparison session with full listing details
│   │   │   │       └── ComparedListingDto.java          # DTO: individual listing within a comparison
│   │   │   │
│   │   │   ├── config/                                  # ── Configuration ──
│   │   │   │   ├── AppConfig.java                       # @Configuration: enables property binding for JWT, Firebase, Cloudinary
│   │   │   │   ├── JwtProperties.java                   # @ConfigurationProperties: JWT secret, access/refresh token expiry
│   │   │   │   ├── FirebaseConfig.java                  # Initializes Firebase Admin SDK from service-account JSON
│   │   │   │   ├── FirebaseProperties.java              # @ConfigurationProperties: Firebase credentials file path
│   │   │   │   ├── CloudinaryConfig.java                # Creates Cloudinary API client bean from env config
│   │   │   │   ├── CloudinaryProperties.java            # @ConfigurationProperties: cloud name, API key, API secret
│   │   │   │   └── CarDataSeeder.java                   # ApplicationRunner: seeds DB with car makes/models on first startup
│   │   │   │
│   │   │   ├── inquiry/                                 # ── Inquiry Module ──
│   │   │   │   ├── InquiryController.java               # REST endpoints: buyer inquiries about specific listings
│   │   │   │   ├── InquiryService.java                  # Service: manages inquiry threads and seller responses
│   │   │   │   ├── Inquiry.java                         # JPA entity: initial buyer inquiry (message, listing ref)
│   │   │   │   ├── InquiryRepository.java               # Repository: inquiry persistence and queries
│   │   │   │   ├── InquiryReply.java                    # JPA entity: seller reply within an inquiry thread
│   │   │   │   ├── InquiryReplyRepository.java          # Repository: inquiry replies
│   │   │   │   └── dto/
│   │   │   │       ├── CreateInquiryRequest.java        # Request DTO: post a new inquiry
│   │   │   │       ├── InquiryThreadDto.java            # DTO: complete inquiry conversation thread
│   │   │   │       ├── InquirySummaryDto.java           # DTO: inquiry summary for listing/dashboard views
│   │   │   │       ├── InquiryReplyDto.java             # DTO: single reply in a thread
│   │   │   │       └── ReplyInquiryRequest.java         # Request DTO: reply to an inquiry
│   │   │   │
│   │   │   ├── listing/                                 # ── Listing Module ──
│   │   │   │   ├── ListingController.java               # REST endpoints: car listing CRUD, search, filtering, images
│   │   │   │   ├── ListingService.java                  # Core service: creation, editing, publishing, images, deletion
│   │   │   │   ├── CarListing.java                      # JPA entity: car listing (specs, pricing, images, status, seller)
│   │   │   │   ├── CarListingRepository.java            # Repository: custom queries for search, filtering, pagination
│   │   │   │   ├── CarImage.java                        # JPA entity: listing image with display-order tracking
│   │   │   │   ├── CarImageRepository.java              # Repository: image persistence
│   │   │   │   ├── ListingSpecifications.java           # JPA Specifications: dynamic search predicates (make, price, year…)
│   │   │   │   ├── SavedListing.java                    # JPA entity: user-saved listing (wishlist/favorites)
│   │   │   │   ├── SavedListingId.java                  # Composite PK for saved-listing join table (user + listing)
│   │   │   │   ├── SavedListingRepository.java          # Repository: saved listings
│   │   │   │   ├── SavedListingController.java          # REST endpoints: save and unsave listings
│   │   │   │   ├── SavedListingService.java             # Service: saved-listing toggle and retrieval
│   │   │   │   ├── storage/
│   │   │   │   │   ├── ImageStorageService.java         # Interface for pluggable image storage backends
│   │   │   │   │   └── CloudinaryImageStorageService.java # Cloudinary impl: uploads, transforms, deletes car images
│   │   │   │   └── dto/
│   │   │   │       ├── CreateListingRequest.java        # Request DTO: create new car listing
│   │   │   │       ├── UpdateListingRequest.java        # Request DTO: edit existing listing
│   │   │   │       ├── ListingCardDto.java              # DTO: listing card for search results and grids
│   │   │   │       ├── ListingDetailDto.java            # DTO: full listing detail view
│   │   │   │       ├── ListingImageDto.java             # DTO: listing image data
│   │   │   │       ├── ListingMutationResponse.java     # Response DTO: create/update/delete confirmation
│   │   │   │       ├── SavedCheckResponse.java          # Response DTO: whether listing is saved by current user
│   │   │   │       └── ReorderListingImagesRequest.java # Request DTO: reorder listing image display order
│   │   │   │
│   │   │   ├── review/                                  # ── Review Module ──
│   │   │   │   ├── SellerReviewController.java          # REST endpoints: post and view seller reviews
│   │   │   │   ├── SellerReviewService.java             # Service: review creation (requires completed booking) & aggregation
│   │   │   │   ├── SellerReview.java                    # JPA entity: seller review with rating (1–5) and comment
│   │   │   │   ├── SellerReviewRepository.java          # Repository: review persistence and average-rating queries
│   │   │   │   └── dto/
│   │   │   │       ├── SellerReviewDto.java             # DTO: seller review data
│   │   │   │       └── CreateSellerReviewRequest.java   # Request DTO: submit a new review
│   │   │   │
│   │   │   ├── security/                                # ── Security ──
│   │   │   │   ├── SecurityConfig.java                  # Spring Security: CORS, stateless sessions, JWT filter chain
│   │   │   │   ├── JwtService.java                      # JWT generation (access + refresh) and validation via JJWT
│   │   │   │   ├── JwtAuthenticationFilter.java         # OncePerRequestFilter: extracts JWT from Authorization header
│   │   │   │   ├── AppUserDetailsService.java           # UserDetailsService impl: loads users from DB for Spring Security
│   │   │   │   ├── UserPrincipal.java                   # Custom UserDetails principal with User entity & authorities
│   │   │   │   └── RestAuthenticationEntryPoint.java    # Returns JSON 401 for unauthenticated REST requests
│   │   │   │
│   │   │   └── user/                                    # ── User Module ──
│   │   │       ├── User.java                            # JPA entity: user profile (email, name, avatar, role, seller rating)
│   │   │       ├── UserRepository.java                  # Repository: find by email, Firebase UID, etc.
│   │   │       └── UserRole.java                        # Enum: BUYER, SELLER, ADMIN
│   │   │
│   │   └── resources/
│   │       ├── application.yml                          # Main Spring Boot config: PostgreSQL, Redis, Kafka, Flyway, JPA
│   │       └── db/migration/
│   │           ├── V1__car_marketplace_schema.sql        # Flyway: core schema (users, makes, models, listings, bookings…)
│   │           ├── V2__auth_schema.sql                   # Flyway: refresh-tokens table with indexes
│   │           ├── V3__search_analytics.sql              # Flyway: search-analytics table with trigram index
│   │           └── V4__add_processed_events.sql          # Flyway: processed-events table for Stripe webhook idempotency
│   │
│   └── test/
│       ├── resources/
│       │   └── application-test.yml                     # Spring Boot test profile config (H2 / Testcontainers)
│       └── java/com/automarket/marketplace/
│           ├── admin/
│           │   └── AdminControllerSecurityWebTest.java   # Security tests: admin endpoints role-based access
│           ├── ai/
│           │   └── AiControllerSecurityWebTest.java      # Security tests: AI recommendation endpoints
│           ├── auth/
│           │   └── AuthControllerIntegrationTest.java    # Integration tests: email registration/login flows
│           ├── booking/
│           │   └── BookingControllerSecurityWebTest.java  # Security tests: booking endpoints auth
│           ├── calculator/
│           │   ├── EmiCalculatorServiceTest.java         # Unit tests: EMI formula & amortization correctness
│           │   └── EmiCalculatorControllerSecurityWebTest.java  # Security tests: EMI endpoint
│           ├── compare/
│           │   └── CompareControllerSecurityWebTest.java  # Security tests: comparison endpoints
│           ├── inquiry/
│           │   └── InquiryControllerSecurityWebTest.java  # Security tests: inquiry endpoints
│           ├── listing/
│           │   ├── ListingServiceImageTest.java          # Unit tests: image upload, deletion, reordering
│           │   ├── ListingControllerImageWebTest.java    # Integration tests: image management endpoints
│           │   ├── ListingControllerImageSecurityWebTest.java  # Security tests: image endpoints
│           │   ├── SavedListingServiceTest.java          # Unit tests: wishlist toggle and retrieval
│           │   └── SavedListingControllerSecurityWebTest.java  # Security tests: saved-listing endpoints
│           └── review/
│               ├── SellerReviewServiceTest.java          # Unit tests: review creation, validation, rating aggregation
│               └── SellerReviewControllerSecurityWebTest.java  # Security tests: review endpoints
│
│
│ ═══════════════════════════════════════════════════════════════
│  FRONTEND — React / Vite
│ ═══════════════════════════════════════════════════════════════
│
├── frontend/
│   ├── package.json                                     # Dependencies: React 18, Vite, Zustand, Tailwind, Axios, Firebase, Stripe
│   ├── vite.config.js                                   # Vite build config with React plugin, @/ path alias & API proxy to localhost:8080
│   ├── tailwind.config.js                               # Tailwind theme: brand colors, shadows, fonts (Montserrat, Inter, Playfair)
│   ├── postcss.config.js                                # PostCSS pipeline: Tailwind CSS + Autoprefixer
│   ├── index.html                                       # HTML entry point with Vite script injection & Google Fonts
│   ├── README.md                                        # Frontend-specific documentation and dev instructions
│   │
│   └── src/
│       ├── main.jsx                                     # Entry point: BrowserRouter, HelmetProvider, ToastProvider
│       ├── App.jsx                                      # Root component: routes, layout (Navbar + Footer), auth init
│       │
│       ├── components/
│       │   ├── ProtectedRoute.jsx                       # Route guard: redirects unauthenticated users to login
│       │   │
│       │   ├── global/                                  # ── Shared UI Components ──
│       │   │   ├── Navbar.jsx                           # Fixed header: nav links, mobile menu, user avatar, auth buttons
│       │   │   ├── Footer.jsx                           # Site footer: company info, quick links, social media icons
│       │   │   ├── Button.jsx                           # Reusable button: primary, secondary, ghost, danger, icon variants
│       │   │   ├── Input.jsx                            # Reusable form input with label, error, Tailwind validation
│       │   │   ├── Badge.jsx                            # Small label for status tags, categories, feature badges
│       │   │   ├── ErrorBoundary.jsx                    # Catches render errors, shows fallback UI
│       │   │   ├── Skeleton.jsx                         # Animated loading-skeleton placeholders
│       │   │   ├── PageTransition.jsx                   # Framer Motion page entrance/exit animations
│       │   │   └── ToastProvider.jsx                    # Context + hook for success/error/info toast notifications
│       │   │
│       │   ├── home/                                    # ── Home Page Components ──
│       │   │   ├── CarCard.jsx                          # Car listing card (grid/list): image, price, specs, save button
│       │   │   └── sections/
│       │   │       ├── HeroSection.jsx                  # Full-width hero banner with background image & CTA buttons
│       │   │       ├── BrowseMakeSection.jsx            # Grid of manufacturer logos for "Browse by Make"
│       │   │       ├── BrowseBodyTypeSection.jsx        # Quick-filter cards: sedan, SUV, truck, coupe, hatchback…
│       │   │       ├── MegaSearchSection.jsx            # Advanced multi-filter search bar
│       │   │       ├── FeaturedListingsSection.jsx      # Carousel/grid of featured promoted listings
│       │   │       ├── EmiTeaserSection.jsx             # Promotional card linking to EMI calculator
│       │   │       ├── AiFinderSection.jsx              # Promotional card linking to AI recommendation wizard
│       │   │       ├── ShowcaseSection.jsx              # Spotlight: recently popular/high-quality listings
│       │   │       └── StatsSection.jsx                 # Animated counters: total listings, users, etc.
│       │   │
│       │   └── listings/                                # ── Listings Page Components ──
│       │       ├── ListingsFilterSidebar.jsx            # Collapsible sidebar: price, make, model, year, fuel, body filters
│       │       ├── ListingsSearchHero.jsx               # Search bar with active-filter pills
│       │       ├── ListingsSortViewBar.jsx              # Sort (newest, price, mileage) + grid/list view toggle
│       │       └── CompareFloatingBar.jsx               # Floating bar: selected comparison cars + "Compare Now"
│       │
│       ├── pages/                                       # ── Pages ──
│       │   ├── HomePage.jsx                             # Landing: hero, search, featured listings, browse sections, stats
│       │   ├── ListingsPage.jsx                         # Search results: filter sidebar, sort bar, grid/list, pagination
│       │   ├── CarDetailPage.jsx                        # Car detail: image gallery, specs table, seller info, booking/inquiry
│       │   ├── AiFinderPage.jsx                         # Multi-step wizard: enter preferences → AI-ranked recommendations
│       │   ├── ComparePage.jsx                          # Side-by-side comparison table (up to 4 listings)
│       │   ├── PostListingPage.jsx                      # Multi-step form: sellers create/edit listings with image uploads
│       │   ├── DashboardPage.jsx                        # User dashboard tabs: listings, bookings, inquiries, saved, profile
│       │   ├── AuthPage.jsx                             # Login/register: email+password form + Firebase social auth
│       │   └── NotFoundPage.jsx                         # 404 error page
│       │
│       ├── store/                                       # ── Zustand State Management ──
│       │   ├── authStore.js                             # Auth state: user, tokens, login/logout/refresh, persisted init
│       │   ├── carStore.js                              # Listings state: filters, results, featured cars, pagination
│       │   ├── compareStore.js                          # Comparison state: selected IDs, add/remove/clear
│       │   └── uiStore.js                               # UI state: modal visibility, mobile sidebar, notification flags
│       │
│       ├── lib/                                         # ── API Service Layer ──
│       │   ├── api.js                                   # Axios instance: JWT interceptor + auto-refresh on 401
│       │   ├── authService.js                           # Auth API: register, login, logout, refreshToken, getCurrentUser
│       │   ├── listingsService.js                       # Listings API: search, detail, similar, AI recommendations
│       │   ├── bookingService.js                        # Booking API: initiate, approve/cancel, user bookings, payment
│       │   ├── inquiryService.js                        # Inquiry API: send, reply, fetch threads
│       │   ├── savedService.js                          # Saved API: check, save, unsave, fetch all saved
│       │   ├── homeService.js                           # Home API: featured listings, popular searches, platform stats
│       │   ├── firebase.js                              # Firebase SDK init + Google auth provider + dev emulator
│       │   ├── stripe.js                                # Stripe.js loader for checkout session redirects
│       │   └── cn.js                                    # Utility: conditional Tailwind class merging (clsx + twMerge)
│       │
│       └── styles/
│           └── globals.css                              # Global CSS: Tailwind @layer imports, scrollbar, animations
│
│
│ ═══════════════════════════════════════════════════════════════
│  ARCHITECTURE
│ ═══════════════════════════════════════════════════════════════
│
│   ┌─────────────────────────────────────────────────────┐
│   │                  FRONTEND (React)                    │
│   │  Vite · Zustand · Tailwind · Firebase · Stripe.js   │
│   └────────────────────┬────────────────────────────────┘
│                        │ REST API (JSON)
│   ┌────────────────────▼────────────────────────────────┐
│   │                BACKEND (Spring Boot)                 │
│   │  JWT · Spring Security · JPA · Flyway · OpenAI      │
│   │  Stripe Webhooks · Cloudinary · Firebase Admin       │
│   ├──────────────┬──────────────┬───────────────────────┤
│   │ PostgreSQL 16│   Redis 7    │      Kafka 3.7        │
│   │ (Primary DB) │  (Caching)   │      (Events)         │
│   └──────────────┴──────────────┴───────────────────────┘
│
└── (end)
```
