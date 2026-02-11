#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "🚀 CIRCUIT BREAKER TEST - SETUP"
echo "=========================================="

echo -e "\n${BLUE}1️⃣  Checking requirements...${NC}"
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ docker-compose not found!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ docker-compose found${NC}"

if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  jq not found (recommended)${NC}"
    echo "   Install with: sudo apt-get install jq"
else
    echo -e "${GREEN}✓ jq found${NC}"
fi

echo -e "\n${BLUE}2️⃣  Starting containers (nginx and toxiproxy)...${NC}"
docker-compose --profile testing up -d nginx-proxy toxiproxy

echo -e "${YELLOW}⏳ Waiting for containers to start...${NC}"
sleep 5

echo -e "\n${BLUE}3️⃣  Checking container status...${NC}"
docker-compose ps nginx-proxy toxiproxy

echo -e "\n${BLUE}4️⃣  Configuring Toxiproxy...${NC}"

EXISTING=$(curl -s http://localhost:8474/proxies | grep -c "openmeteo" || echo "0")

if [ "$EXISTING" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Proxy 'openmeteo' already exists, deleting...${NC}"
    ./scripts/toxiproxy-helper.sh delete openmeteo
fi

echo -e "${YELLOW}📡 Creating proxy 'openmeteo'...${NC}"
./scripts/toxiproxy-helper.sh create openmeteo 0.0.0.0:20001 agrowerk-nginx-proxy:20000

echo -e "\n${BLUE}5️⃣  Configured proxies:${NC}"
./scripts/toxiproxy-helper.sh list

echo -e "\n${BLUE}6️⃣  Testing nginx (connection to Open Meteo API)...${NC}"
if curl -s --max-time 5 "http://localhost:20000/v1/forecast?latitude=-23.55&longitude=-46.63&current=temperature_2m" > /dev/null; then
    echo -e "${GREEN}✓ Nginx working correctly${NC}"
else
    echo -e "${RED}❌ Nginx has issues${NC}"
    echo "   Check logs: docker-compose logs nginx-proxy"
fi

echo -e "\n${BLUE}7️⃣  Testing Toxiproxy...${NC}"
if curl -s --max-time 5 "http://localhost:20001/v1/forecast?latitude=-23.55&longitude=-46.63&current=temperature_2m" > /dev/null; then
    echo -e "${GREEN}✓ Toxiproxy working correctly${NC}"
else
    echo -e "${RED}❌ Toxiproxy has issues${NC}"
    echo "   Check logs: docker-compose logs toxiproxy"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}✅ SETUP COMPLETED!${NC}"
echo "=========================================="
echo ""
echo "📝 Next steps:"
echo ""
echo "1. Configure application.yml:"
echo "   openmeteo.api.base-url=http://localhost:20001/v1/forecast"
echo ""
echo "2. Start your Spring Boot application"
echo ""
echo "3. Run the test:"
echo "   ./scripts/test-circuit-breaker.sh"
echo ""
echo "💡 Useful commands:"
echo "   ./scripts/toxiproxy-helper.sh list              - List proxies"
echo "   ./scripts/toxiproxy-helper.sh toxic-list openmeteo - View active toxics"
echo "   docker-compose logs -f nginx-proxy      - View nginx logs"
echo "   docker-compose logs -f toxiproxy        - View toxiproxy logs"
echo ""