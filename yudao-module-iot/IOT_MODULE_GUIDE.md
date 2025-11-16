# IoT 物联网模块使用指南

## 目录

1. [模块概述](#模块概述)
2. [核心概念](#核心概念)
3. [快速开始](#快速开始)
4. [产品管理](#产品管理)
5. [设备管理](#设备管理)
6. [物模型配置](#物模型配置)
7. [数据采集与存储](#数据采集与存储)
8. [场景联动](#场景联动)
9. [数据流转](#数据流转)
10. [OTA升级](#ota升级)
11. [告警配置](#告警配置)
12. [通信协议](#通信协议)
13. [常见问题](#常见问题)

---

## 模块概述

本 IoT 物联网模块是一个功能完整的物联网平台，提供设备接入、数据采集、规则引擎、数据流转等核心功能。

### 主要特性

- **多协议支持**：MQTT、TCP、HTTP、EMQX、Modbus
- **物模型管理**：属性、事件、服务的标准化定义
- **数据存储**：MySQL（元数据）+ TDengine（时序数据）+ Redis（实时数据）
- **规则引擎**：场景联动、数据流转
- **设备管理**：设备生命周期管理、分组、认证
- **OTA升级**：固件管理、升级任务、升级记录
- **告警管理**：告警配置、告警记录、多渠道通知

### 模块架构

```
┌─────────────────────────────────────────────────────────────┐
│                    IoT 物联网平台架构                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │  iot-gateway (网关层)                               │     │
│  │  - 设备接入 (MQTT/TCP/HTTP/Modbus)                 │     │
│  │  - 协议适配 (编解码)                                │     │
│  │  - 连接管理                                         │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │ 消息总线                                  │
│  ┌────────────────▼───────────────────────────────────┐     │
│  │  iot-biz (业务层)                                   │     │
│  │  - 产品/设备管理                                    │     │
│  │  - 物模型管理                                       │     │
│  │  - 场景联动                                         │     │
│  │  - 数据流转                                         │     │
│  │  - OTA升级                                          │     │
│  │  - 告警管理                                         │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐     │
│  │  iot-core (核心层)                                  │     │
│  │  - 消息总线 (Local/Redis/RocketMQ)                 │     │
│  │  - 工具类                                           │     │
│  │  - 核心枚举                                         │     │
│  └────────────────────────────────────────────────────┘     │
│                                                               │
└─────────────────────────────────────────────────────────────┘

数据存储：
┌──────────┐   ┌──────────┐   ┌──────────┐
│  MySQL   │   │ TDengine │   │  Redis   │
│ (元数据)  │   │(时序数据) │   │(实时数据) │
└──────────┘   └──────────┘   └──────────┘
```

---

## 核心概念

### 1. 产品（Product）

产品是一组具有相同功能定义的设备集合。

**核心属性**：
- **产品名称**：产品的标识名称
- **产品标识（ProductKey）**：唯一标识，用于设备认证
- **设备类型**：直连设备、网关设备、网关子设备
- **联网方式**：Wi-Fi、蜂窝网络、以太网、其他
- **数据格式**：编解码器类型（Alink、自定义等）
- **产品状态**：开发中、已发布

**使用场景**：
- 创建温湿度传感器产品
- 创建智能开关产品
- 创建电能表产品

### 2. 设备（Device）

设备是产品的实例，代表具体的物理设备。

**核心属性**：
- **设备名称（DeviceName）**：在产品内唯一
- **设备序列号**：设备的唯一标识
- **设备密钥（DeviceSecret）**：用于设备认证
- **设备状态**：未激活、在线、离线
- **网关设备ID**：子设备需要指定所属网关
- **固件版本**：当前运行的固件版本

**设备生命周期**：
```
创建 → 未激活 → 激活（首次连接）→ 在线 ⇄ 离线
```

### 3. 物模型（Thing Model）

物模型是对设备功能的数字化描述，包括属性、事件、服务三大要素。

#### 3.1 属性（Property）
表示设备的运行时状态，支持读取和设置。

**示例**：
- 温度传感器的温度值
- 智能灯的开关状态、亮度、颜色

**访问模式**：
- 只读：温度、湿度等传感器数据
- 只写：配置参数
- 读写：开关状态、亮度等

#### 3.2 事件（Event）
设备主动上报的信息，包含告警、故障等。

**事件类型**：
- **信息（INFO）**：一般性通知
- **告警（ALERT）**：需要关注的异常
- **故障（ERROR）**：严重错误

**示例**：
- 温度超过阈值事件
- 设备故障事件
- 电池电量低事件

#### 3.3 服务（Service）
设备可执行的命令或方法。

**调用类型**：
- **同步调用**：等待设备响应
- **异步调用**：不等待设备响应

**示例**：
- 重启设备服务
- 设置参数服务
- 执行校准服务

### 4. 数据类型规范

物模型支持丰富的数据类型：

| 数据类型 | 说明 | 示例 |
|---------|------|------|
| int | 整数 | 温度值（-40~100） |
| float | 单精度浮点数 | 电压值（0.0~380.0） |
| double | 双精度浮点数 | 经纬度坐标 |
| text | 文本字符串 | 设备名称 |
| date | 日期时间 | 最后维护时间 |
| bool | 布尔值 | 开关状态 |
| enum | 枚举 | 运行模式（自动/手动/维护） |
| struct | 结构体 | GPS坐标{经度, 纬度, 海拔} |
| array | 数组 | 传感器数组 |

---

## 快速开始

### 第一步：创建产品

通过管理后台或 API 创建产品：

**API 接口**：`POST /admin-api/iot/product/create`

**请求示例**：
```json
{
  "name": "智能温湿度传感器",
  "categoryId": 1,
  "deviceType": 1,
  "netType": 1,
  "locationType": 1,
  "codecType": "alink",
  "description": "用于监测环境温湿度"
}
```

**响应**：
```json
{
  "code": 0,
  "data": 1001,  // 产品ID
  "msg": "success"
}
```

创建成功后，系统会自动生成产品标识（ProductKey），例如：`a1b2c3d4e5f6`

### 第二步：定义物模型

为产品定义属性、事件、服务。

**创建属性 - 温度**：

**API 接口**：`POST /admin-api/iot/thing-model/create`

```json
{
  "productId": 1001,
  "identifier": "temperature",
  "name": "温度",
  "type": 1,  // 属性
  "property": {
    "identifier": "temperature",
    "name": "温度",
    "accessMode": "r",  // 只读
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

**创建属性 - 湿度**：

```json
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

**创建事件 - 温度告警**：

```json
{
  "productId": 1001,
  "identifier": "temp_alert",
  "name": "温度告警",
  "type": 3,  // 事件
  "event": {
    "identifier": "temp_alert",
    "name": "温度告警",
    "type": "alert",  // 告警类型
    "outputParams": [
      {
        "identifier": "temperature",
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

### 第三步：创建设备

为产品创建具体的设备实例。

**API 接口**：`POST /admin-api/iot/device/create`

```json
{
  "productId": 1001,
  "deviceName": "sensor_001",
  "nickname": "办公室温湿度传感器",
  "serialNumber": "SN20240101001"
}
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "id": 2001,
    "deviceName": "sensor_001",
    "deviceSecret": "abc123def456ghi789",  // 设备密钥，用于认证
    "productKey": "a1b2c3d4e5f6"
  }
}
```

### 第四步：设备接入

设备使用 ProductKey、DeviceName、DeviceSecret 进行认证连接。

#### MQTT 连接示例

**连接参数**：
- **Broker地址**：`mqtt://iot-gateway-server:1883`
- **ClientId**：`{productKey}.{deviceName}`
- **Username**：`{deviceName}&{productKey}`
- **Password**：使用 HMAC-SHA256 签名计算

**认证密码计算**：
```
password = HMAC-SHA256(deviceSecret, "clientId{clientId}deviceName{deviceName}productKey{productKey}")
```

**连接示例（Python）**：
```python
import paho.mqtt.client as mqtt
import hmac
import hashlib

product_key = "a1b2c3d4e5f6"
device_name = "sensor_001"
device_secret = "abc123def456ghi789"

client_id = f"{product_key}.{device_name}"
username = f"{device_name}&{product_key}"

# 计算密码
sign_content = f"clientId{client_id}deviceName{device_name}productKey{product_key}"
password = hmac.new(device_secret.encode(), sign_content.encode(), hashlib.sha256).hexdigest()

client = mqtt.Client(client_id)
client.username_pw_set(username, password)
client.connect("iot-gateway-server", 1883, 60)
client.loop_start()
```

### 第五步：上报数据

设备连接成功后，可以上报属性数据。

**MQTT Topic**：`/sys/{productKey}/{deviceName}/thing/property/post`

**消息格式**：
```json
{
  "id": "123456",
  "method": "thing.property.post",
  "params": {
    "temperature": 25.5,
    "humidity": 60.0
  },
  "version": "1.0"
}
```

**Python 示例**：
```python
import json
import time

topic = f"/sys/{product_key}/{device_name}/thing/property/post"

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
```

### 第六步：查看数据

通过 API 查询设备最新属性：

**API 接口**：`GET /admin-api/iot/device-property/latest?deviceId=2001`

**响应**：
```json
{
  "code": 0,
  "data": {
    "temperature": 25.5,
    "humidity": 60.0,
    "updateTime": "2024-01-15 10:30:00"
  }
}
```

查询设备历史数据：

**API 接口**：`GET /admin-api/iot/device-property/history?deviceId=2001&identifier=temperature&startTime=2024-01-15 00:00:00&endTime=2024-01-15 23:59:59`

---

## 产品管理

### 产品分类

产品可以按照分类进行组织管理。

**创建产品分类**：

```json
POST /admin-api/iot/product-category/create
{
  "name": "传感器",
  "parentId": 0,
  "description": "各类传感器设备"
}
```

**查询产品分类树**：

```
GET /admin-api/iot/product-category/tree
```

### 产品状态

产品有两种状态：

- **开发中**：产品正在开发调试，可以修改物模型
- **已发布**：产品已发布，物模型不可修改（确保已接入设备的稳定性）

**更新产品状态**：

```json
PUT /admin-api/iot/product/update-status
{
  "id": 1001,
  "status": 1  // 0=开发中, 1=已发布
}
```

### 产品查询

**分页查询产品**：

```
GET /admin-api/iot/product/page?pageNo=1&pageSize=10&name=温湿度
```

**获取产品详情**：

```
GET /admin-api/iot/product/get?id=1001
```

**获取产品精简列表**：

```
GET /admin-api/iot/product/simple-list
```

---

## 设备管理

### 设备创建

**单个创建**：

```json
POST /admin-api/iot/device/create
{
  "productId": 1001,
  "deviceName": "sensor_002",
  "nickname": "仓库温湿度传感器",
  "serialNumber": "SN20240101002"
}
```

**批量导入**：

通过 Excel 文件批量导入设备。

```
POST /admin-api/iot/device/import
Content-Type: multipart/form-data

file: devices.xlsx
```

Excel 格式：

| 设备名称 | 设备备注 | 设备序列号 |
|---------|---------|-----------|
| sensor_003 | 车间1传感器 | SN20240101003 |
| sensor_004 | 车间2传感器 | SN20240101004 |

### 设备分组

设备可以按照分组进行管理。

**创建设备分组**：

```json
POST /admin-api/iot/device-group/create
{
  "name": "一号厂房",
  "description": "一号厂房所有设备"
}
```

**更新设备分组**：

```json
PUT /admin-api/iot/device/update-group
{
  "id": 2001,
  "groupId": 100
}
```

### 设备认证

设备认证信息包括：
- ProductKey
- DeviceName
- DeviceSecret

**获取设备认证信息**：

```
GET /admin-api/iot/device/get-auth-info?id=2001
```

**响应**：
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

### 设备状态管理

设备有三种状态：

- **未激活**：设备已创建但从未连接
- **在线**：设备当前在线
- **离线**：设备曾经在线但当前离线

系统会自动维护设备状态：
- 设备首次连接时，状态从"未激活"变为"在线"
- 设备断开连接或超时未上报数据时，状态变为"离线"

**按状态统计设备**：

```
GET /admin-api/iot/device/count-by-state?productId=1001
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "inactive": 5,   // 未激活
    "online": 10,    // 在线
    "offline": 3     // 离线
  }
}
```

### 设备查询

**分页查询设备**：

```
GET /admin-api/iot/device/page?pageNo=1&pageSize=10&productId=1001&state=1
```

**获取设备详情**：

```
GET /admin-api/iot/device/get?id=2001
```

### 设备导出

导出设备列表为 Excel 文件。

```
POST /admin-api/iot/device/export
{
  "productId": 1001
}
```

---

## 物模型配置

### 属性定义

属性表示设备的运行时状态。

#### 数值型属性（int/float/double）

```json
{
  "productId": 1001,
  "identifier": "voltage",
  "name": "电压",
  "type": 1,
  "property": {
    "identifier": "voltage",
    "name": "电压",
    "accessMode": "r",
    "dataType": "float",
    "dataSpecs": {
      "min": 0,
      "max": 380,
      "step": 0.1,
      "unit": "V",
      "unitName": "伏特"
    }
  }
}
```

#### 布尔型属性

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

#### 枚举型属性

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
      "2": "维护模式"
    }
  }
}
```

#### 文本型属性

```json
{
  "productId": 1001,
  "identifier": "device_location",
  "name": "设备位置",
  "type": 1,
  "property": {
    "identifier": "device_location",
    "name": "设备位置",
    "accessMode": "rw",
    "dataType": "text",
    "dataSpecs": {
      "length": 128
    }
  }
}
```

#### 结构体属性

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
          "dataType": "float"
        }
      ]
    }
  }
}
```

#### 数组属性

```json
{
  "productId": 1001,
  "identifier": "sensor_array",
  "name": "传感器数组",
  "type": 1,
  "property": {
    "identifier": "sensor_array",
    "name": "传感器数组",
    "accessMode": "r",
    "dataType": "array",
    "dataSpecs": {
      "size": 10,
      "item": {
        "dataType": "float"
      }
    }
  }
}
```

### 事件定义

事件是设备主动上报的信息。

#### 告警事件

```json
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
      },
      {
        "identifier": "location",
        "name": "位置",
        "dataType": "text"
      }
    ]
  }
}
```

#### 故障事件

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

#### 信息事件

```json
{
  "productId": 1001,
  "identifier": "calibration_completed",
  "name": "校准完成",
  "type": 3,
  "event": {
    "identifier": "calibration_completed",
    "name": "校准完成",
    "type": "info",
    "outputParams": [
      {
        "identifier": "calibration_time",
        "name": "校准时间",
        "dataType": "date"
      }
    ]
  }
}
```

### 服务定义

服务是设备可执行的命令。

#### 同步服务

```json
{
  "productId": 1001,
  "identifier": "get_config",
  "name": "获取配置",
  "type": 2,
  "service": {
    "identifier": "get_config",
    "name": "获取配置",
    "callType": "sync",
    "inputParams": [
      {
        "identifier": "config_key",
        "name": "配置项",
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

#### 异步服务

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

### 物模型查询

**查询产品的所有物模型**：

```
GET /admin-api/iot/thing-model/list?productId=1001
```

**按类型查询物模型**：

```
GET /admin-api/iot/thing-model/list?productId=1001&type=1  // 1=属性, 2=服务, 3=事件
```

---

## 数据采集与存储

### 数据存储架构

系统采用三层存储架构：

1. **MySQL**：存储产品、设备、物模型等元数据
2. **TDengine**：存储设备消息和属性历史数据（时序数据库）
3. **Redis**：存储设备属性最新值、设备状态等实时数据

### 设备属性数据

#### 属性上报

设备通过 MQTT 上报属性数据：

**Topic**：`/sys/{productKey}/{deviceName}/thing/property/post`

**消息格式**：
```json
{
  "id": "123456",
  "method": "thing.property.post",
  "params": {
    "temperature": 25.5,
    "humidity": 60.0,
    "pressure": 101.325
  },
  "version": "1.0"
}
```

系统会自动：
1. 更新 Redis 中的最新属性值
2. 将历史数据写入 TDengine
3. 触发场景规则引擎

#### 属性设置

平台可以向设备下发属性设置命令：

**API 接口**：`POST /admin-api/iot/device/property-set`

```json
{
  "deviceId": 2001,
  "properties": {
    "power_switch": true,
    "work_mode": 0
  }
}
```

系统会：
1. 构建属性设置消息
2. 通过消息总线下发到 Gateway
3. Gateway 通过设备连接发送到设备

设备收到的消息（MQTT）：

**Topic**：`/sys/{productKey}/{deviceName}/thing/property/set`

**消息格式**：
```json
{
  "id": "789012",
  "method": "thing.property.set",
  "params": {
    "power_switch": true,
    "work_mode": 0
  },
  "version": "1.0"
}
```

设备应该回复：

**Topic**：`/sys/{productKey}/{deviceName}/thing/property/set_reply`

```json
{
  "id": "789012",
  "code": 0,
  "data": {}
}
```

#### 查询属性数据

**查询最新属性**：

```
GET /admin-api/iot/device-property/latest?deviceId=2001
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "temperature": 25.5,
    "humidity": 60.0,
    "pressure": 101.325,
    "updateTime": "2024-01-15 10:30:00"
  }
}
```

**查询历史属性**：

```
GET /admin-api/iot/device-property/history?deviceId=2001&identifier=temperature&startTime=2024-01-15 00:00:00&endTime=2024-01-15 23:59:59
```

**响应**：
```json
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

### 设备事件数据

#### 事件上报

设备通过 MQTT 上报事件：

**Topic**：`/sys/{productKey}/{deviceName}/thing/event/{eventIdentifier}/post`

**消息格式**：
```json
{
  "id": "123456",
  "method": "thing.event.{eventIdentifier}.post",
  "params": {
    "current_temp": 85.0,
    "threshold": 80.0,
    "location": "车间1"
  },
  "version": "1.0"
}
```

#### 查询事件数据

```
GET /admin-api/iot/device-message/event-list?deviceId=2001&identifier=high_temp_alert
```

### 服务调用

#### 调用设备服务

**API 接口**：`POST /admin-api/iot/device/service-invoke`

```json
{
  "deviceId": 2001,
  "identifier": "reboot",
  "params": {
    "delay_seconds": 10
  }
}
```

设备收到的消息（MQTT）：

**Topic**：`/sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}`

```json
{
  "id": "345678",
  "method": "thing.service.{serviceIdentifier}",
  "params": {
    "delay_seconds": 10
  },
  "version": "1.0"
}
```

设备应该回复：

**Topic**：`/sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}_reply`

```json
{
  "id": "345678",
  "code": 0,
  "data": {
    // 服务返回数据
  }
}
```

---

## 场景联动

场景联动允许您基于设备状态、时间等条件，自动执行一系列动作。

### 场景规则结构

一个场景规则包含：
- **触发器（Trigger）**：什么情况下触发
- **条件（Condition）**：满足什么条件才执行
- **动作（Action）**：执行什么操作

### 触发器类型

1. **设备状态更新**：设备上线/离线时触发
2. **属性上报**：设备上报属性时触发
3. **事件上报**：设备上报事件时触发
4. **服务调用**：设备服务被调用时触发
5. **定时触发**：按 CRON 表达式定时触发

### 条件类型

1. **设备状态条件**：检查设备是否在线/离线
2. **设备属性条件**：检查设备属性值
3. **时间条件**：检查当前时间范围

### 条件操作符

- **等于（EQUAL）**：值等于指定值
- **不等于（NOT_EQUAL）**：值不等于指定值
- **大于（GREATER_THAN）**：值大于指定值
- **大于等于（GREATER_THAN_OR_EQUAL）**：值大于或等于指定值
- **小于（LESS_THAN）**：值小于指定值
- **小于等于（LESS_THAN_OR_EQUAL）**：值小于或等于指定值
- **区间（BETWEEN）**：值在指定区间内
- **包含（IN）**：值在指定列表中

### 动作类型

1. **属性设置**：设置设备属性
2. **服务调用**：调用设备服务
3. **触发告警**：触发告警通知
4. **恢复告警**：恢复已触发的告警

### 场景示例

#### 示例1：温度超过阈值时开启风扇

```json
POST /admin-api/iot/scene-rule/create
{
  "name": "温度过高开启风扇",
  "description": "当温度超过30℃时，自动开启风扇",
  "status": 1,
  "triggers": [
    {
      "type": "PROPERTY_REPORT",  // 属性上报触发
      "productId": 1001,
      "identifier": "temperature",
      "operator": "GREATER_THAN",
      "value": 30
    }
  ],
  "actions": [
    {
      "type": "PROPERTY_SET",  // 设置属性
      "productId": 1002,  // 风扇产品ID
      "deviceId": 3001,   // 风扇设备ID
      "identifier": "power_switch",
      "params": {
        "power_switch": true
      }
    }
  ]
}
```

#### 示例2：设备离线时发送告警

```json
{
  "name": "设备离线告警",
  "description": "当设备离线时发送告警通知",
  "status": 1,
  "triggers": [
    {
      "type": "DEVICE_STATE",  // 设备状态触发
      "productId": 1001,
      "deviceId": 2001,
      "operator": "EQUAL",
      "value": "offline"
    }
  ],
  "actions": [
    {
      "type": "TRIGGER_ALERT",  // 触发告警
      "alertConfigId": 100
    }
  ]
}
```

#### 示例3：每天定时采集数据

```json
{
  "name": "定时数据采集",
  "description": "每天08:00触发数据采集",
  "status": 1,
  "triggers": [
    {
      "type": "TIMER",  // 定时触发
      "cronExpression": "0 0 8 * * ?"  // 每天08:00
    }
  ],
  "actions": [
    {
      "type": "SERVICE_INVOKE",  // 调用服务
      "productId": 1001,
      "deviceId": 2001,
      "identifier": "collect_data",
      "params": {}
    }
  ]
}
```

#### 示例4：复杂条件联动

温度高于30℃且湿度低于40%时，开启加湿器和降温。

```json
{
  "name": "温湿度联动控制",
  "description": "温度高且湿度低时，开启加湿器和降温",
  "status": 1,
  "triggers": [
    {
      "type": "PROPERTY_REPORT",
      "productId": 1001,
      "identifier": "temperature",
      "operator": "GREATER_THAN",
      "value": 30,
      "conditionGroups": [
        {
          "conditions": [
            {
              "type": "DEVICE_PROPERTY",
              "productId": 1001,
              "identifier": "humidity",
              "operator": "LESS_THAN",
              "param": 40
            }
          ]
        }
      ]
    }
  ],
  "actions": [
    {
      "type": "PROPERTY_SET",
      "productId": 1003,  // 加湿器
      "deviceId": 4001,
      "identifier": "power_switch",
      "params": {
        "power_switch": true
      }
    },
    {
      "type": "PROPERTY_SET",
      "productId": 1002,  // 风扇
      "deviceId": 3001,
      "identifier": "power_switch",
      "params": {
        "power_switch": true
      }
    }
  ]
}
```

### 场景规则管理

**查询场景规则**：

```
GET /admin-api/iot/scene-rule/page?pageNo=1&pageSize=10
```

**启用/禁用场景规则**：

```json
PUT /admin-api/iot/scene-rule/update-status
{
  "id": 100,
  "status": 1  // 0=禁用, 1=启用
}
```

---

## 数据流转

数据流转允许您将设备数据转发到其他系统或服务。

### 数据源配置

定义哪些设备消息需要流转。

**数据源类型**：
- 属性上报
- 事件上报
- 服务调用

**数据源配置示例**：
```json
{
  "sourceConfigs": [
    {
      "method": "thing.property.post",  // 属性上报
      "productId": 1001,
      "identifier": "temperature"
    },
    {
      "method": "thing.event.high_temp_alert.post",  // 事件上报
      "productId": 1001,
      "identifier": "high_temp_alert"
    }
  ]
}
```

### 数据目的类型

支持多种数据流转目的：

1. **HTTP**：通过 HTTP POST 发送数据
2. **MQTT**：发布到 MQTT Broker
3. **TCP**：通过 TCP 连接发送
4. **WebSocket**：通过 WebSocket 发送
5. **Kafka**：发送到 Kafka 主题
6. **RocketMQ**：发送到 RocketMQ 主题
7. **RabbitMQ**：发送到 RabbitMQ 队列
8. **Redis**：写入 Redis

### 数据流转示例

#### 示例1：转发到 HTTP 接口

```json
POST /admin-api/iot/data-sink/create
{
  "name": "转发到业务系统",
  "type": "HTTP",
  "config": {
    "url": "http://business-system.com/api/iot/data",
    "method": "POST",
    "headers": {
      "Content-Type": "application/json",
      "Authorization": "Bearer token123"
    }
  }
}
```

创建数据流转规则：

```json
POST /admin-api/iot/data-rule/create
{
  "name": "温度数据转发",
  "status": 1,
  "sourceConfigs": [
    {
      "method": "thing.property.post",
      "productId": 1001,
      "identifier": "temperature"
    }
  ],
  "sinkIds": [1001]  // 数据目的ID
}
```

#### 示例2：转发到 Kafka

```json
POST /admin-api/iot/data-sink/create
{
  "name": "转发到 Kafka",
  "type": "KAFKA",
  "config": {
    "bootstrapServers": "kafka-server:9092",
    "topic": "iot-device-data",
    "partition": 0
  }
}
```

#### 示例3：转发到 Redis

```json
POST /admin-api/iot/data-sink/create
{
  "name": "写入 Redis",
  "type": "REDIS",
  "config": {
    "host": "redis-server",
    "port": 6379,
    "password": "redis123",
    "database": 0,
    "dataStructure": "STRING",  // STRING, HASH, LIST, SET, ZSET
    "key": "iot:device:${deviceId}:${identifier}"  // 支持变量
  }
}
```

#### 示例4：转发到 RocketMQ

```json
POST /admin-api/iot/data-sink/create
{
  "name": "转发到 RocketMQ",
  "type": "ROCKETMQ",
  "config": {
    "namesrvAddr": "rocketmq-server:9876",
    "topic": "iot-device-data",
    "tag": "property-report"
  }
}
```

---

## OTA升级

OTA（Over-The-Air）升级功能允许远程更新设备固件。

### 固件管理

#### 创建固件

**API 接口**：`POST /admin-api/iot/ota-firmware/create`

```json
{
  "name": "温湿度传感器固件 v2.0",
  "productId": 1001,
  "version": "2.0.0",
  "fileUrl": "https://oss.example.com/firmware/sensor-v2.0.0.bin",
  "fileSize": 1048576,  // 字节
  "fileDigestAlgorithm": "MD5",
  "fileDigestValue": "d41d8cd98f00b204e9800998ecf8427e",
  "description": "修复温度读取偏差问题"
}
```

#### 查询固件列表

```
GET /admin-api/iot/ota-firmware/page?productId=1001
```

### 升级任务

#### 创建升级任务

**API 接口**：`POST /admin-api/iot/ota-task/create`

```json
{
  "name": "传感器固件升级任务",
  "firmwareId": 1001,
  "deviceScope": "ALL",  // ALL=全部设备, SPECIFIC=指定设备
  "deviceIds": [],  // 当 scope=SPECIFIC 时指定设备ID列表
  "description": "升级到v2.0版本"
}
```

**设备范围选项**：
- **ALL**：产品下的所有设备
- **SPECIFIC**：指定的设备列表

#### 任务状态

- **待执行**：任务已创建，等待执行
- **执行中**：正在推送升级消息
- **已完成**：所有设备已推送完成
- **已取消**：任务被取消

#### 查询升级任务

```
GET /admin-api/iot/ota-task/page?productId=1001
```

### 升级流程

1. **平台推送升级通知**

设备收到的消息（MQTT）：

**Topic**：`/sys/{productKey}/{deviceName}/ota/upgrade/push`

```json
{
  "id": "123456",
  "method": "ota.upgrade.push",
  "params": {
    "version": "2.0.0",
    "url": "https://oss.example.com/firmware/sensor-v2.0.0.bin",
    "size": 1048576,
    "md5": "d41d8cd98f00b204e9800998ecf8427e"
  },
  "version": "1.0"
}
```

2. **设备下载固件**

设备从 URL 下载固件文件，并验证 MD5。

3. **设备上报升级进度**

**Topic**：`/sys/{productKey}/{deviceName}/ota/upgrade/progress`

```json
{
  "id": "123457",
  "method": "ota.upgrade.progress",
  "params": {
    "step": "DOWNLOADING",  // DOWNLOADING, VERIFYING, UPGRADING, SUCCESS, FAILED
    "progress": 50,  // 百分比
    "desc": "正在下载固件"
  },
  "version": "1.0"
}
```

**升级步骤**：
- **DOWNLOADING**：下载固件
- **VERIFYING**：校验固件
- **UPGRADING**：升级中
- **SUCCESS**：升级成功
- **FAILED**：升级失败

4. **升级完成**

```json
{
  "id": "123458",
  "method": "ota.upgrade.progress",
  "params": {
    "step": "SUCCESS",
    "progress": 100,
    "desc": "升级成功"
  },
  "version": "1.0"
}
```

### 升级记录

查询每个设备的升级记录：

```
GET /admin-api/iot/ota-task-record/page?taskId=1001
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "deviceId": 2001,
        "deviceName": "sensor_001",
        "status": "SUCCESS",
        "progress": 100,
        "pushTime": "2024-01-15 10:00:00",
        "completeTime": "2024-01-15 10:05:00"
      }
    ]
  }
}
```

---

## 告警配置

告警功能允许您配置告警规则，当满足条件时发送通知。

### 创建告警配置

**API 接口**：`POST /admin-api/iot/alert-config/create`

```json
{
  "name": "设备离线告警",
  "level": "HIGH",  // LOW, MEDIUM, HIGH, CRITICAL
  "status": 1,
  "sceneRuleIds": [100, 101],  // 关联的场景规则ID
  "receiveUserIds": [1, 2, 3],  // 接收告警的用户ID
  "receiveTypes": ["SMS", "EMAIL", "WECHAT"],  // 接收方式
  "description": "关键设备离线时发送告警"
}
```

### 告警级别

- **LOW**：低级别告警（信息提示）
- **MEDIUM**：中级别告警（需要关注）
- **HIGH**：高级别告警（需要处理）
- **CRITICAL**：严重告警（紧急处理）

### 接收方式

- **SMS**：短信通知
- **EMAIL**：邮件通知
- **WECHAT**：微信通知
- **APP_PUSH**：APP推送
- **WEBHOOK**：Webhook回调

### 告警记录

查询告警历史记录：

```
GET /admin-api/iot/alert-record/page?pageNo=1&pageSize=10&level=HIGH
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 1001,
        "alertConfigId": 100,
        "alertName": "设备离线告警",
        "level": "HIGH",
        "deviceId": 2001,
        "deviceName": "sensor_001",
        "message": "设备 sensor_001 已离线",
        "triggerTime": "2024-01-15 10:30:00",
        "status": "TRIGGERED"  // TRIGGERED=已触发, RECOVERED=已恢复
      }
    ]
  }
}
```

---

## 通信协议

### MQTT 协议

MQTT 是最常用的物联网通信协议。

#### 连接配置

**配置文件**：`application.yml`

```yaml
yudao:
  iot:
    gateway:
      protocol:
        mqtt:
          enabled: true
          port: 1883
          ssl-enabled: false
          ssl-port: 8883
          max-message-size: 256000
          connection-timeout: 30
```

#### Topic 规范

所有 MQTT Topic 遵循以下格式：

```
/sys/{productKey}/{deviceName}/{功能}
```

**上行 Topic（设备 → 平台）**：

| Topic | 说明 |
|-------|------|
| `/sys/{pk}/{dn}/thing/property/post` | 属性上报 |
| `/sys/{pk}/{dn}/thing/event/{identifier}/post` | 事件上报 |
| `/sys/{pk}/{dn}/thing/service/{identifier}_reply` | 服务调用响应 |

**下行 Topic（平台 → 设备）**：

| Topic | 说明 |
|-------|------|
| `/sys/{pk}/{dn}/thing/property/set` | 属性设置 |
| `/sys/{pk}/{dn}/thing/service/{identifier}` | 服务调用 |
| `/sys/{pk}/{dn}/ota/upgrade/push` | OTA升级推送 |

#### 消息格式

所有消息使用 JSON 格式：

```json
{
  "id": "消息ID（唯一）",
  "method": "方法名",
  "params": {
    // 参数
  },
  "version": "1.0"
}
```

响应消息：

```json
{
  "id": "对应请求的消息ID",
  "code": 0,  // 0=成功, 非0=失败
  "data": {
    // 响应数据
  },
  "msg": "错误信息（失败时）"
}
```

### TCP 协议

支持 TCP 长连接通信。

#### 配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        tcp:
          enabled: true
          port: 8800
          codec-type: JSON  # JSON 或 BINARY
```

#### 消息编解码

**JSON 编解码**：消息格式与 MQTT 相同

**二进制编解码**：自定义二进制协议，需实现 `IotDeviceMessageCodec` 接口

### HTTP 协议

支持 HTTP/HTTPS 通信。

#### 配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        http:
          enabled: true
          port: 8080
          ssl-enabled: false
```

#### API 端点

**属性上报**：

```
POST /iot/device/property/post
Content-Type: application/json
Authorization: Bearer {deviceToken}

{
  "temperature": 25.5,
  "humidity": 60.0
}
```

**事件上报**：

```
POST /iot/device/event/{eventIdentifier}/post
Content-Type: application/json
Authorization: Bearer {deviceToken}

{
  "current_temp": 85.0,
  "threshold": 80.0
}
```

### Modbus 协议

详细说明请参考 [MODBUS_README.md](yudao-module-iot-gateway/MODBUS_README.md)

#### 主要特性

- 支持 Modbus TCP 和 Modbus RTU
- 主站模式（主动轮询从站设备）
- 支持多种数据类型（INT16/32/64, UINT16/32/64, FLOAT, DOUBLE, STRING）
- 支持多种字节序（BIG_ENDIAN, LITTLE_ENDIAN 及其变种）
- 支持所有常用功能码（FC01~FC06, FC15, FC16）

#### 快速配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus:
          enabled: true
          type: TCP
          host: 192.168.1.100
          port: 502
          polling-enabled: true
          polling-interval-ms: 1000

          slaves:
            - device-id: 2001
              slave-id: 1
              polling-configs:
                - identifier: temperature
                  function-code: READ_HOLDING_REGISTERS
                  start-address: 0
                  quantity: 2
                  data-type: FLOAT
                  byte-order: BIG_ENDIAN
```

### EMQX 集成

支持通过 EMQX 消息代理接入设备。

#### 配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        emqx:
          enabled: true
          auth-url: http://iot-gateway:8080/iot/emqx/auth
          acl-url: http://iot-gateway:8080/iot/emqx/acl
          webhook-url: http://iot-gateway:8080/iot/emqx/webhook
```

#### EMQX 配置

在 EMQX 中配置认证和 Webhook：

```
# 认证
auth.http.auth_req = http://iot-gateway:8080/iot/emqx/auth
auth.http.auth_req.method = post
auth.http.auth_req.params = clientid=%c,username=%u,password=%P

# Webhook
web.hook.url = http://iot-gateway:8080/iot/emqx/webhook
```

---

## 常见问题

### Q1: 如何选择合适的通信协议？

**A**: 根据设备特点选择：

- **MQTT**：适合大多数物联网场景，功耗低，支持 QoS
- **TCP**：需要自定义协议或二进制协议时使用
- **HTTP**：适合间歇性上报数据的设备，简单易用
- **Modbus**：工业设备的标准协议，适合 PLC、仪表等
- **EMQX**：需要大规模设备接入时，使用 EMQX 作为消息代理

### Q2: 设备认证失败怎么办？

**A**: 检查以下项目：

1. ProductKey、DeviceName、DeviceSecret 是否正确
2. 认证密码计算是否正确（注意签名算法）
3. 设备是否已在平台创建
4. 网络连接是否正常

### Q3: 数据上报成功但查询不到？

**A**: 可能的原因：

1. TDengine 未正确配置或未启动
2. 物模型定义与上报的数据不匹配
3. 数据格式错误（检查日志）

### Q4: 场景规则不触发？

**A**: 检查：

1. 场景规则是否已启用
2. 触发条件是否正确配置
3. 设备数据是否满足触发条件
4. 查看系统日志中的规则引擎执行记录

### Q5: OTA 升级失败？

**A**: 常见原因：

1. 固件 URL 无法访问
2. 固件 MD5 校验失败
3. 设备存储空间不足
4. 设备网络不稳定

### Q6: 如何优化系统性能？

**A**: 建议：

1. 合理设置数据上报频率（避免过于频繁）
2. 使用 Redis 缓存热点数据
3. TDengine 定期归档历史数据
4. 场景规则避免过于复杂的条件判断
5. 数据流转使用异步处理

### Q7: 支持的最大设备数量？

**A**: 取决于部署规模：

- **单机部署**：1万~10万设备
- **集群部署**：10万~百万设备
- 关键因素：服务器配置、网络带宽、消息频率

### Q8: 如何保证数据安全？

**A**: 系统提供多层安全保障：

1. **设备认证**：基于密钥的设备认证
2. **传输加密**：支持 SSL/TLS 加密通信
3. **访问控制**：基于角色的权限管理
4. **数据加密**：敏感数据加密存储

### Q9: 如何进行设备调试？

**A**: 调试建议：

1. 使用 MQTT 客户端工具（如 MQTT.fx、MQTTX）模拟设备
2. 查看 Gateway 日志中的设备消息
3. 使用设备消息查询 API 查看历史消息
4. 开启调试日志级别

### Q10: 系统如何扩展？

**A**: 扩展方式：

1. **自定义协议**：实现 `IotDeviceMessageCodec` 接口
2. **自定义数据目的**：扩展数据流转目的类型
3. **自定义规则动作**：扩展场景规则动作类型
4. **插件机制**：通过 Spring Bean 注入自定义组件

---

## 技术支持

如有问题，请联系：

- **技术文档**：查阅项目文档目录
- **问题反馈**：提交 GitHub Issues
- **技术社区**：加入技术交流群

---

**版本信息**：v1.0.0
**最后更新**：2024-01-15
