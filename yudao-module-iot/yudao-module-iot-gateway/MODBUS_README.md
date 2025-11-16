# Modbus 协议支持文档

## 概述

本 IoT 网关现已支持 Modbus 协议，包括 Modbus TCP 和 Modbus RTU 两种通信模式。Modbus 是工业自动化领域广泛使用的通信协议，用于与 PLC、传感器、执行器等设备进行数据交换。

## 支持的协议

### 1. Modbus TCP

Modbus TCP 是基于 TCP/IP 网络的 Modbus 协议变体，使用标准的 502 端口。

**特点：**
- 基于 TCP/IP 网络通信
- 支持多个客户端并发连接
- 默认端口：502
- 无需校验和（TCP 层已提供）

### 2. Modbus RTU

Modbus RTU 是基于串口通信的 Modbus 协议变体，使用二进制格式传输数据。

**特点：**
- 基于串口（RS-232/RS-485）通信
- 使用 CRC-16 校验
- 紧凑的二进制格式
- 适合现场总线应用

## 配置说明

### Modbus TCP 配置

在 `application.yml` 中添加以下配置：

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus-tcp:
          enabled: true              # 是否启用 Modbus TCP
          port: 502                  # 监听端口，默认 502
          max-connections: 100       # 最大并发连接数
          connect-timeout-seconds: 30 # 连接超时时间（秒）
```

### Modbus RTU 配置

在 `application.yml` 中添加以下配置：

```yaml
yudao:
  iot:
    gateway:
      protocol:
        modbus-rtu:
          enabled: true                  # 是否启用 Modbus RTU
          port-name: /dev/ttyUSB0       # 串口名称（Linux: /dev/ttyUSB0, Windows: COM1）
          baud-rate: 9600               # 波特率
          data-bits: 8                  # 数据位
          stop-bits: 1                  # 停止位（1 或 2）
          parity: 0                     # 校验位（0=无, 1=奇校验, 2=偶校验）
          read-timeout-ms: 1000         # 读取超时（毫秒）
          write-timeout-ms: 1000        # 写入超时（毫秒）
```

## 支持的功能码

### 读取功能

| 功能码 | 名称 | 说明 |
|-------|------|------|
| 0x01 | Read Coils | 读取线圈状态 |
| 0x02 | Read Discrete Inputs | 读取离散输入状态 |
| 0x03 | Read Holding Registers | 读取保持寄存器 |
| 0x04 | Read Input Registers | 读取输入寄存器 |

### 写入功能

| 功能码 | 名称 | 说明 |
|-------|------|------|
| 0x05 | Write Single Coil | 写单个线圈 |
| 0x06 | Write Single Register | 写单个寄存器 |
| 0x0F | Write Multiple Coils | 写多个线圈 |
| 0x10 | Write Multiple Registers | 写多个寄存器 |

## 消息格式

### 请求消息示例

```json
{
  "method": "modbus.read",
  "params": {
    "unitId": 1,              // 从站地址
    "functionCode": 3,        // 功能码（0x03 = 读取保持寄存器）
    "startAddress": 0,        // 起始地址
    "quantity": 10            // 读取数量
  }
}
```

### 响应消息示例

```json
{
  "method": "modbus.read",
  "code": 0,                  // 0 = 成功
  "msg": "成功",
  "params": {
    "unitId": 1,
    "functionCode": 3
  },
  "data": {
    "registers": [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000]
  }
}
```

### 写入请求示例

```json
{
  "method": "modbus.write",
  "params": {
    "unitId": 1,              // 从站地址
    "functionCode": 16,       // 功能码（0x10 = 写多个寄存器）
    "startAddress": 0,        // 起始地址
    "values": [100, 200, 300] // 要写入的值
  }
}
```

## 异常处理

当 Modbus 设备返回异常时，消息格式如下：

```json
{
  "method": "modbus.read",
  "code": 2,                  // 异常码
  "msg": "Modbus 异常: 非法数据地址",
  "data": {
    "exceptionCode": 2
  }
}
```

### 常见异常码

| 异常码 | 说明 |
|-------|------|
| 0x01 | 非法功能 |
| 0x02 | 非法数据地址 |
| 0x03 | 非法数据值 |
| 0x04 | 从站设备故障 |
| 0x05 | 确认 |
| 0x06 | 从站设备忙 |
| 0x08 | 存储奇偶性差错 |

## 使用示例

### 1. 读取保持寄存器

读取从站地址为 1 的设备的 40001-40010 寄存器（地址 0-9）：

**Modbus TCP 请求：**
```
Transaction ID: 0x0001
Protocol ID: 0x0000
Length: 0x0006
Unit ID: 0x01
Function Code: 0x03
Start Address: 0x0000 (高字节在前)
Quantity: 0x000A (读取 10 个寄存器)
```

**对应的 IotDeviceMessage：**
```json
{
  "requestId": "modbus-0001",
  "method": "modbus.read",
  "params": {
    "unitId": 1,
    "functionCode": 3,
    "startAddress": 0,
    "quantity": 10
  }
}
```

### 2. 写入多个寄存器

写入从站地址为 1 的设备的 40001-40003 寄存器（地址 0-2）：

**对应的 IotDeviceMessage：**
```json
{
  "requestId": "modbus-0002",
  "method": "modbus.write",
  "params": {
    "unitId": 1,
    "functionCode": 16,
    "startAddress": 0,
    "values": [1000, 2000, 3000]
  }
}
```

## 测试工具

推荐使用以下工具进行 Modbus 协议测试：

1. **Modbus Poll** - Modbus 主站模拟器（Windows）
2. **Modbus Slave** - Modbus 从站模拟器（Windows）
3. **QModMaster** - 开源 Modbus 测试工具（跨平台）
4. **pymodbus** - Python Modbus 库（适合脚本测试）

## 注意事项

1. **端口权限**：Modbus TCP 默认使用 502 端口，在 Linux 系统上需要 root 权限或配置端口转发
2. **串口权限**：Modbus RTU 需要有串口访问权限，Linux 下通常需要将用户添加到 `dialout` 组
3. **从站地址**：Modbus 从站地址范围为 1-247，0 为广播地址，248-255 为保留地址
4. **寄存器地址**：注意区分 Modbus 协议地址（从 0 开始）和 PLC 地址（通常从 1 或 40001 开始）
5. **字节序**：Modbus 使用大端序（Big Endian），高字节在前

## 故障排查

### Modbus TCP 连接失败

1. 检查防火墙是否开放 502 端口
2. 确认配置中的 `enabled: true`
3. 查看日志中的启动信息
4. 使用 `telnet <ip> 502` 测试端口连通性

### Modbus RTU 通信失败

1. 检查串口名称是否正确
2. 确认波特率、数据位、停止位、校验位配置与设备一致
3. 检查串口权限（Linux: `ls -l /dev/ttyUSB*`）
4. 确认 RS-485 转换器工作正常
5. 检查 CRC 校验是否正确

## 扩展开发

如需扩展 Modbus 功能，可以：

1. **自定义功能码处理**：在 `IotModbusTcpDeviceMessageCodec` 或 `IotModbusRtuDeviceMessageCodec` 中添加
2. **添加数据转换**：实现自定义的数据类型转换（如浮点数、长整数等）
3. **实现 Modbus 主站功能**：在业务层实现主动读写设备的功能

## 相关文件

- 编解码器：
  - `IotModbusTcpDeviceMessageCodec.java` - Modbus TCP 编解码器
  - `IotModbusRtuDeviceMessageCodec.java` - Modbus RTU 编解码器
  - `ModbusConstants.java` - Modbus 常量定义

- 协议处理器：
  - `IotModbusTcpUpstreamProtocol.java` - Modbus TCP 协议处理器
  - `IotModbusUpstreamHandler.java` - Modbus 消息处理器

- 配置：
  - `IotGatewayProperties.java` - 网关配置属性
  - `IotGatewayConfiguration.java` - 网关自动配置

## 参考资料

- [Modbus 协议规范](https://modbus.org/docs/Modbus_Application_Protocol_V1_1b3.pdf)
- [Modbus TCP 规范](https://modbus.org/docs/Modbus_Messaging_Implementation_Guide_V1_0b.pdf)
