# BACnet 协议集成指南

## 目录
1. [概述](#概述)
2. [架构设计](#架构设计)
3. [数据库设计](#数据库设计)
4. [配置说明](#配置说明)
5. [使用流程](#使用流程)
6. [API 接口](#api-接口)
7. [开发指南](#开发指南)
8. [故障排查](#故障排查)

---

## 概述

本模块实现了 BACnet 协议与 IoT 平台的集成，支持自动发现 BACnet 设备、采集设备数据、控制设备属性等功能。

### 主要特性

- **自动设备发现**：通过 WHO-IS/I-AM 机制自动发现网络中的 BACnet 设备
- **灵活的属性映射**：将 BACnet 对象属性映射到 IoT 物模型属性
- **定时数据采集**：支持配置轮询间隔，自动采集设备数据
- **属性写入控制**：支持通过物模型下发控制命令到 BACnet 设备
- **数据类型转换**：自动转换 BACnet 数据类型到 Java 类型
- **单位转换和值映射**：支持配置单位转换公式和枚举值映射
- **多设备并发**：支持同时管理多个 BACnet 设备的数据采集

### 技术栈

- **BACnet4J 6.x**：BACnet 协议栈库
- **Spring Boot**：应用框架和依赖注入
- **MyBatis Plus**：数据访问层
- **消息总线**：设备消息的发布和订阅

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────┐
│          BACnet 设备层 (BACnet Devices)              │
│     (空调、照明、传感器等楼宇自动化设备)               │
└──────────────────┬──────────────────────────────────┘
                   │ BACnet/IP (UDP 47808)
┌──────────────────▼──────────────────────────────────┐
│    BACnet 协议层 (yudao-module-iot-core)             │
│  ┌─────────────────────────────────────────────┐    │
│  │ BACnetClient                                │    │
│  │  - 本地设备初始化                             │    │
│  │  - WHO-IS/I-AM 设备发现                      │    │
│  │  - 读写属性请求                               │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │ BACnetDeviceManager                         │    │
│  │  - 设备缓存管理                               │    │
│  │  - 统一接口封装                               │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│    网关层 (yudao-module-iot-gateway)                 │
│  ┌─────────────────────────────────────────────┐    │
│  │ IotBACnetMasterProtocol (数据采集)           │    │
│  │  - 启动轮询调度器                             │    │
│  │  - 定时读取设备属性                           │    │
│  │  - 发布数据到消息总线                         │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │ IotBACnetDownstreamSubscriber (属性控制)     │    │
│  │  - 订阅下行消息                               │    │
│  │  - 处理属性设置命令                           │    │
│  │  - 写入 BACnet 设备                          │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────┬──────────────────────────────────┘
                   │ 消息总线 (Redis/RocketMQ)
┌──────────────────▼──────────────────────────────────┐
│    业务层 (yudao-module-iot-biz)                     │
│  ┌─────────────────────────────────────────────┐    │
│  │ IotBACnetConfigService (配置管理)            │    │
│  │  - 设备配置 CRUD                             │    │
│  │  - 属性映射 CRUD                             │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │ IotBACnetDiscoveryService (设备发现)         │    │
│  │  - 定时自动发现                               │    │
│  │  - 手动触发发现                               │    │
│  │  - 设备绑定                                  │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              数据存储层                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  MySQL   │  │ TDengine │  │  Redis   │          │
│  │(配置数据) │  │(时序数据) │  │(缓存)     │          │
│  └──────────┘  └──────────┘  └──────────┘          │
└─────────────────────────────────────────────────────┘
```

### 模块职责

#### 1. BACnet 协议层 (iot-core)
- **BACnetClient**：封装 bacnet4j 库，提供设备通信能力
- **BACnetDeviceManager**：设备管理和缓存，对外提供统一接口

#### 2. 网关层 (iot-gateway)
- **IotBACnetMasterProtocol**：主站协议，定时轮询设备数据
- **IotBACnetDownstreamSubscriber**：下行订阅器，处理属性写入命令

#### 3. 业务层 (iot-biz)
- **IotBACnetConfigService**：管理设备配置和属性映射
- **IotBACnetDiscoveryService**：自动发现和设备绑定

---

## 数据库设计

### 表结构说明

#### 1. iot_bacnet_device_config（BACnet 设备配置表）

存储 IoT 设备与 BACnet 设备的映射关系和采集配置。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| device_id | BIGINT | IoT 设备 ID（外键） |
| instance_number | INT | BACnet 设备实例号（唯一） |
| ip_address | VARCHAR(50) | BACnet 设备 IP 地址 |
| polling_enabled | BIT | 是否启用轮询 |
| polling_interval | INT | 轮询间隔（毫秒，默认 5000） |
| description | VARCHAR(500) | 配置描述 |

**索引**：
- `uk_device_id`：device_id 唯一索引
- `uk_instance_number`：instance_number 唯一索引

#### 2. iot_bacnet_property_mapping（BACnet 属性映射表）

存储 BACnet 对象属性与物模型属性的映射关系。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| device_id | BIGINT | IoT 设备 ID |
| identifier | VARCHAR(100) | 物模型属性标识符 |
| object_type | VARCHAR(50) | BACnet 对象类型（如 ANALOG_INPUT） |
| object_instance | INT | BACnet 对象实例号 |
| property_identifier | VARCHAR(50) | BACnet 属性标识符（如 presentValue） |
| data_type | VARCHAR(50) | 数据类型 |
| access_mode | VARCHAR(10) | 访问模式（r/w/rw） |
| polling_enabled | BIT | 是否启用轮询 |
| unit_conversion | VARCHAR(200) | 单位转换公式 |
| value_mapping | VARCHAR(1000) | 值映射（JSON 格式） |
| priority | INT | 写入优先级（1-16） |

**索引**：
- `idx_device_id`：device_id 索引
- `uk_device_identifier`：(device_id, identifier) 唯一索引

**BACnet 对象类型示例**：
- `ANALOG_INPUT`：模拟量输入（传感器值）
- `ANALOG_OUTPUT`：模拟量输出（设定值）
- `ANALOG_VALUE`：模拟量变量
- `BINARY_INPUT`：开关量输入（状态）
- `BINARY_OUTPUT`：开关量输出（控制）
- `BINARY_VALUE`：开关量变量
- `MULTI_STATE_INPUT`：多状态输入
- `MULTI_STATE_OUTPUT`：多状态输出

**BACnet 属性标识符示例**：
- `presentValue`：当前值（最常用）
- `statusFlags`：状态标志
- `outOfService`：离线状态
- `units`：工程单位
- `description`：描述

#### 3. iot_bacnet_discovery_record（BACnet 设备发现记录表）

存储自动发现的 BACnet 设备信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| instance_number | INT | BACnet 设备实例号 |
| device_name | VARCHAR(100) | 设备名称 |
| ip_address | VARCHAR(50) | IP 地址 |
| vendor_name | VARCHAR(100) | 厂商名称 |
| model_name | VARCHAR(100) | 型号 |
| device_id | BIGINT | 绑定的 IoT 设备 ID |
| bind_status | TINYINT | 绑定状态（0=未绑定，1=已绑定） |
| online_status | TINYINT | 在线状态（0=离线，1=在线） |
| last_seen_time | DATETIME | 最后发现时间 |

**索引**：
- `idx_instance_number`：instance_number 索引
- `idx_bind_status`：bind_status 索引

---

## 配置说明

### 1. application.yaml 配置

```yaml
yudao:
  iot:
    bacnet:
      # 启用 BACnet 协议
      enabled: true

      # BACnet 本地设备 ID（必须在网络中唯一）
      device-id: 1234

      # BACnet 端口（默认 47808）
      port: 47808

      # 本地绑定地址（可选，多网卡环境需配置）
      local-bind-address: 192.168.1.100

      # 广播地址（可选）
      broadcast-address: 192.168.1.255

      # 设备发现超时时间（毫秒）
      discovery-timeout: 3000

      # 设备发现定时任务 Cron 表达式（每5分钟执行一次）
      discovery-cron: "0 */5 * * * ?"
```

### 2. 配置参数说明

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| enabled | 是 | false | 是否启用 BACnet 协议 |
| device-id | 是 | - | BACnet 本地设备 ID（1-4194303） |
| port | 否 | 47808 | BACnet/IP UDP 端口 |
| local-bind-address | 否 | 所有网卡 | 绑定的本地 IP 地址 |
| broadcast-address | 否 | 自动 | BACnet 广播地址 |
| discovery-timeout | 否 | 3000 | WHO-IS 响应超时时间（毫秒） |
| discovery-cron | 否 | 0 */5 * * * ? | 自动发现定时任务 Cron 表达式 |

---

## 使用流程

### 流程 1：设备发现与绑定

```
┌─────────────────────────────────────────────────────────┐
│ 1. 自动设备发现（定时任务，每 5 分钟）                      │
│    - BACnetDiscoveryService 执行设备发现                 │
│    - 发送 WHO-IS 广播，收集 I-AM 响应                    │
│    - 保存到 iot_bacnet_discovery_record 表               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 2. 管理员查看未绑定设备列表                               │
│    GET /iot/bacnet/discovery/unbound-list               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 3. 创建 IoT 产品和物模型                                  │
│    - 在 IoT 平台创建产品（如"空调控制器"）                 │
│    - 定义物模型属性（如 temperature, setPoint 等）        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 4. 创建 IoT 设备                                         │
│    - 选择产品，创建设备实例                               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 5. 绑定 BACnet 设备                                      │
│    POST /iot/bacnet/discovery/bind                      │
│    {                                                    │
│      "discoveryRecordId": 1,                            │
│      "deviceId": 1001,                                  │
│      "pollingEnabled": true,                            │
│      "pollingInterval": 5000                            │
│    }                                                    │
│    - 创建 device_config 记录                             │
│    - 可选：智能映射属性（autoMap=true）                    │
└─────────────────────────────────────────────────────────┘
```

### 流程 2：属性映射配置

```
┌─────────────────────────────────────────────────────────┐
│ 1. 创建属性映射                                           │
│    POST /iot/bacnet/config/mapping/create               │
│    {                                                    │
│      "deviceId": 1001,                                  │
│      "identifier": "temperature",  // 物模型属性标识符    │
│      "objectType": "ANALOG_INPUT", // BACnet 对象类型    │
│      "objectInstance": 0,          // BACnet 对象实例号  │
│      "propertyIdentifier": "presentValue", // 属性 ID    │
│      "dataType": "float",                               │
│      "accessMode": "r",           // 只读                │
│      "pollingEnabled": true       // 启用轮询             │
│    }                                                    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 2. 配置多个属性映射                                       │
│    - temperature（温度传感器）                            │
│    - setPoint（温度设定值）                               │
│    - fanSpeed（风扇速度）                                │
│    - powerStatus（开关状态）                             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 3. 配置单位转换和值映射（可选）                            │
│    {                                                    │
│      "unitConversion": "value * 0.1",  // 单位转换公式   │
│      "valueMapping": "{\"0\":\"关\",\"1\":\"开\"}"      │
│    }                                                    │
└─────────────────────────────────────────────────────────┘
```

### 流程 3：数据采集

```
┌─────────────────────────────────────────────────────────┐
│ 1. IotBACnetMasterProtocol 启动                          │
│    - @PostConstruct 初始化                               │
│    - 加载所有 pollingEnabled=true 的设备配置              │
│    - 为每个设备启动独立的轮询任务                          │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ 每隔 pollingInterval 毫秒
                     │
┌────────────────────▼────────────────────────────────────┐
│ 2. 轮询设备数据                                           │
│    - 遍历设备的所有 pollingEnabled 属性映射               │
│    - 调用 BACnetDeviceManager.readDeviceProperty()      │
│    - 读取 BACnet 对象属性值                              │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 3. 数据转换                                              │
│    - BACnet Encodable → Java 基本类型                    │
│    - 应用单位转换公式                                     │
│    - 应用值映射                                          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 4. 发布设备消息                                           │
│    - 构建 IotDeviceMessage                              │
│    - method = "thing.property.report"                   │
│    - params = { "temperature": 25.5, ... }             │
│    - 发布到消息总线                                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┘
│ 5. 业务层处理                                            │
│    - 保存到 TDengine 时序数据库                          │
│    - 触发规则引擎                                        │
│    - 更新设备影子                                        │
└─────────────────────────────────────────────────────────┘
```

### 流程 4：属性写入（设备控制）

```
┌─────────────────────────────────────────────────────────┐
│ 1. 前端发起属性设置请求                                   │
│    POST /iot/device/property/set                        │
│    {                                                    │
│      "deviceId": 1001,                                  │
│      "properties": {                                    │
│        "setPoint": 26.0,     // 设定温度                │
│        "powerStatus": 1      // 开机                    │
│      }                                                  │
│    }                                                    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 2. 业务层处理                                            │
│    - 验证设备存在                                        │
│    - 验证属性定义                                        │
│    - 构建下行消息                                        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 3. 发布下行消息                                           │
│    - method = "thing.property.set"                      │
│    - topic = "iot_device_message_bacnet"                │
│    - serverId = BACnet 网关 ID                          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 4. IotBACnetDownstreamSubscriber 接收消息                │
│    - 验证 serverId 匹配                                  │
│    - 验证消息方法                                        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 5. 查找属性映射并验证权限                                 │
│    - 根据 identifier 查找映射配置                         │
│    - 检查 accessMode 是否包含 'w'                        │
│    - 应用反向单位转换                                     │
│    - 应用反向值映射                                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 6. 写入 BACnet 设备                                      │
│    - 调用 BACnetDeviceManager.writeProperty()           │
│    - 发送 WritePropertyRequest 到 BACnet 设备           │
│    - 使用配置的 priority（1-16）                         │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│ 7. 返回执行结果                                           │
│    - 构建响应消息                                        │
│    - method = "thing.property.set.reply"                │
│    - params = { "code": 200, "message": "成功" }        │
│    - 发布到消息总线                                       │
└─────────────────────────────────────────────────────────┘
```

---

## API 接口

### 设备发现接口

#### 1. 立即执行设备发现
```
POST /iot/bacnet/discovery/discover-now
```
**响应**：发现的设备列表

#### 2. 获取未绑定设备列表
```
GET /iot/bacnet/discovery/unbound-list
```

#### 3. 获取已绑定设备列表
```
GET /iot/bacnet/discovery/bound-list
```

#### 4. 绑定 BACnet 设备
```
POST /iot/bacnet/discovery/bind
Content-Type: application/json

{
  "discoveryRecordId": 1,
  "deviceId": 1001,
  "pollingEnabled": true,
  "pollingInterval": 5000,
  "autoMap": false
}
```

### 设备配置接口

#### 1. 创建 BACnet 设备配置
```
POST /iot/bacnet/config/device/create
Content-Type: application/json

{
  "deviceId": 1001,
  "instanceNumber": 12345,
  "ipAddress": "192.168.1.10",
  "pollingEnabled": true,
  "pollingInterval": 5000,
  "description": "1号楼空调控制器"
}
```

#### 2. 更新设备配置
```
PUT /iot/bacnet/config/device/update
```

#### 3. 删除设备配置
```
DELETE /iot/bacnet/config/device/delete?id=1
```

#### 4. 查询设备配置
```
GET /iot/bacnet/config/device/get?id=1
GET /iot/bacnet/config/device/get-by-device?deviceId=1001
GET /iot/bacnet/config/device/page
```

### 属性映射接口

#### 1. 创建属性映射
```
POST /iot/bacnet/config/mapping/create
Content-Type: application/json

{
  "deviceId": 1001,
  "identifier": "temperature",
  "objectType": "ANALOG_INPUT",
  "objectInstance": 0,
  "propertyIdentifier": "presentValue",
  "dataType": "float",
  "accessMode": "r",
  "pollingEnabled": true,
  "unitConversion": "value * 0.1",
  "valueMapping": null,
  "priority": null
}
```

#### 2. 更新属性映射
```
PUT /iot/bacnet/config/mapping/update
```

#### 3. 删除属性映射
```
DELETE /iot/bacnet/config/mapping/delete?id=1
```

#### 4. 查询属性映射
```
GET /iot/bacnet/config/mapping/get?id=1
GET /iot/bacnet/config/mapping/page
GET /iot/bacnet/config/mapping/list-by-device?deviceId=1001
```

---

## 开发指南

### 添加新的数据类型转换

在 `IotBACnetMasterProtocol.convertBACnetValue()` 方法中添加：

```java
private Object convertBACnetValue(Object value, String dataType) {
    // 添加新的类型转换
    if (value instanceof com.serotonin.bacnet4j.type.primitive.Date) {
        return convertBACnetDate((com.serotonin.bacnet4j.type.primitive.Date) value);
    }
    // ...
}
```

### 实现单位转换

在 `IotBACnetMasterProtocol.convertValue()` 中实现：

```java
private Object convertValue(Object value, IotBACnetPropertyMappingDO mapping) {
    if (mapping.getUnitConversion() != null) {
        // 使用 SpEL 或 MVEL 表达式引擎
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(mapping.getUnitConversion());

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("value", value);

        return exp.getValue(context);
    }
    return value;
}
```

### 实现值映射

在 `IotBACnetMasterProtocol.convertValue()` 中实现：

```java
private Object convertValue(Object value, IotBACnetPropertyMappingDO mapping) {
    if (mapping.getValueMapping() != null) {
        // 解析 JSON 映射
        Map<String, Object> mappingTable = JsonUtils.parseObject(
            mapping.getValueMapping(),
            new TypeReference<Map<String, Object>>() {}
        );

        String key = String.valueOf(value);
        return mappingTable.getOrDefault(key, value);
    }
    return value;
}
```

---

## 故障排查

### 问题 1：无法发现 BACnet 设备

**可能原因**：
1. BACnet 协议未启用
2. 网络配置错误
3. 防火墙阻止 UDP 47808 端口
4. BACnet 设备未开启 BACnet/IP 功能

**解决方法**：
```bash
# 1. 检查配置
检查 application.yaml 中 yudao.iot.bacnet.enabled = true

# 2. 检查网络连通性
ping 192.168.1.10

# 3. 检查端口监听
netstat -an | grep 47808

# 4. 手动测试 BACnet 通信（使用 BACnet 工具）
使用 Yabe 或其他 BACnet 工具测试设备响应
```

### 问题 2：属性读取失败

**可能原因**：
1. 对象类型或实例号配置错误
2. 属性标识符错误
3. BACnet 设备不支持该属性
4. 网络延迟或超时

**解决方法**：
```bash
# 查看错误日志
grep "读取 BACnet 属性失败" application.log

# 验证对象配置
使用 BACnet 工具查看设备的对象列表

# 增加日志级别
logging:
  level:
    cn.iocoder.yudao.module.iot.gateway.protocol.bacnet: DEBUG
```

### 问题 3：属性写入失败

**可能原因**：
1. accessMode 配置不正确（未包含 'w'）
2. BACnet 对象不可写
3. priority 配置错误
4. 数据类型不匹配

**解决方法**：
```sql
-- 检查属性映射配置
SELECT * FROM iot_bacnet_property_mapping
WHERE identifier = 'setPoint';

-- 确认 accessMode 包含 'w'
UPDATE iot_bacnet_property_mapping
SET access_mode = 'rw'
WHERE identifier = 'setPoint';

-- 调整 priority（1-16，数字越小优先级越高）
UPDATE iot_bacnet_property_mapping
SET priority = 8
WHERE identifier = 'setPoint';
```

### 问题 4：轮询不工作

**可能原因**：
1. pollingEnabled 未启用
2. pollingInterval 配置过大
3. 设备配置未正确加载
4. 调度器未启动

**解决方法**：
```sql
-- 检查设备配置
SELECT * FROM iot_bacnet_device_config WHERE device_id = 1001;

-- 启用轮询
UPDATE iot_bacnet_device_config
SET polling_enabled = 1, polling_interval = 5000
WHERE device_id = 1001;

-- 检查属性映射
SELECT * FROM iot_bacnet_property_mapping
WHERE device_id = 1001 AND polling_enabled = 1;
```

```bash
# 重启应用使配置生效
# 查看日志确认轮询启动
grep "启动设备轮询" application.log
```

### 问题 5：数据未存储到数据库

**可能原因**：
1. 消息总线未正常工作
2. 业务层处理异常
3. 数据库连接问题

**解决方法**：
```bash
# 检查消息发布日志
grep "发布 BACnet 设备消息" application.log

# 检查消息总线配置
yudao:
  iot:
    message-bus:
      type: redis  # 或 local、rocketmq

# 检查 Redis 连接
redis-cli ping
```

---

## 附录

### A. BACnet 对象类型参考

| 对象类型 | 说明 | 常用属性 |
|---------|------|---------|
| ANALOG_INPUT | 模拟量输入 | presentValue, units, description |
| ANALOG_OUTPUT | 模拟量输出 | presentValue, units, priorityArray |
| ANALOG_VALUE | 模拟量变量 | presentValue, units |
| BINARY_INPUT | 开关量输入 | presentValue, polarity |
| BINARY_OUTPUT | 开关量输出 | presentValue, polarity, priorityArray |
| BINARY_VALUE | 开关量变量 | presentValue |
| MULTI_STATE_INPUT | 多状态输入 | presentValue, numberOfStates, stateText |
| MULTI_STATE_OUTPUT | 多状态输出 | presentValue, numberOfStates, priorityArray |

### B. 访问模式说明

| accessMode | 说明 | 用途 |
|-----------|------|------|
| r | 只读 | 传感器数据采集 |
| w | 只写 | 纯控制命令 |
| rw | 读写 | 设定值（可读可写） |

### C. 优先级说明

BACnet 写入优先级范围：1-16
- 1-2：生命安全（手动/自动）
- 3-4：关键设备控制
- 5-7：运行优化
- 8：手动操作（推荐默认值）
- 9-16：低优先级控制

---

## 更新日志

- **2025-11-22**：初始版本，实现基本的设备发现、数据采集和属性写入功能
