# TDengine 设备属性表创建问题修复指南

## 问题描述

设备上报属性数据时，TDengine报错：
```
Table does not exist: product_property_1003
```

这是因为产品的TDengine超级表（Super Table）还没有被创建。

## 原因分析

系统在产品**发布**时会自动创建TDengine超级表（见 `IotProductServiceImpl:147`）。
如果产品还未发布，或发布时出现异常，超级表就不会被创建，导致设备上报数据时失败。

## 解决方案

### 方案1：通过后台发布产品（推荐）

1. 登录后台管理系统
2. 进入 **IoT管理** → **产品管理**
3. 找到产品ID为 `1003` 的产品
4. 点击**发布**按钮
5. 系统会自动创建 `product_property_1003` 超级表

### 方案2：使用API手动触发（临时方案）

如果产品已经是发布状态，可以通过以下方式重新触发表创建：

#### 方式A：调用内部服务方法

可以创建一个临时Controller端点来手动触发（需要重启服务）：

```java
@RestController
@RequestMapping("/iot/debug")
public class IotDebugController {

    @Resource
    private IotDevicePropertyService devicePropertyService;

    @GetMapping("/init-property-table")
    public CommonResult<Boolean> initPropertyTable(@RequestParam Long productId) {
        devicePropertyService.defineDevicePropertyData(productId);
        return success(true);
    }
}
```

然后调用：`GET /iot/debug/init-property-table?productId=1003`

#### 方式B：直接在TDengine中创建（需要知道物模型结构）

1. 连接到TDengine数据库
2. 执行以下SQL（需要根据实际物模型属性调整）：

```sql
-- 切换到IoT数据库
USE iot_db;

-- 创建超级表（示例，需要根据实际物模型属性调整）
CREATE STABLE product_property_1003 (
    ts TIMESTAMP,
    report_time TIMESTAMP,
    current FLOAT,
    power FLOAT,
    energy FLOAT,
    voltage FLOAT
)
TAGS (
    device_id BIGINT
);
```

**注意：** 方式B需要您知道产品1003的所有物模型属性定义。

### 方案3：修改产品状态触发表创建

使用SQL直接修改产品状态，然后再通过API更新状态来触发表创建：

```sql
-- 1. 先将产品改为草稿状态
UPDATE iot_product SET status = 0 WHERE id = 1003;

-- 2. 然后通过API接口将产品发布（调用 PUT /iot/product/update-status）
```

## 预防措施

1. **创建产品后务必发布**：产品创建后，在添加设备之前必须先发布产品
2. **检查TDengine连接**：确保TDengine服务正常运行，应用能够正常连接
3. **查看发布日志**：产品发布时查看后台日志，确认表创建成功

## 验证表是否存在

连接TDengine，执行以下命令验证：

```sql
USE iot_db;

-- 查看所有超级表
SHOW STABLES;

-- 查看特定产品的超级表结构
DESCRIBE product_property_1003;

-- 查看所有子表
SHOW TABLES;
```

## 相关代码位置

- 超级表创建逻辑：`IotDevicePropertyServiceImpl.java:82-109`
- 发布时触发创建：`IotProductServiceImpl.java:147`
- 插入数据SQL：`IotDevicePropertyMapper.xml:48-63`
- TDengine表字段映射：`IotDevicePropertyServiceImpl.java:51-61`

## 常见错误

### 错误1：Table does not exist
- **原因**：超级表未创建
- **解决**：按照上述方案1发布产品

### 错误2：invalid column name
- **原因**：设备上报的属性在物模型中不存在
- **解决**：检查物模型定义，确保所有上报的属性都已定义

### 错误3：Tag value too long
- **原因**：设备ID超过BIGINT范围
- **解决**：检查设备ID是否正常

