# SCADA/HMI Visualization System - Specification

> **Status**: In Progress
> **Created**: 2026-01-03
> **Last Updated**: 2026-01-03
> **Type**: Full-Stack Feature
> **Priority**: High
> **Complexity**: High

---

## Executive Summary

Integrate FUXA (open-source SCADA/HMI platform) into hr-vue3 frontend to create industrial-grade real-time monitoring dashboards for IoT devices, particularly focused on water pump systems, energy management, and facility automation.

**Business Value**:
- Real-time visual monitoring of industrial equipment
- Improved operational visibility and decision-making
- Reduced downtime through early fault detection
- Enhanced user experience with interactive SCADA interfaces
- Historical trend analysis for optimization

---

## Overview

### What is This Feature?

A **SCADA (Supervisory Control and Data Acquisition) visualization system** that provides:

1. **Real-Time Monitoring**: Visual representation of industrial equipment (pumps, valves, sensors) with live data updates
2. **Interactive Dashboards**: Custom SVG-based graphics showing process flows, equipment status, and measurements
3. **Historical Analytics**: Trend charts for analyzing historical data patterns
4. **Alarm Management**: Visual indicators for equipment faults and threshold violations
5. **Control Capabilities**: Remote control of equipment (manual/auto mode switching, start/stop)

### Why Do We Need It?

Current hr-iot system provides:
- ✅ Device management and data collection
- ✅ Energy monitoring and statistics
- ✅ REST APIs for data access

**Missing capabilities**:
- ❌ Industrial-grade visual monitoring interfaces
- ❌ Real-time process flow visualization
- ❌ Interactive SCADA dashboards
- ❌ Visual trend analysis
- ❌ Operator-friendly control interfaces

### Target Users

1. **Plant Operators**: Monitor equipment status, control operations
2. **Facility Managers**: Overview of entire facility performance
3. **Maintenance Engineers**: Identify issues, analyze trends
4. **Energy Managers**: Monitor consumption patterns

---

## Requirements Analysis

### Functional Requirements

#### FR1: FUXA Integration
**Requirement**: Integrate FUXA platform into hr-vue3 frontend
- **FR1.1**: Embed FUXA as a module within Vue 3 application
- **FR1.2**: Support navigation to FUXA dashboards from main menu
- **FR1.3**: Maintain consistent authentication and session management
- **FR1.4**: Support multiple SCADA screens/dashboards

**Acceptance Criteria**:
- FUXA accessible within hr-vue3 application
- Single sign-on working (no separate login)
- Tenant isolation maintained

#### FR2: Real-Time Data Binding
**Requirement**: Connect FUXA visualizations to hr-iot backend real-time data
- **FR2.1**: Pump status display (running/stopped, auto/manual mode)
- **FR2.2**: Sensor readings display (pressure, temperature, flow rate, frequency)
- **FR2.3**: Valve positions (open/closed status)
- **FR2.4**: Equipment health indicators
- **FR2.5**: Alarm states (normal/warning/fault)

**Data Refresh Rate**: 1-5 seconds for real-time values

**Acceptance Criteria**:
- SCADA interface displays actual device data from backend
- Updates occur within specified refresh rate
- No stale data after 10 seconds

#### FR3: Historical Trend Visualization
**Requirement**: Display historical data trends within SCADA interface
- **FR3.1**: Time-series charts for selected parameters
- **FR3.2**: Configurable time ranges (1 hour, 24 hours, 7 days, custom)
- **FR3.3**: Multi-parameter comparison on same chart
- **FR3.4**: Zoom and pan capabilities

**Acceptance Criteria**:
- Historical charts load within 3 seconds
- Support at least 10,000 data points per chart
- Smooth rendering without lag

#### FR4: Equipment Control Interface
**Requirement**: Allow operators to control equipment through SCADA interface
- **FR4.1**: Start/Stop pump commands
- **FR4.2**: Auto/Manual mode switching
- **FR4.3**: Open/Close valve commands
- **FR4.4**: Setpoint adjustments (pressure, flow targets)
- **FR4.5**: Control interlocks and safety confirmations

**Acceptance Criteria**:
- Control commands execute within 2 seconds
- Confirmation dialogs for critical operations
- Audit log of all control actions
- Permission-based control access

#### FR5: Dashboard Management
**Requirement**: Create and manage multiple SCADA dashboards
- **FR5.1**: Pre-configured dashboards (water system, HVAC, energy)
- **FR5.2**: Custom dashboard creation using FUXA designer
- **FR5.3**: Dashboard sharing and permissions
- **FR5.4**: Dashboard versioning

**Acceptance Criteria**:
- At least 3 pre-built dashboards available
- Authorized users can create custom dashboards
- Dashboards persist across sessions

#### FR6: Alarm and Event Display
**Requirement**: Visual alarm management within SCADA
- **FR6.1**: Real-time alarm indicators on equipment graphics
- **FR6.2**: Alarm summary panel
- **FR6.3**: Alarm history log
- **FR6.4**: Alarm acknowledgment workflow

**Acceptance Criteria**:
- Alarms appear within 1 second of occurrence
- Visual differentiation (colors, blinking) for severity levels
- Alarm count badges visible

#### FR7: Energy Statistics Integration
**Requirement**: Display energy consumption data in SCADA format
- **FR7.1**: Real-time power consumption displays
- **FR7.2**: Daily/weekly/monthly energy bar charts
- **FR7.3**: Energy efficiency rankings
- **FR7.4**: Fault statistics visualization

**Acceptance Criteria**:
- Energy data matches existing energy module
- Charts update automatically
- Export capability for reports

---

### Non-Functional Requirements

#### NFR1: Performance
- **NFR1.1**: SCADA page load time < 3 seconds
- **NFR1.2**: Real-time data update latency < 2 seconds
- **NFR1.3**: Support 100+ concurrent dashboard viewers
- **NFR1.4**: Handle 1000+ data points per dashboard

#### NFR2: Usability
- **NFR2.1**: Intuitive interface requiring < 30 minutes training
- **NFR2.2**: Mobile-responsive design (tablet support)
- **NFR2.3**: Color-coded status indicators (green=normal, yellow=warning, red=fault)
- **NFR2.4**: Clear visual hierarchy

#### NFR3: Reliability
- **NFR3.1**: 99.9% uptime for SCADA service
- **NFR3.2**: Graceful degradation if real-time data unavailable
- **NFR3.3**: Automatic reconnection on connection loss
- **NFR3.4**: Data integrity during network interruptions

#### NFR4: Security
- **NFR4.1**: Role-based access control (view-only vs. control permissions)
- **NFR4.2**: Audit logging for all control actions
- **NFR4.3**: Secure WebSocket connections (WSS)
- **NFR4.4**: Input validation on all control commands

#### NFR5: Scalability
- **NFR5.1**: Support 500+ devices per dashboard
- **NFR5.2**: Handle 10+ simultaneous dashboards
- **NFR5.3**: Horizontal scaling for FUXA backend
- **NFR5.4**: Efficient data storage for historical trends

#### NFR6: Maintainability
- **NFR6.1**: Configuration-driven dashboard definitions
- **NFR6.2**: Version control for dashboard designs
- **NFR6.3**: Clear separation between FUXA and hr-vue3 code
- **NFR6.4**: Documented integration points

---

## User Stories

### US1: Operator Monitoring
**As a** plant operator
**I want to** view real-time status of all water pumps on a visual dashboard
**So that** I can quickly identify issues and take corrective action

**Acceptance Criteria**:
- See pump running status (on/off)
- See pump mode (auto/manual)
- See real-time flow rates and pressures
- See alarm indicators on faulty equipment
- Access within 2 clicks from main menu

### US2: Equipment Control
**As an** authorized operator
**I want to** start/stop pumps and switch between auto/manual modes
**So that** I can respond to operational needs

**Acceptance Criteria**:
- Control buttons visible on SCADA interface
- Confirmation dialog for critical actions
- Visual feedback on command execution
- Control restricted by permissions

### US3: Trend Analysis
**As a** maintenance engineer
**I want to** view historical trends of pump performance
**So that** I can identify degradation patterns and plan maintenance

**Acceptance Criteria**:
- Select parameters to chart (pressure, flow, temperature)
- Choose time range (last hour, day, week, custom)
- Zoom and pan on charts
- Export chart data to CSV

### US4: Energy Monitoring
**As an** energy manager
**I want to** see real-time and historical energy consumption
**So that** I can optimize energy usage

**Acceptance Criteria**:
- Real-time power display per equipment
- Daily energy consumption bar charts
- Comparison across equipment or periods
- Integration with existing energy module data

### US5: Alarm Management
**As an** operator
**I want to** be notified of equipment faults immediately
**So that** I can take prompt action

**Acceptance Criteria**:
- Visual alarm indicators (blinking, color-coded)
- Audio alerts (optional, configurable)
- Alarm list with severity and timestamp
- Acknowledge alarms to clear indicators

### US6: Dashboard Customization
**As a** facility manager
**I want to** create custom dashboards for different areas
**So that** operators can focus on relevant equipment

**Acceptance Criteria**:
- Drag-and-drop interface for adding components
- Library of pre-built equipment graphics
- Save and share dashboards
- Set as default dashboard

---

## Detailed Feature Breakdown

### Feature 1: Water Pump System Dashboard (Priority: HIGH)

**Scope**: Replicate the dashboard shown in the provided screenshot

**Components**:
1. **Header Section**
   - Mode indicator (自动/手动)
   - Pump controls (NO.1水泵, NO.2水泵, NO.1循环泵, NO.2循环泵)
   - Each pump: Start/Stop buttons (开/关)

2. **Pipeline Visualization**
   - SVG-based pipe layout
   - Flow direction indicators
   - Pressure sensors (显示 Mpa 读数)
   - Temperature sensors (显示 ℃ 读数)
   - Flow rate sensors (显示 m³/h 或 GJ 读数)
   - Frequency indicators (Hz)
   - Valve positions

3. **Equipment Graphics**
   - Pump symbols (animated when running)
   - Tank/reservoir graphics
   - Heat exchanger symbols
   - Valve symbols
   - Motor symbols

4. **Left Sidebar Panels**
   - **Historical Curve** (历史曲线): Time-series chart with 3 data series
   - **Energy Ranking** (能耗排名): Bar chart showing consumption by category
   - **Fault Statistics** (故障统计): Bar chart showing fault counts
   - **Data Report** (数据报表): Line chart with multiple series

5. **Real-Time Values**
   - Pressure readings at multiple points
   - Temperature readings
   - Flow rates (瞬时流量, 瞬时热量)
   - Equipment status indicators

**Data Sources** (from hr-iot backend):
- Device status from `iot_device` table
- Real-time property values from Redis/TDengine
- Historical data from TDengine
- Energy data from `iot_energy_*` tables
- Alarm/fault data from `iot_alert_*` tables

### Feature 2: FUXA-Backend Integration Layer

**Purpose**: Bridge between FUXA and hr-iot backend

**Components**:
1. **WebSocket Server** (for real-time data push)
   - Endpoint: `ws://backend/scada/realtime`
   - Push device property changes
   - Push alarm events

2. **SCADA Data API** (REST endpoints)
   - `GET /api/scada/devices` - Get device list with current values
   - `GET /api/scada/devices/{id}/realtime` - Get real-time values
   - `GET /api/scada/devices/{id}/history` - Get historical data
   - `POST /api/scada/devices/{id}/control` - Send control command
   - `GET /api/scada/alarms` - Get active alarms
   - `POST /api/scada/alarms/{id}/ack` - Acknowledge alarm

3. **Data Transformation Service**
   - Convert hr-iot data format to FUXA-compatible format
   - Map device properties to SCADA tags
   - Handle unit conversions

### Feature 3: Dashboard Designer Integration

**Purpose**: Allow users to create custom SCADA dashboards

**Approach**: Leverage FUXA's built-in designer

**Customizations Needed**:
1. Pre-load hr-iot device tags
2. Custom widget library for common equipment types
3. Template dashboards for quick start
4. Save dashboard definitions to hr-iot database

### Feature 4: Mobile Tablet Support

**Purpose**: Enable monitoring on tablets for field operators

**Requirements**:
- Responsive layout for 10-12 inch tablets
- Touch-friendly controls
- Optimized for portrait and landscape
- Reduced data usage for mobile connections

---

## Technical Considerations

### FUXA Architecture

FUXA consists of:
1. **FUXA Server** (Node.js + TypeScript)
   - Backend API server
   - WebSocket server for real-time communication
   - Device driver management

2. **FUXA Client** (Angular)
   - Dashboard renderer
   - SVG graphics editor
   - Configuration UI

3. **Data Storage**
   - SQLite for dashboard definitions
   - In-memory for real-time tag values

### Integration Challenges

**Challenge 1: Technology Stack Mismatch**
- FUXA: Node.js backend, Angular frontend
- hr-iot: Spring Boot backend, Vue 3 frontend

**Proposed Solution**:
- Deploy FUXA as separate service (Docker container)
- Embed FUXA client in hr-vue3 using iframe or micro-frontend approach
- Create adapter service to bridge FUXA ↔ hr-iot backend

**Challenge 2: Real-Time Data Synchronization**
- FUXA expects devices to push data via protocols (Modbus, OPC-UA, MQTT)
- hr-iot stores data in MySQL/TDengine

**Proposed Solution**:
- Implement MQTT bridge service that:
  - Subscribes to hr-iot device property changes
  - Publishes to MQTT broker in FUXA-compatible format
  - FUXA subscribes to MQTT topics

**Challenge 3: Authentication & Authorization**
- FUXA has its own user management
- hr-iot has Spring Security + JWT

**Proposed Solution**:
- Single Sign-On (SSO) integration
- Pass JWT token from hr-vue3 to FUXA
- FUXA validates token against hr-iot backend
- Map hr-iot roles to FUXA permissions

**Challenge 4: Multi-Tenancy**
- FUXA doesn't natively support multi-tenancy
- hr-iot requires strict tenant isolation

**Proposed Solution**:
- Deploy separate FUXA instance per tenant (Docker-based)
- OR modify FUXA to add tenant_id filtering
- Dashboard URLs include tenant context

---

## Success Criteria

### Minimum Viable Product (MVP)

**Phase 1 Deliverables**:
1. ✅ FUXA deployed and accessible from hr-vue3
2. ✅ One pre-built water pump system dashboard (as shown in screenshot)
3. ✅ Real-time data display for 5+ device properties
4. ✅ Basic equipment control (start/stop pumps)
5. ✅ Historical trend chart (1 parameter, 24-hour range)
6. ✅ Authentication integration working

**Success Metrics**:
- Dashboard loads within 3 seconds
- Data updates every 5 seconds
- 95%+ user satisfaction in usability testing
- Zero critical security vulnerabilities

### Full Feature Set

**Phase 2-4 Deliverables**:
1. ✅ 3+ pre-built dashboards (water, HVAC, energy)
2. ✅ Custom dashboard designer accessible
3. ✅ Alarm management with visual indicators
4. ✅ Mobile tablet support
5. ✅ Energy statistics integration
6. ✅ Fault statistics visualization
7. ✅ Control interlocks and safety features
8. ✅ Audit logging of control actions
9. ✅ Dashboard versioning and sharing
10. ✅ Export/import dashboard configurations

---

## Out of Scope

The following are **explicitly excluded** from this feature:

### Phase 1 Exclusions
- ❌ Video surveillance integration (cameras)
- ❌ Advanced analytics and AI predictions
- ❌ Report generation and scheduling
- ❌ Integration with external SCADA systems
- ❌ PLC programming or ladder logic
- ❌ Distributed control system (DCS) capabilities
- ❌ Full mobile app (native iOS/Android)
- ❌ Voice control or natural language commands
- ❌3D visualization or digital twin
- ❌ Augmented reality (AR) overlays

### Future Enhancements (Post-Phase 4)
- Advanced alarm analytics (alarm flooding detection)
- Predictive maintenance using ML
- Integration with ERP/MES systems
- Custom scripting language for automation
- Video analytics (motion detection, object recognition)
- Geospatial mapping of distributed facilities

---

## Risks and Mitigation

### Risk 1: FUXA Customization Complexity
**Risk**: FUXA source code modifications may be extensive
**Impact**: High development effort, difficult upgrades
**Probability**: Medium
**Mitigation**:
- Minimize FUXA code changes
- Use configuration and plugins where possible
- Fork FUXA repository if major changes needed
- Document all customizations thoroughly

### Risk 2: Performance with Large Datasets
**Risk**: Slow performance with 1000+ devices
**Impact**: Poor user experience, unacceptable latency
**Probability**: Medium
**Mitigation**:
- Implement data aggregation/downsampling
- Use pagination for device lists
- Optimize database queries
- Load test with realistic data volumes
- Consider time-series database (TDengine) optimizations

### Risk 3: WebSocket Connection Stability
**Risk**: Frequent disconnections disrupt real-time updates
**Impact**: Stale data, user frustration
**Probability**: Medium
**Mitigation**:
- Implement automatic reconnection logic
- Show connection status indicator
- Buffer data during disconnection
- Heartbeat mechanism to detect dead connections
- Fallback to polling if WebSocket fails

### Risk 4: Multi-Tenancy Isolation
**Risk**: Tenant data leakage through FUXA
**Impact**: Critical security issue
**Probability**: Low (but high severity)
**Mitigation**:
- Thorough security testing
- Code review of tenant filtering logic
- Separate FUXA instances per tenant (if needed)
- Penetration testing before production

### Risk 5: Learning Curve for FUXA
**Risk**: Team unfamiliar with FUXA internals
**Impact**: Slow development, bugs
**Probability**: High
**Mitigation**:
- Assign dedicated developer to learn FUXA
- Review FUXA documentation and examples
- Start with simple proof-of-concept
- Engage with FUXA community/maintainers

---

## Dependencies

### External Dependencies
- **FUXA**: v1.1.x (MIT license, open-source)
- **MQTT Broker**: Mosquitto or EMQX
- **WebSocket Library**: Socket.io or native WebSocket
- **Chart Library**: Chart.js or ECharts (for historical trends)

### Internal Dependencies
- **hr-iot Backend**: Device management, data APIs
- **hr-vue3 Frontend**: Navigation, authentication
- **IoT Gateway**: Device protocol handlers
- **TDengine**: Historical time-series data
- **Redis**: Real-time data caching

### Infrastructure Dependencies
- Docker for containerization
- Nginx for reverse proxy (optional)
- SSL certificates for WSS connections

---

## Compliance and Standards

### Industrial Standards
- **ISA-101**: Human-Machine Interface standard (color usage, alarms)
- **IEC 62264**: Enterprise-control system integration
- **OPC UA**: Open platform communications (if using OPC devices)

### UI/UX Guidelines
- Color coding: Green=OK, Yellow=Warning, Red=Fault, Gray=Offline
- Alarm priorities: Critical, High, Medium, Low
- Consistent iconography for equipment types
- Accessibility (WCAG 2.1 Level AA)

### Security Standards
- OWASP Top 10 compliance
- Secure authentication (JWT tokens)
- Role-based access control (RBAC)
- Audit logging for compliance

---

## Acceptance Testing Plan

### Test Scenarios

**TS1: Dashboard Load**
1. Navigate to SCADA menu
2. Select water pump dashboard
3. Verify: Dashboard loads within 3 seconds
4. Verify: All equipment graphics visible
5. Verify: Real-time values displayed

**TS2: Real-Time Data Update**
1. Open water pump dashboard
2. Change device property value in backend
3. Verify: SCADA display updates within 5 seconds
4. Verify: No page refresh required
5. Verify: Value matches backend data

**TS3: Equipment Control**
1. Click "Start" button on NO.1水泵
2. Confirm action in dialog
3. Verify: Command sent to backend
4. Verify: Pump status changes to "Running"
5. Verify: Visual indicator updates (animation, color)
6. Verify: Audit log entry created

**TS4: Historical Trend**
1. Open data report panel
2. Select parameter (e.g., 回水压力)
3. Choose time range (24 hours)
4. Verify: Chart loads within 3 seconds
5. Verify: Data points visible
6. Verify: Zoom and pan work

**TS5: Alarm Display**
1. Trigger alarm condition (e.g., pressure exceeds threshold)
2. Verify: Alarm indicator appears on equipment within 1 second
3. Verify: Alarm listed in alarm panel
4. Verify: Color coded by severity
5. Acknowledge alarm
6. Verify: Indicator clears

**TS6: Multi-Tenancy Isolation**
1. Log in as Tenant A user
2. Open SCADA dashboard
3. Verify: Only Tenant A devices visible
4. Log out, log in as Tenant B user
5. Verify: Only Tenant B devices visible
6. Verify: No data leakage between tenants

**TS7: Permission Control**
1. Log in as view-only user
2. Verify: Control buttons disabled/hidden
3. Verify: Read-only access to dashboards
4. Log in as operator user
5. Verify: Control buttons enabled
6. Verify: Can execute commands

---

## User Training Requirements

### Operator Training (2 hours)
1. **Introduction** (15 min): Overview of SCADA interface, navigation
2. **Monitoring** (30 min): Reading real-time values, understanding indicators
3. **Alarms** (30 min): Recognizing alarms, acknowledging, troubleshooting
4. **Controls** (30 min): Starting/stopping equipment, mode switching, safety procedures
5. **Practice** (15 min): Hands-on simulation

### Administrator Training (4 hours)
1. All operator training content (2 hours)
2. **Dashboard Designer** (1 hour): Creating custom dashboards, using widget library
3. **Configuration** (30 min): Managing users, setting permissions, configuring alarms
4. **Troubleshooting** (30 min): Diagnosing connection issues, reviewing logs

### Documentation Deliverables
- User manual (PDF, 30+ pages)
- Quick reference guide (1-page cheat sheet)
- Video tutorials (5-10 short videos)
- FAQ document

---

## Glossary

- **SCADA**: Supervisory Control and Data Acquisition - system for monitoring and controlling industrial processes
- **HMI**: Human-Machine Interface - graphical interface for operators
- **PLC**: Programmable Logic Controller - industrial computer for automation
- **OPC-UA**: Open Platform Communications Unified Architecture - industrial communication protocol
- **MQTT**: Message Queuing Telemetry Transport - lightweight publish-subscribe protocol
- **Tag**: A named data point in SCADA system (e.g., "Pump1_Status", "Pressure_Sensor_1")
- **SVG**: Scalable Vector Graphics - XML-based vector image format
- **WebSocket**: Protocol for full-duplex communication over TCP
- **Interlock**: Safety logic preventing unsafe operations
- **Setpoint**: Desired target value for a controlled variable

---

## References

- FUXA GitHub: https://github.com/frangoteam/FUXA
- FUXA Documentation: https://github.com/frangoteam/FUXA/wiki
- ISA-101 Standard: Human-Machine Interfaces for Process Automation Systems
- hr-iot Project Documentation: `CLAUDE.md`, `IOT_MODULE_GUIDE.md`
- Energy Management Requirements: `能源管理模块需求计划.md`

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-03 | Claude | Initial specification created |

---

**Next Steps**: Proceed to architecture planning (`/speckit.plan`) to design the technical implementation approach.
