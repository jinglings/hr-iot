# 设备标签系统 - 功能规格说明

> **Status**: In Progress
> **Created**: 2026-01-11
> **Last Updated**: 2026-01-11
> **Developers**: TBD

---

## Overview

设备标签系统允许用户为 IoT 设备添加自定义标签（key-value 形式），实现设备的灵活分类、快速检索和批量管理。

**Feature Scope**:
- Backend: 标签 CRUD API、设备-标签关联、基于标签的设备查询
- Frontend: 标签管理界面、设备标签编辑、标签过滤器

**User Value**:
- 灵活的设备分类：不依赖固定层级，通过标签实现多维度分类
- 快速检索设备：通过标签快速定位目标设备
- 批量操作：基于标签选择设备进行批量操作

---

## Requirements

### Functional Requirements

**FR1**: 用户可以创建自定义标签（key-value 格式，如 `location=车间A`、`priority=high`）
**FR2**: 用户可以为单个设备添加多个标签（最多 20 个）
**FR3**: 用户可以批量为多个设备添加/移除标签
**FR4**: 用户可以基于标签搜索和过滤设备列表
**FR5**: 系统预置常用标签模板（如位置、用途、状态等）
**FR6**: 标签在租户范围内唯一，不同租户可使用相同标签
**FR7**: 删除标签时，自动解除与设备的关联

### Non-Functional Requirements

**NFR1 - Performance**: 标签查询响应时间 < 200ms，支持万级设备标签检索
**NFR2 - Security**: 标签操作需权限验证，租户隔离
**NFR3 - Usability**: 标签支持自动补全、最近使用标签快速选择
**NFR4 - Multi-Tenancy**: 完整的租户隔离，标签数据按租户分离

### Out of Scope

- 标签权限细分（如只读标签）- 后续版本
- 标签继承（产品级标签自动继承到设备）- 后续版本
- 标签组/标签分类 - 后续版本

---

## Architecture Overview

### System Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Frontend (Vue 3)                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐  │
│  │ 标签管理页面     │  │ 设备详情-标签   │  │ 设备列表-过滤  │  │
│  │ - 标签CRUD      │  │ - 添加/移除标签  │  │ - 按标签筛选   │  │
│  └────────┬────────┘  └────────┬────────┘  └───────┬────────┘  │
└───────────┼────────────────────┼───────────────────┼────────────┘
            │                    │                   │
            ▼                    ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│  Backend (Spring Boot)                                          │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ IotDeviceTagController                                      ││
│  │ - POST /create          - PUT /update                       ││
│  │ - DELETE /delete        - GET /list                         ││
│  │ - POST /batch-bind      - POST /batch-unbind                ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ IotDeviceTagService                                         ││
│  │ - 标签CRUD逻辑          - 设备-标签关联逻辑                   ││
│  │ - 标签唯一性校验         - 批量操作处理                       ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│  Database (MySQL)                                               │
│  ┌─────────────────┐  ┌─────────────────────────────────────┐  │
│  │ iot_device_tag  │  │ iot_device_tag_relation             │  │
│  │ - 标签定义表     │  │ - 设备-标签关联表                    │  │
│  └─────────────────┘  └─────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 1: Backend Implementation

### Database Schema

#### Table: `iot_device_tag` - 标签定义表

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | 主键 | Auto-increment |
| tag_key | varchar(64) | 标签键 | 不能为空 |
| tag_value | varchar(128) | 标签值 | 可为空（仅定义键） |
| description | varchar(255) | 标签描述 | 可选 |
| color | varchar(16) | 标签显示颜色 | 如 #FF5733 |
| is_preset | tinyint(1) | 是否预置标签 | 0=否, 1=是 |
| usage_count | int(11) | 使用次数 | 用于排序推荐 |
| tenant_id | bigint(20) | 租户ID | 自动过滤 |
| creator | varchar(64) | 创建者 | |
| create_time | datetime | 创建时间 | |
| updater | varchar(64) | 更新者 | |
| update_time | datetime | 更新时间 | |
| deleted | bit(1) | 是否删除 | 软删除 |

**索引**:
- `uk_tag_key_value_tenant` UNIQUE (tag_key, tag_value, tenant_id, deleted)
- `idx_tenant_id` (tenant_id)

#### Table: `iot_device_tag_relation` - 设备标签关联表

| Column | Type | Description | Notes |
|--------|------|-------------|-------|
| id | bigint(20) | 主键 | Auto-increment |
| device_id | bigint(20) | 设备ID | 关联 iot_device |
| tag_id | bigint(20) | 标签ID | 关联 iot_device_tag |
| tenant_id | bigint(20) | 租户ID | 自动过滤 |
| creator | varchar(64) | 创建者 | |
| create_time | datetime | 创建时间 | |

**索引**:
- `uk_device_tag` UNIQUE (device_id, tag_id, tenant_id)
- `idx_device_id` (device_id)
- `idx_tag_id` (tag_id)

### API Endpoints

| Method | Endpoint | Permission | Purpose |
|--------|----------|------------|---------|
| POST | `/iot/device-tag/create` | `iot:device-tag:create` | 创建标签 |
| PUT | `/iot/device-tag/update` | `iot:device-tag:update` | 更新标签 |
| DELETE | `/iot/device-tag/delete` | `iot:device-tag:delete` | 删除标签 |
| GET | `/iot/device-tag/get` | `iot:device-tag:query` | 获取单个标签 |
| GET | `/iot/device-tag/page` | `iot:device-tag:query` | 分页查询标签 |
| GET | `/iot/device-tag/list` | `iot:device-tag:query` | 获取标签列表 |
| GET | `/iot/device-tag/simple-list` | `iot:device-tag:query` | 标签下拉列表 |
| POST | `/iot/device-tag/bind` | `iot:device:update` | 设备绑定标签 |
| POST | `/iot/device-tag/unbind` | `iot:device:update` | 设备解绑标签 |
| POST | `/iot/device-tag/batch-bind` | `iot:device:update` | 批量绑定标签 |
| POST | `/iot/device-tag/batch-unbind` | `iot:device:update` | 批量解绑标签 |
| GET | `/iot/device-tag/by-device` | `iot:device:query` | 获取设备的所有标签 |
| GET | `/iot/device/page` (扩展) | `iot:device:query` | 支持标签过滤参数 |

### Key Business Rules

1. **唯一性**: 同一租户内，tag_key + tag_value 组合唯一
2. **关联限制**: 每个设备最多绑定 20 个标签
3. **删除级联**: 删除标签时自动删除关联关系
4. **使用统计**: 绑定/解绑时更新 usage_count

### Files to Create (Backend)

- [ ] `sql/mysql/iot/iot_device_tag.sql`
- [ ] `IotDeviceTagDO.java`
- [ ] `IotDeviceTagRelationDO.java`
- [ ] `IotDeviceTagMapper.java`
- [ ] `IotDeviceTagRelationMapper.java`
- [ ] `IotDeviceTagService.java` + `IotDeviceTagServiceImpl.java`
- [ ] `IotDeviceTagController.java`
- [ ] `IotDeviceTagCreateReqVO.java`
- [ ] `IotDeviceTagUpdateReqVO.java`
- [ ] `IotDeviceTagPageReqVO.java`
- [ ] `IotDeviceTagRespVO.java`
- [ ] `IotDeviceTagBindReqVO.java`
- [ ] `IotDeviceTagConvert.java`
- [ ] 扩展 `IotDevicePageReqVO` 添加 tagIds 参数
- [ ] 扩展 `IotDeviceMapper` 支持标签过滤

---

## Part 2: Frontend Implementation

### Page Structure

**标签管理页面**: `src/views/iot/device/tag/index.vue`
- 标签列表（表格展示）
- 创建/编辑标签对话框
- 删除确认
- 标签使用统计

**设备标签编辑组件**: `src/views/iot/device/components/DeviceTagEditor.vue`
- 当前标签展示（Tag 组件）
- 标签选择（下拉+自动完成）
- 添加/移除标签

**设备列表标签过滤**: 扩展 `src/views/iot/device/index.vue`
- 标签多选过滤器
- 标签快速筛选标签云

### Route Configuration

```typescript
{
  path: '/iot/device/tag',
  component: () => import('@/views/iot/device/tag/index.vue'),
  name: 'IotDeviceTag',
  meta: {
    title: '设备标签',
    icon: 'ep:price-tag',
    permission: 'iot:device-tag:query'
  }
}
```

### API Service

**File**: `src/api/iot/device/tag.ts`

```typescript
export interface DeviceTagVO {
  id?: number
  tagKey: string
  tagValue?: string
  description?: string
  color?: string
  isPreset?: boolean
  usageCount?: number
}

export interface DeviceTagBindReqVO {
  deviceId: number
  tagIds: number[]
}

export interface DeviceTagBatchBindReqVO {
  deviceIds: number[]
  tagIds: number[]
}

export const getTagPage = (params) => { ... }
export const createTag = (data: DeviceTagVO) => { ... }
export const updateTag = (data: DeviceTagVO) => { ... }
export const deleteTag = (id: number) => { ... }
export const getTagList = () => { ... }
export const bindTagsToDevice = (data: DeviceTagBindReqVO) => { ... }
export const unbindTagsFromDevice = (data: DeviceTagBindReqVO) => { ... }
export const batchBindTags = (data: DeviceTagBatchBindReqVO) => { ... }
export const getDeviceTags = (deviceId: number) => { ... }
```

### Files to Create (Frontend)

- [ ] `src/api/iot/device/tag.ts`
- [ ] `src/views/iot/device/tag/index.vue`
- [ ] `src/views/iot/device/tag/TagForm.vue`
- [ ] `src/views/iot/device/components/DeviceTagEditor.vue`
- [ ] 修改 `src/views/iot/device/index.vue` 添加标签过滤
- [ ] 修改 `src/views/iot/device/DeviceForm.vue` 添加标签编辑
- [ ] 路由配置

---

## Part 3: Testing

### Backend Testing

- [ ] 创建标签（正常、重复键值校验）
- [ ] 更新标签
- [ ] 删除标签（级联删除关联）
- [ ] 设备绑定标签（单个、上限校验）
- [ ] 设备解绑标签
- [ ] 批量绑定/解绑
- [ ] 设备列表按标签过滤
- [ ] 多租户隔离测试

### Frontend Testing

- [ ] 标签列表展示
- [ ] 标签创建/编辑对话框
- [ ] 设备标签编辑器
- [ ] 标签过滤器
- [ ] 权限按钮显示/隐藏

---

## Success Criteria

1. ✅ 用户可以通过标签对设备进行多维度分类
2. ✅ 支持基于标签的设备快速检索
3. ✅ 批量操作功能可用
4. ✅ 标签使用统计帮助用户了解标签使用情况
5. ✅ 完整的租户隔离

---

**Estimated Effort**: 
- Backend: 8-10 hours
- Frontend: 6-8 hours
- Testing: 3-4 hours
- **Total**: 17-22 hours
