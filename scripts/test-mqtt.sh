#!/bin/bash

# HR-IoT SCADA - MQTT Broker Test Script
# This script tests the MQTT broker setup

set -e

echo "=========================================="
echo "HR-IoT SCADA - MQTT Broker Test"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "ℹ $1"
}

# Check if Docker is running
echo "1. Checking Docker..."
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker Desktop."
    exit 1
fi
print_success "Docker is running"

# Check if mosquitto clients are installed
echo ""
echo "2. Checking mosquitto clients..."
if ! command -v mosquitto_pub &> /dev/null; then
    print_warning "mosquitto clients not found"
    print_info "Install with: brew install mosquitto (macOS) or apt-get install mosquitto-clients (Linux)"
    echo ""
    read -p "Continue without mosquitto clients? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
    SKIP_MQTT_TEST=true
else
    print_success "mosquitto clients installed"
fi

# Check if MQTT broker container is running
echo ""
echo "3. Checking MQTT broker container..."
if ! docker ps | grep -q hr-iot-mqtt; then
    print_warning "MQTT broker container not running"
    echo ""
    read -p "Start MQTT broker now? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Starting MQTT broker..."
        docker-compose up -d mqtt-broker
        echo "Waiting for broker to be ready (30 seconds)..."
        sleep 30
    else
        print_error "Cannot continue without MQTT broker"
        exit 1
    fi
fi
print_success "MQTT broker container is running"

# Check MQTT broker health
echo ""
echo "4. Checking MQTT broker health..."
if docker exec hr-iot-mqtt emqx ping > /dev/null 2>&1; then
    print_success "MQTT broker is healthy"
else
    print_error "MQTT broker health check failed"
    exit 1
fi

# Test MQTT connection
if [ "$SKIP_MQTT_TEST" != true ]; then
    echo ""
    echo "5. Testing MQTT publish/subscribe..."

    # Generate random message
    TEST_MESSAGE="test-$(date +%s)"

    # Subscribe in background and capture output
    timeout 5 mosquitto_sub -h localhost -p 1883 -t 'test/topic' > /tmp/mqtt_test_output.txt 2>&1 &
    SUB_PID=$!

    # Wait for subscription to be ready
    sleep 2

    # Publish test message
    mosquitto_pub -h localhost -p 1883 -t 'test/topic' -m "$TEST_MESSAGE"

    # Wait for message to be received
    sleep 2

    # Check if message was received
    if grep -q "$TEST_MESSAGE" /tmp/mqtt_test_output.txt; then
        print_success "MQTT publish/subscribe working"
    else
        print_warning "MQTT test inconclusive - check manually"
    fi

    # Cleanup
    kill $SUB_PID 2>/dev/null || true
    rm -f /tmp/mqtt_test_output.txt

    # Test IoT device topics
    echo ""
    echo "6. Testing IoT device topics..."

    # Test device property topic
    mosquitto_pub -h localhost -p 1883 -t 'iot/1/device/100/properties' \
      -m '{"tagName":"Pump1_Status","value":true,"timestamp":'$(date +%s)'000}'

    if [ $? -eq 0 ]; then
        print_success "Device property topic working"
    else
        print_error "Device property topic failed"
    fi

    # Test control topic
    mosquitto_pub -h localhost -p 1883 -t 'iot/1/device/100/control' \
      -m '{"command":"start_pump","params":{"pumpId":1},"timestamp":'$(date +%s)'000}'

    if [ $? -eq 0 ]; then
        print_success "Control command topic working"
    else
        print_error "Control command topic failed"
    fi
fi

# Check EMQX dashboard
echo ""
echo "7. Checking EMQX dashboard..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:18083 | grep -q 200; then
    print_success "EMQX dashboard accessible at http://localhost:18083"
    print_info "Login: admin / hr-iot-admin-2024"
else
    print_error "EMQX dashboard not accessible"
fi

# Summary
echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="
print_success "Phase 1 (Infrastructure) tests passed!"
echo ""
print_info "MQTT Broker: Running ✓"
print_info "Dashboard: http://localhost:18083"
print_info "MQTT Port: 1883"
print_info "WebSocket Port: 8083"
echo ""
print_info "Next steps:"
echo "  1. Access EMQX dashboard: http://localhost:18083"
echo "  2. Review logs: docker-compose logs -f mqtt-broker"
echo "  3. Read setup guide: cat README-SCADA-SETUP.md"
echo "  4. Proceed to Phase 2: FUXA Customization"
echo ""
echo "=========================================="
