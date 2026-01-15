# HR-IoT SCADA System Setup Guide

## Phase 1: Infrastructure Setup - MQTT Broker

This guide helps you set up the MQTT broker (EMQX) for the SCADA system.

---

## Prerequisites

- Docker Desktop installed and running
- Ports available: 1883, 8083, 8084, 18083
- At least 2GB RAM available for containers

---

## Quick Start

### Step 1: Start MQTT Broker

```bash
# Start the MQTT broker
docker-compose up -d mqtt-broker

# Check logs
docker-compose logs -f mqtt-broker

# Wait for "EMQX 5.0 is running now!" message
```

### Step 2: Verify MQTT Broker

**Access Management Dashboard**:
- URL: http://localhost:18083
- Username: `admin`
- Password: `hr-iot-admin-2024`

**Test MQTT Connection**:
```bash
# Install mosquitto clients (if not already installed)
# macOS
brew install mosquitto

# Ubuntu/Debian
sudo apt-get install mosquitto-clients

# Windows - Download from https://mosquitto.org/download/

# Test Publish/Subscribe
# Terminal 1: Subscribe
mosquitto_sub -h localhost -p 1883 -t 'test/topic' -v

# Terminal 2: Publish
mosquitto_pub -h localhost -p 1883 -t 'test/topic' -m 'Hello SCADA!'

# You should see "Hello SCADA!" in Terminal 1
```

### Step 3: Test IoT Device Topics

**Simulate HR-IoT Device Publishing**:
```bash
# Subscribe to device property updates (Terminal 1)
mosquitto_sub -h localhost -p 1883 -t 'iot/1/device/+/properties' -v

# Publish device property update (Terminal 2)
mosquitto_pub -h localhost -p 1883 -t 'iot/1/device/100/properties' \
  -m '{"tagName":"Pump1_Status","value":true,"timestamp":'$(date +%s)'000}'

# Publish multiple properties (Terminal 2)
mosquitto_pub -h localhost -p 1883 -t 'iot/1/device/100/properties' \
  -m '{"tags":[{"name":"Pressure_Inlet","value":52.0,"unit":"Mpa"},{"name":"Temperature_Inlet","value":61.7,"unit":"℃"}],"timestamp":'$(date +%s)'000}'
```

**Test Control Commands**:
```bash
# Subscribe to control commands (Terminal 1 - simulating backend)
mosquitto_sub -h localhost -p 1883 -t 'iot/+/device/+/control' -v

# Publish control command (Terminal 2 - simulating FUXA)
mosquitto_pub -h localhost -p 1883 -t 'iot/1/device/100/control' \
  -m '{"command":"start_pump","params":{"pumpId":1},"timestamp":'$(date +%s)'000}'
```

---

## MQTT Topic Structure

### Topic Naming Convention

```
iot/{tenant_id}/device/{device_id}/properties    # Real-time device properties
iot/{tenant_id}/device/{device_id}/events        # Device events (alarms, state changes)
iot/{tenant_id}/device/{device_id}/control       # Control commands to device
iot/{tenant_id}/alarms                           # Tenant-wide alarms
```

### Message Formats

**Property Update**:
```json
{
  "tagName": "Pump1_Status",
  "value": true,
  "timestamp": 1704268800000
}
```

**Multiple Properties**:
```json
{
  "tags": [
    {"name": "Pressure_Inlet", "value": 52.0, "unit": "Mpa"},
    {"name": "Temperature_Inlet", "value": 61.7, "unit": "℃"},
    {"name": "Flow_Rate", "value": 54.8, "unit": "m³/h"}
  ],
  "timestamp": 1704268800000
}
```

**Control Command**:
```json
{
  "command": "start_pump",
  "params": {
    "pumpId": 1,
    "mode": "auto"
  },
  "userId": 123,
  "timestamp": 1704268800000
}
```

---

## EMQX Dashboard Guide

### Access Dashboard
- URL: http://localhost:18083
- Default credentials: admin / hr-iot-admin-2024

### Key Features

**1. Clients**:
- View connected MQTT clients
- Monitor client connections/disconnections
- Check client subscriptions

**2. Topics**:
- Browse active topics
- See topic subscription counts
- Monitor message rates

**3. Subscriptions**:
- View all active subscriptions
- Filter by client or topic

**4. Metrics**:
- Messages sent/received
- Connection statistics
- System performance

**5. ACL (Access Control)**:
- Manage topic permissions
- Configure user access rules

---

## Troubleshooting

### Issue: MQTT broker won't start

**Check port conflicts**:
```bash
# Check if ports are in use
lsof -i :1883
lsof -i :8083
lsof -i :18083

# Kill conflicting process if needed
kill -9 <PID>

# Restart broker
docker-compose restart mqtt-broker
```

### Issue: Cannot connect to MQTT

**Check broker status**:
```bash
# View logs
docker-compose logs mqtt-broker

# Check if container is running
docker ps | grep mqtt

# Restart if needed
docker-compose restart mqtt-broker
```

**Test with mosquitto_pub**:
```bash
# Test connection
mosquitto_pub -h localhost -p 1883 -t 'test' -m 'test' -d

# If fails, check:
# 1. Docker container running
# 2. Firewall not blocking ports
# 3. Docker network configuration
```

### Issue: WebSocket connection fails

**Test WebSocket**:
```bash
# Using websocat (install: cargo install websocat)
websocat ws://localhost:8083/mqtt

# Or use online MQTT WebSocket client:
# http://www.hivemq.com/demos/websocket-client/
```

---

## Performance Tuning

### EMQX Configuration

**For development** (current setup):
- Max connections: 10,000
- Max topics: Unlimited
- Message retention: No persistence

**For production** (update in docker-compose.yml):
```yaml
environment:
  # Increase max connections
  - EMQX_LISTENERS__TCP__DEFAULT__MAX_CONNECTIONS=1000000

  # Enable persistence
  - EMQX_RETAINER__ENABLE=true

  # Set message TTL
  - EMQX_RETAINER__MSG_EXPIRY_INTERVAL=3600s
```

---

## Security Hardening (Production)

### 1. Disable Anonymous Access

Update `docker-compose.yml`:
```yaml
environment:
  - EMQX_ALLOW_ANONYMOUS=false
```

### 2. Enable Authentication

**Using built-in database**:
```bash
# Access EMQX CLI
docker exec -it hr-iot-mqtt emqx_ctl

# Add user
./emqx_ctl users add <username> <password>
```

**Using MySQL authentication** (recommended):
See Phase 3 backend integration for MySQL auth setup.

### 3. Enable TLS/SSL

Add SSL certificates:
```yaml
volumes:
  - ./emqx/ssl:/opt/emqx/etc/certs
environment:
  - EMQX_LISTENERS__SSL__DEFAULT__ENABLE=true
  - EMQX_LISTENERS__SSL__DEFAULT__BIND=8883
  - EMQX_LISTENERS__SSL__DEFAULT__CERTFILE=/opt/emqx/etc/certs/cert.pem
  - EMQX_LISTENERS__SSL__DEFAULT__KEYFILE=/opt/emqx/etc/certs/key.pem
```

---

## Monitoring

### Resource Usage

```bash
# Check container stats
docker stats hr-iot-mqtt

# Expected usage (idle):
# CPU: < 5%
# Memory: 100-200 MB

# Under load (1000 msg/sec):
# CPU: 10-20%
# Memory: 200-400 MB
```

### Message Metrics

**Via EMQX Dashboard**:
- Metrics → Overview
- View messages/sec, bytes/sec, connections

**Via REST API**:
```bash
# Get metrics
curl -u admin:hr-iot-admin-2024 http://localhost:18083/api/v5/metrics

# Get connections
curl -u admin:hr-iot-admin-2024 http://localhost:18083/api/v5/clients
```

---

## Nginx Reverse Proxy

### Overview

Nginx serves as the unified entry point for all HR-IoT services, providing:
- SSL/TLS termination
- HTTP/2 support
- Service routing
- WebSocket proxying for MQTT
- Security headers

### Access Points

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | https://localhost/ | Vue 3 application |
| Backend API | https://localhost/admin-api/ | Spring Boot REST API |
| MQTT WebSocket | wss://localhost/mqtt | MQTT over secure WebSocket |
| EMQX Dashboard | https://localhost/mqtt-dashboard/ | MQTT broker management |
| Health Check | https://localhost/health | Nginx health endpoint |

### SSL Certificates

**Development (Self-Signed)**:
```bash
# Regenerate certificates
./scripts/generate-ssl-certs.sh

# View certificate details
openssl x509 -in nginx/ssl/hr-iot.crt -text -noout
```

**Browser Warning**: Self-signed certificates will show security warnings. Click "Advanced" → "Proceed to localhost" to bypass.

### Configuration

**Main config**: `nginx/nginx.conf`
**Service routes**: `nginx/conf.d/hr-iot.conf`

Update routing and reload:
```bash
# Test configuration
docker exec hr-iot-nginx nginx -t

# Reload without downtime
docker exec hr-iot-nginx nginx -s reload
```

### Troubleshooting

**502 Bad Gateway**:
- Verify backend service is running
- Check `host.docker.internal` connectivity
- Review nginx error logs: `docker logs hr-iot-nginx`

**Certificate errors**:
- Expected with self-signed certs
- Use `-k` flag with curl for testing: `curl -k https://localhost/health`

For detailed Nginx documentation, see [`nginx/README.md`](./nginx/README.md).

---

## Next Steps

After infrastructure is running:

1. ✅ **Phase 1 Complete**: MQTT broker + Nginx infrastructure ready
2. ⏭️ **Phase 2**: Customize FUXA (see `specs/features/in-progress/scada-visualization/03-tasks.md`)
3. ⏭️ **Phase 3**: Integrate with hr-iot backend

---

## Useful Commands

```bash
# Start all services
docker-compose up -d

# Start only MQTT broker
docker-compose up -d mqtt-broker

# View logs
docker-compose logs -f mqtt-broker

# Restart broker
docker-compose restart mqtt-broker

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Access EMQX shell
docker exec -it hr-iot-mqtt sh

# Check EMQX status
docker exec hr-iot-mqtt emqx ping
```

---

## Documentation

- EMQX Official Docs: https://www.emqx.io/docs/en/v5.0/
- MQTT Protocol: https://mqtt.org/
- Mosquitto Clients: https://mosquitto.org/man/mosquitto_pub-1.html

---

**Last Updated**: 2026-01-03
**Phase**: 1 - Infrastructure Setup
**Status**: MQTT Broker Deployed ✅
