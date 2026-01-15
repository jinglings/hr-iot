# SCADA/HMI Visualization System - Project Overview

> **Feature**: Industrial SCADA/HMI visualization using FUXA integration
> **Status**: Planning Complete, Ready for Implementation
> **Created**: 2026-01-03
> **Development Approach**: Spec-Kit Methodology

---

## Quick Start Guide

This directory contains the complete specification, architecture design, and implementation plan for integrating FUXA (open-source SCADA platform) into the hr-iot project.

### Documents in This Feature

1. **[01-specification.md](./01-specification.md)** - Requirements Specification
   - What we're building and why
   - User stories and use cases
   - Functional and non-functional requirements
   - Success criteria and acceptance tests

2. **[02-plan.md](./02-plan.md)** - Architecture Design
   - System architecture and data flow
   - Technology decisions and rationale
   - Component designs (backend, frontend, FUXA)
   - Database schema
   - Security architecture
   - Deployment strategy

3. **[03-tasks.md](./03-tasks.md)** - Implementation Tasks
   - 38 detailed tasks across 7 phases
   - 12-week timeline with 2-3 developers
   - Task dependencies and critical path
   - Acceptance criteria for each task
   - Files to create/modify

4. **[README.md](./README.md)** - This file
   - Project overview and quick start
   - Key decisions summary
   - Getting started guide

---

## Executive Summary

### What Are We Building?

A **real-time SCADA/HMI visualization system** that provides:

- ✅ Industrial-grade monitoring dashboards for IoT devices
- ✅ Real-time data visualization with SVG graphics
- ✅ Interactive equipment control (start/stop pumps, valves, etc.)
- ✅ Historical trend analysis and reporting
- ✅ Alarm management with visual indicators
- ✅ Mobile-responsive design for tablets

### Why FUXA?

FUXA is an open-source web-based SCADA/HMI platform that provides:

- Modern web technology (Node.js + Angular)
- SVG-based graphics editor
- Multi-protocol support (MQTT, Modbus, OPC-UA)
- MIT license (commercial use allowed)
- Active community and ongoing development

### Target Dashboard

The primary deliverable is a **water pump system monitoring dashboard** (as shown in user's screenshot) featuring:

- 4 pumps with start/stop controls
- Real-time pressure, temperature, flow rate, frequency displays
- Pipeline visualization with flow direction indicators
- Historical trend charts
- Energy consumption statistics
- Fault statistics visualization
- Auto/Manual mode switching

---

## Architecture At a Glance

```
┌──────────────┐
│   Browser    │ (User accesses hr-vue3)
└──────┬───────┘
       │
┌──────▼───────┐
│   hr-vue3    │ (Vue 3 Frontend)
│   SCADA Page │ (Embeds FUXA in iframe)
└──────┬───────┘
       │ WebSocket + REST API
       │
┌──────▼──────────────────────┐
│  Nginx Reverse Proxy        │
└──────┬──────────┬───────────┘
       │          │
  ┌────▼────┐  ┌─▼────────┐
  │  FUXA   │  │ hr-iot   │
  │ Server  │  │ Backend  │
  │(Node.js)│  │(Spring)  │
  └────┬────┘  └─┬────────┘
       │         │
       └────┬────┘
            │
      ┌─────▼──────┐
      │MQTT Broker │ (EMQX)
      │Real-time   │
      │Data Stream │
      └────────────┘
```

### Key Integration Points

1. **Authentication**: JWT token from hr-vue3 passed to FUXA for SSO
2. **Real-Time Data**: hr-iot publishes device updates to MQTT → FUXA subscribes
3. **Control Commands**: FUXA publishes to MQTT → hr-iot executes via device service
4. **Historical Data**: FUXA queries hr-iot REST API → TDengine time-series data

---

## Key Technology Decisions

### 1. FUXA as Separate Service ✅

**Decision**: Deploy FUXA as independent Docker container, not fork and heavily modify

**Rationale**:
- Cleaner separation of concerns
- Easier FUXA upgrades
- Minimal custom code in FUXA
- Use adapter/bridge pattern

**Trade-off**: Additional service to manage, but worth it for maintainability

---

### 2. MQTT for Real-Time Transport ✅

**Decision**: Use MQTT broker (EMQX) for real-time data between hr-iot and FUXA

**Rationale**:
- FUXA has native MQTT support
- Industry standard for IoT
- Scalable pub-sub model
- Low latency (< 1 second)

**Alternative Considered**: WebSocket (custom protocol) - rejected due to implementation complexity

---

### 3. Shared JWT Authentication ✅

**Decision**: Pass JWT token from hr-vue3 to FUXA, validate against hr-iot backend

**Rationale**:
- Seamless SSO experience
- No separate login
- Centralized authentication

**Implementation**: Requires FUXA modification to add JWT validation middleware

---

### 4. Single FUXA with Tenant Filtering ✅

**Decision**: One FUXA instance for all tenants with MQTT topic filtering

**Rationale**:
- Resource efficient
- Scalable to 100+ tenants
- Standard multi-tenant pattern

**Security**: MQTT topics include tenant_id: `iot/{tenant_id}/device/{id}/properties`

---

## Development Phases

### Phase 1: Infrastructure Setup (Week 1-2)
- Deploy MQTT broker (EMQX)
- Configure Docker Compose
- Set up Nginx reverse proxy
- SSL certificates

**Deliverable**: MQTT + Docker + Nginx working

---

### Phase 2: FUXA Customization (Week 3-4)
- Clone FUXA repository
- Add JWT authentication middleware
- Add tenant filtering to MQTT subscriptions
- Replace SQLite with MySQL
- Build custom Docker image

**Deliverable**: Customized FUXA with SSO and multi-tenancy

---

### Phase 3: Backend Integration (Week 5-6)
- Create SCADA data models (DO, VO)
- Implement SCADA Bridge Service
- Configure Spring MQTT integration
- Create SCADA REST API endpoints
- Implement device property change listener

**Deliverable**: hr-iot backend publishing to MQTT and providing SCADA APIs

---

### Phase 4: Frontend Integration (Week 7-8)
- Create SCADA module in hr-vue3
- Build FUXA iframe embed component
- Create dashboard selector
- Add connection status indicator
- Configure routes and navigation

**Deliverable**: SCADA page in hr-vue3 with FUXA embedded

---

### Phase 5: Dashboard Creation (Week 9-10)
- Design SVG graphics for water pump system
- Configure tag mappings (20+ data points)
- Build dashboard in FUXA designer
- Add left sidebar panels (charts, statistics)
- Configure alarms and animations

**Deliverable**: Complete water pump monitoring dashboard

---

### Phase 6: Testing & Optimization (Week 11)
- Performance load testing (100 users)
- Security testing (OWASP, penetration test)
- User acceptance testing
- Documentation (user manual, admin guide)
- User training

**Deliverable**: Production-ready system with documentation

---

### Phase 7: Production Deployment (Week 12)
- Production environment setup
- Deployment
- Post-deployment verification
- Go-live announcement

**Deliverable**: Live SCADA system in production

---

## Effort Estimation

| Phase | Tasks | Hours | Duration |
|-------|-------|-------|----------|
| 1. Infrastructure | 4 | 24 | 2 weeks |
| 2. FUXA Custom | 5 | 50 | 2 weeks |
| 3. Backend Integration | 7 | 70 | 2 weeks |
| 4. Frontend Integration | 8 | 48 | 2 weeks |
| 5. Dashboard Creation | 5 | 64 | 2 weeks |
| 6. Testing & Docs | 5 | 74 | 1 week |
| 7. Deployment | 4 | 28 | 1 week |
| **Total** | **38** | **358** | **12 weeks** |

**Team**: 2-3 developers (1 backend, 1 frontend, 1 DevOps/full-stack)

**With 30% buffer**: 466 hours

---

## Getting Started - First Steps

### For Project Manager

1. **Review Documents**:
   - Read 01-specification.md for requirements
   - Read 02-plan.md for architecture
   - Read 03-tasks.md for implementation plan

2. **Approve and Kick Off**:
   - Get stakeholder approval on specification
   - Approve architecture decisions
   - Assign developers to phases

3. **Set Up Tracking**:
   - Create project in Jira/GitHub
   - Create 38 tickets from 03-tasks.md
   - Set up milestones for each phase

### For Backend Developer

1. **Phase 1-2 Focus**:
   - Task SCADA-001: Deploy MQTT broker
   - Task SCADA-005 to SCADA-009: Customize FUXA

2. **Phase 3 Focus**:
   - Task SCADA-010 to SCADA-016: Backend integration
   - Implement SCADA Bridge Service
   - Create REST APIs

**Preparation**:
- Familiarize with FUXA architecture (Node.js + TypeScript)
- Review hr-iot device management code
- Understand MQTT protocol basics

### For Frontend Developer

1. **Phase 4 Focus**:
   - Task SCADA-017 to SCADA-024: Frontend integration
   - Create SCADA module in hr-vue3
   - Embed FUXA iframe

2. **Phase 5 Focus**:
   - Task SCADA-025: Design SVG graphics (or work with designer)
   - Task SCADA-027 to SCADA-029: Build dashboards in FUXA

**Preparation**:
- Review hr-vue3 codebase structure
- Learn FUXA dashboard designer interface
- Basic SVG graphics skills (or coordinate with designer)

### For DevOps

1. **Phase 1 Focus**:
   - Task SCADA-001 to SCADA-004: Infrastructure setup
   - Docker Compose configuration
   - Nginx reverse proxy

2. **Phase 7 Focus**:
   - Task SCADA-035 to SCADA-038: Production deployment

**Preparation**:
- Review existing hr-iot Docker setup
- Install EMQX locally for testing
- Review SSL certificate process

---

## Dependencies to Install

### Development Environment

**Backend Developer**:
- JDK 8
- Maven 3.6+
- Node.js 18 LTS (for FUXA)
- Docker Desktop
- MySQL client
- MQTT client (mosquitto_pub/sub for testing)

**Frontend Developer**:
- Node.js 16+
- pnpm 8.6+
- Vue DevTools browser extension
- Inkscape or Adobe Illustrator (for SVG editing)

**DevOps**:
- Docker + Docker Compose
- Nginx
- EMQX (MQTT broker)

### External Services

**MQTT Broker**:
```bash
docker run -d --name emqx \
  -p 1883:1883 -p 8083:8083 -p 18083:18083 \
  emqx/emqx:5.0
```

**FUXA (Development)**:
```bash
git clone https://github.com/frangoteam/FUXA.git
cd FUXA
npm install
npm run build
npm start
# Access at http://localhost:1881
```

---

## Risk Assessment

### High Priority Risks

**Risk 1: FUXA Learning Curve**
- **Probability**: High
- **Impact**: Medium (delays in Phase 2)
- **Mitigation**: Assign dedicated developer to learn FUXA early, create POC

**Risk 2: Performance with Many Devices**
- **Probability**: Medium
- **Impact**: High (user experience)
- **Mitigation**: Load test early (Phase 6 Task SCADA-030), optimize before go-live

**Risk 3: Multi-Tenancy Data Leakage**
- **Probability**: Low
- **Impact**: Critical (security breach)
- **Mitigation**: Security testing in Phase 6 (Task SCADA-031), penetration test

### Medium Priority Risks

**Risk 4: Dashboard Design Complexity**
- **Probability**: Medium
- **Impact**: Medium (extended Phase 5)
- **Mitigation**: Use FUXA widget library, hire designer if needed

**Risk 5: WebSocket Connection Stability**
- **Probability**: Medium
- **Impact**: Medium (user frustration)
- **Mitigation**: Implement reconnection logic, show connection status

---

## Success Criteria

### Minimum Viable Product (MVP)

✅ FUXA accessible from hr-vue3 with SSO
✅ Water pump system dashboard (as per screenshot)
✅ Real-time data display for 5+ properties (updating every 5 seconds)
✅ Basic equipment control (start/stop pumps)
✅ Historical trend chart (1 parameter, 24-hour range)
✅ Multi-tenant isolation working

### Full Feature Set (All Phases Complete)

✅ 3+ pre-built dashboards (water, HVAC, energy)
✅ Dashboard designer accessible to admins
✅ Alarm management with visual indicators
✅ Mobile tablet support
✅ Energy statistics integration
✅ Control audit logging
✅ User training completed
✅ Documentation delivered

---

## Monitoring Post-Launch

### Key Metrics to Track

**Performance**:
- Dashboard load time (target: < 3s)
- Real-time update latency (target: < 2s)
- WebSocket connection uptime (target: 99.9%)

**Usage**:
- Daily active users
- Most viewed dashboards
- Control commands executed per day

**System Health**:
- MQTT message throughput
- FUXA CPU/memory usage
- Error rate

**Business Impact**:
- Reduction in equipment downtime
- Faster incident response time
- User satisfaction score

---

## Troubleshooting Quick Reference

### Common Issues

**Issue**: FUXA not loading in iframe
- **Check**: CORS headers in Nginx
- **Check**: JWT token in URL
- **Check**: FUXA server logs

**Issue**: Real-time data not updating
- **Check**: MQTT broker connection
- **Check**: Tag mappings correct
- **Check**: hr-iot publishing to MQTT

**Issue**: Control commands not working
- **Check**: User permissions (iot:scada:control)
- **Check**: MQTT control topic subscription
- **Check**: Backend logs for errors

**Issue**: Slow dashboard performance
- **Check**: Number of data points on dashboard
- **Check**: Browser console for errors
- **Check**: FUXA server CPU usage

---

## Next Actions

### Immediate (This Week)

1. **Stakeholder Review**: Present specification and plan to stakeholders
2. **Approval**: Get sign-off on approach and timeline
3. **Team Assignment**: Assign developers to project
4. **Environment Setup**: Developers set up local development environment

### Short Term (Next 2 Weeks)

1. **Kickoff Meeting**: Align team on objectives and responsibilities
2. **Start Phase 1**: Begin infrastructure setup tasks
3. **FUXA POC**: Create proof-of-concept for JWT auth
4. **Project Tracking**: Set up Jira/GitHub project board

### Medium Term (Next Month)

1. **Complete Phase 1-2**: Infrastructure + FUXA customization
2. **Backend Development**: Start Phase 3 tasks
3. **Weekly Status**: Track progress against timeline
4. **Risk Monitoring**: Review and mitigate identified risks

---

## Contact and Resources

### Documentation

- **FUXA GitHub**: https://github.com/frangoteam/FUXA
- **FUXA Wiki**: https://github.com/frangoteam/FUXA/wiki
- **EMQX Docs**: https://www.emqx.io/docs/en/v5.0/
- **hr-iot Docs**: `CLAUDE.md`, `IOT_MODULE_GUIDE.md`

### Support Channels

- **Slack**: #scada-project channel
- **Email**: scada-team@company.com
- **Weekly Meeting**: Fridays 2-3 PM

### Key Contacts

- **Project Manager**: [Name]
- **Backend Lead**: [Name]
- **Frontend Lead**: [Name]
- **DevOps**: [Name]
- **Product Owner**: [Name]

---

## Appendix

### Glossary

- **SCADA**: Supervisory Control and Data Acquisition
- **HMI**: Human-Machine Interface
- **FUXA**: Web-based SCADA platform (open source)
- **MQTT**: Message Queuing Telemetry Transport protocol
- **Tag**: Named data point in SCADA (e.g., "Pump1_Status")
- **SVG**: Scalable Vector Graphics

### Useful Commands

**MQTT Testing**:
```bash
# Subscribe to all device topics
mosquitto_sub -h localhost -t 'iot/+/device/+/properties'

# Publish test message
mosquitto_pub -h localhost -t 'iot/1/device/100/properties' \
  -m '{"tagName":"Pump1_Status","value":true,"timestamp":1234567890}'
```

**Docker Compose**:
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f fuxa-server

# Restart service
docker-compose restart fuxa-server

# Stop all
docker-compose down
```

**FUXA API**:
```bash
# Get dashboards
curl http://localhost:1881/api/project

# Get tags
curl http://localhost:1881/api/tags
```

---

**Last Updated**: 2026-01-03
**Document Version**: 1.0
**Status**: Ready for Kickoff
