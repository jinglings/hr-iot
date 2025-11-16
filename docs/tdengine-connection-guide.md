# TDengine 数据库连接配置指南

## 问题说明

在运行 IoT 模块时，如果遇到以下错误：

```
org.springframework.jdbc.BadSqlGrammarException:
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'STABLES LIKE 'device_message'' at line 1
### SQL: SHOW STABLES LIKE 'device_message'
```

这是因为 **TDengine 数据源配置未启用**，系统默认使用了 MySQL 驱动连接数据库，导致 TDengine 特有的 SQL 语法（如 `SHOW STABLES`）无法执行。

## 解决方案

### 1. 安装 TDengine

首先确保已安装 TDengine 数据库。可以参考官方文档：
- 官方文档：https://docs.tdengine.com/
- 快速开始：https://docs.tdengine.com/get-started/

使用 Docker 快速启动 TDengine（推荐）：

```bash
docker run -d \
  --name tdengine \
  -p 6030:6030 \
  -p 6041:6041 \
  -p 6043-6049:6043-6049 \
  -p 6043-6049:6043-6049/udp \
  tdengine/tdengine:latest
```

### 2. 启用 TDengine 数据源配置

在配置文件中启用 TDengine 数据源配置（已在本次修复中完成）：

**文件位置：**
- `yudao-server/src/main/resources/application-local.yaml`
- `yudao-server/src/main/resources/application-dev.yaml`

**配置内容：**

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        # ... 其他数据源配置 ...
        tdengine: # IoT 数据库
          url: jdbc:TAOS-RS://127.0.0.1:6041/ruoyi_vue_pro
          driver-class-name: com.taosdata.jdbc.rs.RestfulDriver
          username: root
          password: taosdata
          druid:
            validation-query: SELECT SERVER_STATUS()
```

### 3. 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `url` | TDengine 连接地址，使用 RESTful 接口 | `jdbc:TAOS-RS://127.0.0.1:6041/数据库名` |
| `driver-class-name` | JDBC 驱动类名 | `com.taosdata.jdbc.rs.RestfulDriver` |
| `username` | 数据库用户名 | `root` |
| `password` | 数据库密码 | `taosdata` |
| `validation-query` | 连接有效性检查 SQL | `SELECT SERVER_STATUS()` |

**端口说明：**
- `6030`: TDengine 原生连接端口
- `6041`: TDengine RESTful 接口端口（本项目使用此端口）

### 4. 连接方式对比

TDengine JDBC 驱动提供两种连接方式：

#### 4.1 RESTful 连接（推荐，本项目使用）

**优点：**
- 跨平台，无需安装 TDengine 客户端
- 适合在容器化环境中使用
- 部署简单

**配置示例：**
```yaml
url: jdbc:TAOS-RS://127.0.0.1:6041/ruoyi_vue_pro
driver-class-name: com.taosdata.jdbc.rs.RestfulDriver
```

#### 4.2 原生连接

**优点：**
- 性能更高
- 支持更多高级特性

**缺点：**
- 需要安装 TDengine 客户端库
- 平台依赖性强

**配置示例：**
```yaml
url: jdbc:TAOS://127.0.0.1:6030/ruoyi_vue_pro
driver-class-name: com.taosdata.jdbc.TSDBDriver
```

### 5. 数据库初始化

启动应用时，系统会自动执行以下操作：

1. 检查超级表（STable）`device_message` 是否存在
2. 如果不存在，自动创建超级表及相关结构

相关代码：
- 初始化类：`cn.iocoder.yudao.module.iot.framework.tdengine.config.TDengineTableInitRunner`
- Mapper 接口：`cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper`

### 6. 验证连接

启动应用后，查看日志：

**成功情况：**
```
[defineDeviceMessageStable][设备消息超级表已存在，创建跳过]
```
或
```
[defineDeviceMessageStable][设备消息超级表不存在，创建开始...]
[defineDeviceMessageStable][设备消息超级表不存在，创建成功]
```

**失败情况：**
```
[run][TDengine初始化设备消息表结构失败，系统无法正常运行，即将退出]
```

如果失败，请检查：
1. TDengine 服务是否正常运行
2. 端口 6041 是否可访问
3. 数据库名称、用户名、密码是否正确

### 7. 常见问题

#### Q1: 连接超时
**原因：** TDengine 服务未启动或端口未开放
**解决：**
```bash
# 检查 TDengine 服务状态
systemctl status taosd  # Linux 系统服务
# 或检查 Docker 容器状态
docker ps | grep tdengine
```

#### Q2: 认证失败
**原因：** 用户名或密码错误
**解决：** 检查配置文件中的用户名和密码，TDengine 默认用户名为 `root`，默认密码为 `taosdata`

#### Q3: 数据库不存在
**原因：** 指定的数据库不存在
**解决：**
```sql
-- 使用 taos CLI 或 RESTful API 创建数据库
CREATE DATABASE IF NOT EXISTS ruoyi_vue_pro;
```

#### Q4: 驱动类找不到
**原因：** Maven 依赖未正确引入
**解决：** 确认 `pom.xml` 中包含以下依赖：
```xml
<dependency>
    <groupId>com.taosdata.jdbc</groupId>
    <artifactId>taos-jdbcdriver</artifactId>
</dependency>
```

### 8. 参考资料

- TDengine 官方文档：https://docs.tdengine.com/
- TDengine Java 连接器：https://docs.tdengine.com/developer-guide/connecting-to-tdengine/
- TDengine JDBC 驱动：https://github.com/taosdata/taos-connector-jdbc

## 技术架构说明

### 数据源切换机制

本项目使用 `dynamic-datasource` 实现多数据源动态切换：

- **MySQL**：存储业务数据（设备信息、产品信息等）
- **TDengine**：存储时序数据（设备消息、属性数据等）

通过 `@TDengineDS` 注解标记需要使用 TDengine 数据源的 Mapper：

```java
@Mapper
@TDengineDS  // 指定使用 tdengine 数据源
public interface IotDeviceMessageMapper {
    // ...
}
```

### 超级表（STable）设计

TDengine 使用超级表来管理时序数据：

- **超级表**：`device_message`，定义通用的列结构
- **子表**：`device_message_{deviceId}`，每个设备一个子表
- **标签**：`device_id`，用于区分不同设备的数据

这种设计可以高效地存储和查询海量设备消息数据。
