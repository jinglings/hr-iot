# 在线调试工具 - 功能规格说明

> **Status**: In Progress
> **Created**: 2026-01-11
> **Last Updated**: 2026-01-11
> **Developers**: TBD

---

## Overview

在线调试工具为开发者和运维人员提供设备调试能力，包括模拟设备上报数据、在线下发指令、实时查看通信日志、物模型属性测试等功能。

**Feature Scope**:
- Backend: 调试消息发送/接收接口、设备消息日志查询、物模型调试接口
- Frontend: 调试面板UI、实时日志WebSocket、物模型测试界面

**User Value**:
- 快速验证设备通信是否正常
- 无需物理设备即可模拟测试
- 实时查看设备消息收发情况
- 降低开发调试成本

---

## Requirements

### Functional Requirements

**FR1**: 用户可以模拟设备上报属性数据（输入属性值，系统按物模型格式发送）
**FR2**: 用户可以向设备下发属性设置指令，查看响应结果
**FR3**: 用户可以调用设备服务（同步/异步），查看调用结果
**FR4**: 用户可以实时查看设备消息日志（上下行消息、时间戳、内容）
**FR5**: 用户可以模拟事件上报
**FR6**: 调试日志支持按设备、时间范围、消息类型过滤
**FR7**: 支持导出调试日志

### Non-Functional Requirements

**NFR1 - Performance**: 实时日志延迟 < 1s，日志查询响应 < 500ms
**NFR2 - Security**: 调试操作需要特殊权限，记录操作日志
**NFR3 - Usability**: 界面直观，支持 JSON 格式化显示
**NFR4 - Multi-Tenancy**: 只能调试本租户的设备

### Out of Scope

- 设备模拟器（完整虚拟设备）- 后续版本
- 自动化测试脚本 - 后续版本
- 消息录制重放 - 后续版本

---

## Architecture Overview

### System Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Frontend (Vue 3)                                               │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ DeviceDebugPanel                                            ││
│  │ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐││
│  │ │ 属性调试    │ │ 服务调用    │ │ 实时日志 (WebSocket)   │││
│  │ │ - 上报模拟  │ │ - 服务列表  │ │ - 上行消息            │││
│  │ │ - 设置下发  │ │ - 参数输入  │ │ - 下行消息            │││
│  │ │ - 响应查看  │ │ - 结果查看  │ │ - 时间过滤            │││
│  │ └─────────────┘ └─────────────┘ └─────────────────────────┘││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
            │                    │                   │
            │ REST API           │ REST API          │ WebSocket
            ▼                    ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│  Backend (Spring Boot)                                          │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ IotDeviceDebugController                                    ││
│  │ - POST /property-report      模拟上报属性                   ││
│  │ - POST /property-set         下发属性设置                   ││
│  │ - POST /service-invoke       调用服务                       ││
│  │ - POST /event-report         模拟事件上报                   ││
│  │ - GET  /message-log          查询消息日志                   ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ DeviceDebugWebSocketHandler                                 ││
│  │ - 设备消息实时推送                                           ││
│  │ - 订阅指定设备的消息                                         ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ MessageBus Integration                                      ││
│  │ - 拦截设备消息用于调试展示                                   ││
│  │ - 模拟消息注入                                               ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 1: Backend Implementation

### Database Schema

#### Table: `iot_device_debug_log` - 调试日志表

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | 主键 | Auto-increment |
| device_id | bigint(20) | 设备ID | |
| product_id | bigint(20) | 产品ID | |
| direction | tinyint(4) | 消息方向 | 1=上行, 2=下行 |
| message_type | varchar(32) | 消息类型 | PROPERTY_REPORT/PROPERTY_SET/... |
| topic | varchar(255) | MQTT Topic | |
| payload | text | 消息内容 | JSON格式 |
| result_code | int(11) | 结果码 | 0=成功 |
| result_message | varchar(255) | 结果信息 | |
| debug_type | tinyint(4) | 调试类型 | 1=真实消息, 2=模拟消息 |
| operator_id | bigint(20) | 操作者ID | 模拟消息时记录 |
| tenant_id | bigint(20) | 租户ID | |
| create_time | datetime | 创建时间 | |

**索引**:
- `idx_device_id_create_time` (device_id, create_time)
- `idx_tenant_id` (tenant_id)

**注意**: 调试日志可考虑存储在 TDengine 中以支持高频写入和时间范围查询

### API Endpoints

| Method | Endpoint | Permission | Purpose |
|--------|----------|------------|---------|
| POST | `/iot/device-debug/property-report` | `iot:device-debug:operate` | 模拟设备上报属性 |
| POST | `/iot/device-debug/property-set` | `iot:device-debug:operate` | 下发属性设置 |
| POST | `/iot/device-debug/service-invoke` | `iot:device-debug:operate` | 调用设备服务 |
| POST | `/iot/device-debug/event-report` | `iot:device-debug:operate` | 模拟事件上报 |
| GET | `/iot/device-debug/message-log` | `iot:device-debug:query` | 查询消息日志 |
| GET | `/iot/device-debug/thing-model` | `iot:device-debug:query` | 获取物模型供调试 |
| WebSocket | `/ws/iot/device-debug/{deviceId}` | - | 实时消息推送 |

### API Request/Response Examples

**模拟属性上报**:
```json
// Request: POST /iot/device-debug/property-report
{
  "deviceId": 1001,
  "properties": {
    "temperature": 25.5,
    "humidity": 60
  }
}

// Response:
{
  "code": 0,
  "data": {
    "success": true,
    "messageId": "msg_123456",
    "timestamp": "2026-01-11T10:30:00"
  }
}
```

**下发属性设置**:
```json
// Request: POST /iot/device-debug/property-set
{
  "deviceId": 1001,
  "properties": {
    "switch": true,
    "brightness": 80
  },
  "timeout": 5000
}

// Response:
{
  "code": 0,
  "data": {
    "success": true,
    "messageId": "msg_123457",
    "deviceResponse": {
      "code": 0,
      "data": {}
    }
  }
}
```

**调用服务**:
```json
// Request: POST /iot/device-debug/service-invoke
{
  "deviceId": 1001,
  "identifier": "reboot",
  "params": {
    "delay": 10
  },
  "timeout": 10000
}

// Response:
{
  "code": 0,
  "data": {
    "success": true,
    "messageId": "msg_123458",
    "deviceResponse": {
      "code": 0,
      "data": {
        "result": "rebooting"
      }
    }
  }
}
```

### WebSocket Message Format

```json
// 订阅设备消息
{"action": "subscribe", "deviceId": 1001}

// 取消订阅
{"action": "unsubscribe", "deviceId": 1001}

// 服务端推送消息
{
  "type": "device_message",
  "deviceId": 1001,
  "direction": "upstream",
  "messageType": "PROPERTY_REPORT",
  "topic": "/sys/pk/dn/thing/property/post",
  "payload": "{\"temperature\": 25.5}",
  "timestamp": "2026-01-11T10:30:00.123"
}
```

### Key Business Rules

1. **权限控制**: 调试操作需要 `iot:device-debug:operate` 权限
2. **设备校验**: 只能调试本租户下的设备
3. **日志保留**: 调试日志保留 7 天（可配置）
4. **超时处理**: 同步调用默认超时 5 秒
5. **模拟标记**: 模拟消息需标记 debug_type = 2

### Files to Create (Backend)

- [ ] `sql/mysql/iot/iot_device_debug_log.sql`
- [ ] `IotDeviceDebugLogDO.java`
- [ ] `IotDeviceDebugLogMapper.java`
- [ ] `IotDeviceDebugService.java` + `IotDeviceDebugServiceImpl.java`
- [ ] `IotDeviceDebugController.java`
- [ ] `IotDeviceDebugPropertyReportReqVO.java`
- [ ] `IotDeviceDebugPropertySetReqVO.java`
- [ ] `IotDeviceDebugServiceInvokeReqVO.java`
- [ ] `IotDeviceDebugEventReportReqVO.java`
- [ ] `IotDeviceDebugLogPageReqVO.java`
- [ ] `IotDeviceDebugLogRespVO.java`
- [ ] `IotDeviceDebugResultVO.java`
- [ ] `DeviceDebugWebSocketHandler.java`

---

## Part 2: Frontend Implementation

### Page Structure

**设备调试面板**: `src/views/iot/device/debug/index.vue`
- 设备选择器
- Tab 切换（属性调试/服务调用/事件模拟）
- 实时日志面板

**属性调试组件**: `src/views/iot/device/debug/PropertyDebug.vue`
- 物模型属性列表
- 属性值输入表单
- 上报/设置按钮
- 响应结果展示

**服务调用组件**: `src/views/iot/device/debug/ServiceInvoke.vue`
- 服务列表
- 参数表单（根据物模型动态生成）
- 调用结果展示

**实时日志组件**: `src/views/iot/device/debug/MessageLog.vue`
- WebSocket 连接管理
- 日志列表（虚拟滚动）
- 日志详情（JSON 格式化）
- 清空/导出功能

### Route Configuration

```typescript
{
  path: '/iot/device/debug',
  component: () => import('@/views/iot/device/debug/index.vue'),
  name: 'IotDeviceDebug',
  meta: {
    title: '设备调试',
    icon: 'ep:monitor',
    permission: 'iot:device-debug:query'
  }
}
```

### API Service

**File**: `src/api/iot/device/debug.ts`

```typescript
export interface PropertyReportReqVO {
  deviceId: number
  properties: Record<string, any>
}

export interface ServiceInvokeReqVO {
  deviceId: number
  identifier: string
  params: Record<string, any>
  timeout?: number
}

export interface DebugLogVO {
  id: number
  deviceId: number
  direction: number
  messageType: string
  payload: string
  resultCode: number
  createTime: string
}

export const simulatePropertyReport = (data: PropertyReportReqVO) => { ... }
export const sendPropertySet = (data) => { ... }
export const invokeService = (data: ServiceInvokeReqVO) => { ... }
export const simulateEventReport = (data) => { ... }
export const getMessageLogPage = (params) => { ... }
export const getDeviceThingModel = (deviceId: number) => { ... }
```

### WebSocket Service

**File**: `src/utils/deviceDebugSocket.ts`

```typescript
export class DeviceDebugSocket {
  private socket: WebSocket | null = null
  
  connect(deviceId: number, onMessage: (msg: any) => void): void { ... }
  disconnect(): void { ... }
  subscribe(deviceId: number): void { ... }
  unsubscribe(deviceId: number): void { ... }
}
```

### Key UI Components

- **设备选择器**: 带搜索的设备下拉
- **属性表单**: 根据物模型动态生成输入控件
- **JSON编辑器**: 支持格式化和语法高亮
- **日志表格**: 虚拟滚动支持大量日志
- **状态指示器**: 连接状态、设备在线状态

### Files to Create (Frontend)

- [ ] `src/api/iot/device/debug.ts`
- [ ] `src/utils/deviceDebugSocket.ts`
- [ ] `src/views/iot/device/debug/index.vue`
- [ ] `src/views/iot/device/debug/PropertyDebug.vue`
- [ ] `src/views/iot/device/debug/ServiceInvoke.vue`
- [ ] `src/views/iot/device/debug/EventReport.vue`
- [ ] `src/views/iot/device/debug/MessageLog.vue`
- [ ] 路由配置

---

## Part 3: Testing

### Backend Testing

- [ ] 模拟属性上报（正常、无效属性）
- [ ] 属性设置下发（设备在线/离线）
- [ ] 服务调用（同步超时、异步）
- [ ] 消息日志查询（分页、过滤）
- [ ] WebSocket 连接和消息推送
- [ ] 权限校验

### Frontend Testing

- [ ] 设备选择器功能
- [ ] 属性调试表单生成
- [ ] WebSocket 连接稳定性
- [ ] JSON 格式化显示
- [ ] 日志虚拟滚动性能

---

## Success Criteria

1. ✅ 开发者可以无需物理设备进行调试
2. ✅ 实时日志展示设备通信情况
3. ✅ 物模型驱动的表单自动生成
4. ✅ 调试结果即时反馈

---

**Estimated Effort**: 
- Backend: 10-12 hours
- Frontend: 10-12 hours
- Testing: 4-5 hours
- **Total**: 24-29 hours
