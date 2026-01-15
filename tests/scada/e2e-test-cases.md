# SCADA End-to-End Test Cases

## Part of SCADA-032: End-to-End User Acceptance Testing

---

## Test Environment

| Item | Value |
|------|-------|
| Backend URL | http://localhost:8080 |
| Frontend URL | http://localhost:80 |
| FUXA URL | http://localhost:1881 |
| MQTT Broker | localhost:1883 |
| Database | MySQL 8.0 |

---

## 1. Dashboard Viewing Tests

### TC-001: Load Main SCADA Page

**Preconditions:**
- User logged in with SCADA view permission
- Water pump dashboard configured

**Steps:**
1. Navigate to `/iot/scada/index`
2. Wait for page to load completely

**Expected Results:**
- [ ] Page loads within 3 seconds
- [ ] Header shows "SCADA 监控中心"
- [ ] Left sidebar shows 4 panels (历史曲线, 能耗排名, 故障统计, 数据报表)
- [ ] Main area shows dashboard selector
- [ ] Right sidebar shows device list and alarm list
- [ ] Connection status shows "已连接"

---

### TC-002: Switch Dashboard

**Preconditions:**
- Multiple dashboards available

**Steps:**
1. Click dashboard selector dropdown
2. Select "水泵系统监控" dashboard
3. Wait for dashboard to load

**Expected Results:**
- [ ] Dashboard list shows available dashboards
- [ ] Selected dashboard loads in iframe
- [ ] FUXA viewer displays correctly
- [ ] No authentication prompts appear

---

### TC-003: View Dashboard in Fullscreen

**Steps:**
1. Click fullscreen button on dashboard toolbar
2. View dashboard in fullscreen mode
3. Press ESC to exit fullscreen

**Expected Results:**
- [ ] Dashboard enters fullscreen mode
- [ ] All elements visible
- [ ] ESC exits fullscreen
- [ ] Returns to normal view

---

## 2. Real-time Data Tests

### TC-004: Real-time Pressure Display

**Preconditions:**
- Pressure sensor sending data via MQTT

**Steps:**
1. View dashboard with pressure indicators
2. Wait for data update (5 seconds)
3. Verify displayed value

**Expected Results:**
- [ ] Pressure value displays with 2 decimal places
- [ ] Unit shows "MPa"
- [ ] Value updates automatically
- [ ] Timestamp shows recent time

---

### TC-005: Pump Status Animation

**Preconditions:**
- Pump device configured and running

**Steps:**
1. View pump SVG graphic on dashboard
2. Start pump (if not running)
3. Observe animation

**Expected Results:**
- [ ] Running pump shows rotating blade animation
- [ ] Status indicator shows green
- [ ] Stopped pump shows gray with no animation

---

### TC-006: Data Update Latency

**Steps:**
1. Note current time
2. Send MQTT message with new tag value
3. Observe update in dashboard
4. Note update time

**Expected Results:**
- [ ] Update appears within 2 seconds
- [ ] Value matches sent MQTT message

---

## 3. Control Command Tests

### TC-007: Start Pump via Dashboard

**Preconditions:**
- User has control permission
- Pump is in stopped state

**Steps:**
1. Click on pump graphic to open control panel
2. Click "启动" button
3. Confirm control action
4. Wait for response

**Expected Results:**
- [ ] Control panel shows current pump status
- [ ] Confirmation dialog appears
- [ ] Success message displayed
- [ ] Pump status changes to "运行"
- [ ] Animation starts
- [ ] Control log entry created

---

### TC-008: Stop Pump via Dashboard

**Preconditions:**
- User has control permission
- Pump is running

**Steps:**
1. Click on pump graphic
2. Click "停止" button
3. Confirm action

**Expected Results:**
- [ ] Success message displayed
- [ ] Pump status changes to "停止"
- [ ] Animation stops
- [ ] Control log entry created

---

### TC-009: Control Without Permission

**Preconditions:**
- User has view-only permission

**Steps:**
1. Navigate to dashboard
2. Attempt to click control button

**Expected Results:**
- [ ] Control buttons are disabled or hidden
- [ ] Or clicking shows "权限不足" message

---

### TC-010: Control with Timeout

**Preconditions:**
- Device is offline or slow to respond

**Steps:**
1. Send control command to slow device
2. Wait for timeout (30 seconds)

**Expected Results:**
- [ ] Loading indicator shown during wait
- [ ] Timeout message after 30s
- [ ] Control log shows timeout status

---

## 4. Alarm Tests

### TC-011: Alarm Display on Dashboard

**Preconditions:**
- Alarm configured for high pressure
- Current pressure below threshold

**Steps:**
1. Increase pressure above threshold (simulate)
2. Wait for alarm to trigger

**Expected Results:**
- [ ] Alarm indicator appears on equipment
- [ ] Blinking animation for critical alarms
- [ ] Alarm appears in alarm list
- [ ] Alarm sound/notification (if configured)

---

### TC-012: Acknowledge Alarm

**Steps:**
1. View active alarm in alarm list
2. Click "确认" button
3. Enter acknowledgment notes (optional)

**Expected Results:**
- [ ] Alarm marked as acknowledged
- [ ] Blinking stops
- [ ] Acknowledger name and time recorded

---

### TC-013: Alarm History Query

**Steps:**
1. Navigate to alarm history page
2. Set date range filter
3. Filter by alarm level
4. Click search

**Expected Results:**
- [ ] Historical alarms displayed
- [ ] Filters work correctly
- [ ] Pagination works
- [ ] Export function available

---

## 5. Multi-tenant Tests

### TC-014: Tenant A Only Sees Own Data

**Preconditions:**
- Logged in as Tenant A user

**Steps:**
1. View device list
2. View dashboard list
3. View alarm list

**Expected Results:**
- [ ] Only Tenant A devices shown
- [ ] Only Tenant A dashboards shown
- [ ] Only Tenant A alarms shown

---

### TC-015: Cannot Access Other Tenant

**Preconditions:**
- Logged in as Tenant A user
- Know Tenant B dashboard ID

**Steps:**
1. Try to access Tenant B dashboard via URL
2. Try to control Tenant B device via API

**Expected Results:**
- [ ] Dashboard not found or access denied
- [ ] Control command rejected

---

## 6. Responsive Tests

### TC-016: Tablet View (1024x768)

**Steps:**
1. Resize browser to 1024x768
2. Navigate through SCADA pages

**Expected Results:**
- [ ] Layout adapts to tablet size
- [ ] Sidebar collapsible
- [ ] Dashboard remains functional
- [ ] Control buttons accessible

---

### TC-017: Mobile View (375x667)

**Steps:**
1. Resize browser to 375x667 (iPhone size)
2. Navigate through SCADA pages

**Expected Results:**
- [ ] Layout adapts to mobile
- [ ] Navigation drawer available
- [ ] Dashboard scrollable
- [ ] Touch-friendly controls

---

## 7. Error Handling Tests

### TC-018: Backend Unavailable

**Steps:**
1. Stop backend server
2. Attempt to load dashboard
3. Observe error handling

**Expected Results:**
- [ ] User-friendly error message
- [ ] Retry button available
- [ ] Connection status shows "断开"

---

### TC-019: MQTT Disconnection

**Steps:**
1. Disconnect MQTT broker
2. Observe dashboard behavior

**Expected Results:**
- [ ] Connection status shows "重连中..."
- [ ] Old values retained
- [ ] Auto-reconnect attempts
- [ ] Reconnection successful when broker returns

---

### TC-020: Invalid Dashboard Configuration

**Steps:**
1. Load dashboard with missing SVG
2. Observe error handling

**Expected Results:**
- [ ] Placeholder shown for missing SVG
- [ ] Error logged to console
- [ ] Other elements still functional

---

## 8. Performance Tests

### TC-021: Page Load Performance

**Steps:**
1. Clear browser cache
2. Load SCADA main page
3. Measure load time with DevTools

**Expected Results:**
- [ ] First Contentful Paint < 1.5s
- [ ] Largest Contentful Paint < 3s
- [ ] Time to Interactive < 5s

---

### TC-022: Memory Usage

**Steps:**
1. Open SCADA page
2. Leave running for 30 minutes
3. Monitor memory usage

**Expected Results:**
- [ ] No significant memory growth
- [ ] No memory leaks detected
- [ ] Page remains responsive

---

## Test Execution Summary

| Category | Total | Pass | Fail | Block |
|----------|-------|------|------|-------|
| Dashboard Viewing | 3 | | | |
| Real-time Data | 3 | | | |
| Control Commands | 4 | | | |
| Alarms | 3 | | | |
| Multi-tenant | 2 | | | |
| Responsive | 2 | | | |
| Error Handling | 3 | | | |
| Performance | 2 | | | |
| **Total** | **22** | | | |

---

## Sign-off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| QA Lead | | | |
| Product Owner | | | |
| Tech Lead | | | |
