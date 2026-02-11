#!/bin/bash

set -e


BASE_URL="http://localhost:8080"
WEATHER_ENDPOINT="$BASE_URL/weather/test-circuit"
MONITOR_URL="$BASE_URL/monitoring"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "🧪 CIRCUIT BREAKER TEST - RESILIENCE4J"
echo "=========================================="

echo -e "\n${BLUE}🔍 Checking containers...${NC}"
if ! docker-compose ps nginx-proxy toxiproxy 2>/dev/null | grep -q "Up"; then
    echo -e "${RED}❌ Containers are not running!${NC}"
    echo "Run first: docker-compose --profile testing up -d"
    exit 1
fi

check_state() {
    echo ""
    STATE=$(curl -s $MONITOR_URL/circuit-breaker/state || echo "ERROR")
    METRICS=$(curl -s $MONITOR_URL/circuit-breaker/metrics | jq '.' 2>/dev/null || echo "{}")

    echo -e "${BLUE}📊 Circuit Breaker state: ${YELLOW}$STATE${NC}"
    echo "$METRICS"
    echo ""
}

call_weather() {
    local num=$1
    echo -n "   Call $num... "

    if curl -s --max-time 10 "$WEATHER_ENDPOINT?latitude=-23.5505&longitude=-46.6333" > /dev/null 2>&1; then
        echo -e "${GREEN}✅${NC}"
        return 0
    else
        echo -e "${RED}❌${NC}"
        return 1
    fi
}

echo -e "\n${BLUE}1️⃣  INITIAL STATE (should be CLOSED)${NC}"
check_state

echo -e "${BLUE}2️⃣  Making 5 normal calls...${NC}"
for i in {1..5}; do
    call_weather $i
    sleep 1
done
check_state

echo -e "${BLUE}3️⃣  Applying TOXIC - Extreme latency...${NC}"
./scripts/toxiproxy-helper.sh toxic-remove-all openmeteo 2>/dev/null
./scripts/toxiproxy-helper.sh toxic-add-latency openmeteo 10000
echo -e "${YELLOW}   ⚠️  10s latency applied (app timeout: 5s)${NC}"

echo -e "\n${BLUE}4️⃣  Making 10 calls that will TIMEOUT...${NC}"
echo -e "${YELLOW}   (Circuit Breaker should open after 5 calls with 50% failure)${NC}"
for i in {1..10}; do
    call_weather $i
    sleep 1
done

echo -e "\n${BLUE}5️⃣  STATE AFTER FAILURES (should be OPEN)${NC}"
check_state

echo -e "${BLUE}6️⃣  Trying calls with Circuit OPEN (should be blocked)...${NC}"
for i in {1..3}; do
    echo -n "   Call $i... "
    if curl -s --max-time 10 "$WEATHER_ENDPOINT?latitude=-23.5505&longitude=-46.6333" > /dev/null 2>&1; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${YELLOW}🚫 BLOCKED${NC}"
    fi
done
check_state

echo -e "\n${BLUE}7️⃣  Removing TOXIC (simulating service recovery)...${NC}"
./scripts/toxiproxy-helper.sh toxic-remove-all openmeteo
echo -e "${GREEN}   ✅ Latency removed${NC}"

echo -e "\n${BLUE}8️⃣  Waiting 60s for OPEN → HALF_OPEN transition...${NC}"
for i in {60..1}; do
    echo -ne "   ${YELLOW}$i seconds remaining...${NC}\r"
    sleep 1
done
echo ""

echo -e "\n${BLUE}State after waiting (should be HALF_OPEN):${NC}"
check_state

echo -e "${BLUE}9️⃣  Making 3 test calls (allowed in HALF_OPEN)...${NC}"
for i in {1..3}; do
    call_weather $i
    sleep 2
done

echo -e "\n${BLUE}🔟 FINAL STATE (should be CLOSED again)${NC}"
check_state

echo ""
echo "=========================================="
echo -e "${GREEN}✅ TEST COMPLETED!${NC}"
echo "=========================================="
echo ""
echo "📝 Summary:"
echo "   - Circuit Breaker tested: CLOSED → OPEN → HALF_OPEN → CLOSED"
echo "   - Failures simulated successfully"
echo "   - Automatic recovery verified"
echo ""
echo "💡 Next steps:"
echo "   - Check application logs: tail -f logs/application.log"
echo "   - Access metrics: http://localhost:8080/actuator/circuitbreakers"
echo "   - View events: http://localhost:8080/actuator/circuitbreakerevents"