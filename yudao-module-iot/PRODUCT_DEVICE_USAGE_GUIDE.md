# IoT 产品与设备使用详细指南

## 目录

1. [概述](#概述)
2. [核心概念](#核心概念)
3. [产品管理](#产品管理)
4. [设备管理](#设备管理)
5. [物模型管理](#物模型管理)
6. [完整使用流程](#完整使用流程)
7. [数据库表结构说明](#数据库表结构说明)
8. [常见场景示例](#常见场景示例)

---

## 概述

本文档详细说明了 HR-IoT 平台中产品（Product）和设备（Device）的使用方法,包括创建、配置、管理等全流程操作。

### 产品与设备的关系

```
产品 (Product)
    ├── 定义一类设备的共同特征
    ├── 包含物模型定义(属性、事件、服务)
    └── 包含多个设备实例
        ├── 设备1 (Device Instance)
        ├── 设备2 (Device Instance)
        └── 设备N (Device Instance)
```

**类比理解**:
- **产品** = 手机型号(如: iPhone 15)
- **设备** = 具体的某一台手机(序列号: ABC123456)
- **物模型** = 手机的功能规格(屏幕尺寸、电池容量、摄像头参数等)

---

## 核心概念

### 1. 产品 (Product)

产品是一组具有相同功能定义的设备集合。创建产品后,系统会自动生成唯一的 `ProductKey`。

**关键字段**:

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `id` | BIGINT | 产品编号(主键) | 1001 |
| `name` | VARCHAR(255) | 产品名称 | "智能温湿度传感器" |
| `product_key` | VARCHAR(100) | 产品标识(唯一) | "a1b2c3d4e5f6" |
| `category_id` | BIGINT | 产品分类编号 | 10 |
| `device_type` | TINYINT | 设备类型 | 1(直连设备) |
| `net_type` | TINYINT | 联网方式 | 1(Wi-Fi) |
| `codec_type` | VARCHAR(50) | 数据格式 | "alink" |
| `status` | TINYINT | 产品状态 | 0(开发中)/1(已发布) |

**设备类型枚举** (`device_type`):
- `1`: 直连设备 - 可以直接连接到云端
- `2`: 网关设备 - 作为其他设备的网关
- `3`: 网关子设备 - 通过网关连接到云端

**联网方式枚举** (`net_type`):
- `1`: Wi-Fi
- `2`: 蜂窝网络(2G/3G/4G/5G)
- `3`: 以太网
- `4`: LoRa
- `5`: NB-IoT
- `6`: 其他

**产品状态** (`status`):
- `0`: 开发中 - 可以修改物模型
- `1`: 已发布 - 物模型不可修改,确保已接入设备的稳定性

### 2. 设备 (Device)

设备是产品的实例,代表具体的物理设备。每个设备拥有唯一的认证信息。

**关键字段**:

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `id` | BIGINT | 设备编号(主键) | 2001 |
| `device_name` | VARCHAR(255) | 设备名称(产品内唯一) | "sensor_001" |
| `nickname` | VARCHAR(255) | 设备备注名称 | "办公室温湿度传感器" |
| `serial_number` | VARCHAR(100) | 设备序列号 | "SN20240101001" |
| `product_id` | BIGINT | 产品编号 | 1001 |
| `product_key` | VARCHAR(100) | 产品标识(冗余) | "a1b2c3d4e5f6" |
| `device_secret` | VARCHAR(100) | 设备密钥(用于认证) | "abc123def456" |
| `state` | TINYINT | 设备状态 | 0/1/2 |
| `gateway_id` | BIGINT | 网关设备编号 | NULL(直连设备) |
| `online_time` | DATETIME | 最后上线时间 | 2024-01-15 10:30:00 |
| `active_time` | DATETIME | 设备激活时间 | 2024-01-15 09:00:00 |

**设备状态枚举** (`state`):
- `0`: 未激活 - 设备已创建但从未连接
- `1`: 在线 - 设备当前在线
- `2`: 离线 - 设备曾经在线但当前离线

**设备认证三元组**:
1. `ProductKey` - 产品标识
2. `DeviceName` - 设备名称
3. `DeviceSecret` - 设备密钥

### 3. 物模型 (Thing Model)

物模型是设备功能的数字化描述,包含三大要素:

#### 3.1 属性 (Property)

表示设备的运行时状态,支持读取和设置。

**关键字段**:
```json
{
  "identifier": "temperature",
  "name": "温度",
  "accessMode": "r",  // r=只读, w=只写, rw=可读可写
  "dataType": "float",
  "dataSpecs": {
    "min": -40,
    "max": 100,
    "step": 0.1,
    "unit": "℃"
  }
}
```

**数据类型**:
- `int`: 整数
- `float`: 单精度浮点数
- `double`: 双精度浮点数
- `text`: 文本字符串
- `date`: 日期时间
- `bool`: 布尔值
- `enum`: 枚举
- `struct`: 结构体
- `array`: 数组

#### 3.2 事件 (Event)

设备主动上报的信息。

**事件类型**:
- `info`: 信息事件
- `alert`: 告警事件
- `error`: 故障事件

**示例**:
```json
{
  "identifier": "high_temp_alert",
  "name": "高温告警",
  "type": "alert",
  "outputParams": [
    {
      "identifier": "current_temp",
      "name": "当前温度",
      "dataType": "float"
    }
  ]
}
```

#### 3.3 服务 (Service)

设备可执行的命令或方法。

**调用类型**:
- `sync`: 同步调用 - 等待设备响应
- `async`: 异步调用 - 不等待设备响应

**示例**:
```json
{
  "identifier": "reboot",
  "name": "重启设备",
  "callType": "async",
  "inputParams": [
    {
      "identifier": "delay_seconds",
      "name": "延迟秒数",
      "dataType": "int"
    }
  ],
  "outputParams": []
}
```

---

## 产品管理

### 1. 创建产品分类

产品分类用于组织和管理产品。

**API 接口**: `POST /admin-api/iot/product-category/create`

**请求示例**:
```json
{
  "name": "环境监测传感器",
  "description": "用于监测环境参数的各类传感器",
  "sort": 1,
  "status": 0
}
```

**响应**:
```json
{
  "code": 0,
  "data": 10,  // 分类ID
  "msg": "success"
}
```

### 2. 创建产品

**API 接口**: `POST /admin-api/iot/product/create`

**请求示例**:
```json
{
  "name": "智能温湿度传感器",
  "categoryId": 10,
  "deviceType": 1,
  "netType": 1,
  "locationType": 1,
  "codecType": "alink",
  "description": "支持温度和湿度实时监测,具备高精度传感器"
}
```

**字段说明**:
- `name`: 产品名称,建议清晰描述产品功能
- `categoryId`: 产品分类ID,可选
- `deviceType`: 设备类型 (1=直连设备, 2=网关设备, 3=网关子设备)
- `netType`: 联网方式 (1=Wi-Fi, 2=蜂窝, 3=以太网, 等)
- `locationType`: 定位方式 (1=GPS, 2=LBS, 3=固定位置)
- `codecType`: 数据格式,常用 "alink"
- `description`: 产品描述

**响应**:
```json
{
  "code": 0,
  "data": 1001,  // 产品ID
  "msg": "success"
}
```

**系统自动生成**:
- `product_key`: 产品标识,如 "a1b2c3d4e5f6"

### 3. 查询产品

#### 3.1 分页查询产品列表

**API 接口**: `GET /admin-api/iot/product/page`

**请求参数**:
```
pageNo=1&pageSize=10&name=温湿度&status=0
```

**响应**:
```json
{
  "code": 0,
  "data": {
    "total": 1,
    "list": [
      {
        "id": 1001,
        "name": "智能温湿度传感器",
        "productKey": "a1b2c3d4e5f6",
        "categoryId": 10,
        "deviceType": 1,
        "netType": 1,
        "status": 0,
        "createTime": "2024-01-15 08:00:00"
      }
    ]
  }
}
```

#### 3.2 获取产品详情

**API 接口**: `GET /admin-api/iot/product/get?id=1001`

**响应**:
```json
{
  "code": 0,
  "data": {
    "id": 1001,
    "name": "智能温湿度传感器",
    "productKey": "a1b2c3d4e5f6",
    "categoryId": 10,
    "deviceType": 1,
    "netType": 1,
    "locationType": 1,
    "codecType": "alink",
    "status": 0,
    "description": "支持温度和湿度实时监测,具备高精度传感器",
    "createTime": "2024-01-15 08:00:00"
  }
}
```

### 4. 更新产品

**API 接口**: `PUT /admin-api/iot/product/update`

**请求示例**:
```json
{
  "id": 1001,
  "name": "智能温湿度传感器 Pro",
  "description": "升级版,增加气压监测功能"
}
```

### 5. 发布产品

产品发布后,物模型将不可修改,确保已接入设备的稳定性。

**API 接口**: `PUT /admin-api/iot/product/update-status`

**请求示例**:
```json
{
  "id": 1001,
  "status": 1  // 0=开发中, 1=已发布
}
```

### 6. 删除产品

**API 接口**: `DELETE /admin-api/iot/product/delete?id=1001`

**注意事项**:
- 只能删除状态为"开发中"的产品
- 产品下有设备时不能删除
- 删除操作是软删除,数据仍保留在数据库中

---

## 设备管理

### 1. 创建设备

#### 1.1 单个创建设备

**API 接口**: `POST /admin-api/iot/device/create`

**请求示例**:
```json
{
  "productId": 1001,
  "deviceName": "sensor_001",
  "nickname": "办公室温湿度传感器",
  "serialNumber": "SN20240101001"
}
```

**字段说明**:
- `productId`: 产品ID,必填
- `deviceName`: 设备名称,在产品内唯一,必填
- `nickname`: 设备备注名称,便于识别,可选
- `serialNumber`: 设备序列号,可选

**响应**:
```json
{
  "code": 0,
  "data": {
    "id": 2001,
    "deviceName": "sensor_001",
    "deviceSecret": "abc123def456ghi789",  // 设备密钥
    "productKey": "a1b2c3d4e5f6",
    "productId": 1001
  }
}
```

**系统自动生成**:
- `id`: 设备ID
- `device_secret`: 设备密钥,用于设备认证
- `product_key`: 从产品复制
- `state`: 初始状态为 0(未激活)

#### 1.2 批量导入设备

**API 接口**: `POST /admin-api/iot/device/import`

**请求格式**: `multipart/form-data`

**Excel 文件格式**:

| 设备名称 | 设备备注 | 设备序列号 |
|---------|---------|-----------|
| sensor_002 | 车间1传感器 | SN20240101002 |
| sensor_003 | 车间2传感器 | SN20240101003 |
| sensor_004 | 仓库传感器 | SN20240101004 |

**cURL 示例**:
```bash
curl -X POST http://localhost:48080/admin-api/iot/device/import \
  -H "Authorization: Bearer {token}" \
  -F "file=@devices.xlsx" \
  -F "productId=1001"
```

### 2. 查询设备

#### 2.1 分页查询设备列表

**API 接口**: `GET /admin-api/iot/device/page`

**请求参数**:
```
pageNo=1&pageSize=10&productId=1001&state=1&deviceName=sensor
```

**查询条件**:
- `productId`: 产品ID
- `state`: 设备状态 (0=未激活, 1=在线, 2=离线)
- `deviceName`: 设备名称(模糊查询)
- `nickname`: 设备备注(模糊查询)

**响应**:
```json
{
  "code": 0,
  "data": {
    "total": 3,
    "list": [
      {
        "id": 2001,
        "deviceName": "sensor_001",
        "nickname": "办公室温湿度传感器",
        "serialNumber": "SN20240101001",
        "productId": 1001,
        "productKey": "a1b2c3d4e5f6",
        "state": 1,
        "onlineTime": "2024-01-15 09:00:00",
        "activeTime": "2024-01-15 09:00:00",
        "createTime": "2024-01-15 08:30:00"
      }
    ]
  }
}
```

#### 2.2 获取设备详情

**API 接口**: `GET /admin-api/iot/device/get?id=2001`

**响应**:
```json
{
  "code": 0,
  "data": {
    "id": 2001,
    "deviceName": "sensor_001",
    "nickname": "办公室温湿度传感器",
    "serialNumber": "SN20240101001",
    "productId": 1001,
    "productKey": "a1b2c3d4e5f6",
    "deviceSecret": "abc123def456ghi789",
    "deviceType": 1,
    "state": 1,
    "onlineTime": "2024-01-15 09:00:00",
    "offlineTime": null,
    "activeTime": "2024-01-15 09:00:00",
    "ip": "192.168.1.100",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "address": "北京市朝阳区xxx大厦5层",
    "createTime": "2024-01-15 08:30:00"
  }
}
```

#### 2.3 获取设备认证信息

**API 接口**: `GET /admin-api/iot/device/get-auth-info?id=2001`

**响应**:
```json
{
  "code": 0,
  "data": {
    "productKey": "a1b2c3d4e5f6",
    "deviceName": "sensor_001",
    "deviceSecret": "abc123def456ghi789"
  }
}
```

### 3. 设备分组管理

#### 3.1 创建设备分组

**API 接口**: `POST /admin-api/iot/device-group/create`

**请求示例**:
```json
{
  "name": "一号厂房",
  "description": "一号厂房所有设备",
  "status": 0
}
```

**响应**:
```json
{
  "code": 0,
  "data": 100,  // 分组ID
  "msg": "success"
}
```

#### 3.2 更新设备分组

**API 接口**: `PUT /admin-api/iot/device/update`

**请求示例**:
```json
{
  "id": 2001,
  "groupIds": [100, 101]  // 设备可以属于多个分组
}
```

### 4. 设备状态统计

**API 接口**: `GET /admin-api/iot/device/count-by-state?productId=1001`

**响应**:
```json
{
  "code": 0,
  "data": {
    "total": 18,
    "inactive": 5,   // 未激活
    "online": 10,    // 在线
    "offline": 3     // 离线
  }
}
```

### 5. 设备导出

导出设备列表为 Excel 文件。

**API 接口**: `POST /admin-api/iot/device/export`

**请求示例**:
```json
{
  "productId": 1001,
  "state": 1
}
```

**响应**: Excel 文件下载

### 6. 删除设备

**API 接口**: `DELETE /admin-api/iot/device/delete?id=2001`

**注意事项**:
- 在线设备不能删除,需要先断开连接
- 删除操作是软删除

---

## 物模型管理

### 1. 创建属性

#### 1.1 数值型属性(温度)

**API 接口**: `POST /admin-api/iot/thing-model/create`

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "temperature",
  "name": "温度",
  "description": "环境温度",
  "type": 1,  // 1=属性, 2=服务, 3=事件
  "property": {
    "identifier": "temperature",
    "name": "温度",
    "accessMode": "r",  // 只读
    "dataType": "float",
    "dataSpecs": {
      "min": -40,
      "max": 100,
      "step": 0.1,
      "unit": "℃",
      "unitName": "摄氏度"
    }
  }
}
```

#### 1.2 布尔型属性(开关)

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "power_switch",
  "name": "电源开关",
  "type": 1,
  "property": {
    "identifier": "power_switch",
    "name": "电源开关",
    "accessMode": "rw",  // 可读可写
    "dataType": "bool",
    "dataSpecs": {
      "0": "关闭",
      "1": "开启"
    }
  }
}
```

#### 1.3 枚举型属性(工作模式)

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "work_mode",
  "name": "工作模式",
  "type": 1,
  "property": {
    "identifier": "work_mode",
    "name": "工作模式",
    "accessMode": "rw",
    "dataType": "enum",
    "dataSpecs": {
      "0": "自动模式",
      "1": "手动模式",
      "2": "节能模式",
      "3": "维护模式"
    }
  }
}
```

#### 1.4 结构体属性(GPS坐标)

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "gps_location",
  "name": "GPS坐标",
  "type": 1,
  "property": {
    "identifier": "gps_location",
    "name": "GPS坐标",
    "accessMode": "r",
    "dataType": "struct",
    "dataSpecs": {
      "specs": [
        {
          "identifier": "longitude",
          "name": "经度",
          "dataType": "double"
        },
        {
          "identifier": "latitude",
          "name": "纬度",
          "dataType": "double"
        },
        {
          "identifier": "altitude",
          "name": "海拔",
          "dataType": "float",
          "unit": "m"
        }
      ]
    }
  }
}
```

### 2. 创建事件

#### 2.1 告警事件(高温告警)

**API 接口**: `POST /admin-api/iot/thing-model/create`

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "high_temp_alert",
  "name": "高温告警",
  "description": "温度超过阈值时触发",
  "type": 3,  // 事件
  "event": {
    "identifier": "high_temp_alert",
    "name": "高温告警",
    "type": "alert",  // info, alert, error
    "outputParams": [
      {
        "identifier": "current_temp",
        "name": "当前温度",
        "dataType": "float"
      },
      {
        "identifier": "threshold",
        "name": "阈值温度",
        "dataType": "float"
      },
      {
        "identifier": "location",
        "name": "设备位置",
        "dataType": "text"
      }
    ]
  }
}
```

#### 2.2 故障事件(传感器故障)

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "sensor_fault",
  "name": "传感器故障",
  "type": 3,
  "event": {
    "identifier": "sensor_fault",
    "name": "传感器故障",
    "type": "error",
    "outputParams": [
      {
        "identifier": "fault_code",
        "name": "故障代码",
        "dataType": "int"
      },
      {
        "identifier": "fault_message",
        "name": "故障信息",
        "dataType": "text"
      }
    ]
  }
}
```

### 3. 创建服务

#### 3.1 同步服务(获取配置)

**API 接口**: `POST /admin-api/iot/thing-model/create`

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "get_config",
  "name": "获取配置",
  "description": "获取设备配置参数",
  "type": 2,  // 服务
  "service": {
    "identifier": "get_config",
    "name": "获取配置",
    "callType": "sync",
    "inputParams": [
      {
        "identifier": "config_key",
        "name": "配置键",
        "dataType": "text"
      }
    ],
    "outputParams": [
      {
        "identifier": "config_value",
        "name": "配置值",
        "dataType": "text"
      }
    ]
  }
}
```

#### 3.2 异步服务(重启设备)

**请求示例**:
```json
{
  "productId": 1001,
  "identifier": "reboot",
  "name": "重启设备",
  "type": 2,
  "service": {
    "identifier": "reboot",
    "name": "重启设备",
    "callType": "async",
    "inputParams": [
      {
        "identifier": "delay_seconds",
        "name": "延迟秒数",
        "dataType": "int"
      }
    ],
    "outputParams": []
  }
}
```

### 4. 查询物模型

**API 接口**: `GET /admin-api/iot/thing-model/list?productId=1001`

**按类型查询**:
```
GET /admin-api/iot/thing-model/list?productId=1001&type=1  // 查询属性
GET /admin-api/iot/thing-model/list?productId=1001&type=2  // 查询服务
GET /admin-api/iot/thing-model/list?productId=1001&type=3  // 查询事件
```

### 5. 更新物模型

**API 接口**: `PUT /admin-api/iot/thing-model/update`

**注意**: 产品状态为"已发布"时,物模型不可修改

### 6. 删除物模型

**API 接口**: `DELETE /admin-api/iot/thing-model/delete?id=1001`

---

## 完整使用流程

### 场景: 创建智能温湿度传感器产品并接入设备

#### 步骤 1: 创建产品分类

```bash
POST /admin-api/iot/product-category/create
{
  "name": "环境监测传感器",
  "description": "各类环境参数监测传感器",
  "sort": 1
}

# 响应: categoryId = 10
```

#### 步骤 2: 创建产品

```bash
POST /admin-api/iot/product/create
{
  "name": "智能温湿度传感器",
  "categoryId": 10,
  "deviceType": 1,
  "netType": 1,
  "locationType": 1,
  "codecType": "alink",
  "description": "支持温度和湿度实时监测"
}

# 响应:
# productId = 1001
# productKey = "a1b2c3d4e5f6"
```

#### 步骤 3: 定义物模型 - 添加属性

**3.1 添加温度属性**:
```bash
POST /admin-api/iot/thing-model/create
{
  "productId": 1001,
  "identifier": "temperature",
  "name": "温度",
  "type": 1,
  "property": {
    "identifier": "temperature",
    "name": "温度",
    "accessMode": "r",
    "dataType": "float",
    "dataSpecs": {
      "min": -40,
      "max": 100,
      "step": 0.1,
      "unit": "℃"
    }
  }
}
```

**3.2 添加湿度属性**:
```bash
POST /admin-api/iot/thing-model/create
{
  "productId": 1001,
  "identifier": "humidity",
  "name": "湿度",
  "type": 1,
  "property": {
    "identifier": "humidity",
    "name": "湿度",
    "accessMode": "r",
    "dataType": "float",
    "dataSpecs": {
      "min": 0,
      "max": 100,
      "step": 0.1,
      "unit": "%RH"
    }
  }
}
```

#### 步骤 4: 添加事件

**高温告警事件**:
```bash
POST /admin-api/iot/thing-model/create
{
  "productId": 1001,
  "identifier": "high_temp_alert",
  "name": "高温告警",
  "type": 3,
  "event": {
    "identifier": "high_temp_alert",
    "name": "高温告警",
    "type": "alert",
    "outputParams": [
      {
        "identifier": "current_temp",
        "name": "当前温度",
        "dataType": "float"
      },
      {
        "identifier": "threshold",
        "name": "阈值",
        "dataType": "float"
      }
    ]
  }
}
```

#### 步骤 5: 添加服务

**重启设备服务**:
```bash
POST /admin-api/iot/thing-model/create
{
  "productId": 1001,
  "identifier": "reboot",
  "name": "重启设备",
  "type": 2,
  "service": {
    "identifier": "reboot",
    "name": "重启设备",
    "callType": "async",
    "inputParams": [
      {
        "identifier": "delay_seconds",
        "name": "延迟秒数",
        "dataType": "int"
      }
    ],
    "outputParams": []
  }
}
```

#### 步骤 6: 发布产品

```bash
PUT /admin-api/iot/product/update-status
{
  "id": 1001,
  "status": 1
}
```

#### 步骤 7: 创建设备

**单个创建**:
```bash
POST /admin-api/iot/device/create
{
  "productId": 1001,
  "deviceName": "sensor_001",
  "nickname": "办公室温湿度传感器",
  "serialNumber": "SN20240101001"
}

# 响应:
# deviceId = 2001
# deviceSecret = "abc123def456ghi789"
```

**批量导入**(可选):
```bash
POST /admin-api/iot/device/import
Content-Type: multipart/form-data

productId: 1001
file: devices.xlsx
```

#### 步骤 8: 设备接入 - MQTT 连接

**Python 示例**:
```python
import paho.mqtt.client as mqtt
import hmac
import hashlib
import json
import time

# 设备认证信息
product_key = "a1b2c3d4e5f6"
device_name = "sensor_001"
device_secret = "abc123def456ghi789"

# 构建 MQTT 连接参数
client_id = f"{product_key}.{device_name}"
username = f"{device_name}&{product_key}"

# 计算认证密码
sign_content = f"clientId{client_id}deviceName{device_name}productKey{product_key}"
password = hmac.new(device_secret.encode(), sign_content.encode(), hashlib.sha256).hexdigest()

# 连接回调
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("设备连接成功")
        # 订阅下行 Topic
        client.subscribe(f"/sys/{product_key}/{device_name}/thing/property/set")
        client.subscribe(f"/sys/{product_key}/{device_name}/thing/service/#")
    else:
        print(f"连接失败,错误代码: {rc}")

# 消息接收回调
def on_message(client, userdata, msg):
    print(f"收到消息: {msg.topic} - {msg.payload.decode()}")

# 创建 MQTT 客户端
client = mqtt.Client(client_id)
client.username_pw_set(username, password)
client.on_connect = on_connect
client.on_message = on_message

# 连接到服务器
client.connect("iot-gateway-server", 1883, 60)
client.loop_start()
```

#### 步骤 9: 设备上报数据

**上报属性**:
```python
# Topic: /sys/{productKey}/{deviceName}/thing/property/post
topic = f"/sys/{product_key}/{device_name}/thing/property/post"

# 消息内容
message = {
    "id": str(int(time.time() * 1000)),
    "method": "thing.property.post",
    "params": {
        "temperature": 25.5,
        "humidity": 60.0
    },
    "version": "1.0"
}

client.publish(topic, json.dumps(message))
print("属性上报成功")
```

**上报事件**:
```python
# Topic: /sys/{productKey}/{deviceName}/thing/event/{eventIdentifier}/post
topic = f"/sys/{product_key}/{device_name}/thing/event/high_temp_alert/post"

message = {
    "id": str(int(time.time() * 1000)),
    "method": "thing.event.high_temp_alert.post",
    "params": {
        "current_temp": 85.0,
        "threshold": 80.0
    },
    "version": "1.0"
}

client.publish(topic, json.dumps(message))
print("事件上报成功")
```

#### 步骤 10: 查询设备数据

**查询最新属性值**:
```bash
GET /admin-api/iot/device-property/latest?deviceId=2001

# 响应:
{
  "code": 0,
  "data": {
    "temperature": 25.5,
    "humidity": 60.0,
    "updateTime": "2024-01-15 10:30:00"
  }
}
```

**查询历史数据**:
```bash
GET /admin-api/iot/device-property/history?deviceId=2001&identifier=temperature&startTime=2024-01-15 00:00:00&endTime=2024-01-15 23:59:59

# 响应:
{
  "code": 0,
  "data": [
    {
      "value": 25.5,
      "timestamp": "2024-01-15 10:30:00"
    },
    {
      "value": 25.8,
      "timestamp": "2024-01-15 10:31:00"
    }
  ]
}
```

---

## 数据库表结构说明

### 1. 产品表 (iot_product)

**表结构**:
```sql
CREATE TABLE `iot_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品 ID',
  `name` varchar(255) NOT NULL COMMENT '产品名称',
  `product_key` varchar(100) NOT NULL COMMENT '产品标识',
  `category_id` bigint NULL DEFAULT NULL COMMENT '产品分类编号',
  `icon` varchar(500) NULL DEFAULT NULL COMMENT '产品图标',
  `pic_url` varchar(500) NULL DEFAULT NULL COMMENT '产品图片',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '产品状态（0=开发中, 1=已发布）',
  `device_type` tinyint NOT NULL DEFAULT 0 COMMENT '设备类型（1=直连, 2=网关, 3=子设备）',
  `net_type` tinyint NOT NULL DEFAULT 0 COMMENT '联网方式',
  `location_type` tinyint NULL DEFAULT NULL COMMENT '定位方式',
  `codec_type` varchar(50) NULL DEFAULT NULL COMMENT '数据格式',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_product_key`(`product_key`),
  INDEX `idx_category_id`(`category_id`)
) ENGINE = InnoDB COMMENT = 'IoT 产品表';
```

**关键索引**:
- 主键索引: `id`
- 唯一索引: `product_key`
- 普通索引: `category_id`

### 2. 设备表 (iot_device)

**表结构**:
```sql
CREATE TABLE `iot_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备 ID',
  `device_name` varchar(255) NOT NULL COMMENT '设备名称',
  `nickname` varchar(255) NULL DEFAULT NULL COMMENT '设备备注名称',
  `serial_number` varchar(100) NULL DEFAULT NULL COMMENT '设备序列号',
  `pic_url` varchar(500) NULL DEFAULT NULL COMMENT '设备图片',
  `group_ids` varchar(1000) NULL DEFAULT NULL COMMENT '设备分组编号集合',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `product_key` varchar(100) NULL DEFAULT NULL COMMENT '产品标识',
  `device_type` tinyint NULL DEFAULT NULL COMMENT '设备类型',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '网关设备编号',
  `state` tinyint NULL DEFAULT 0 COMMENT '设备状态（0=未激活, 1=在线, 2=离线）',
  `online_time` datetime NULL DEFAULT NULL COMMENT '最后上线时间',
  `offline_time` datetime NULL DEFAULT NULL COMMENT '最后离线时间',
  `active_time` datetime NULL DEFAULT NULL COMMENT '设备激活时间',
  `ip` varchar(50) NULL DEFAULT NULL COMMENT '设备的 IP 地址',
  `firmware_id` bigint NULL DEFAULT NULL COMMENT '固件编号',
  `device_secret` varchar(100) NULL DEFAULT NULL COMMENT '设备密钥',
  `auth_type` varchar(50) NULL DEFAULT NULL COMMENT '认证类型',
  `location_type` tinyint NULL DEFAULT NULL COMMENT '定位方式',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `area_id` int NULL DEFAULT NULL COMMENT '地区编码',
  `address` varchar(500) NULL DEFAULT NULL COMMENT '设备详细地址',
  `config` text NULL COMMENT '设备配置（JSON）',
  `creator` varchar(64) NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NULL DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_product_device`(`product_id`, `device_name`),
  INDEX `idx_product_id`(`product_id`),
  INDEX `idx_gateway_id`(`gateway_id`)
) ENGINE = InnoDB COMMENT = 'IoT 设备表';
```

**关键索引**:
- 主键索引: `id`
- 唯一索引: `product_id` + `device_name`
- 普通索引: `product_id`, `gateway_id`

### 3. 物模型表 (iot_thing_model)

**表结构**:
```sql
CREATE TABLE `iot_thing_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物模型功能编号',
  `identifier` varchar(100) NOT NULL COMMENT '功能标识',
  `name` varchar(255) NOT NULL COMMENT '功能名称',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '功能描述',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `product_key` varchar(100) NULL DEFAULT NULL COMMENT '产品标识',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '功能类型（1=属性, 2=服务, 3=事件）',
  `property` text NULL COMMENT '属性（JSON）',
  `event` text NULL COMMENT '事件（JSON）',
  `service` text NULL COMMENT '服务（JSON）',
  `creator` varchar(64) NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NULL DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_product_identifier`(`product_id`, `identifier`),
  INDEX `idx_product_id`(`product_id`)
) ENGINE = InnoDB COMMENT = 'IoT 产品物模型功能表';
```

**关键索引**:
- 主键索引: `id`
- 唯一索引: `product_id` + `identifier`
- 普通索引: `product_id`

---

## 常见场景示例

### 场景 1: 智能家居 - 智能灯泡

#### 产品定义
```json
{
  "name": "智能LED灯泡",
  "deviceType": 1,
  "netType": 1,
  "codecType": "alink"
}
```

#### 物模型定义

**属性**:
- `power_switch`: 电源开关 (bool, rw)
- `brightness`: 亮度 (int, 0-100, rw)
- `color_temp`: 色温 (int, 2700-6500K, rw)
- `rgb_color`: RGB颜色 (struct, rw)

**事件**:
- `fault`: 故障事件 (error)

**服务**:
- `set_scene`: 设置场景 (async)

### 场景 2: 工业物联网 - 温湿度传感器

#### 产品定义
```json
{
  "name": "工业级温湿度传感器",
  "deviceType": 1,
  "netType": 2,
  "codecType": "modbus"
}
```

#### 物模型定义

**属性**:
- `temperature`: 温度 (float, -40~100℃, r)
- `humidity`: 湿度 (float, 0~100%RH, r)
- `battery_level`: 电池电量 (int, 0-100%, r)
- `sampling_interval`: 采样间隔 (int, 秒, rw)

**事件**:
- `high_temp_alert`: 高温告警 (alert)
- `low_battery_alert`: 低电量告警 (alert)

**服务**:
- `calibrate`: 校准传感器 (sync)
- `reset`: 恢复出厂设置 (async)

### 场景 3: 智慧农业 - 土壤监测站

#### 产品定义
```json
{
  "name": "土壤环境监测站",
  "deviceType": 1,
  "netType": 5,
  "locationType": 1,
  "codecType": "alink"
}
```

#### 物模型定义

**属性**:
- `soil_moisture`: 土壤湿度 (float, %, r)
- `soil_temp`: 土壤温度 (float, ℃, r)
- `soil_ph`: 土壤pH值 (float, 0-14, r)
- `soil_ec`: 土壤电导率 (float, mS/cm, r)
- `air_temp`: 空气温度 (float, ℃, r)
- `air_humidity`: 空气湿度 (float, %, r)
- `light_intensity`: 光照强度 (float, lux, r)
- `gps_location`: GPS位置 (struct, r)

**事件**:
- `drought_alert`: 干旱告警 (alert)
- `flood_alert`: 水涝告警 (alert)

**服务**:
- `start_irrigation`: 启动灌溉 (async)
- `stop_irrigation`: 停止灌溉 (async)

### 场景 4: 智能交通 - 车载OBD设备

#### 产品定义
```json
{
  "name": "车载OBD诊断仪",
  "deviceType": 1,
  "netType": 2,
  "locationType": 1,
  "codecType": "alink"
}
```

#### 物模型定义

**属性**:
- `speed`: 车速 (int, km/h, r)
- `rpm`: 发动机转速 (int, r/min, r)
- `fuel_level`: 燃油液位 (float, %, r)
- `coolant_temp`: 冷却液温度 (float, ℃, r)
- `mileage`: 里程 (float, km, r)
- `gps_location`: GPS位置 (struct, r)

**事件**:
- `engine_fault`: 发动机故障 (error)
- `overspeed_alert`: 超速告警 (alert)
- `collision_detected`: 碰撞检测 (error)

**服务**:
- `clear_fault_codes`: 清除故障码 (async)
- `read_fault_codes`: 读取故障码 (sync)

---

## 总结

本文档详细介绍了 HR-IoT 平台中产品和设备的使用方法,包括:

1. ✅ 产品的创建、配置、发布流程
2. ✅ 设备的创建、接入、管理方法
3. ✅ 物模型的定义和配置
4. ✅ 完整的使用流程示例
5. ✅ 数据库表结构说明
6. ✅ 多种实际场景示例

**下一步建议**:
1. 参考 `IOT_MODULE_GUIDE.md` 了解 IoT 模块的整体架构
2. 查看 `sql/mysql/iot_sample_data.sql` 获取示例数据
3. 阅读 `MODBUS_README.md` 了解 Modbus 协议支持
4. 查看设备影子、边缘计算等高级功能文档

**相关文档**:
- `IOT_MODULE_GUIDE.md` - IoT 模块使用指南
- `CLAUDE.md` - 项目整体开发指南
- `sql/mysql/iot_sample_data.sql` - 示例数据SQL

**技术支持**:
- GitHub Issues: 提交问题反馈
- 技术文档: 查阅项目文档目录

---

**文档版本**: v1.0.0
**最后更新**: 2024-01-18
**适用版本**: HR-IoT 2025.10-jdk8-SNAPSHOT
