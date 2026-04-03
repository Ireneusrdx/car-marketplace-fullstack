# AutoVault — Local Development Quick Start

## Prerequisites

Make sure you have these installed locally:

- **Java 17+** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **Node.js 20+** — `node -v`
- **PostgreSQL 16** — `psql --version`
- **Redis 7** — `redis-cli --version`
- **Apache Kafka 3.7+** (KRaft mode, no Zookeeper)

---

## Step 1: Install & Start PostgreSQL

### macOS
```bash
brew install postgresql@16
brew services start postgresql@16
createdb autovault
```

### Linux (Debian/Ubuntu)
```bash
sudo apt update && sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo -u postgres createdb autovault
```

### Windows
Download from https://www.postgresql.org/download/windows/, install, then:
```powershell
psql -U postgres -c "CREATE DATABASE autovault;"
```

Verify: `psql -U postgres -d autovault -c "SELECT 1;"`

---

## Step 2: Install & Start Redis

### macOS
```bash
brew install redis
brew services start redis
```

### Linux
```bash
sudo apt install redis-server
sudo systemctl start redis
```

### Windows
Install via WSL2, or download from https://github.com/microsoftarchive/redis/releases

Verify: `redis-cli ping` → should print `PONG`

---

## Step 3: Install & Start Kafka (KRaft Mode)

1. Download Kafka from https://kafka.apache.org/downloads (binary, not source)
2. Extract and run in KRaft mode (no Zookeeper):

```bash
cd /path/to/kafka

# Generate a cluster ID and format storage
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties

# Start Kafka
bin/kafka-server-start.sh config/kraft/server.properties
```

Verify: `bin/kafka-topics.sh --bootstrap-server localhost:9092 --list`

---

## Step 4: Configure Environment Variables

Edit the `.env` file in the project root:

```bash
# Database
DB_HOST=localhost
DB_URL=jdbc:postgresql://localhost:5432/autovault
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_BROKER=localhost:9092
```

Fill in your real API keys for Stripe, OpenAI, Cloudinary, and Firebase.

Also configure the frontend env in `frontend/.env.local`:
```bash
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
VITE_FIREBASE_API_KEY=...
VITE_CLOUDINARY_CLOUD_NAME=...
```

---

## Step 5: Start the Backend

```bash
cd /path/to/project
mvn spring-boot:run
```

The backend will:
- Run Flyway migrations automatically
- Seed 50 demo listings on first start (`CarDataSeeder`)
- Expose API at http://localhost:8080
- Swagger UI at http://localhost:8080/swagger-ui.html

---

## Step 6: Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 in your browser.

---

## Step 7: Stripe Webhooks (Optional — for payments)

Install the Stripe CLI, then forward webhooks to your local backend:

```bash
stripe listen --forward-to localhost:8080/api/webhooks/stripe
```

Copy the `whsec_...` signing secret into your `.env` as `STRIPE_WEBHOOK_SECRET`.

---

## Quick Reference

| Service      | URL / Port                              |
|--------------|-----------------------------------------|
| Frontend     | http://localhost:5173                   |
| Backend API  | http://localhost:8080/api               |
| Swagger UI   | http://localhost:8080/swagger-ui.html   |
| PostgreSQL   | localhost:5432                          |
| Redis        | localhost:6379                          |
| Kafka        | localhost:9092                          |

---

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running: `psql -U postgres -d autovault -c "SELECT 1;"`
- Check Redis is running: `redis-cli ping`
- Check port 8080 isn't in use: `netstat -ano | findstr :8080` (Windows) or `lsof -i :8080` (macOS/Linux)

### Frontend shows blank page
- Clear browser cache: Ctrl+Shift+Delete
- Check browser console for errors (F12)
- Restart dev server: Ctrl+C then `npm run dev`

### Maven build fails
- Check Java version: `java -version` (must be 17+)
- Clear cache: `mvn clean`
- Rebuild: `mvn clean install -DskipTests`

### Kafka connection refused
- Ensure Kafka is running in KRaft mode on port 9092
- Check logs in the Kafka terminal for errors

---

## Useful Commands

```bash
# Frontend
npm run dev          # Start Vite dev server
npm run build        # Production build
npm run preview      # Preview production build

# Backend
mvn spring-boot:run  # Start Spring Boot
mvn clean compile    # Compile only
mvn clean package    # Build JAR
mvn clean test       # Run tests

# Database
psql -U postgres -d autovault   # Connect to DB

# Convenience
./start-local.sh     # Check services & start backend
```

