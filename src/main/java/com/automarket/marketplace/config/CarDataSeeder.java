package com.automarket.marketplace.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class CarDataSeeder implements ApplicationRunner {

    private static final String[] CONDITIONS = {"NEW", "USED", "CERTIFIED"};
    private static final String[] FUEL_TYPES = {"PETROL", "DIESEL", "ELECTRIC", "HYBRID", "CNG"};
    private static final String[] TRANSMISSIONS = {"MANUAL", "AUTOMATIC", "CVT"};
    private static final String[] DRIVE_TYPES = {"FWD", "RWD", "AWD", "4WD"};
    private static final String[] BODY_TYPES = {"SEDAN", "SUV", "HATCHBACK", "COUPE", "TRUCK", "VAN", "CONVERTIBLE"};
    private static final String[] STATES = {"California", "Texas", "Florida", "New York", "Illinois", "Washington", "Arizona", "Colorado"};

    private static final List<String> FEATURE_POOL = List.of(
        "Sunroof", "Leather Seats", "GPS", "Backup Camera", "Cruise Control", "Bluetooth",
        "Parking Sensors", "Heated Seats", "Apple CarPlay", "Android Auto", "Ventilated Seats",
        "Wireless Charging", "Blind Spot Monitor", "Lane Assist", "Adaptive Cruise", "Premium Audio"
    );

    private static final Map<String, List<String>> MAKE_MODELS = new LinkedHashMap<>();
    private static final Map<String, List<String>> BODY_IMAGES = new LinkedHashMap<>();

    static {
        MAKE_MODELS.put("Toyota", List.of("Camry", "Corolla", "RAV4", "Fortuner", "Innova"));
        MAKE_MODELS.put("Honda", List.of("Civic", "City", "CR-V", "Accord", "Jazz"));
        MAKE_MODELS.put("Ford", List.of("Mustang", "Escape", "Explorer", "F-150", "EcoSport"));
        MAKE_MODELS.put("BMW", List.of("3 Series", "5 Series", "X3", "X5", "M3"));
        MAKE_MODELS.put("Mercedes-Benz", List.of("C-Class", "E-Class", "GLA", "GLE", "S-Class"));
        MAKE_MODELS.put("Audi", List.of("A4", "A6", "Q3", "Q5", "Q7"));
        MAKE_MODELS.put("Hyundai", List.of("Elantra", "Tucson", "Venue", "Creta", "Verna"));
        MAKE_MODELS.put("Kia", List.of("Seltos", "Sonet", "Sportage", "Carnival", "Rio"));
        MAKE_MODELS.put("Volkswagen", List.of("Polo", "Virtus", "Taigun", "Tiguan", "Jetta"));
        MAKE_MODELS.put("Tesla", List.of("Model 3", "Model Y", "Model S", "Model X", "Cybertruck"));

        BODY_IMAGES.put("SEDAN", List.of(
            "https://images.unsplash.com/photo-1549924231-f129b911e442?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1493238792000-8113da705763?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("SUV", List.of(
            "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("HATCHBACK", List.of(
            "https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1542282088-fe8426682b8f?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1583121274602-3e2820c69888?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("COUPE", List.of(
            "https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("TRUCK", List.of(
            "https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1558980394-df22a1d24a9a?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1606611013016-969c19ba27bb?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("VAN", List.of(
            "https://images.unsplash.com/photo-1617654112368-307921291f42?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1489516408517-0c0a15662682?auto=format&fit=crop&w=1400&q=80"
        ));
        BODY_IMAGES.put("CONVERTIBLE", List.of(
            "https://images.unsplash.com/photo-1562911791-c7a97b729ec5?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1584345604476-8ec5f452d1f2?auto=format&fit=crop&w=1400&q=80",
            "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1400&q=80"
        ));
    }

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.listings-count:50}")
    private int listingsCount;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long listingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM car_listings", Long.class);
        if (listingCount != null && listingCount > 0) {
            log.info("Skipping seed because car_listings already contains {} records", listingCount);
            return;
        }

        Faker faker = new Faker(new Locale("en"));

        seedUsers(faker);
        seedMakesAndModels();
        seedListings(faker, listingsCount);

        log.info("CarDataSeeder completed successfully with {} listings", listingsCount);
    }

    private void seedUsers(Faker faker) {
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        if (userCount != null && userCount > 0) {
            return;
        }

        for (int i = 1; i <= 12; i++) {
            String role = i <= 8 ? "SELLER" : "DEALER";
            String email = "seller" + i + "@automarket.dev";
            String phone = "+1555000" + String.format("%04d", i);

            jdbcTemplate.update("""
                    INSERT INTO users (
                      email, phone, full_name, auth_provider, email_verified,
                      role, is_verified_seller, seller_rating, total_listings,
                      total_sales, bio, location
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (email) DO NOTHING
                    """,
                email,
                phone,
                faker.name().fullName(),
                "EMAIL",
                true,
                role,
                i % 3 == 0,
                BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(3.8, 5.0)).setScale(2, RoundingMode.HALF_UP),
                ThreadLocalRandom.current().nextInt(4, 40),
                ThreadLocalRandom.current().nextInt(2, 25),
                "Trusted seller focused on transparent vehicle history and fair pricing.",
                faker.address().cityName()
            );
        }
    }

    private void seedMakesAndModels() {
        for (Map.Entry<String, List<String>> entry : MAKE_MODELS.entrySet()) {
            String make = entry.getKey();

            jdbcTemplate.update("""
                    INSERT INTO car_makes (name, country)
                    VALUES (?, ?)
                    ON CONFLICT (name) DO NOTHING
                    """, make, countryForMake(make));

            UUID makeId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_makes WHERE name = ?",
                (rs, rowNum) -> UUID.fromString(rs.getString("id")),
                make
            );

            if (makeId == null) {
                continue;
            }

            for (String model : entry.getValue()) {
                jdbcTemplate.update("""
                        INSERT INTO car_models (make_id, name)
                        VALUES (?, ?)
                        ON CONFLICT (make_id, name) DO NOTHING
                        """, makeId, model);
            }
        }
    }

    private void seedListings(Faker faker, int totalListings) {
        List<UUID> sellerIds = jdbcTemplate.query(
            "SELECT id FROM users WHERE role IN ('SELLER', 'DEALER') ORDER BY created_at",
            (rs, rowNum) -> UUID.fromString(rs.getString("id"))
        );

        Map<String, UUID> makeIds = jdbcTemplate.query("SELECT id, name FROM car_makes", rs -> {
            Map<String, UUID> map = new LinkedHashMap<>();
            while (rs.next()) {
                map.put(rs.getString("name"), UUID.fromString(rs.getString("id")));
            }
            return map;
        });

        Map<String, UUID> modelIds = jdbcTemplate.query(
            """
                SELECT cm.id, cm.name, mk.name AS make_name
                FROM car_models cm
                JOIN car_makes mk ON mk.id = cm.make_id
                """,
            rs -> {
                Map<String, UUID> map = new LinkedHashMap<>();
                while (rs.next()) {
                    map.put(rs.getString("make_name") + "::" + rs.getString("name"), UUID.fromString(rs.getString("id")));
                }
                return map;
            }
        );

        if (sellerIds.isEmpty()) {
            throw new IllegalStateException("No seller users found. Unable to seed listings.");
        }

        List<String> makes = new ArrayList<>(MAKE_MODELS.keySet());

        for (int i = 0; i < totalListings; i++) {
            String make = randomOf(makes);
            String model = randomOf(MAKE_MODELS.get(make));
            int year = ThreadLocalRandom.current().nextInt(2015, 2025);
            String bodyType = weightedBodyType();
            String condition = CONDITIONS[year >= 2023 ? ThreadLocalRandom.current().nextInt(0, 2) : ThreadLocalRandom.current().nextInt(0, CONDITIONS.length)];
            String fuelType = make.equals("Tesla") ? "ELECTRIC" : randomOf(Arrays.asList(FUEL_TYPES));
            String transmission = make.equals("Tesla") ? "AUTOMATIC" : randomOf(Arrays.asList(TRANSMISSIONS));
            String driveType = randomOf(Arrays.asList(DRIVE_TYPES));

            String city = faker.address().cityName();
            String state = randomOf(Arrays.asList(STATES));
            String color = faker.color().name();
            int mileage = condition.equals("NEW") ? ThreadLocalRandom.current().nextInt(500, 12000) : ThreadLocalRandom.current().nextInt(8000, 150000);
            BigDecimal price = priceForSegment(make, bodyType, condition, year);
            int engineCc = fuelType.equals("ELECTRIC") ? 0 : ThreadLocalRandom.current().nextInt(999, 4200);
            int power = fuelType.equals("ELECTRIC") ? ThreadLocalRandom.current().nextInt(180, 600) : ThreadLocalRandom.current().nextInt(70, 520);
            int torque = fuelType.equals("ELECTRIC") ? ThreadLocalRandom.current().nextInt(220, 900) : ThreadLocalRandom.current().nextInt(110, 780);
            int seats = bodyType.equals("TRUCK") || bodyType.equals("VAN") ? ThreadLocalRandom.current().nextInt(2, 8) : ThreadLocalRandom.current().nextInt(4, 8);

            String variant = variantFor(model);
            String title = year + " " + make + " " + model + " " + variant;
            String slug = toSlug(title + " " + city + " " + (i + 1));

            List<String> pickedFeatures = pickFeatures();
            String featuresArray = pickedFeatures.stream()
                .map(f -> "\"" + f.replace("\"", "") + "\"")
                .collect(Collectors.joining(",", "{", "}"));

            UUID listingId = UUID.randomUUID();
            UUID makeId = makeIds.get(make);
            UUID modelId = modelIds.get(make + "::" + model);
            UUID sellerId = randomOf(sellerIds);

            jdbcTemplate.update("""
                    INSERT INTO car_listings (
                      id, seller_id, title, slug, make_id, model_id, year, variant, price,
                      is_negotiable, mileage, fuel_type, transmission, drive_type, engine_cc,
                      power_bhp, torque_nm, seats, color, condition, body_type, ownership_count,
                      insurance_valid, insurance_expiry, registration_year, registration_state, vin,
                      description, features, location_city, location_state, location_lat, location_lng,
                      status, is_featured, is_verified, view_count, inquiry_count, expires_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                              CURRENT_DATE + INTERVAL '1 year', ?, ?, ?, ?, ?::text[], ?, ?, ?, ?,
                              'ACTIVE', ?, ?, ?, ?, NOW() + INTERVAL '60 day')
                    """,
                listingId,
                sellerId,
                title,
                slug,
                makeId,
                modelId,
                year,
                variant,
                price,
                ThreadLocalRandom.current().nextBoolean(),
                mileage,
                fuelType,
                transmission,
                driveType,
                engineCc,
                power,
                torque,
                seats,
                color,
                condition,
                bodyType,
                ThreadLocalRandom.current().nextInt(1, 4),
                ThreadLocalRandom.current().nextBoolean(),
                year,
                state,
                faker.vehicle().vin(),
                buildDescription(make, model, condition, mileage, fuelType),
                featuresArray,
                city,
                state,
                Double.valueOf(faker.address().latitude()),
                Double.valueOf(faker.address().longitude()),
                ThreadLocalRandom.current().nextInt(0, 10) > 7,
                ThreadLocalRandom.current().nextInt(0, 10) > 5,
                ThreadLocalRandom.current().nextInt(20, 800),
                ThreadLocalRandom.current().nextInt(1, 60)
            );

            List<String> listingImages = BODY_IMAGES.getOrDefault(bodyType, BODY_IMAGES.get("SEDAN"));
            for (int imageOrder = 0; imageOrder < listingImages.size(); imageOrder++) {
                String imageUrl = listingImages.get(imageOrder);
                String angle = switch (imageOrder) {
                    case 0 -> "FRONT";
                    case 1 -> "SIDE";
                    default -> "INTERIOR";
                };

                jdbcTemplate.update("""
                        INSERT INTO car_images (listing_id, url, thumbnail_url, is_primary, display_order, angle)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                    listingId,
                    imageUrl,
                    imageUrl + "&w=480",
                    imageOrder == 0,
                    imageOrder,
                    angle
                );
            }
        }
    }

    private String weightedBodyType() {
        int pick = ThreadLocalRandom.current().nextInt(100);
        if (pick < 30) return "SEDAN";
        if (pick < 65) return "SUV";
        if (pick < 85) return "HATCHBACK";
        return randomOf(List.of("COUPE", "TRUCK", "VAN", "CONVERTIBLE"));
    }

    private BigDecimal priceForSegment(String make, String bodyType, String condition, int year) {
        int min = 3000;
        int max = 85000;

        if (List.of("BMW", "Mercedes-Benz", "Audi", "Tesla").contains(make)) {
            min = 25000;
            max = 85000;
        } else if (bodyType.equals("SUV") || bodyType.equals("TRUCK")) {
            min = 12000;
            max = 65000;
        } else if (bodyType.equals("HATCHBACK")) {
            min = 3000;
            max = 28000;
        }

        if (condition.equals("NEW")) {
            min += 7000;
            max += 5000;
        }

        if (year <= 2018) {
            max = Math.max(min + 5000, max - 12000);
        }

        return BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(min, max + 1));
    }

    private String buildDescription(String make, String model, String condition, int mileage, String fuelType) {
        return """
            Well maintained %s %s in %s condition. %s mileage and full inspection completed.
            Smooth %s performance, clean interior, and ready for immediate transfer.
            """.formatted(make, model, condition.toLowerCase(), mileage + " km", fuelType.toLowerCase());
    }

    private String variantFor(String model) {
        String[] trims = {"Base", "Sport", "Premium", "Limited", "ZX", "GT", "Signature"};
        return model + " " + trims[ThreadLocalRandom.current().nextInt(trims.length)];
    }

    private List<String> pickFeatures() {
        List<String> pool = new ArrayList<>(FEATURE_POOL);
        int count = ThreadLocalRandom.current().nextInt(4, 9);
        List<String> picked = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if (pool.isEmpty()) {
                break;
            }
            String next = pool.remove(ThreadLocalRandom.current().nextInt(pool.size()));
            picked.add(next);
        }

        return picked;
    }

    private String countryForMake(String make) {
        return switch (make) {
            case "Toyota", "Honda", "Nissan" -> "Japan";
            case "Ford", "Tesla", "Chevrolet", "Jeep" -> "USA";
            case "BMW", "Mercedes-Benz", "Audi", "Volkswagen" -> "Germany";
            case "Hyundai", "Kia" -> "South Korea";
            default -> "Global";
        };
    }

    private <T> T randomOf(List<T> options) {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    private String toSlug(String value) {
        return value.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }
}

