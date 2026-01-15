# TDengine 数据库初始化指南

本目录包含 HR-IoT 项目中 TDengine 时序数据库的初始化脚本。

## 目录结构

```
sql/tdengine/
├── README.md                    # 本文件
├── energy_realtime_data.sql     # 能源实时数据表创建脚本
└── init_all.sql                 # 一键初始化所有表(即将添加)
```

## 前置要求

1. **安装 TDengine**
   - 版本要求: TDengine 3.x+
   - 下载地址: https://www.taosdata.com/cn/getting-started/

2. **启动 TDengine 服务**
   ```bash
   # Linux/macOS
   sudo systemctl start taosd

   # 或直接启动
   taosd
   ```

3. **验证安装**
   ```bash
   taos
   # 能成功连接即表示安装正确
   ```

## 快速开始

### 方法 1: 使用 taos 命令行

```bash
# 1. 连接到 TDengine
taos

# 2. 创建数据库(根据实际情况修改数据库名称)
CREATE DATABASE IF NOT EXISTS iot_energy KEEP 3650 DURATION 10 BUFFER 16;

# 3. 切换到数据库
USE iot_energy;

# 4. 执行建表脚本
SOURCE /path/to/hr-iot/sql/tdengine/energy_realtime_data.sql;

# 5. 验证表是否创建成功
SHOW STABLES;
DESCRIBE energy_realtime_data;
```

### 方法 2: 命令行一键执行

```bash
# 假设数据库名为 iot_energy
taos -s "CREATE DATABASE IF NOT EXISTS iot_energy KEEP 3650 DURATION 10 BUFFER 16; USE iot_energy; SOURCE /path/to/hr-iot/sql/tdengine/energy_realtime_data.sql;"
```

### 方法 3: 使用 RESTful API

```bash
# 创建数据库
curl -u root:taosdata -d "CREATE DATABASE IF NOT EXISTS iot_energy" http://localhost:6041/rest/sql

# 执行 SQL 脚本(需要先读取文件内容)
curl -u root:taosdata -d "$(cat energy_realtime_data.sql)" http://localhost:6041/rest/sql/iot_energy
```

## 数据库配置说明

### 数据库参数

```sql
CREATE DATABASE IF NOT EXISTS iot_energy
    KEEP 3650          -- 数据保留天数(3650天 = 10年)
    DURATION 10        -- 数据文件存储时长(10天)
    BUFFER 16          -- 写入缓存大小(16MB)
    PAGES 256          -- VNODE 中元数据存储引擎的缓存页数
    REPLICA 1;         -- 副本数(单机为1，集群建议3)
```

**参数说明**:
- **KEEP**: 数据保留时长，根据业务需求调整
  - 短期监控: 30-90 天
  - 长期存储: 365-3650 天
- **DURATION**: 单个数据文件的时间跨度，影响查询性能
  - 数据量大: 建议 1-7 天
  - 数据量小: 建议 10-30 天
- **BUFFER**: 写入缓冲区大小，影响写入性能
  - 高频写入: 建议 16-64 MB
  - 低频写入: 建议 4-16 MB

### 应用配置

在 `application.yaml` 中配置 TDengine 连接:

```yaml
spring:
  datasource:
    # TDengine 配置
    tdengine:
      driver-class-name: com.taosdata.jdbc.rs.RestfulDriver
      url: jdbc:TAOS-RS://localhost:6041/iot_energy?timezone=UTC-8
      username: root
      password: taosdata
```

## 表结构说明

### 能源实时数据表 (energy_realtime_data)

这是一个 TDengine **超级表 (Super Table)**，用于存储所有能源计量设备的实时数据。

#### 数据字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| ts | TIMESTAMP | 时间戳(主键) |
| meter_id | BIGINT | 计量点ID |
| instant_power | DOUBLE | 瞬时功率/流量 |
| cumulative_value | DOUBLE | 累计值 |
| voltage | DOUBLE | 电压(电表专用) |
| current | DOUBLE | 电流(电表专用) |
| power_factor | DOUBLE | 功率因数(电表专用) |
| data_quality | TINYINT | 数据质量: 0=异常, 1=正常 |

#### 标签字段 (TAGS)

| 标签名 | 类型 | 说明 |
|--------|------|------|
| energy_type_id | BIGINT | 能源类型ID(电/水/气等) |
| building_id | BIGINT | 所属建筑ID |
| area_id | BIGINT | 所属区域ID |
| floor_id | BIGINT | 所属楼层ID |
| room_id | BIGINT | 所属房间ID |
| tenant_id | BIGINT | 租户ID(多租户隔离) |

#### 子表命名规则

子表会在插入数据时自动创建，命名规则: `energy_realtime_data_{meterId}`

例如:
- 计量点 ID 为 1: `energy_realtime_data_1`
- 计量点 ID 为 999: `energy_realtime_data_999`

## 常用操作

### 查看表信息

```sql
-- 查看所有超级表
SHOW STABLES;

-- 查看超级表结构
DESCRIBE energy_realtime_data;

-- 查看所有子表
SELECT TBNAME FROM energy_realtime_data;

-- 统计子表数量
SELECT COUNT(DISTINCT TBNAME) FROM energy_realtime_data;
```

### 插入数据示例

```sql
-- 插入单条数据(子表不存在时自动创建)
INSERT INTO energy_realtime_data_1
USING energy_realtime_data
TAGS (1, 1001, 1, 1, 101, 1)
VALUES (NOW, 1, 5.5, 100.5, 220.0, 25.0, 0.95, 1);

-- 批量插入
INSERT INTO energy_realtime_data_1 VALUES
    (NOW, 1, 5.5, 100.5, 220.0, 25.0, 0.95, 1)
    (NOW + 1s, 1, 5.6, 100.6, 220.0, 25.1, 0.95, 1)
    (NOW + 2s, 1, 5.7, 100.7, 220.0, 25.2, 0.95, 1);
```

### 查询数据示例

```sql
-- 查询某个计量点的最新数据
SELECT * FROM energy_realtime_data_1
ORDER BY ts DESC
LIMIT 1;

-- 查询某个建筑的所有数据(最近1小时)
SELECT * FROM energy_realtime_data
WHERE building_id = 1001
  AND ts >= NOW - 1h;

-- 查询某种能源类型的统计数据(按小时聚合)
SELECT
    TIMETRUNCATE(ts, 1h) AS hour,
    AVG(instant_power) AS avg_power,
    MAX(instant_power) AS max_power,
    MIN(instant_power) AS min_power
FROM energy_realtime_data
WHERE energy_type_id = 1
  AND ts >= NOW - 1d
GROUP BY TIMETRUNCATE(ts, 1h)
ORDER BY hour;

-- 查询租户的总能耗
SELECT
    SUM(cumulative_value) AS total_consumption,
    COUNT(*) AS data_points
FROM energy_realtime_data
WHERE tenant_id = 1
  AND ts >= NOW - 1d;
```

## 数据维护

### 数据保留策略

TDengine 会根据 `KEEP` 参数自动删除过期数据，无需手动维护。

```sql
-- 查看数据库配置
SHOW DATABASES;

-- 修改保留期限(例如改为90天)
ALTER DATABASE iot_energy KEEP 90;
```

### 清理数据

```sql
-- 删除某个子表
DROP TABLE IF EXISTS energy_realtime_data_1;

-- 删除超级表(会删除所有子表和数据,慎用!)
DROP STABLE IF EXISTS energy_realtime_data;
```

### 性能优化

1. **查询优化**:
   - 尽量使用标签字段进行过滤
   - 指定时间范围缩小查询数据量
   - 使用子表查询而非超级表查询(当知道具体 meter_id 时)

2. **写入优化**:
   - 批量写入数据
   - 使用预处理语句
   - 合理设置 BUFFER 大小

3. **存储优化**:
   - 定期检查磁盘空间
   - 根据数据量调整 DURATION 参数
   - 对历史数据进行降采样

## 故障排查

### 常见问题

**问题1: 连接失败**
```bash
# 检查 TDengine 服务状态
sudo systemctl status taosd

# 查看日志
tail -f /var/log/taos/taosd.log
```

**问题2: 表已存在且结构不同**
```sql
-- 方案1: 删除并重建(会丢失数据)
DROP STABLE IF EXISTS energy_realtime_data;
-- 然后重新执行建表脚本

-- 方案2: 修改表结构(TDengine 不支持直接修改列)
-- 需要创建新表，迁移数据，删除旧表
```

**问题3: 查询性能慢**
- 检查是否使用了标签字段过滤
- 检查时间范围是否过大
- 考虑使用连续查询(Continuous Query)预聚合数据

**问题4: 磁盘空间不足**
- 减小 KEEP 参数
- 删除不需要的子表
- 对历史数据进行归档

## 相关文档

- [TDengine 官方文档](https://docs.taosdata.com/zh/)
- [TDengine 最佳实践](https://docs.taosdata.com/zh/reference/best-practices/)
- [HR-IoT 项目文档](../../README.md)

## 注意事项

1. **备份**: 生产环境操作前请先备份数据
2. **权限**: 确保数据库用户有足够的权限
3. **时区**: 建议统一使用 UTC 或 UTC+8 时区
4. **监控**: 建议配置监控告警，及时发现问题

## 更新日志

- 2025-01-11: 初始版本，创建能源实时数据表
