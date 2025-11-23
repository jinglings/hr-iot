# BACnet 协议部署指南

## 一、代码修复说明

已修复的问题：

### 1. 消息发布机制
- **问题**: 原本使用不存在的 `IotDeviceMessageService.publishDeviceMessage()` 方法
- **修复**: 改用 `IotDeviceMessageProducer.sendDeviceMessage()` 直接发送到消息总线
- **影响文件**: `IotBACnetMasterProtocol.java`

### 2. 消息方法枚举
- **问题**: 使用不存在的 `PROPERTY_REPORT` 枚举值
- **修复**: 改用 `PROPERTY_POST`（属性上报）
- **影响文件**: `IotBACnetMasterProtocol.java`

### 3. 下行消息订阅机制
- **问题**: 使用不存在的订阅方法
- **修复**: 实现 `IotMessageSubscriber` 接口，使用 `IotMessageBus.register()` 注册订阅器
- **影响文件**: `IotBACnetDownstreamSubscriber.java`

### 4. 类型转换问题
- **问题**: `message.getParams()` 返回 `Object` 类型，直接赋值给 `Map` 导致编译错误
- **修复**: 添加类型检查和强制转换，使用 `@SuppressWarnings("unchecked")` 注解
- **影响文件**: `IotBACnetDownstreamSubscriber.java`

### 5. 依赖注入问题
- **问题**: 配置类中的依赖注入参数与实际构造函数不匹配
- **修复**: 更新为正确的依赖注入参数
- **影响文件**: `BACnetProtocolConfiguration.java`

---

## 二、核心实现说明

### 2.1 消息流转机制

#### 上行消息流（设备→平台）

```
BACnet设备
    ↓ (BACnet/IP协议)
BACnetDeviceManager.readDeviceProperty()
    ↓
IotBACnetMasterProtocol.pollDevice()
    ↓ 构建 IotDeviceMessage
    │ - method: "thing.property.post"
    │ - params: { "temperature": 25.5, ... }
    ↓
IotDeviceMessageProducer.sendDeviceMessage()
    ↓ 发送到消息总线
IotMessageBus.post(topic, message)
    ↓
业务层处理
    ├─> 保存到TDengine时序数据库
    ├─> 触发规则引擎
    └─> 更新设备影子
```

#### 下行消息流（平台→设备）

```
前端/业务层
    ↓ 发起属性设置请求
IotDeviceMessageProducer.sendDeviceMessageToGateway()
    ↓ 发送到网关Topic
    │ topic = "iot:device:message:gateway:{serverId}"
    │ method = "thing.property.set"
    │ params = { "setPoint": 26.0 }
    ↓
IotMessageBus (消息总线)
    ↓ 分发给订阅者
IotBACnetDownstreamSubscriber.onMessage()
    ↓ 处理属性设置
    │ - 查找属性映射配置
    │ - 验证写入权限
    │ - 应用值映射和单位转换
    ↓
BACnetDeviceManager.writeProperty()
    ↓ (BACnet/IP协议)
BACnet设备
```

### 2.2 核心类说明

#### IotBACnetMasterProtocol（主站协议）
- **职责**: 定时轮询BACnet设备，采集数据
- **依赖**:
  - `IotDeviceMessageProducer`: 发送设备消息到消息总线
  - `BACnetDeviceManager`: 读取BACnet设备属性
  - `IotDeviceService`: 获取IoT设备信息（缓存）
  - Mapper: 获取配置和映射信息

#### IotBACnetDownstreamSubscriber（下行订阅器）
- **职责**: 订阅下行消息，执行属性写入
- **实现**: `IotMessageSubscriber<IotDeviceMessage>` 接口
- **Topic**: `iot:device:message:gateway:{serverId}`
- **依赖**:
  - `IotMessageBus`: 注册订阅器
  - `BACnetDeviceManager`: 写入BACnet设备属性
  - Mapper: 获取配置和映射信息

---

## 三、部署步骤

### 步骤 1: 数据库初始化

```bash
# 执行BACnet数据库脚本
mysql -h localhost -u root -p your_database < sql/mysql/iot_bacnet_protocol.sql
```

验证表创建成功：
```sql
SHOW TABLES LIKE 'iot_bacnet%';

-- 应该看到3个表：
-- iot_bacnet_device_config
-- iot_bacnet_property_mapping
-- iot_bacnet_discovery_record
```

### 步骤 2: 配置BACnet参数

**重要**: 必须配置BACnet参数，否则应用无法正常启动。

在 `yudao-server/src/main/resources/application-local.yaml` 中添加：

```yaml
yudao:
  iot:
    bacnet:
      # 启用BACnet协议
      enabled: true

      # BACnet本地设备ID（必须在网络中唯一，范围1-4194303）
      device-id: 1234

      # BACnet端口（标准端口47808）
      port: 47808

      # 本地绑定地址（多网卡环境必须指定）
      local-bind-address: 192.168.1.100

      # 广播地址（自动发现使用）
      broadcast-address: 192.168.1.255

      # 设备发现超时（毫秒）
      discovery-timeout: 3000

      # 设备发现定时任务Cron（每5分钟）
      discovery-cron: "0 */5 * * * ?"
```

**配置说明**：

| 配置项 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| enabled | 否 | false | 设为 true 才能启用BACnet功能 |
| device-id | 否 | 1234 | BACnet网络中的唯一设备ID |
| port | 否 | 47808 | BACnet监听端口 |
| local-bind-address | 否 | 0.0.0.0 | 多网卡环境建议指定 |
| broadcast-address | 否 | 255.255.255.255 | 自动发现设备时使用 |

**最小配置（禁用BACnet）**：
```yaml
yudao:
  iot:
    bacnet:
      enabled: false  # 不启用BACnet功能
```

**完整配置（启用BACnet）**：
```yaml
yudao:
  iot:
    bacnet:
      enabled: true
      device-id: 1234
      port: 47808
      local-bind-address: 192.168.1.100
      broadcast-address: 192.168.1.255
```

### 步骤 3: 启动应用

```bash
cd yudao-server
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

查看启动日志，确认BACnet模块已启动：
```
[main] INFO  BACnetDeviceManager - BACnet 设备管理器初始化成功
[main] INFO  IotBACnetMasterProtocol - [start][BACnet 主站协议启动成功，服务器 ID: bacnet-xxx, 设备数量: 0]
[main] INFO  IotBACnetDownstreamSubscriber - [init][BACnet 下行消息订阅器初始化完成，服务器 ID: bacnet-xxx, Topic: iot:device:message:gateway:bacnet-xxx]
```

### 步骤 4: 网络检查

确保防火墙开放UDP 47808端口：

**Linux (firewalld)**:
```bash
sudo firewall-cmd --add-port=47808/udp --permanent
sudo firewall-cmd --reload
```

**Linux (iptables)**:
```bash
sudo iptables -A INPUT -p udp --dport 47808 -j ACCEPT
sudo iptables-save
```

**Windows**:
```powershell
netsh advfirewall firewall add rule name="BACnet" dir=in action=allow protocol=UDP localport=47808
```

验证端口监听：
```bash
# Linux/Mac
netstat -an | grep 47808

# Windows
netstat -an | findstr 47808
```

---

## 四、使用流程

### 4.1 设备发现与绑定

#### 手动触发发现

```bash
curl -X POST http://localhost:48080/admin-api/iot/bacnet/discovery/discover-now \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应示例：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "instanceNumber": 12345,
      "deviceName": "VAV-1",
      "ipAddress": "192.168.1.10",
      "vendorName": "Honeywell",
      "modelName": "VAV Controller",
      "bindStatus": 0,
      "onlineStatus": 1
    }
  ]
}
```

#### 查看未绑定设备

```bash
curl http://localhost:48080/admin-api/iot/bacnet/discovery/unbound-list \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 创建IoT产品和设备

1. **创建产品**（通过管理后台或API）
   - 产品名称：空调控制器
   - 产品标识：vav-controller

2. **定义物模型**
   ```json
   {
     "properties": [
       {
         "identifier": "temperature",
         "name": "温度",
         "dataType": "float",
         "accessMode": "r",
         "unit": "℃"
       },
       {
         "identifier": "setPoint",
         "name": "温度设定值",
         "dataType": "float",
         "accessMode": "rw",
         "unit": "℃"
       },
       {
         "identifier": "fanSpeed",
         "name": "风扇速度",
         "dataType": "int",
         "accessMode": "rw"
       }
     ]
   }
   ```

3. **创建设备**
   - 设备名称：1号楼空调
   - 设备标识：device001

#### 绑定BACnet设备

```bash
curl -X POST http://localhost:48080/admin-api/iot/bacnet/discovery/bind \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "discoveryRecordId": 1,
    "deviceId": 1001,
    "pollingEnabled": true,
    "pollingInterval": 5000,
    "autoMap": false
  }'
```

### 4.2 配置属性映射

#### 创建温度传感器映射

```bash
curl -X POST http://localhost:48080/admin-api/iot/bacnet/config/mapping/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "deviceId": 1001,
    "identifier": "temperature",
    "objectType": "ANALOG_INPUT",
    "objectInstance": 0,
    "propertyIdentifier": "presentValue",
    "dataType": "float",
    "accessMode": "r",
    "pollingEnabled": true,
    "unitConversion": null,
    "valueMapping": null,
    "priority": null
  }'
```

#### 创建温度设定值映射（可读可写）

```bash
curl -X POST http://localhost:48080/admin-api/iot/bacnet/config/mapping/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "deviceId": 1001,
    "identifier": "setPoint",
    "objectType": "ANALOG_VALUE",
    "objectInstance": 1,
    "propertyIdentifier": "presentValue",
    "dataType": "float",
    "accessMode": "rw",
    "pollingEnabled": true,
    "unitConversion": null,
    "valueMapping": null,
    "priority": 8
  }'
```

#### 创建开关量映射（带值映射）

```bash
curl -X POST http://localhost:48080/admin-api/iot/bacnet/config/mapping/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "deviceId": 1001,
    "identifier": "powerStatus",
    "objectType": "BINARY_OUTPUT",
    "objectInstance": 0,
    "propertyIdentifier": "presentValue",
    "dataType": "int",
    "accessMode": "rw",
    "pollingEnabled": true,
    "unitConversion": null,
    "valueMapping": "{\"0\":\"关\",\"1\":\"开\"}",
    "priority": 8
  }'
```

### 4.3 查看采集数据

配置完成后，系统会自动开始采集数据。查看日志：

```bash
tail -f logs/application.log | grep BACnet
```

预期日志：
```
INFO  IotBACnetMasterProtocol - [startPollingForDevice][启动设备轮询，设备 ID: 1001, Instance: 12345, 轮询项数量: 3, 轮询间隔: 5000ms]
DEBUG IotBACnetMasterProtocol - [publishDeviceMessage][发布 BACnet 设备消息，设备 ID: 1001，属性数量: 3]
```

### 4.4 下发控制命令

通过业务层API下发属性设置命令（具体API需要业务层实现）：

```bash
curl -X POST http://localhost:48080/admin-api/iot/device/property/set \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "deviceId": 1001,
    "properties": {
      "setPoint": 26.0,
      "powerStatus": 1
    }
  }'
```

查看下行消息处理日志：
```
INFO  IotBACnetDownstreamSubscriber - [handlePropertySet][写入成功] deviceId=1001, identifier=setPoint, value=26.0
INFO  IotBACnetDownstreamSubscriber - [handlePropertySet][写入成功] deviceId=1001, identifier=powerStatus, value=1
```

---

## 五、常见问题排查

### 问题1: 无法发现设备

**检查项**:
1. 确认BACnet设备在同一网段
   ```bash
   ping 192.168.1.10
   ```

2. 检查端口监听
   ```bash
   netstat -an | grep 47808
   ```

3. 检查广播地址配置
   ```bash
   # 查看网络接口
   ip addr show  # Linux
   ifconfig      # Mac
   ipconfig      # Windows
   ```

4. 使用BACnet工具测试（如YABE）

### 问题2: 属性读取失败

**检查项**:
1. 查看错误日志
   ```bash
   grep "读取 BACnet 属性失败" logs/application.log
   ```

2. 验证对象配置
   - objectType 是否正确（ANALOG_INPUT, BINARY_OUTPUT等）
   - objectInstance 是否正确
   - propertyIdentifier 是否正确（通常是presentValue）

3. 使用BACnet工具验证对象是否存在

### 问题3: 属性写入失败

**检查项**:
1. 检查accessMode配置
   ```sql
   SELECT identifier, access_mode
   FROM iot_bacnet_property_mapping
   WHERE device_id = 1001;
   ```

2. 检查priority配置
   - 优先级1-16，推荐使用8
   - 某些属性可能不支持特定优先级

3. 检查BACnet设备是否支持写入

### 问题4: 轮询不工作

**检查项**:
1. 确认轮询已启用
   ```sql
   SELECT * FROM iot_bacnet_device_config
   WHERE device_id = 1001;
   -- 检查 polling_enabled = 1
   ```

2. 确认属性映射轮询已启用
   ```sql
   SELECT * FROM iot_bacnet_property_mapping
   WHERE device_id = 1001;
   -- 检查 polling_enabled = 1
   ```

3. 重启应用使配置生效

### 问题5: 编译错误

如果遇到编译错误，检查以下依赖：

```xml
<!-- pom.xml 确保包含BACnet依赖 -->
<dependency>
    <groupId>com.serotonin.bacnet4j</groupId>
    <artifactId>bacnet4j</artifactId>
    <version>6.0.0</version>
</dependency>
```

---

## 六、性能优化建议

### 6.1 轮询间隔设置

- **温度传感器**: 5000-10000ms（5-10秒）
- **开关状态**: 2000-5000ms（2-5秒）
- **能耗数据**: 60000ms（1分钟）

### 6.2 并发控制

线程池大小根据设备数量动态调整：
```java
Math.min(deviceCount, Runtime.getRuntime().availableProcessors())
```

### 6.3 数据存储优化

- 实时数据存入 TDengine 时序数据库
- 配置数据存入 MySQL
- 设备在线状态缓存在 Redis

---

## 七、监控与告警

### 7.1 关键指标

- BACnet消息发送成功率
- 设备在线率
- 属性读取失败率
- 轮询任务执行时间

### 7.2 日志监控

```bash
# 监控错误日志
tail -f logs/application.log | grep ERROR | grep BACnet

# 监控消息发布
tail -f logs/application.log | grep "发布 BACnet 设备消息"

# 监控属性写入
tail -f logs/application.log | grep "写入成功\|写入失败"
```

---

## 八、总结

BACnet协议集成已完成以下核心功能：

✅ **设备自动发现** - 定时扫描网络中的BACnet设备
✅ **设备配置管理** - 数据库存储设备配置和属性映射
✅ **定时数据采集** - 支持自定义轮询间隔
✅ **属性写入控制** - 支持下发控制命令到BACnet设备
✅ **数据类型转换** - 自动转换BACnet和Java类型
✅ **消息总线集成** - 与IoT平台消息总线无缝集成
✅ **多设备并发** - 支持多设备同时轮询采集

**待实现功能**:
- 单位转换公式执行引擎（表达式计算）
- 值映射功能（JSON格式枚举映射）
- BACnet COV（Change of Value）订阅机制
- 设备健康监控和自动重连

**部署前检查清单**:
- [ ] 数据库表已创建
- [ ] BACnet配置已添加到application.yaml
- [ ] 防火墙UDP 47808端口已开放
- [ ] BACnet设备在同一网段
- [ ] 产品和物模型已定义
- [ ] 设备已创建并绑定BACnet配置
- [ ] 属性映射已配置

现在可以开始部署和测试BACnet协议集成功能了！
