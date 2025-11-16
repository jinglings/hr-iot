# BACnet 协议集成文档

## 概述

本模块基于 [BACnet4J](https://github.com/RadixIoT/BACnet4J) 库实现了 BACnet 协议的集成，为 IoT 平台提供 BACnet 设备的接入和管理能力。

## 功能特性

- ✅ BACnet 设备自动发现
- ✅ 设备信息读取
- ✅ 对象属性读取
- ✅ 设备状态管理
- ✅ 配置化管理
- ✅ Spring Boot 自动装配

## 技术栈

- BACnet4J 6.0.2
- Spring Boot 2.7.18
- Java 8+

## 快速开始

### 1. 配置启用

在 `application.yaml` 或 `application.properties` 中添加以下配置：

```yaml
yudao:
  iot:
    bacnet:
      enabled: true                           # 启用 BACnet 协议
      device-id: 1234                         # 本地设备 ID（必须唯一）
      local-device-name: YudaoIoT-BACnet      # 本地设备名称
      port: 47808                             # BACnet 端口
      local-bind-address: 0.0.0.0             # 本地绑定地址
      broadcast-address: 255.255.255.255      # 广播地址
      discovery-timeout: 5000                 # 设备发现超时（毫秒）
      communication-timeout: 3000             # 通信超时（毫秒）
```

### 2. 代码示例

#### 2.1 设备发现

```java
@Autowired
private BACnetDeviceManager deviceManager;

// 发现网络中的 BACnet 设备
List<BACnetDeviceInfo> devices = deviceManager.discoverAndCacheDevices();
for (BACnetDeviceInfo device : devices) {
    System.out.println("发现设备: " + device.getDeviceName() +
                       ", 实例号: " + device.getInstanceNumber());
}
```

#### 2.2 读取设备信息

```java
// 获取指定设备的详细信息
Integer instanceNumber = 1001;
BACnetDeviceInfo deviceInfo = deviceManager.getDeviceInfo(instanceNumber);
System.out.println("设备名称: " + deviceInfo.getDeviceName());
System.out.println("供应商: " + deviceInfo.getVendorName());
System.out.println("型号: " + deviceInfo.getModelName());
```

#### 2.3 读取对象属性

```java
// 读取模拟输入对象的当前值
Integer deviceInstance = 1001;
ObjectType objectType = ObjectType.analogInput;
Integer objectInstance = 0;
PropertyIdentifier property = PropertyIdentifier.presentValue;

Object value = deviceManager.readDeviceProperty(
    deviceInstance,
    objectType,
    objectInstance,
    property
);
System.out.println("属性值: " + value);
```

#### 2.4 获取设备对象列表

```java
// 获取设备的所有对象
List<BACnetObjectInfo> objects = deviceManager.getDeviceObjects(instanceNumber);
for (BACnetObjectInfo obj : objects) {
    System.out.println("对象: " + obj.getObjectName() +
                       ", 类型: " + obj.getObjectType() +
                       ", 当前值: " + obj.getPresentValue());
}
```

## 核心组件

### 1. BACnetProperties

BACnet 协议配置属性类，包含所有可配置参数。

### 2. BACnetClient

BACnet 客户端核心类，负责与 BACnet 设备的底层通信。

主要方法：
- `initialize()` - 初始化本地设备
- `discoverDevices()` - 发现网络设备
- `getDeviceInfo()` - 获取设备信息
- `readProperty()` - 读取对象属性
- `shutdown()` - 关闭客户端

### 3. BACnetDeviceManager

BACnet 设备管理器，提供高层次的设备管理 API。

特性：
- 设备缓存管理
- 自动初始化和销毁
- 统一异常处理
- Spring Bean 生命周期管理

### 4. DTO 类

- **BACnetDeviceInfo**: 设备信息 DTO
  - 设备实例号、名称、IP 地址
  - 供应商信息、型号、固件版本
  - 在线状态、最后通信时间

- **BACnetObjectInfo**: 对象信息 DTO
  - 对象类型、实例号、名称
  - 当前值、单位、状态标志
  - 是否可写

## 目录结构

```
protocol/bacnet/
├── config/
│   ├── BACnetAutoConfiguration.java      # Spring 自动配置
│   └── BACnetProperties.java             # 配置属性
├── core/
│   ├── BACnetClient.java                 # BACnet 客户端
│   └── BACnetDeviceManager.java          # 设备管理器
├── dto/
│   ├── BACnetDeviceInfo.java             # 设备信息 DTO
│   └── BACnetObjectInfo.java             # 对象信息 DTO
└── util/
    └── BACnetUtils.java                  # 工具类
```

## 常见 BACnet 对象类型

- `analogInput` - 模拟输入（如温度传感器）
- `analogOutput` - 模拟输出（如阀门控制）
- `analogValue` - 模拟值
- `binaryInput` - 二进制输入（如开关状态）
- `binaryOutput` - 二进制输出（如继电器）
- `binaryValue` - 二进制值
- `multiStateInput` - 多状态输入
- `multiStateOutput` - 多状态输出
- `device` - 设备对象

## 常见 BACnet 属性

- `presentValue` - 当前值
- `objectName` - 对象名称
- `description` - 描述
- `units` - 单位
- `statusFlags` - 状态标志
- `outOfService` - 是否停用
- `deviceType` - 设备类型
- `modelName` - 型号名称
- `vendorName` - 供应商名称

## 注意事项

1. **端口配置**: BACnet 标准端口为 47808，确保端口未被占用
2. **设备 ID**: 每个 BACnet 网络中的设备 ID 必须唯一
3. **网络权限**: 需要网络广播权限，确保防火墙配置正确
4. **线程安全**: BACnetDeviceManager 是线程安全的，可在多线程环境使用
5. **资源释放**: 应用关闭时会自动释放资源，无需手动调用

## 故障排查

### 1. 无法发现设备

- 检查网络连接和防火墙设置
- 确认 BACnet 设备在同一网络段
- 验证广播地址配置是否正确
- 增加 discovery-timeout 时间

### 2. 读取属性失败

- 确认设备实例号正确
- 检查对象类型和实例号是否存在
- 验证属性是否支持读取
- 查看日志获取详细错误信息

### 3. 启动失败

- 检查端口是否被占用
- 验证设备 ID 是否唯一
- 确认 BACnet4J 依赖已正确引入

## 参考资料

- [BACnet4J GitHub](https://github.com/RadixIoT/BACnet4J)
- [BACnet 协议标准](http://www.bacnet.org/)
- [BACnet4J 官方文档](https://infiniteautomation.com/bacnet4j/)

## 未来扩展

- [ ] 支持写入对象属性
- [ ] 支持 COV (Change of Value) 订阅
- [ ] 支持告警和事件处理
- [ ] 支持时间同步
- [ ] 支持设备备份和恢复
- [ ] 提供 REST API 接口
- [ ] 增加 Web 管理界面

## 更新日志

### v1.0.0 (2025-11-16)
- ✨ 初始版本发布
- ✨ 支持设备发现
- ✨ 支持设备信息读取
- ✨ 支持对象属性读取
- ✨ Spring Boot 自动配置

---

如有问题或建议，请联系开发团队。
