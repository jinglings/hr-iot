# HR-IoT SCADA - Nginx Reverse Proxy Configuration

## Overview

This directory contains the Nginx reverse proxy configuration for the HR-IoT SCADA system. Nginx acts as a unified entry point for all services, providing SSL termination, load balancing, and routing.

## Directory Structure

```
nginx/
├── nginx.conf           # Main Nginx configuration
├── conf.d/
│   └── hr-iot.conf     # HR-IoT service routing configuration
├── ssl/
│   ├── hr-iot.crt      # SSL certificate (self-signed for development)
│   ├── hr-iot.key      # SSL private key
│   └── .gitkeep
└── README.md           # This file
```

## Service Routing

### Port Mapping

| Port | Protocol | Service | Description |
|------|----------|---------|-------------|
| 80 | HTTP | Nginx | Redirects to HTTPS |
| 443 | HTTPS | Nginx | Main entry point |

### URL Routes

| Path | Backend Service | Description |
|------|----------------|-------------|
| `/` | hr-vue3 (port 3000) | Frontend Vue 3 application |
| `/admin-api/` | hr-iot-backend (port 48080) | Backend REST API |
| `/mqtt-dashboard/` | EMQX Dashboard (port 18083) | MQTT broker management |
| `/mqtt` | MQTT WebSocket (port 8083) | MQTT over WebSocket |
| `/health` | Nginx | Health check endpoint |

*Note: `/scada/` route for FUXA will be added in Phase 2*

## SSL Certificates

### Development (Self-Signed)

The included certificates are self-signed and suitable for development only:

```bash
# Generate new certificates
./scripts/generate-ssl-certs.sh

# Certificate details
openssl x509 -in nginx/ssl/hr-iot.crt -text -noout
```

**Important**: Browsers will show security warnings for self-signed certificates. This is expected and can be bypassed during development.

### Production (Let's Encrypt)

For production, use certificates from a trusted CA like Let's Encrypt:

```bash
# Install certbot
brew install certbot  # macOS
apt-get install certbot  # Linux

# Obtain certificate
certbot certonly --webroot -w /var/www/certbot \
  -d your-domain.com

# Update nginx/conf.d/hr-iot.conf with new certificate paths
ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

# Set up auto-renewal
certbot renew --dry-run
```

## Configuration Details

### Upstream Servers

Defined in `nginx/conf.d/hr-iot.conf`:

```nginx
upstream hr_iot_backend {
    server host.docker.internal:48080;
    keepalive 32;
}

upstream mqtt_websocket {
    server mqtt-broker:8083;
    keepalive 32;
}
```

- `host.docker.internal`: Resolves to host machine (for services running outside Docker)
- `mqtt-broker`: Docker service name (for services inside the same network)

### WebSocket Support

MQTT WebSocket connections require special handling:

```nginx
location /mqtt {
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";

    # Long-lived connections
    proxy_connect_timeout 7d;
    proxy_send_timeout 7d;
    proxy_read_timeout 7d;
}
```

### Security Headers

All HTTPS responses include security headers:

- `Strict-Transport-Security`: Force HTTPS for 1 year
- `X-Frame-Options`: Prevent clickjacking
- `X-Content-Type-Options`: Prevent MIME sniffing
- `X-XSS-Protection`: Enable XSS filter

## Testing

### Health Check

```bash
# From host (with self-signed cert)
curl -k https://localhost/health

# Expected output: "healthy"
```

### Service Endpoints

```bash
# Frontend (should redirect to HTTPS)
curl -I http://localhost/

# Backend API health
curl -k https://localhost/admin-api/health

# MQTT Dashboard
curl -k https://localhost/mqtt-dashboard/

# WebSocket connection (requires MQTT client)
wscat -c wss://localhost/mqtt --no-check
```

## Troubleshooting

### Certificate Errors

**Problem**: Browser shows "Your connection is not private"

**Solution**: This is expected with self-signed certificates. Click "Advanced" → "Proceed to localhost" to bypass.

### Connection Refused

**Problem**: `curl: (7) Failed to connect to localhost port 443`

**Solution**:
1. Check if Nginx container is running: `docker ps | grep nginx`
2. Check Nginx logs: `docker logs hr-iot-nginx`
3. Verify ports are exposed: `docker port hr-iot-nginx`

### 502 Bad Gateway

**Problem**: Nginx returns 502 when accessing backend services

**Solution**:
1. Verify backend service is running
2. Check upstream configuration in `nginx/conf.d/hr-iot.conf`
3. For host services, ensure `host.docker.internal` resolves correctly
4. Check Nginx error logs: `docker logs hr-iot-nginx 2>&1 | grep error`

### Configuration Syntax Errors

**Problem**: Nginx fails to start after configuration changes

**Solution**:
```bash
# Test configuration before reloading
docker exec hr-iot-nginx nginx -t

# Reload if test passes
docker exec hr-iot-nginx nginx -s reload
```

## Maintenance

### Reload Configuration

After making changes to configuration files:

```bash
# Test configuration
docker exec hr-iot-nginx nginx -t

# Reload (zero-downtime)
docker exec hr-iot-nginx nginx -s reload
```

### View Logs

```bash
# Access logs
docker exec hr-iot-nginx tail -f /var/log/nginx/access.log

# Error logs
docker exec hr-iot-nginx tail -f /var/log/nginx/error.log

# Combined
docker logs -f hr-iot-nginx
```

### Update SSL Certificates

```bash
# Regenerate self-signed certificates
./scripts/generate-ssl-certs.sh

# Reload Nginx to use new certificates
docker exec hr-iot-nginx nginx -s reload
```

## Performance Tuning

### Worker Processes

Default: `auto` (matches CPU cores)

```nginx
# nginx.conf
worker_processes auto;
```

### Keepalive Connections

```nginx
# Upstream keepalive connections
upstream hr_iot_backend {
    server host.docker.internal:48080;
    keepalive 32;  # Keep 32 idle connections
}
```

### Gzip Compression

Enabled for text-based content:

```nginx
gzip on;
gzip_comp_level 6;
gzip_types text/plain text/css application/json ...;
```

## Security Considerations

1. **Self-signed certificates**: Only for development. Use Let's Encrypt in production.
2. **Private keys**: Never commit `nginx/ssl/*.key` to version control.
3. **HTTP to HTTPS**: All HTTP traffic automatically redirects to HTTPS.
4. **HSTS**: Browsers will remember to use HTTPS for 1 year.
5. **TLS versions**: Only TLSv1.2 and TLSv1.3 are allowed.

## Phase 2 Integration

When FUXA is integrated in Phase 2, add:

```nginx
upstream fuxa_server {
    server fuxa-server:1881;
    keepalive 32;
}

# In HTTPS server block
location /scada/ {
    proxy_pass http://fuxa_server/;
    # ... proxy headers
}
```

## References

- Nginx Documentation: https://nginx.org/en/docs/
- Nginx Reverse Proxy Guide: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
- WebSocket Proxying: https://nginx.org/en/docs/http/websocket.html
- Let's Encrypt: https://letsencrypt.org/
