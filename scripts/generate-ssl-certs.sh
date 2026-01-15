#!/bin/bash

# HR-IoT SCADA - SSL Certificate Generation Script
# This script generates self-signed SSL certificates for development

set -e

echo "=========================================="
echo "HR-IoT SCADA - SSL Certificate Generation"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Create SSL directory if it doesn't exist
mkdir -p nginx/ssl

# Check if certificates already exist
if [ -f "nginx/ssl/hr-iot.crt" ] && [ -f "nginx/ssl/hr-iot.key" ]; then
    echo -e "${YELLOW}⚠ SSL certificates already exist${NC}"
    echo ""
    read -p "Do you want to regenerate them? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Keeping existing certificates"
        exit 0
    fi
fi

echo "Generating self-signed SSL certificate..."
echo ""

# Generate private key and certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/hr-iot.key \
    -out nginx/ssl/hr-iot.crt \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=HR-IoT/OU=IT/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,DNS:*.localhost,IP:127.0.0.1"

# Set appropriate permissions
chmod 600 nginx/ssl/hr-iot.key
chmod 644 nginx/ssl/hr-iot.crt

echo ""
echo -e "${GREEN}✓ SSL certificates generated successfully${NC}"
echo ""
echo "Certificate details:"
echo "  - Certificate: nginx/ssl/hr-iot.crt"
echo "  - Private Key: nginx/ssl/hr-iot.key"
echo "  - Valid for: 365 days"
echo "  - Common Name: localhost"
echo ""
echo -e "${YELLOW}⚠ Note: This is a self-signed certificate for development only${NC}"
echo "   Your browser will show a security warning. This is expected."
echo "   For production, use a certificate from a trusted CA (e.g., Let's Encrypt)"
echo ""
echo "=========================================="
