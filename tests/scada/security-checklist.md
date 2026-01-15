# SCADA Security Testing Checklist

## Part of SCADA-031: Security Testing

---

## 1. Authentication & Authorization

### 1.1 Authentication Tests

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-A01 | Login without credentials | 401 Unauthorized | ⬜ |
| SEC-A02 | Login with invalid credentials | 401 Unauthorized | ⬜ |
| SEC-A03 | Login with valid credentials | 200 OK + Token | ⬜ |
| SEC-A04 | Access API without token | 401 Unauthorized | ⬜ |
| SEC-A05 | Access API with expired token | 401 Unauthorized | ⬜ |
| SEC-A06 | Access API with tampered token | 401/403 | ⬜ |
| SEC-A07 | Brute force login attempts | Account locked after N attempts | ⬜ |

### 1.2 Authorization Tests

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-Z01 | Access without permission | 403 Forbidden | ⬜ |
| SEC-Z02 | Control device without permission | 403 Forbidden | ⬜ |
| SEC-Z03 | Admin API with regular user | 403 Forbidden | ⬜ |
| SEC-Z04 | Permission escalation attempt | 403 Forbidden | ⬜ |

---

## 2. Multi-Tenant Isolation

### 2.1 Data Isolation Tests

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-T01 | Query other tenant's devices | Empty result or 403 | ⬜ |
| SEC-T02 | Access other tenant's dashboard | 404 or 403 | ⬜ |
| SEC-T03 | Control other tenant's device | 403 Forbidden | ⬜ |
| SEC-T04 | View other tenant's alarms | Empty result or 403 | ⬜ |
| SEC-T05 | Tamper tenant-id header | Request rejected or data isolated | ⬜ |

### 2.2 Test Commands

```bash
# Test cross-tenant access
curl -X GET "http://localhost:8080/admin-api/iot/scada/devices" \
  -H "Authorization: Bearer {TENANT_A_TOKEN}" \
  -H "tenant-id: 2"  # Attempting to access Tenant B data

# Expected: Empty list or 403
```

---

## 3. Injection Attacks

### 3.1 SQL Injection Tests

| ID | Test Case | Payload | Status |
|----|-----------|---------|--------|
| SEC-I01 | Device ID injection | `1 OR 1=1` | ⬜ |
| SEC-I02 | Search param injection | `'; DROP TABLE devices;--` | ⬜ |
| SEC-I03 | Tag name injection | `tag' OR '1'='1` | ⬜ |
| SEC-I04 | Order by injection | `ORDER BY 1,2,3--` | ⬜ |

### 3.2 Command Injection Tests

| ID | Test Case | Payload | Status |
|----|-----------|---------|--------|
| SEC-C01 | MQTT topic injection | `topic; cat /etc/passwd` | ⬜ |
| SEC-C02 | Control command injection | `{"cmd": "$(whoami)"}` | ⬜ |
| SEC-C03 | Script injection | `<script>alert(1)</script>` | ⬜ |

### 3.3 Test Commands

```bash
# SQL injection test
curl -X GET "http://localhost:8080/admin-api/iot/scada/devices?name=test' OR '1'='1" \
  -H "Authorization: Bearer {TOKEN}"

# Expected: 400 Bad Request or escaped query
```

---

## 4. XSS & CSRF

### 4.1 XSS Tests

| ID | Test Case | Payload | Status |
|----|-----------|---------|--------|
| SEC-X01 | Stored XSS in device name | `<script>alert('XSS')</script>` | ⬜ |
| SEC-X02 | Stored XSS in alarm message | `<img src=x onerror=alert(1)>` | ⬜ |
| SEC-X03 | Reflected XSS in search | `"><script>alert(1)</script>` | ⬜ |
| SEC-X04 | DOM XSS in dashboard | `javascript:alert(1)` | ⬜ |

### 4.2 CSRF Tests

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-F01 | Control command without CSRF token | Request rejected | ⬜ |
| SEC-F02 | Cross-origin control request | Request rejected | ⬜ |
| SEC-F03 | Forged form submission | Request rejected | ⬜ |

---

## 5. MQTT Security

### 5.1 MQTT Authentication

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-M01 | Connect without credentials | Connection rejected | ⬜ |
| SEC-M02 | Connect with invalid credentials | Connection rejected | ⬜ |
| SEC-M03 | Subscribe to unauthorized topic | Subscription rejected | ⬜ |
| SEC-M04 | Publish to unauthorized topic | Publish rejected | ⬜ |

### 5.2 MQTT Topic Security

| ID | Test Case | Topic Pattern | Status |
|----|-----------|---------------|--------|
| SEC-M05 | Subscribe to wildcard # | Rejected or tenant-filtered | ⬜ |
| SEC-M06 | Subscribe to other tenant topic | Rejected | ⬜ |
| SEC-M07 | Subscribe to control topic (readonly) | Rejected | ⬜ |

### 5.3 Test Commands

```bash
# MQTT subscription test
mosquitto_sub -h localhost -p 1883 \
  -u test_user -P test_pass \
  -t "fuxa/tenant/2/#"

# Expected: Subscription rejected for tenant 2 topics
```

---

## 6. API Security

### 6.1 Rate Limiting

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-R01 | 100 requests/second | Rate limited after threshold | ⬜ |
| SEC-R02 | Control command spam | Rate limited | ⬜ |

### 6.2 Input Validation

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-V01 | Oversized request body | 413 Payload Too Large | ⬜ |
| SEC-V02 | Invalid JSON | 400 Bad Request | ⬜ |
| SEC-V03 | Invalid data types | 400 Bad Request | ⬜ |
| SEC-V04 | Missing required fields | 400 Bad Request | ⬜ |

---

## 7. Audit Logging

### 7.1 Audit Log Tests

| ID | Test Case | Expected Log Entry | Status |
|----|-----------|-------------------|--------|
| SEC-L01 | User login | LOGIN event with user/IP/time | ⬜ |
| SEC-L02 | Control command | CONTROL event with command details | ⬜ |
| SEC-L03 | Failed authentication | AUTH_FAILED event | ⬜ |
| SEC-L04 | Permission denied | ACCESS_DENIED event | ⬜ |
| SEC-L05 | Configuration change | CONFIG_CHANGE event | ⬜ |

### 7.2 Log Integrity

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-L06 | Logs cannot be modified | Write-only access | ⬜ |
| SEC-L07 | Logs include timestamp | ISO 8601 format | ⬜ |
| SEC-L08 | Logs include user context | User ID, tenant ID | ⬜ |

---

## 8. FUXA iframe Security

### 8.1 iframe Security

| ID | Test Case | Expected Result | Status |
|----|-----------|-----------------|--------|
| SEC-IF01 | postMessage origin check | Messages from other origins ignored | ⬜ |
| SEC-IF02 | Token in postMessage | Token validated server-side | ⬜ |
| SEC-IF03 | Sandbox attributes | iframe properly sandboxed | ⬜ |

---

## Test Execution Summary

| Category | Total | Pass | Fail | Skip |
|----------|-------|------|------|------|
| Authentication | 7 | - | - | - |
| Authorization | 4 | - | - | - |
| Tenant Isolation | 5 | - | - | - |
| Injection | 7 | - | - | - |
| XSS/CSRF | 7 | - | - | - |
| MQTT | 7 | - | - | - |
| API Security | 6 | - | - | - |
| Audit Logging | 8 | - | - | - |
| iframe Security | 3 | - | - | - |
| **Total** | **54** | - | - | - |

---

## Tools Required

- **OWASP ZAP**: Web vulnerability scanner
- **Burp Suite**: HTTP interception and testing
- **SQLMap**: SQL injection testing
- **Mosquitto**: MQTT client for testing
- **curl/httpie**: API testing

---

## Sign-off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Security Engineer | | | |
| Tech Lead | | | |
| Project Manager | | | |
