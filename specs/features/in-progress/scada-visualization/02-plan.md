# SCADA/HMI Visualization System - Architecture Plan

> **Status**: In Progress
> **Created**: 2026-01-03
> **Based On**: 01-specification.md
> **Architecture Type**: Microservices + Micro-Frontend

---

## Architecture Overview

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    User Browser                                  │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  hr-vue3 (Main Vue 3 Application)                          │ │
│  │                                                              │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │  SCADA Module (iframe or Web Component)               │  │ │
│  │  │  ┌──────────────────────────────────────────────┐    │  │ │
│  │  │  │  FUXA Client (Angular - Embedded)            │    │  │ │
│  │  │  │  - Dashboard Renderer                         │    │  │ │
│  │  │  │  - SVG Graphics Engine                        │    │  │ │
│  │  │  │  - Real-time Data Binding                     │    │  │ │
│  │  │  └──────────────────────────────────────────────┘    │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  │                                                              │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │  SCADA Configuration Panel (Vue 3 Component)         │  │ │
│  │  │  - Dashboard Selector                                 │  │ │
│  │  │  - Device Mapping                                     │  │ │
│  │  │  - Permission Settings                                │  │ │
│  │  └──────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
└───────────────────┬──────────────────────────────────────────────┘
                    │ HTTP/WebSocket
                    │
┌───────────────────▼──────────────────────────────────────────────┐
│                  Nginx Reverse Proxy                              │
│                  (API Gateway)                                    │
└────────────┬──────────────────────────┬──────────────────────────┘
             │                          │
       ┌─────▼──────┐          ┌───────▼────────┐
       │  hr-iot    │          │  FUXA Server   │
       │  Backend   │          │  (Node.js)     │
       │ (Spring    │          │                │
       │  Boot)     │          │  ┌──────────┐  │
       │            │◀────────▶│  │  SCADA   │  │
       │  ┌──────┐  │  Adapter │  │  Bridge  │  │
       │  │SCADA │  │  Service │  │  Service │  │
       │  │API   │  │          │  └──────────┘  │
       │  └──────┘  │          │                │
       └────┬───────┘          │  ┌──────────┐  │
            │                  │  │  MQTT    │  │
            │                  │  │  Client  │  │
            │                  │  └──────────┘  │
            │                  └────────┬───────┘
            │                           │
       ┌────▼───────────────────────────▼───────┐
       │         MQTT Broker (EMQX)              │
       │         - Topic: iot/{tenant}/device/*  │
       │         - Real-time message routing     │
       └────────────────────────────────────────┘
            │                           │
       ┌────▼────────┐         ┌───────▼────────┐
       │   MySQL     │         │   TDengine     │
       │  (Metadata) │         │  (Time-series) │
       └─────────────┘         └────────────────┘
       ┌─────────────┐
       │   Redis     │
       │ (Real-time) │
       └─────────────┘
```

---

## Design Decisions

### Decision 1: FUXA Integration Approach

**Options Considered**:

**Option A: Full FUXA Fork and Modification**
- Clone FUXA repository
- Modify to integrate directly with hr-iot APIs
- Rebuild FUXA with hr-iot authentication

*Pros*:
- Deep integration
- Full control over features

*Cons*:
- High maintenance burden
- Difficult to upgrade FUXA
- Large development effort

**Option B: FUXA as Separate Service (SELECTED)**
- Deploy FUXA as independent Docker container
- Create adapter/bridge service between FUXA and hr-iot
- Embed FUXA client in hr-vue3 via iframe

*Pros*:
- Clean separation of concerns
- Easier FUXA upgrades
- Minimal FUXA code changes

*Cons*:
- Additional service to manage
- Cross-origin complexity

**Option C: Replace FUXA with Custom Solution**
- Build SCADA frontend from scratch in Vue 3
- Use existing SVG libraries

*Pros*:
- Full control
- Native Vue 3 integration

*Cons*:
- Massive development effort (6-12 months)
- Reinventing the wheel

**DECISION**: **Option B** - FUXA as Separate Service

**Rationale**:
- Leverages existing FUXA capabilities
- Faster time to market
- Maintainable long-term
- Allows incremental customization

---

### Decision 2: Real-Time Data Transport

**Options Considered**:

**Option A: MQTT Protocol (SELECTED)**
- FUXA subscribes to MQTT topics
- hr-iot publishes device data to MQTT broker
- Standard IoT protocol

*Pros*:
- Native FUXA support
- Industry standard for IoT
- Efficient pub-sub model
- Scalable

*Cons*:
- Requires MQTT broker infrastructure
- Additional component to manage

**Option B: Custom WebSocket**
- Direct WebSocket connection FUXA ↔ hr-iot
- Custom protocol

*Pros*:
- No additional broker needed
- Direct connection

*Cons*:
- Need to implement in FUXA
- Less scalable

**Option C: HTTP Polling**
- FUXA polls hr-iot REST APIs

*Pros*:
- Simple to implement

*Cons*:
- High latency (seconds)
- Inefficient (constant polling)
- High server load

**DECISION**: **Option A** - MQTT Protocol

**Rationale**:
- FUXA has built-in MQTT client
- Aligns with IoT best practices
- Scalable for future growth
- Low latency (< 1 second)

---

### Decision 3: Authentication Strategy

**Options Considered**:

**Option A: Shared JWT Token (SELECTED)**
- hr-vue3 passes JWT token to FUXA iframe
- FUXA validates token against hr-iot backend
- Single sign-on experience

*Pros*:
- Seamless user experience
- No separate login
- Centralized authentication

*Cons*:
- Requires FUXA modification for JWT validation

**Option B: Separate FUXA Authentication**
- FUXA uses its own user database
- Users login separately

*Pros*:
- No FUXA code changes

*Cons*:
- Poor user experience (double login)
- User management duplication

**DECISION**: **Option A** - Shared JWT Token

**Rationale**:
- Critical for good UX
- Aligns with SSO requirements
- Acceptable FUXA modification effort

---

### Decision 4: Multi-Tenancy Approach

**Options Considered**:

**Option A: Separate FUXA Instance Per Tenant**
- Deploy dedicated FUXA container for each tenant
- Complete isolation

*Pros*:
- Perfect tenant isolation
- Simple implementation

*Cons*:
- High resource usage
- Difficult to manage 100+ tenants

**Option B: Single FUXA with Tenant Filtering (SELECTED)**
- One FUXA instance serves all tenants
- Add tenant_id to all MQTT topics
- FUXA filters by tenant in subscription

*Pros*:
- Resource efficient
- Centralized management

*Cons*:
- Requires careful implementation
- Security risk if not done correctly

**DECISION**: **Option B** - Single FUXA with Tenant Filtering

**Rationale**:
- Scalable solution
- Acceptable with proper security testing
- Standard multi-tenant pattern

**Security Measures**:
- MQTT topic structure: `iot/{tenant_id}/device/{device_id}/*`
- FUXA subscribes only to authorized tenant topics
- Backend validates tenant access on all API calls

---

## Component Design

### 1. FUXA Server (Docker Container)

**Technology**: Node.js 18+ with TypeScript

**Responsibilities**:
- Render SCADA dashboards
- Handle WebSocket connections for real-time updates
- Manage MQTT subscriptions
- Store dashboard configurations

**Key Modifications Needed**:
1. **JWT Authentication Middleware**
   ```typescript
   // server/runtime/authentication.ts
   export function validateJwtToken(token: string): Promise<UserContext> {
     // Validate against hr-iot backend
     return axios.post('http://hr-iot-backend/api/auth/validate', { token })
   }
   ```

2. **Tenant-Aware MQTT Subscription**
   ```typescript
   // server/runtime/devices/mqtt/index.ts
   const topic = `iot/${tenantId}/device/+/properties`
   client.subscribe(topic)
   ```

3. **Dashboard Storage Backend**
   - Replace SQLite with MySQL (hr-iot database)
   - Store in table: `scada_dashboard`

**Docker Configuration**:
```dockerfile
FROM node:18-alpine

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

EXPOSE 1881

CMD ["npm", "start"]
```

**Environment Variables**:
```env
FUXA_PORT=1881
FUXA_AUTH_TYPE=jwt
FUXA_AUTH_ENDPOINT=http://hr-iot-backend:48080/api/auth/validate
MQTT_BROKER_URL=mqtt://mqtt-broker:1883
DATABASE_TYPE=mysql
DATABASE_HOST=mysql
DATABASE_PORT=3306
DATABASE_NAME=hr_iot
DATABASE_USER=fuxa_user
DATABASE_PASSWORD=***
```

---

### 2. SCADA Bridge Service (Spring Boot)

**Purpose**: Adapter between hr-iot and FUXA/MQTT

**Location**: `yudao-module-iot/yudao-module-iot-biz/src/main/java/.../iot/service/scada/`

**Responsibilities**:
- Publish device property changes to MQTT
- Translate hr-iot data format to SCADA tags
- Handle control commands from FUXA
- Provide SCADA-specific REST APIs

**Key Classes**:

```java
@Service
public class ScadaBridgeService {

    @Autowired
    private MqttTemplate mqttTemplate;

    @Autowired
    private IotDeviceService deviceService;

    /**
     * Publish device property update to MQTT
     */
    public void publishPropertyUpdate(Long tenantId, Long deviceId,
                                       String property, Object value) {
        String topic = String.format("iot/%d/device/%d/properties",
                                     tenantId, deviceId);

        ScadaTagUpdate update = new ScadaTagUpdate();
        update.setTagName(deviceId + "." + property);
        update.setValue(value);
        update.setTimestamp(System.currentTimeMillis());

        mqttTemplate.convertAndSend(topic, update);
    }

    /**
     * Handle control command from SCADA
     */
    @Transactional
    public void handleControlCommand(ScadaControlCommand command) {
        // Validate permissions
        validateControlPermission(command.getTenantId(),
                                  command.getDeviceId(),
                                  command.getUserId());

        // Execute command via device service
        deviceService.executeCommand(command.getDeviceId(),
                                      command.getCommandName(),
                                      command.getParameters());

        // Audit log
        auditLog(command);
    }
}
```

**MQTT Configuration**:
```java
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(
                "hr-iot-bridge", mqttClientFactory(),
                "iot/+/device/+/control");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }
}
```

---

### 3. SCADA REST API (Controller Layer)

**File**: `IotScadaController.java`

**Endpoints**:

```java
@Tag(name = "管理后台 - SCADA 可视化")
@RestController
@RequestMapping("/iot/scada")
@Validated
public class IotScadaController {

    @Autowired
    private ScadaBridgeService scadaBridgeService;

    /**
     * Get SCADA device list with real-time values
     */
    @GetMapping("/devices")
    @Operation(summary = "获取 SCADA 设备列表")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaDeviceVO>> getScadaDevices() {
        return success(scadaBridgeService.getScadaDevices());
    }

    /**
     * Get real-time values for specific device
     */
    @GetMapping("/devices/{id}/realtime")
    @Operation(summary = "获取设备实时数据")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<Map<String, Object>> getDeviceRealtime(
            @PathVariable("id") Long deviceId) {
        return success(scadaBridgeService.getDeviceRealtime(deviceId));
    }

    /**
     * Get historical data for device property
     */
    @GetMapping("/devices/{id}/history")
    @Operation(summary = "获取设备历史数据")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaHistoryPoint>> getDeviceHistory(
            @PathVariable("id") Long deviceId,
            @RequestParam("property") String property,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime) {
        return success(scadaBridgeService.getDeviceHistory(
            deviceId, property, startTime, endTime));
    }

    /**
     * Send control command to device
     */
    @PostMapping("/devices/{id}/control")
    @Operation(summary = "发送控制命令")
    @PreAuthorize("@ss.hasPermission('iot:scada:control')")
    public CommonResult<Boolean> controlDevice(
            @PathVariable("id") Long deviceId,
            @Valid @RequestBody ScadaControlCommand command) {
        scadaBridgeService.handleControlCommand(command);
        return success(true);
    }

    /**
     * Get active alarms
     */
    @GetMapping("/alarms")
    @Operation(summary = "获取活动告警")
    @PreAuthorize("@ss.hasPermission('iot:scada:query')")
    public CommonResult<List<ScadaAlarmVO>> getActiveAlarms() {
        return success(scadaBridgeService.getActiveAlarms());
    }

    /**
     * Acknowledge alarm
     */
    @PostMapping("/alarms/{id}/ack")
    @Operation(summary = "确认告警")
    @PreAuthorize("@ss.hasPermission('iot:scada:control')")
    public CommonResult<Boolean> acknowledgeAlarm(
            @PathVariable("id") Long alarmId) {
        scadaBridgeService.acknowledgeAlarm(alarmId);
        return success(true);
    }
}
```

---

### 4. Frontend: SCADA Module (Vue 3)

**Location**: `hr-vue3/src/views/iot/scada/`

**Component Structure**:

```
hr-vue3/src/views/iot/scada/
├── index.vue                    # Main SCADA page
├── components/
│   ├── FuxaEmbed.vue            # FUXA iframe wrapper
│   ├── DashboardSelector.vue    # Dashboard switcher
│   ├── ConnectionStatus.vue     # WebSocket status indicator
│   └── ControlPanel.vue         # Quick control panel
├── composables/
│   └── useFuxaAuth.ts           # Authentication logic
└── types/
    └── scada.ts                 # TypeScript type definitions
```

**Main Page Component** (`index.vue`):

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import FuxaEmbed from './components/FuxaEmbed.vue'
import DashboardSelector from './components/DashboardSelector.vue'
import ConnectionStatus from './components/ConnectionStatus.vue'

const userStore = useUserStore()
const selectedDashboard = ref('water-pump-system')
const fuxaUrl = ref('')

onMounted(() => {
  // Generate FUXA URL with authentication token
  const token = userStore.token
  const tenantId = userStore.tenantId
  fuxaUrl.value = `${import.meta.env.VITE_FUXA_URL}?token=${token}&tenant=${tenantId}&dashboard=${selectedDashboard.value}`
})

const handleDashboardChange = (dashboardId: string) => {
  selectedDashboard.value = dashboardId
  // Reload FUXA iframe with new dashboard
}
</script>

<template>
  <div class="scada-container">
    <!-- Header -->
    <div class="scada-header">
      <dashboard-selector
        v-model="selectedDashboard"
        @change="handleDashboardChange"
      />
      <connection-status />
    </div>

    <!-- FUXA Embed -->
    <div class="scada-content">
      <fuxa-embed :url="fuxaUrl" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.scada-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .scada-header {
    height: 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid var(--el-border-color);
  }

  .scada-content {
    flex: 1;
    overflow: hidden;
  }
}
</style>
```

**FUXA Embed Component** (`FuxaEmbed.vue`):

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  url: string
}

const props = defineProps<Props>()
const iframeRef = ref<HTMLIFrameElement>()

// Listen for messages from FUXA iframe
window.addEventListener('message', (event) => {
  // Validate origin
  if (event.origin !== import.meta.env.VITE_FUXA_URL) {
    return
  }

  // Handle FUXA events
  const { type, data } = event.data

  switch (type) {
    case 'FUXA_READY':
      console.log('FUXA loaded successfully')
      break
    case 'CONTROL_COMMAND':
      // Handle control command (additional validation)
      handleControlCommand(data)
      break
    case 'ALARM_EVENT':
      // Show notification
      showAlarmNotification(data)
      break
  }
})

const handleControlCommand = (data: any) => {
  // Additional permission check or confirmation
}

const showAlarmNotification = (data: any) => {
  ElNotification({
    title: 'Alarm',
    message: data.message,
    type: 'warning',
    duration: 0
  })
}

watch(() => props.url, () => {
  // Reload iframe when URL changes
})
</script>

<template>
  <iframe
    ref="iframeRef"
    :src="url"
    frameborder="0"
    class="fuxa-iframe"
    sandbox="allow-same-origin allow-scripts allow-forms"
  />
</template>

<style scoped lang="scss">
.fuxa-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
```

---

### 5. Database Schema

**Table: scada_dashboard**

```sql
CREATE TABLE `scada_dashboard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) NOT NULL COMMENT '仪表板名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `dashboard_type` varchar(50) NOT NULL COMMENT '类型: water, hvac, energy, custom',
  `config_json` text NOT NULL COMMENT 'FUXA dashboard configuration (JSON)',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图 URL',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认仪表板',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',

  -- Standard fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_type` (`dashboard_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 仪表板配置表';
```

**Table: scada_tag_mapping**

```sql
CREATE TABLE `scada_tag_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_name` varchar(255) NOT NULL COMMENT 'SCADA tag 名称 (e.g., Pump1_Status)',
  `device_id` bigint(20) NOT NULL COMMENT '设备 ID',
  `property_identifier` varchar(100) NOT NULL COMMENT '物模型属性标识符',
  `data_type` varchar(50) NOT NULL COMMENT '数据类型: number, string, boolean',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `scale_factor` decimal(10,4) DEFAULT '1.0000' COMMENT '缩放系数',
  `offset` decimal(10,4) DEFAULT '0.0000' COMMENT '偏移量',

  -- Standard fields
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_tag` (`tenant_id`, `tag_name`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA Tag 映射表';
```

**Table: scada_control_log**

```sql
CREATE TABLE `scada_control_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` bigint(20) NOT NULL COMMENT '设备 ID',
  `command_name` varchar(100) NOT NULL COMMENT '命令名称',
  `command_params` varchar(1000) DEFAULT NULL COMMENT '命令参数 (JSON)',
  `execution_status` tinyint(4) NOT NULL COMMENT '执行状态: 1-成功, 0-失败',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `user_id` bigint(20) NOT NULL COMMENT '操作用户 ID',
  `user_name` varchar(64) NOT NULL COMMENT '操作用户名称',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端 IP',

  -- Standard fields
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tenant_id` bigint(20) NOT NULL DEFAULT '0',

  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_tenant_create_time` (`tenant_id`, `create_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 控制操作日志';
```

---

## Data Flow Diagrams

### Real-Time Data Flow

```
┌─────────────┐
│   Device    │ (Physical equipment: pump, sensor)
│  Hardware   │
└──────┬──────┘
       │ (Modbus/MQTT/OPC-UA)
       ▼
┌─────────────┐
│  IoT        │
│  Gateway    │ (Protocol translation)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  hr-iot     │ 1. Store in MySQL/TDengine
│  Backend    │ 2. Cache in Redis
└──────┬──────┘ 3. Publish to MQTT
       │
       ▼
┌─────────────┐
│    MQTT     │ Topic: iot/{tenant}/device/{id}/properties
│   Broker    │ Message: {tagName: value, timestamp: ...}
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    FUXA     │ 1. Subscribe to topics
│   Server    │ 2. Update internal tag database
└──────┬──────┘ 3. Push to connected clients via WebSocket
       │
       ▼
┌─────────────┐
│    FUXA     │ 1. Receive tag updates
│   Client    │ 2. Update SVG graphics
│  (Browser)  │ 3. Refresh displayed values
└─────────────┘
```

### Control Command Flow

```
┌─────────────┐
│  Operator   │ (Click "Start Pump" button)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    FUXA     │ 1. Validate permissions (client-side)
│   Client    │ 2. Show confirmation dialog
└──────┬──────┘ 3. Send command via WebSocket
       │
       ▼
┌─────────────┐
│    FUXA     │ 1. Receive command
│   Server    │ 2. Publish to MQTT control topic
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    MQTT     │ Topic: iot/{tenant}/device/{id}/control
│   Broker    │ Message: {command: "start", params: {...}}
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  SCADA      │ 1. Subscribe to control topics
│  Bridge     │ 2. Validate tenant + permissions (server-side)
│  Service    │ 3. Translate to hr-iot command format
└──────┬──────┘ 4. Call device service
       │        5. Log audit trail
       ▼
┌─────────────┐
│  Device     │ 1. Execute command
│  Service    │ 2. Update device state in database
└──────┬──────┘ 3. Send command to physical device via gateway
       │
       ▼
┌─────────────┐
│  IoT        │ Send command to device (Modbus/MQTT/etc.)
│  Gateway    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Device    │ Execute physical action (start pump motor)
│  Hardware   │
└─────────────┘
```

---

## Security Architecture

### Authentication Flow

```
1. User logs into hr-vue3
   ├─ Spring Security validates credentials
   └─ JWT token generated

2. User navigates to SCADA page
   ├─ hr-vue3 passes JWT token to FUXA iframe URL
   └─ FUXA receives token

3. FUXA validates token
   ├─ POST request to hr-iot: /api/auth/validate
   ├─ hr-iot verifies JWT signature and expiration
   ├─ Returns user info + tenant_id + permissions
   └─ FUXA creates session

4. FUXA subscribes to MQTT topics
   ├─ Filter by tenant_id: iot/{tenant_id}/device/+/properties
   └─ Only receives authorized data

5. Control commands validated
   ├─ Client-side: Check user has 'iot:scada:control' permission
   ├─ FUXA server-side: Verify token still valid
   └─ hr-iot backend: Final permission check before execution
```

### Tenant Isolation

**MQTT Topic Structure**:
```
iot/{tenant_id}/device/{device_id}/properties    # Real-time data
iot/{tenant_id}/device/{device_id}/control       # Control commands
iot/{tenant_id}/alarms                           # Alarm events
```

**Subscription Filtering**:
- FUXA subscribes only to topics matching user's tenant_id
- MQTT broker ACL enforces topic permissions
- hr-iot backend double-checks tenant ownership on all API calls

**Data Isolation**:
- Dashboard configs stored with tenant_id foreign key
- All database queries filtered by tenant_id
- Redis keys prefixed with tenant_id

---

## Performance Optimization

### Caching Strategy

**Level 1: Browser Cache**
- FUXA client caches SVG graphics and dashboard configs
- Cache invalidation on dashboard update

**Level 2: Redis Cache** (hr-iot backend)
- Key: `scada:device:{tenant_id}:{device_id}:realtime`
- TTL: 5 seconds
- Stores latest device property values

**Level 3: FUXA In-Memory Cache**
- Tag database in FUXA server memory
- Updated via MQTT subscriptions
- Serves WebSocket clients with minimal latency

### Data Aggregation

**Historical Data Queries**:
- For time ranges > 24 hours, use downsampled data
- TDengine aggregation functions: `avg`, `max`, `min` by interval
- Example: 7-day chart shows hourly averages instead of raw points

**Pagination**:
- Device lists paginated (100 devices per page)
- Alarm lists paginated (50 alarms per page)

### WebSocket Optimization

**Message Batching**:
- Batch multiple tag updates into single WebSocket message
- Send updates every 1 second (not per change)
- Reduces message overhead

**Selective Subscriptions**:
- Only subscribe to tags visible on current dashboard
- Unsubscribe when switching dashboards
- Reduces unnecessary data transfer

---

## Deployment Architecture

### Docker Compose Configuration

```yaml
version: '3.8'

services:
  # MQTT Broker
  mqtt-broker:
    image: emqx/emqx:5.0
    ports:
      - "1883:1883"      # MQTT
      - "8083:8083"      # WebSocket
      - "18083:18083"    # Dashboard
    environment:
      - EMQX_NAME=hr-iot-mqtt
      - EMQX_HOST=mqtt-broker
    volumes:
      - ./emqx/data:/opt/emqx/data
      - ./emqx/log:/opt/emqx/log
    networks:
      - hr-iot-network

  # FUXA Server
  fuxa-server:
    build:
      context: ./fuxa
      dockerfile: Dockerfile
    ports:
      - "1881:1881"
    environment:
      - FUXA_PORT=1881
      - FUXA_AUTH_TYPE=jwt
      - FUXA_AUTH_ENDPOINT=http://hr-iot-backend:48080/api/auth/validate
      - MQTT_BROKER_URL=mqtt://mqtt-broker:1883
      - DATABASE_TYPE=mysql
      - DATABASE_HOST=mysql
      - DATABASE_PORT=3306
      - DATABASE_NAME=hr_iot
      - DATABASE_USER=fuxa_user
      - DATABASE_PASSWORD=${FUXA_DB_PASSWORD}
    depends_on:
      - mqtt-broker
      - mysql
    networks:
      - hr-iot-network

  # Nginx Reverse Proxy
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - fuxa-server
      - hr-iot-backend
    networks:
      - hr-iot-network

  # Existing hr-iot services
  hr-iot-backend:
    # ... existing configuration
    networks:
      - hr-iot-network

  mysql:
    # ... existing configuration
    networks:
      - hr-iot-network

  redis:
    # ... existing configuration
    networks:
      - hr-iot-network

  tdengine:
    # ... existing configuration
    networks:
      - hr-iot-network

networks:
  hr-iot-network:
    driver: bridge
```

### Nginx Configuration

```nginx
upstream fuxa_backend {
    server fuxa-server:1881;
}

upstream hriot_backend {
    server hr-iot-backend:48080;
}

server {
    listen 80;
    server_name your-domain.com;

    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # FUXA frontend and WebSocket
    location /scada/ {
        proxy_pass http://fuxa_backend/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket timeouts
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    # hr-iot backend API
    location /api/ {
        proxy_pass http://hriot_backend/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # hr-vue3 frontend (static files)
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
}
```

---

## Migration and Rollout Strategy

### Phase 1: Infrastructure Setup (Week 1-2)

**Tasks**:
1. Deploy MQTT broker (EMQX)
2. Configure Docker containers
3. Set up Nginx reverse proxy
4. Configure SSL certificates

**Deliverables**:
- MQTT broker accessible and tested
- Docker Compose working locally
- Nginx routing verified

**Testing**:
- MQTT pub/sub test with mosquitto_pub/sub
- WebSocket connection test
- SSL certificate validation

### Phase 2: FUXA Customization (Week 3-4)

**Tasks**:
1. Clone FUXA repository
2. Implement JWT authentication middleware
3. Add tenant filtering to MQTT subscriptions
4. Modify database layer for MySQL integration
5. Build custom Docker image

**Deliverables**:
- FUXA Docker image with customizations
- JWT validation working
- Tenant isolation tested

**Testing**:
- Authentication flow end-to-end
- Multi-tenant data isolation test
- Load test with 100 simulated users

### Phase 3: Backend Integration (Week 5-6)

**Tasks**:
1. Implement SCADA Bridge Service
2. Create SCADA REST API endpoints
3. Configure MQTT publishing for device updates
4. Create database tables
5. Implement control command handling

**Deliverables**:
- SCADA API endpoints operational
- MQTT publishing working
- Database migrations applied
- Unit tests passing

**Testing**:
- API endpoint testing via Swagger
- MQTT message flow verification
- Control command execution test
- Audit logging verification

### Phase 4: Frontend Integration (Week 7-8)

**Tasks**:
1. Create SCADA module in hr-vue3
2. Implement FUXA iframe embedding
3. Add dashboard selector component
4. Create connection status indicator
5. Add route configuration

**Deliverables**:
- SCADA page accessible from main menu
- Dashboard selector working
- Authentication seamless (SSO)

**Testing**:
- Navigation testing
- Token passing verification
- Responsive design testing (desktop/tablet)

### Phase 5: Dashboard Creation (Week 9-10)

**Tasks**:
1. Design water pump system dashboard SVG
2. Configure tag mappings
3. Add real-time data bindings
4. Implement control buttons
5. Add historical trend charts
6. Create alarm indicators

**Deliverables**:
- Water pump dashboard complete (as per screenshot)
- Tag mappings configured for 20+ data points
- Control buttons functional

**Testing**:
- Real-time data display verification
- Control command execution
- Historical trend chart loading
- Alarm indicator testing

### Phase 6: Testing and Optimization (Week 11)

**Tasks**:
1. Performance testing (load, stress)
2. Security testing (penetration, vulnerability scan)
3. User acceptance testing
4. Bug fixes and optimization

**Deliverables**:
- Performance test report
- Security audit report
- UAT sign-off

**Testing**:
- 100 concurrent users load test
- Security scan with OWASP ZAP
- End-to-end user scenarios

### Phase 7: Production Deployment (Week 12)

**Tasks**:
1. Production environment setup
2. Data migration (if any)
3. User training
4. Go-live

**Deliverables**:
- Production deployment complete
- User training materials
- Monitoring dashboards
- Runbook documentation

---

## Monitoring and Observability

### Metrics to Monitor

**FUXA Server**:
- WebSocket connections (current, total)
- MQTT messages processed (rate, errors)
- Dashboard load time (p50, p95, p99)
- Memory usage, CPU usage

**SCADA Bridge Service**:
- MQTT publish rate (messages/second)
- Control command execution time
- API endpoint response time
- Error rate

**MQTT Broker**:
- Connected clients
- Message throughput (in/out)
- Topic subscriptions
- Queue depths

### Logging Strategy

**Log Levels**:
- **ERROR**: Control command failures, authentication errors, system errors
- **WARN**: Connection losses, retry attempts, high latency
- **INFO**: User logins, dashboard loads, control commands
- **DEBUG**: Detailed message flow (disabled in production)

**Log Aggregation**:
- Use ELK Stack (Elasticsearch, Logstash, Kibana) or similar
- Centralized logging for all services
- Log retention: 30 days

### Alerting

**Critical Alerts** (immediate response):
- FUXA server down
- MQTT broker down
- Database connection lost
- Authentication service unavailable

**Warning Alerts** (respond within 1 hour):
- High WebSocket connection latency (> 5s)
- MQTT message queue buildup (> 1000 messages)
- Disk space low (< 10%)
- High error rate (> 5%)

---

## Backup and Disaster Recovery

### Data Backup

**Dashboard Configurations**:
- Backed up daily to S3/NAS
- Retention: 30 days
- Includes: scada_dashboard table, tag mappings

**Control Logs**:
- Archived monthly to cold storage
- Retention: 1 year for compliance

**FUXA State**:
- Not critical (can be recreated from database)
- Optional backup of FUXA data directory

### Recovery Procedures

**Scenario 1: FUXA Server Failure**
1. Start new FUXA container from image
2. Restore dashboard configs from backup
3. Verify MQTT connection
4. Estimated RTO: 15 minutes

**Scenario 2: Database Corruption**
1. Restore from latest MySQL backup
2. Replay MQTT messages from broker (if available)
3. Verify data integrity
4. Estimated RTO: 30 minutes

**Scenario 3: Complete System Failure**
1. Restore infrastructure from IaC (Terraform/Docker Compose)
2. Restore database from backup
3. Redeploy all services
4. Estimated RTO: 2 hours

---

## Compliance and Documentation

### Compliance Requirements

**Data Privacy**:
- GDPR compliance for EU operations
- Tenant data isolation
- Right to erasure (delete user data)

**Audit Trail**:
- All control commands logged with timestamp, user, IP
- Retention: 1 year minimum
- Exportable format (CSV)

**Security Standards**:
- OWASP Top 10 compliance
- Regular vulnerability scanning
- Penetration testing annually

### Documentation Deliverables

1. **User Manual** (30+ pages)
   - Dashboard navigation
   - Reading real-time data
   - Executing controls
   - Understanding alarms
   - FAQ

2. **Administrator Guide** (20+ pages)
   - System architecture overview
   - Dashboard designer tutorial
   - Tag mapping configuration
   - User and permission management
   - Troubleshooting

3. **API Documentation**
   - OpenAPI/Swagger spec for SCADA APIs
   - Example requests/responses
   - Authentication guide

4. **Operations Runbook** (15+ pages)
   - Deployment procedures
   - Backup and restore procedures
   - Monitoring and alerting
   - Incident response
   - Escalation contacts

5. **Developer Guide** (25+ pages)
   - Architecture overview
   - Code structure
   - FUXA customizations
   - Adding new dashboards
   - Extending functionality

---

## Risk Mitigation Plan

### Technical Risks

**Risk: FUXA Performance Degradation**
- **Mitigation**: Load testing before deployment
- **Contingency**: Horizontal scaling (multiple FUXA instances with load balancer)

**Risk: MQTT Broker Overload**
- **Mitigation**: Message rate limiting, QoS configuration
- **Contingency**: Clustered MQTT broker setup

**Risk: Data Synchronization Lag**
- **Mitigation**: Optimize MQTT message size, use compression
- **Contingency**: Fallback to HTTP polling if MQTT fails

### Operational Risks

**Risk: Insufficient Training**
- **Mitigation**: Comprehensive training program, user manuals
- **Contingency**: On-call support during initial rollout

**Risk: User Resistance to New UI**
- **Mitigation**: User involvement in design, pilot testing
- **Contingency**: Gradual rollout, keep old interface temporarily

---

## Next Steps

After architecture approval, proceed to **Task Breakdown** (`/speckit.tasks`) to create detailed implementation tasks for each phase.

---

**Last Updated**: 2026-01-03
**Version**: 1.0
**Architecture Approved By**: [Pending]
