# 标准物模型库 - 功能规格说明

> **Status**: In Progress
> **Created**: 2026-01-11
> **Last Updated**: 2026-01-11
> **Developers**: TBD

---

## Overview

标准物模型库提供预置的行业标准物模型模板，用户可以快速选用或基于模板创建自定义物模型，并支持物模型的导入导出功能，加速设备接入流程。

**Feature Scope**:
- Backend: 物模型模板管理、模板应用、物模型导入导出 API
- Frontend: 模板库浏览界面、模板应用向导、物模型导入导出功能

**User Value**:
- 快速接入常见设备类型，无需从零定义物模型
- 行业标准物模型保证互操作性
- 物模型复用减少重复工作
- 导入导出便于迁移和备份

---

## Requirements

### Functional Requirements

**FR1**: 系统提供预置物模型模板（温湿度传感器、智能开关、电表、水表等）
**FR2**: 用户可以浏览模板库，查看模板详情
**FR3**: 用户可以将模板应用到产品，自动创建物模型
**FR4**: 用户可以基于模板创建并自定义物模型
**FR5**: 用户可以导出产品物模型为 JSON 格式
**FR6**: 用户可以从 JSON 文件导入物模型到产品
**FR7**: 租户可以创建自定义模板保存到自己的模板库
**FR8**: 支持模板分类和搜索

### Non-Functional Requirements

**NFR1 - Performance**: 模板库加载 < 1s，模板应用 < 2s
**NFR2 - Security**: 系统预置模板只读，用户自定义模板按租户隔离
**NFR3 - Usability**: 模板预览直观，支持物模型结构可视化
**NFR4 - Extensibility**: 支持后续添加更多行业模板

### Out of Scope

- 物模型版本管理 - 后续版本
- 模板市场/社区分享 - 后续版本
- 物模型在线编辑器（可视化拖拽）- 后续版本

---

## Architecture Overview

### System Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Frontend (Vue 3)                                               │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ ThingModelTemplateLibrary                                   ││
│  │ ┌─────────────────┐ ┌─────────────────┐ ┌────────────────┐ ││
│  │ │ 模板分类         │ │ 模板列表         │ │ 模板详情       │ ││
│  │ │ - 传感器         │ │ - 温湿度传感器   │ │ - 属性列表     │ ││
│  │ │ - 执行器         │ │ - 智能开关       │ │ - 事件列表     │ ││
│  │ │ - 仪表           │ │ - 电能表         │ │ - 服务列表     │ ││
│  │ │ - 自定义         │ │ - 更多...        │ │ - 应用按钮     │ ││
│  │ └─────────────────┘ └─────────────────┘ └────────────────┘ ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ Import/Export                                               ││
│  │ - 导出物模型 JSON    - 导入物模型 JSON                       ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│  Backend (Spring Boot)                                          │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ IotThingModelTemplateController                             ││
│  │ - GET  /list                 获取模板列表                   ││
│  │ - GET  /get                  获取模板详情                   ││
│  │ - POST /apply                应用模板到产品                 ││
│  │ - POST /create               创建自定义模板                 ││
│  │ - POST /import               导入物模型JSON                 ││
│  │ - GET  /export               导出物模型JSON                 ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│  Database (MySQL)                                               │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ iot_thing_model_template     物模型模板表                    ││
│  │ iot_thing_model_template_category  模板分类表               ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 1: Backend Implementation

### Database Schema

#### Table: `iot_thing_model_template_category` - 模板分类表

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | 主键 | Auto-increment |
| name | varchar(64) | 分类名称 | |
| code | varchar(32) | 分类编码 | |
| icon | varchar(64) | 分类图标 | |
| sort | int(11) | 排序 | |
| is_system | tinyint(1) | 是否系统分类 | 1=系统, 0=自定义 |
| tenant_id | bigint(20) | 租户ID | 系统分类为0 |
| creator | varchar(64) | 创建者 | |
| create_time | datetime | 创建时间 | |
| updater | varchar(64) | 更新者 | |
| update_time | datetime | 更新时间 | |
| deleted | bit(1) | 是否删除 | |

**索引**:
- `uk_code_tenant` UNIQUE (code, tenant_id, deleted)

#### Table: `iot_thing_model_template` - 物模型模板表

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | 主键 | Auto-increment |
| name | varchar(64) | 模板名称 | |
| code | varchar(32) | 模板编码 | |
| category_id | bigint(20) | 分类ID | |
| description | varchar(500) | 模板描述 | |
| icon | varchar(64) | 模板图标 | |
| tsl | text | 物模型TSL定义 | JSON格式 |
| is_system | tinyint(1) | 是否系统模板 | 1=系统, 0=自定义 |
| usage_count | int(11) | 使用次数 | |
| sort | int(11) | 排序 | |
| status | tinyint(4) | 状态 | 1=启用, 0=禁用 |
| tenant_id | bigint(20) | 租户ID | 系统模板为0 |
| creator | varchar(64) | 创建者 | |
| create_time | datetime | 创建时间 | |
| updater | varchar(64) | 更新者 | |
| update_time | datetime | 更新时间 | |
| deleted | bit(1) | 是否删除 | |

**索引**:
- `uk_code_tenant` UNIQUE (code, tenant_id, deleted)
- `idx_category_id` (category_id)

### TSL (Thing Specification Language) Format

```json
{
  "profile": {
    "productKey": "optional",
    "version": "1.0"
  },
  "properties": [
    {
      "identifier": "temperature",
      "name": "温度",
      "accessMode": "r",
      "required": true,
      "dataType": {
        "type": "float",
        "specs": {
          "min": -40,
          "max": 100,
          "step": 0.1,
          "unit": "°C"
        }
      }
    }
  ],
  "events": [
    {
      "identifier": "high_temp_alert",
      "name": "高温告警",
      "type": "alert",
      "outputData": [
        {
          "identifier": "temperature",
          "name": "当前温度",
          "dataType": { "type": "float" }
        }
      ]
    }
  ],
  "services": [
    {
      "identifier": "calibrate",
      "name": "校准",
      "callType": "async",
      "inputData": [],
      "outputData": []
    }
  ]
}
```

### API Endpoints

| Method | Endpoint | Permission | Purpose |
|--------|----------|------------|---------|
| GET | `/iot/thing-model-template/category-list` | `iot:thing-model-template:query` | 获取模板分类 |
| GET | `/iot/thing-model-template/list` | `iot:thing-model-template:query` | 获取模板列表 |
| GET | `/iot/thing-model-template/get` | `iot:thing-model-template:query` | 获取模板详情 |
| POST | `/iot/thing-model-template/create` | `iot:thing-model-template:create` | 创建自定义模板 |
| PUT | `/iot/thing-model-template/update` | `iot:thing-model-template:update` | 更新自定义模板 |
| DELETE | `/iot/thing-model-template/delete` | `iot:thing-model-template:delete` | 删除自定义模板 |
| POST | `/iot/thing-model-template/apply` | `iot:thing-model:create` | 应用模板到产品 |
| GET | `/iot/thing-model/export` | `iot:thing-model:query` | 导出物模型JSON |
| POST | `/iot/thing-model/import` | `iot:thing-model:create` | 导入物模型JSON |

### Preset Templates

系统预置以下模板：

**传感器类**:
1. 温湿度传感器 (temperature_humidity_sensor)
2. 光照传感器 (light_sensor)
3. 烟雾传感器 (smoke_sensor)
4. 门磁传感器 (door_sensor)
5. 水浸传感器 (water_leak_sensor)

**执行器类**:
1. 智能开关 (smart_switch)
2. 调光灯 (dimmable_light)
3. RGB彩灯 (rgb_light)
4. 智能插座 (smart_plug)
5. 电动窗帘 (electric_curtain)

**仪表类**:
1. 单相电表 (single_phase_meter)
2. 三相电表 (three_phase_meter)
3. 水表 (water_meter)
4. 燃气表 (gas_meter)

**工业设备类**:
1. 通用PLC (generic_plc)
2. 变频器 (frequency_converter)
3. 空调控制器 (ac_controller)

### Key Business Rules

1. **系统模板只读**: 系统预置模板不可修改删除
2. **租户隔离**: 自定义模板按租户隔离
3. **唯一编码**: 同租户内模板编码唯一
4. **使用统计**: 应用模板时更新 usage_count
5. **导入校验**: 导入时校验 TSL 格式正确性

### Files to Create (Backend)

- [ ] `sql/mysql/iot/iot_thing_model_template.sql`
- [ ] `sql/mysql/iot/iot_thing_model_template_data.sql` (预置数据)
- [ ] `IotThingModelTemplateCategoryDO.java`
- [ ] `IotThingModelTemplateDO.java`
- [ ] `IotThingModelTemplateCategoryMapper.java`
- [ ] `IotThingModelTemplateMapper.java`
- [ ] `IotThingModelTemplateService.java` + `IotThingModelTemplateServiceImpl.java`
- [ ] `IotThingModelTemplateController.java`
- [ ] `IotThingModelTemplateCreateReqVO.java`
- [ ] `IotThingModelTemplateUpdateReqVO.java`
- [ ] `IotThingModelTemplateListReqVO.java`
- [ ] `IotThingModelTemplateRespVO.java`
- [ ] `IotThingModelApplyReqVO.java`
- [ ] `IotThingModelImportReqVO.java`
- [ ] `IotThingModelExportRespVO.java`
- [ ] `IotThingModelTemplateConvert.java`
- [ ] `TslValidator.java` (TSL格式校验工具)

---

## Part 2: Frontend Implementation

### Page Structure

**模板库页面**: `src/views/iot/thingmodel/template/index.vue`
- 分类侧边栏
- 模板卡片网格
- 搜索过滤

**模板详情对话框**: `src/views/iot/thingmodel/template/TemplateDetail.vue`
- 物模型结构树
- 属性/事件/服务列表
- 应用按钮

**应用模板向导**: `src/views/iot/thingmodel/template/ApplyWizard.vue`
- 选择目标产品
- 预览将创建的物模型
- 确认应用

**导入导出组件**: 扩展产品物模型页面
- 导出JSON按钮
- 导入JSON上传

### Route Configuration

```typescript
{
  path: '/iot/thing-model/template',
  component: () => import('@/views/iot/thingmodel/template/index.vue'),
  name: 'IotThingModelTemplate',
  meta: {
    title: '物模型模板库',
    icon: 'ep:files',
    permission: 'iot:thing-model-template:query'
  }
}
```

### API Service

**File**: `src/api/iot/thingmodel/template.ts`

```typescript
export interface ThingModelTemplateCategoryVO {
  id: number
  name: string
  code: string
  icon: string
  isSystem: boolean
}

export interface ThingModelTemplateVO {
  id: number
  name: string
  code: string
  categoryId: number
  categoryName?: string
  description: string
  icon: string
  tsl: string  // JSON string
  isSystem: boolean
  usageCount: number
}

export interface ThingModelApplyReqVO {
  templateId: number
  productId: number
  overwrite?: boolean  // 是否覆盖现有物模型
}

export const getCategoryList = () => { ... }
export const getTemplateList = (params) => { ... }
export const getTemplate = (id: number) => { ... }
export const createTemplate = (data: ThingModelTemplateVO) => { ... }
export const updateTemplate = (data: ThingModelTemplateVO) => { ... }
export const deleteTemplate = (id: number) => { ... }
export const applyTemplate = (data: ThingModelApplyReqVO) => { ... }
export const exportThingModel = (productId: number) => { ... }
export const importThingModel = (productId: number, file: File) => { ... }
```

### Key UI Components

- **分类树**: 左侧分类导航
- **模板卡片**: 展示模板基本信息和预览
- **TSL预览器**: 结构化展示物模型定义
- **JSON编辑器**: 用于导入/导出预览

### Files to Create (Frontend)

- [ ] `src/api/iot/thingmodel/template.ts`
- [ ] `src/views/iot/thingmodel/template/index.vue`
- [ ] `src/views/iot/thingmodel/template/TemplateDetail.vue`
- [ ] `src/views/iot/thingmodel/template/ApplyWizard.vue`
- [ ] `src/views/iot/thingmodel/template/TemplateForm.vue`
- [ ] `src/views/iot/thingmodel/template/TslPreview.vue`
- [ ] 修改 `src/views/iot/product/thingmodel/` 添加导入导出按钮
- [ ] 路由配置

---

## Part 3: Testing

### Backend Testing

- [ ] 获取分类列表
- [ ] 获取模板列表（分类过滤、搜索）
- [ ] 获取模板详情
- [ ] 创建自定义模板（TSL校验）
- [ ] 应用模板到产品
- [ ] 导出物模型
- [ ] 导入物模型（格式校验、覆盖处理）
- [ ] 系统模板不可删除
- [ ] 租户隔离测试

### Frontend Testing

- [ ] 模板库页面渲染
- [ ] 分类筛选功能
- [ ] 模板详情展示
- [ ] 应用模板向导
- [ ] 导入导出功能
- [ ] TSL预览器

---

## Preset Template Examples

### 温湿度传感器模板

```json
{
  "profile": { "version": "1.0" },
  "properties": [
    {
      "identifier": "temperature",
      "name": "温度",
      "accessMode": "r",
      "required": true,
      "dataType": {
        "type": "float",
        "specs": { "min": -40, "max": 100, "step": 0.1, "unit": "°C" }
      }
    },
    {
      "identifier": "humidity",
      "name": "湿度",
      "accessMode": "r",
      "required": true,
      "dataType": {
        "type": "float",
        "specs": { "min": 0, "max": 100, "step": 0.1, "unit": "%RH" }
      }
    },
    {
      "identifier": "battery",
      "name": "电池电量",
      "accessMode": "r",
      "dataType": {
        "type": "int",
        "specs": { "min": 0, "max": 100, "unit": "%" }
      }
    }
  ],
  "events": [
    {
      "identifier": "high_temp_alert",
      "name": "高温告警",
      "type": "alert",
      "outputData": [
        { "identifier": "temperature", "name": "当前温度", "dataType": { "type": "float" } },
        { "identifier": "threshold", "name": "阈值", "dataType": { "type": "float" } }
      ]
    },
    {
      "identifier": "low_battery",
      "name": "低电量告警",
      "type": "alert",
      "outputData": [
        { "identifier": "battery", "name": "当前电量", "dataType": { "type": "int" } }
      ]
    }
  ],
  "services": []
}
```

### 智能开关模板

```json
{
  "profile": { "version": "1.0" },
  "properties": [
    {
      "identifier": "switch",
      "name": "开关状态",
      "accessMode": "rw",
      "required": true,
      "dataType": {
        "type": "bool",
        "specs": { "0": "关", "1": "开" }
      }
    },
    {
      "identifier": "power",
      "name": "功率",
      "accessMode": "r",
      "dataType": {
        "type": "float",
        "specs": { "min": 0, "max": 5000, "unit": "W" }
      }
    }
  ],
  "events": [
    {
      "identifier": "overload",
      "name": "过载告警",
      "type": "alert",
      "outputData": [
        { "identifier": "power", "name": "当前功率", "dataType": { "type": "float" } }
      ]
    }
  ],
  "services": [
    {
      "identifier": "toggle",
      "name": "切换开关",
      "callType": "async",
      "inputData": [],
      "outputData": [
        { "identifier": "switch", "name": "新状态", "dataType": { "type": "bool" } }
      ]
    }
  ]
}
```

---

## Success Criteria

1. ✅ 系统预置 15+ 常用物模型模板
2. ✅ 用户可一键应用模板到产品
3. ✅ 支持物模型 JSON 导入导出
4. ✅ 用户可创建和管理自定义模板

---

**Estimated Effort**: 
- Backend: 8-10 hours
- Frontend: 8-10 hours
- Preset Data: 3-4 hours
- Testing: 3-4 hours
- **Total**: 22-28 hours
