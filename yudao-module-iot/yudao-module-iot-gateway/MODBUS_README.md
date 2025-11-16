# Modbus 主站功能使用指南

## 概述

本模块实现了完整的 Modbus 主站功能，支持主动读写 Modbus 从站设备，并与 IoT 平台无缝集成。

### 主要特性

1. **Modbus 协议支持**
   - Modbus TCP
   - Modbus RTU（串口）

2. **主站功能**
   - 主动轮询读取设备数据
   - 下行命令写入设备
   - 连接管理和自动重连

3. **数据类型扩展**
   - 基础类型：INT16, UINT16, BOOL
   - 扩展整数：INT32, UINT32, INT64, UINT64
   - 浮点数：FLOAT (32位), DOUBLE (64位)
   - 字符串：STRING

4. **字节序支持**
   - BIG_ENDIAN (ABCD)
   - LITTLE_ENDIAN (DCBA)
   - BIG_ENDIAN_SWAP (BADC)
   - LITTLE_ENDIAN_SWAP (CDAB)

5. **Modbus 功能码**
   - FC01: READ_COILS (读取线圈)
   - FC02: READ_DISCRETE_INPUTS (读取离散输入)
   - FC03: READ_HOLDING_REGISTERS (读取保持寄存器)
   - FC04: READ_INPUT_REGISTERS (读取输入寄存器)
   - FC05: WRITE_SINGLE_COIL (写单个线圈)
   - FC06: WRITE_SINGLE_REGISTER (写单个寄存器)
   - FC15: WRITE_MULTIPLE_COILS (写多个线圈)
   - FC16: WRITE_MULTIPLE_REGISTERS (写多个寄存器)

## 架构设计

### 组件说明

```
┌─────────────────────────────────────────────────────────────┐
│                   Modbus 主站架构                             │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │  IotModbusMasterProtocol (主站协议)                 │     │
│  │  - 定时轮询从站设备                                  │     │
│  │  - 发布设备数据到消息总线                            │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐     │
│  │  IotModbusConnectionManager (连接管理器)            │     │
│  │  - TCP/RTU 连接池管理                               │     │
│  │  - 读写 Modbus 寄存器/线圈                          │     │
│  │  - 设备在线状态管理                                 │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
│  ┌────────────────▼───────────────────────────────────┐     │
│  │  ModbusDataTypeConverter (数据类型转换器)           │     │
│  │  - 寄存器 ↔ 各种数据类型转换                        │     │
│  │  - 字节序处理                                       │     │
│  └────────────────────────────────────────────────────┘     │
│                                                               │
│  ┌──────────────────────────────────────────────────┐       │
│  │  IotModbusDownstreamSubscriber (下行订阅器)        │       │
│  │  - 订阅消息总线的下行命令                          │       │
│  └────────────────┬─────────────────────────────────┘       │
│                   │                                          │
│  ┌────────────────▼─────────────────────────────────┐       │
│  │  IotModbusDownstreamHandler (下行处理器)           │       │
│  │  - 处理属性设置命令                                │       │
│  │  - 处理写寄存器/线圈命令                           │       │
│  └────────────────────────────────────────────────────       │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### 数据流向

#### 上行数据流（设备 → 平台）
```
Modbus 从站设备
    ↓ (Modbus TCP/RTU)
IotModbusMasterProtocol (定时轮询)
    ↓
IotModbusConnectionManager (读取寄存器)
    ↓
ModbusDataTypeConverter (数据类型转换)
    ↓
IotDeviceMessage (构建消息)
    ↓
IotMessageBus (消息总线)
    ↓
IoT Biz 层处理 (规则引擎、数据存储等)
```

#### 下行数据流（平台 → 设备）
```
IoT Biz 层 (发送命令)
    ↓
IotMessageBus (消息总线)
    ↓
IotModbusDownstreamSubscriber (订阅)
    ↓
IotModbusDownstreamHandler (处理)
    ↓
ModbusDataTypeConverter (数据类型转换)
    ↓
IotModbusConnectionManager (写入寄存器)
    ↓ (Modbus TCP/RTU)
Modbus 从站设备
```

## 配置说明

### 1. 启用 Modbus 协议

在 `application.yml` 中添加配置：

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus:
          enabled: true  # 启用 Modbus 协议
```

### 2. Modbus TCP 配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus:
          enabled: true
          type: TCP
          host: 192.168.1.100  # Modbus 从站 IP
          port: 502            # Modbus TCP 端口
          connect-timeout-ms: 3000
          read-timeout-ms: 3000
          reconnect-delay-ms: 5000
          max-retries: 3
```

### 3. Modbus RTU 配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus:
          enabled: true
          type: RTU
          serial-port: /dev/ttyUSB0  # 串口名称
          baud-rate: 9600
          data-bits: 8
          stop-bits: 1
          parity: NONE  # NONE, ODD, EVEN
```

### 4. 从站设备配置

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus:
          # ... 基础配置 ...
          polling-enabled: true
          polling-interval-ms: 1000  # 轮询间隔（毫秒）

          slaves:
            - device-id: 1001          # IoT 平台设备 ID
              slave-id: 1              # Modbus 从站地址 (1-247)
              device-name: sensor-01
              product-key: temp-sensor
              # 可选：覆盖全局 host/port
              host: 192.168.1.101
              port: 502

              polling-configs:
                - identifier: temperature     # 属性标识符
                  function-code: READ_HOLDING_REGISTERS
                  start-address: 0            # 起始地址
                  quantity: 2                 # 读取数量（寄存器个数）
                  data-type: FLOAT            # 数据类型
                  byte-order: BIG_ENDIAN      # 字节序
```

## 使用示例

### 示例 1：温湿度传感器（浮点数）

```yaml
slaves:
  - device-id: 1001
    slave-id: 1
    device-name: temp-sensor-01
    product-key: temp-sensor

    polling-configs:
      # 温度（32位浮点数，占用2个寄存器）
      - identifier: temperature
        function-code: READ_HOLDING_REGISTERS
        start-address: 0
        quantity: 2
        data-type: FLOAT
        byte-order: BIG_ENDIAN

      # 湿度（32位浮点数，占用2个寄存器）
      - identifier: humidity
        function-code: READ_HOLDING_REGISTERS
        start-address: 2
        quantity: 2
        data-type: FLOAT
        byte-order: BIG_ENDIAN
```

### 示例 2：电能表（多种数据类型）

```yaml
slaves:
  - device-id: 1002
    slave-id: 2
    device-name: power-meter-01
    product-key: power-meter

    polling-configs:
      # 电压（浮点数）
      - identifier: voltage
        function-code: READ_INPUT_REGISTERS
        start-address: 0
        quantity: 2
        data-type: FLOAT
        byte-order: BIG_ENDIAN

      # 电流（浮点数）
      - identifier: current
        function-code: READ_INPUT_REGISTERS
        start-address: 2
        quantity: 2
        data-type: FLOAT
        byte-order: BIG_ENDIAN

      # 功率（32位整数）
      - identifier: power
        function-code: READ_INPUT_REGISTERS
        start-address: 4
        quantity: 2
        data-type: INT32
        byte-order: BIG_ENDIAN

      # 累计电量（64位整数）
      - identifier: total-energy
        function-code: READ_INPUT_REGISTERS
        start-address: 6
        quantity: 4
        data-type: INT64
        byte-order: BIG_ENDIAN
```

### 示例 3：PLC 控制器（线圈和寄存器）

```yaml
slaves:
  - device-id: 1003
    slave-id: 3
    device-name: plc-controller-01
    product-key: plc-controller

    polling-configs:
      # 运行状态（线圈）
      - identifier: running
        function-code: READ_COILS
        start-address: 0
        quantity: 1
        data-type: BOOL
        byte-order: BIG_ENDIAN

      # 报警状态（离散输入）
      - identifier: alarm
        function-code: READ_DISCRETE_INPUTS
        start-address: 0
        quantity: 1
        data-type: BOOL
        byte-order: BIG_ENDIAN

      # 计数器（无符号32位整数）
      - identifier: counter
        function-code: READ_HOLDING_REGISTERS
        start-address: 0
        quantity: 2
        data-type: UINT32
        byte-order: BIG_ENDIAN
```

## 下行命令

### 1. 设置属性值

通过消息总线发送属性设置命令：

```java
IotDeviceMessage message = new IotDeviceMessage();
message.setDeviceId(1001L);
message.setMethod("thing.property.set");

Map<String, Object> params = new HashMap<>();
params.put("temperature", 25.5);
params.put("humidity", 60.0);
message.setParams(params);

// 发送到消息总线
messageProducer.sendMessage(message);
```

### 2. 写单个寄存器

```java
IotDeviceMessage message = new IotDeviceMessage();
message.setDeviceId(1001L);
message.setMethod("modbus.write.register");

Map<String, Object> params = new HashMap<>();
params.put("address", 0);           // 寄存器地址
params.put("value", 1234);          // 写入值
params.put("dataType", "INT16");    // 数据类型
params.put("byteOrder", "BIG_ENDIAN");
message.setParams(params);

messageProducer.sendMessage(message);
```

### 3. 写单个线圈

```java
IotDeviceMessage message = new IotDeviceMessage();
message.setDeviceId(1003L);
message.setMethod("modbus.write.coil");

Map<String, Object> params = new HashMap<>();
params.put("address", 0);    // 线圈地址
params.put("value", true);   // 线圈值
message.setParams(params);

messageProducer.sendMessage(message);
```

## 数据类型详解

### 寄存器占用说明

| 数据类型 | 寄存器数量 | 取值范围 | 说明 |
|---------|-----------|---------|------|
| INT16 | 1 | -32,768 ~ 32,767 | 有符号16位整数 |
| UINT16 | 1 | 0 ~ 65,535 | 无符号16位整数 |
| INT32 | 2 | -2,147,483,648 ~ 2,147,483,647 | 有符号32位整数 |
| UINT32 | 2 | 0 ~ 4,294,967,295 | 无符号32位整数 |
| INT64 | 4 | -9,223,372,036,854,775,808 ~ 9,223,372,036,854,775,807 | 有符号64位整数 |
| UINT64 | 4 | 0 ~ 18,446,744,073,709,551,615 | 无符号64位整数（返回字符串） |
| FLOAT | 2 | ±3.4E+38 (7位精度) | 32位浮点数 (IEEE 754) |
| DOUBLE | 4 | ±1.7E+308 (15位精度) | 64位浮点数 (IEEE 754) |
| BOOL | 1 | true/false | 布尔值 (0=false, 非0=true) |
| STRING | N | - | 字符串（每个寄存器2个字符） |

### 字节序说明

以32位浮点数 `123.45` 为例（IEEE 754 表示：0x42F6E666）

| 字节序 | 寄存器排列 | 适用场景 |
|-------|-----------|---------|
| BIG_ENDIAN | [0x42F6, 0xE666] | 大多数 PLC、仪表 |
| LITTLE_ENDIAN | [0x66E6, 0xF642] | 部分嵌入式设备 |
| BIG_ENDIAN_SWAP | [0xF642, 0x66E6] | 某些 Modicon PLC |
| LITTLE_ENDIAN_SWAP | [0xE666, 0x42F6] | 某些 ABB 设备 |

## 常见问题

### Q1: 如何确定设备的字节序？

A: 可以通过以下方法：
1. 查阅设备手册
2. 读取已知值进行测试（例如读取设备序列号）
3. 尝试不同的字节序配置，观察数据是否正常

### Q2: 读取数据不正确怎么办？

A: 检查以下配置：
1. 从站地址（slave-id）是否正确
2. 起始地址（start-address）是否正确（注意：Modbus 地址从 0 开始）
3. 数据类型（data-type）是否匹配
4. 字节序（byte-order）是否正确
5. 读取数量（quantity）是否足够

### Q3: 如何处理设备断线重连？

A: 系统已实现自动重连机制：
- 读取失败时会标记设备离线
- 下次轮询时自动重试
- 可通过 `reconnect-delay-ms` 配置重连延迟
- 可通过 `max-retries` 配置最大重试次数

### Q4: 如何优化轮询性能？

A: 建议：
1. 合理设置 `polling-interval-ms`（避免过于频繁）
2. 将相邻的寄存器合并为一次读取
3. 使用 `READ_INPUT_REGISTERS` 代替 `READ_HOLDING_REGISTERS`（如果设备支持）
4. 减少不必要的轮询配置项

### Q5: 支持 Modbus ASCII 吗？

A: 当前版本仅支持 Modbus TCP 和 Modbus RTU，不支持 Modbus ASCII。如需支持，可基于现有代码进行扩展。

## 扩展开发

### 添加自定义数据类型

1. 在 `ModbusDataTypeConverter.DataType` 枚举中添加新类型
2. 实现转换方法（`convertFromRegisters` 和 `convertToRegisters`）
3. 更新配置文档

### 支持更多功能码

1. 在 `IotModbusConnectionManager` 中添加新的读写方法
2. 在 `IotModbusMasterProtocol.readModbusData` 中添加处理逻辑
3. 更新配置和文档

## 技术支持

如有问题，请联系技术支持或查阅：
- 项目文档：[文档链接]
- 问题反馈：[GitHub Issues]
- 技术社区：[社区链接]
