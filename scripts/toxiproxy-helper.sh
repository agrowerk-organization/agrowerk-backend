#!/bin/bash

TOXIPROXY_URL="http://localhost:8474"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

case "$1" in
    "create")
        NAME=$2
        LISTEN=$3
        UPSTREAM=$4

        echo -e "${YELLOW}Creating proxy '$NAME'...${NC}"
        curl -X POST "$TOXIPROXY_URL/proxies" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"$NAME\",
                \"listen\": \"$LISTEN\",
                \"upstream\": \"$UPSTREAM\",
                \"enabled\": true
            }" | jq
        ;;

    "list")
        echo -e "${YELLOW}Listing proxies...${NC}"
        curl -s "$TOXIPROXY_URL/proxies" | jq
        ;;

    "delete")
        NAME=$2
        echo -e "${YELLOW}Deleting proxy '$NAME'...${NC}"
        curl -X DELETE "$TOXIPROXY_URL/proxies/$NAME" | jq
        ;;

    "toxic-add-latency")
        PROXY=$2
        LATENCY=${3:-5000}

        echo -e "${YELLOW}Adding ${LATENCY}ms latency to proxy '$PROXY'...${NC}"
        curl -X POST "$TOXIPROXY_URL/proxies/$PROXY/toxics" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"latency_toxic\",
                \"type\": \"latency\",
                \"attributes\": {
                    \"latency\": $LATENCY
                }
            }" | jq
        ;;

    "toxic-add-down")
        PROXY=$2

        echo -e "${YELLOW}Bringing down connection for proxy '$PROXY'...${NC}"
        curl -X POST "$TOXIPROXY_URL/proxies/$PROXY/toxics" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"down_toxic\",
                \"type\": \"down\",
                \"attributes\": {}
            }" | jq
        ;;

    "toxic-add-timeout")
        PROXY=$2
        TIMEOUT=${3:-10000}

        echo -e "${YELLOW}Adding ${TIMEOUT}ms timeout to proxy '$PROXY'...${NC}"
        curl -X POST "$TOXIPROXY_URL/proxies/$PROXY/toxics" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"timeout_toxic\",
                \"type\": \"timeout\",
                \"attributes\": {
                    \"timeout\": $TIMEOUT
                }
            }" | jq
        ;;

    "toxic-list")
        PROXY=$2
        echo -e "${YELLOW}Listing toxics for proxy '$PROXY'...${NC}"
        curl -s "$TOXIPROXY_URL/proxies/$PROXY/toxics" | jq
        ;;

    "toxic-remove")
        PROXY=$2
        TOXIC=$3

        echo -e "${YELLOW}Removing toxic '$TOXIC' from proxy '$PROXY'...${NC}"
        curl -X DELETE "$TOXIPROXY_URL/proxies/$PROXY/toxics/$TOXIC" | jq
        ;;

    "toxic-remove-all")
        PROXY=$2
        echo -e "${YELLOW}Removing all toxics from proxy '$PROXY'...${NC}"

        TOXICS=$(curl -s "$TOXIPROXY_URL/proxies/$PROXY/toxics" | jq -r '.[].name')

        for toxic in $TOXICS; do
            echo "  Removing $toxic..."
            curl -X DELETE "$TOXIPROXY_URL/proxies/$PROXY/toxics/$toxic" 2>/dev/null
        done

        echo -e "${GREEN}✓ All toxics removed${NC}"
        ;;

    "reset")
        echo -e "${YELLOW}Resetting Toxiproxy...${NC}"
        curl -X POST "$TOXIPROXY_URL/reset" | jq
        ;;

    *)
        echo "Toxiproxy Helper - REST API Management"
        echo ""
        echo "Usage: $0 <command> [arguments]"
        echo ""
        echo "Commands:"
        echo "  create <n> <listen> <upstream>   - Create proxy"
        echo "  list                                - List proxies"
        echo "  delete <n>                       - Delete proxy"
        echo ""
        echo "  toxic-add-latency <proxy> [ms]      - Add latency (default: 5000ms)"
        echo "  toxic-add-down <proxy>              - Bring connection down"
        echo "  toxic-add-timeout <proxy> [ms]      - Add timeout (default: 10000ms)"
        echo "  toxic-list <proxy>                  - List toxics"
        echo "  toxic-remove <proxy> <toxic-name>   - Remove specific toxic"
        echo "  toxic-remove-all <proxy>            - Remove all toxics"
        echo ""
        echo "  reset                               - Reset everything"
        echo ""
        echo "Examples:"
        echo "  $0 create openmeteo 0.0.0.0:20001 agrowerk-nginx-proxy:20000"
        echo "  $0 list"
        echo "  $0 toxic-add-latency openmeteo 10000"
        echo "  $0 toxic-remove-all openmeteo"
        ;;
esac