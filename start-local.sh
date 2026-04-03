#!/usr/bin/env bash
# start-local.sh — AutoVault local development startup helper
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ALL_OK=true

echo ""
echo "══════════════════════════════════════════════"
echo "  AutoVault — Local Service Check"
echo "══════════════════════════════════════════════"
echo ""

# ── 1. Check PostgreSQL ─────────────────────────────
printf "Checking PostgreSQL ... "
if pg_isready -q 2>/dev/null; then
  echo -e "${GREEN}RUNNING${NC}"
else
  echo -e "${RED}NOT RUNNING${NC}"
  echo -e "${YELLOW}  → macOS:  brew services start postgresql@16${NC}"
  echo -e "${YELLOW}  → Linux:  sudo systemctl start postgresql${NC}"
  echo -e "${YELLOW}  → Windows: net start postgresql-x64-16${NC}"
  ALL_OK=false
fi

# ── 2. Check Redis ──────────────────────────────────
printf "Checking Redis ...      "
if redis-cli ping 2>/dev/null | grep -q PONG; then
  echo -e "${GREEN}RUNNING${NC}"
else
  echo -e "${RED}NOT RUNNING${NC}"
  echo -e "${YELLOW}  → macOS:  brew services start redis${NC}"
  echo -e "${YELLOW}  → Linux:  sudo systemctl start redis${NC}"
  ALL_OK=false
fi

# ── 3. Check Kafka (port 9092) ──────────────────────
printf "Checking Kafka ...      "
if (echo > /dev/tcp/localhost/9092) 2>/dev/null; then
  echo -e "${GREEN}RUNNING${NC}"
else
  echo -e "${RED}NOT RUNNING${NC}"
  echo -e "${YELLOW}  → Start Kafka in KRaft mode:${NC}"
  echo -e "${YELLOW}    cd /path/to/kafka${NC}"
  echo -e "${YELLOW}    bin/kafka-server-start.sh config/kraft/server.properties${NC}"
  ALL_OK=false
fi

echo ""

# ── 4. Start backend if all services are up ─────────
if [ "$ALL_OK" = true ]; then
  echo -e "${GREEN}All services are running!${NC}"
  echo ""
  echo "Starting backend (mvn spring-boot:run) ..."
  echo ""
  echo -e "${YELLOW}TIP: In a second terminal, start the frontend:${NC}"
  echo -e "${YELLOW}  cd frontend && npm install && npm run dev${NC}"
  echo ""
  mvn spring-boot:run
else
  echo -e "${RED}Some services are not running. Please start them first.${NC}"
  exit 1
fi
