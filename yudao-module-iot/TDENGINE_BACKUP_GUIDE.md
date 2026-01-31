# TDengine 备份恢复功能设计文档

> **文档版本**: 1.0
> **创建日期**: 2025-01-21
> **最后更新**: 2025-01-21
> **状态**: 设计阶段

---

## 目录

- [一、背景与目标](#一背景与目标)
- [二、技术调研](#二技术调研)
- [三、功能架构设计](#三功能架构设计)
- [四、数据库表结构设计](#四数据库表结构设计)
- [五、API接口设计](#五api接口设计)
- [六、核心实现要点](#六核心实现要点)
- [七、文件存储方案](#七文件存储方案)
- [八、配置和权限](#八配置和权限)
- [九、实施步骤](#九实施步骤)
- [十、注意事项和最佳实践](#十注意事项和最佳实践)
- [十一、前端页面设计](#十一前端页面设计)
- [十二、扩展功能](#十二扩展功能)

---

## 一、背景与目标

### 1.1 背景

HR-IoT 项目使用 TDengine 作为时序数据库存储海量 IoT 设备消息、属性数据和能源数据。由于使用的是开源版本的 TDengine，**不提供自动备份功能**，因此需要自行开发数据备份和恢复功能，以保障数据安全。

### 1.2 目标

开发一套完整的 TDengine 数据备份和恢复解决方案，实现以下功能：

- ✅ **手动备份**：支持随时手动触发备份
- ✅ **定时备份**：支持定时自动备份（Cron 表达式配置）
- ✅ **多种备份类型**：全量备份、数据库备份、表备份
- ✅ **数据恢复**：支持从备份文件恢复数据
- ✅ **备份管理**：备份列表、下载、删除、清理过期备份
- ✅ **友好界面**：提供 Web 管理界面
- ✅ **安全可靠**：备份验证、错误恢复、审计日志

---

## 二、技术调研

### 2.1 TDengine 备份恢复机制

TDengine 提供以下几种备份方式：

#### 2.1.1 命令行工具方式（不采用）

**taosdump** - TDengine 官方备份工具

```bash
# 导出数据
taosdump -o /backup/path -D database_name

# 导入数据
taosdump -i /backup/path
```

**优点**：
- 官方工具，功能完善
- 支持全量备份、增量备份
- 性能较好

**缺点**：
- 需要单独安装
- 需要调用系统命令，依赖服务器环境
- 不便于集成到 Web 应用中

#### 2.1.2 SQL 导出方式（采用）

**方式一：SELECT INTO OUTFILE**

```sql
SELECT * FROM database.table INTO OUTFILE '/path/to/file.csv';
```

**方式二：应用层查询导出**

```java
// 通过 JDBC 查询数据，写入 CSV 文件
try (ResultSet rs = stmt.executeQuery("SELECT * FROM database.table")) {
    while (rs.next()) {
        // 写入 CSV
    }
}
```

**优点**：
- 纯 SQL 操作，无需额外工具
- 便于集成到应用中
- 灵活可控

**缺点**：
- 需要自行处理数据格式
- 大表导出需要分批处理

#### 2.1.3 RESTful API 方式（采用）

项目已使用 RESTful 连接方式（`jdbc:TAOS-RS://127.0.0.1:6041`），可以直接通过 JDBC 执行 SQL 进行备份和恢复。

**结论**：采用 **SQL 导出 + RESTful API** 方式，符合项目现有架构。

---

## 三、功能架构设计

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                    前端管理界面                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │备份管理  │  │恢复管理  │  │定时配置  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└───────────────────┬─────────────────────────────────────┘
                    │ HTTP API
┌───────────────────▼─────────────────────────────────────┐
│                Controller Layer                          │
│  IotBackupController - 备份恢复API端点                  │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                Service Layer                             │
│  ┌───────────────────────────────────────────────┐     │
│  │ IotBackupService - 备份业务逻辑               │     │
│  │  - createBackup()    手动备份                 │     │
│  │  - restoreBackup()   数据恢复                 │     │
│  │  - listBackups()     备份列表                 │     │
│  │  - deleteBackup()    删除备份                 │     │
│  │  - downloadBackup()  下载备份                 │     │
│  └───────────────────────────────────────────────┘     │
│                                                          │
│  ┌───────────────────────────────────────────────┐     │
│  │ IotBackupExecutor - 备份执行引擎              │     │
│  │  - exportDatabase()   导出数据库               │     │
│  │  - exportTable()      导出表                  │     │
│  │  - importData()       导入数据                │     │
│  │  - compress()         压缩备份文件            │     │
│  │  - decompress()       解压备份文件            │     │
│  └───────────────────────────────────────────────┘     │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                Data Access Layer                         │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │IotBackupMapper   │  │TDengine Mapper   │            │
│  │(MySQL - 元数据)  │  │(备份数据操作)     │            │
│  └──────────────────┘  └──────────────────┘            │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│              Storage & Database                          │
│  ┌────────┐  ┌────────┐  ┌────────┐                    │
│  │ MySQL  │  │TDengine│  │文件存储│                    │
│  │(元数据)│  │(时序数据)│ │(备份文件)│                   │
│  └────────┘  └────────┘  └────────┘                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              定时任务调度 (Quartz)                       │
│  BackupScheduleJob - 自动执行定时备份                   │
└─────────────────────────────────────────────────────────┘
```

### 3.2 模块职责

| 模块 | 职责 | 技术实现 |
|------|------|----------|
| **前端界面** | 提供用户操作界面 | Vue 3 + Element Plus |
| **Controller** | 接收 HTTP 请求，参数验证 | Spring MVC |
| **BackupService** | 备份恢复业务逻辑 | Spring Service |
| **BackupExecutor** | 备份执行引擎，数据导出导入 | JDBC + CSV 处理 |
| **Mapper** | 数据库操作 | MyBatis Plus |
| **定时任务** | 定时触发备份 | Quartz Scheduler |

---

## 四、数据库表结构设计

### 4.1 备份记录表（iot_backup_record）

存储每次备份的详细信息。

```sql
CREATE TABLE `iot_backup_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `backup_name` VARCHAR(255) NOT NULL COMMENT '备份名称',
  `backup_type` TINYINT NOT NULL COMMENT '备份类型：1-全量备份，2-数据库备份，3-表备份',
  `backup_scope` VARCHAR(500) NULL COMMENT '备份范围（JSON格式）：数据库列表或表列表',
  `backup_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '备份模式：1-手动，2-自动定时',
  `backup_status` TINYINT NOT NULL DEFAULT 0 COMMENT '备份状态：0-备份中，1-成功，2-失败',
  `file_path` VARCHAR(500) NULL COMMENT '备份文件路径',
  `file_size` BIGINT NULL COMMENT '备份文件大小（字节）',
  `file_format` VARCHAR(20) DEFAULT 'zip' COMMENT '文件格式：zip, tar.gz',
  `start_time` DATETIME NOT NULL COMMENT '备份开始时间',
  `end_time` DATETIME NULL COMMENT '备份结束时间',
  `duration` INT NULL COMMENT '备份耗时（秒）',
  `record_count` BIGINT NULL COMMENT '备份数据条数',
  `error_message` TEXT NULL COMMENT '错误信息',
  `remark` VARCHAR(500) NULL COMMENT '备份备注',

  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_backup_status` (`backup_status`),
  INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine备份记录表';
```

**字段说明**：
- `backup_type`: 1-全量（所有数据库）, 2-指定数据库, 3-指定表
- `backup_scope`: JSON 格式，例如 `["ruoyi_vue_pro"]` 或 `["device_message", "device_property"]`
- `backup_mode`: 1-手动触发, 2-定时任务
- `backup_status`: 0-进行中, 1-成功, 2-失败

### 4.2 备份配置表（iot_backup_config）

存储定时备份的配置。

```sql
CREATE TABLE `iot_backup_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `enabled` BIT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `backup_type` TINYINT NOT NULL COMMENT '备份类型：1-全量备份，2-数据库备份，3-表备份',
  `backup_scope` VARCHAR(500) NULL COMMENT '备份范围（JSON格式）',
  `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
  `retention_days` INT NOT NULL DEFAULT 7 COMMENT '保留天数',
  `max_backup_count` INT NOT NULL DEFAULT 10 COMMENT '最大备份数量',
  `compression_enabled` BIT NOT NULL DEFAULT 1 COMMENT '是否启用压缩',
  `notify_on_success` BIT NOT NULL DEFAULT 0 COMMENT '成功时是否通知',
  `notify_on_failure` BIT NOT NULL DEFAULT 1 COMMENT '失败时是否通知',
  `notify_emails` VARCHAR(500) NULL COMMENT '通知邮箱列表（逗号分隔）',

  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_name` (`config_name`, `tenant_id`, `deleted`),
  INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine备份配置表';
```

**字段说明**：
- `cron_expression`: Quartz Cron 表达式，例如 `0 0 2 * * ?` 表示每天凌晨 2 点
- `retention_days`: 备份保留天数，超过自动删除
- `max_backup_count`: 最大保留备份数量，超过删除最旧的

### 4.3 恢复记录表（iot_restore_record）

存储数据恢复的历史记录。

```sql
CREATE TABLE `iot_restore_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '恢复ID',
  `backup_id` BIGINT NOT NULL COMMENT '备份记录ID',
  `restore_type` TINYINT NOT NULL COMMENT '恢复类型：1-完整恢复，2-选择性恢复',
  `restore_target` VARCHAR(500) NULL COMMENT '恢复目标（JSON格式）',
  `restore_status` TINYINT NOT NULL DEFAULT 0 COMMENT '恢复状态：0-恢复中，1-成功，2-失败',
  `start_time` DATETIME NOT NULL COMMENT '恢复开始时间',
  `end_time` DATETIME NULL COMMENT '恢复结束时间',
  `duration` INT NULL COMMENT '恢复耗时（秒）',
  `record_count` BIGINT NULL COMMENT '恢复数据条数',
  `error_message` TEXT NULL COMMENT '错误信息',
  `remark` VARCHAR(500) NULL COMMENT '恢复备注',

  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`),
  INDEX `idx_backup_id` (`backup_id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine恢复记录表';
```

---

## 五、API接口设计

### 5.1 备份管理接口

#### 5.1.1 创建备份（手动）

```http
POST /admin-api/iot/backup/create
Content-Type: application/json

Request Body:
{
  "backupName": "backup_20250121_001",
  "backupType": 1,
  "backupScope": ["ruoyi_vue_pro"],
  "remark": "手动全量备份"
}

Response:
{
  "code": 0,
  "data": 123,  // 备份记录ID
  "msg": "success"
}
```

**参数说明**：
- `backupType`: 1-全量, 2-数据库, 3-表
- `backupScope`: 备份范围，全量备份时可为空

#### 5.1.2 备份列表（分页）

```http
GET /admin-api/iot/backup/page?pageNo=1&pageSize=10&backupType=1&backupStatus=1

Response:
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 123,
        "backupName": "backup_20250121_001",
        "backupType": 1,
        "backupStatus": 1,
        "fileSize": 2684354560,
        "startTime": "2025-01-21 02:00:00",
        "endTime": "2025-01-21 02:15:30",
        "duration": 930,
        "recordCount": 1000000
      }
    ],
    "total": 50
  }
}
```

#### 5.1.3 删除备份

```http
DELETE /admin-api/iot/backup/delete?id=123

Response:
{
  "code": 0,
  "data": true,
  "msg": "success"
}
```

#### 5.1.4 下载备份

```http
GET /admin-api/iot/backup/download?id=123

Response: File Stream (application/zip)
Content-Disposition: attachment; filename="backup_20250121_001.zip"
```

#### 5.1.5 获取备份详情

```http
GET /admin-api/iot/backup/get?id=123

Response:
{
  "code": 0,
  "data": {
    "id": 123,
    "backupName": "backup_20250121_001",
    "backupType": 1,
    "backupScope": ["ruoyi_vue_pro"],
    "backupStatus": 1,
    "filePath": "/data/iot-backup/backups/20250121/backup_001.zip",
    "fileSize": 2684354560,
    "startTime": "2025-01-21 02:00:00",
    "endTime": "2025-01-21 02:15:30",
    "duration": 930,
    "recordCount": 1000000,
    "remark": "手动全量备份"
  }
}
```

### 5.2 恢复管理接口

#### 5.2.1 执行恢复

```http
POST /admin-api/iot/restore/execute
Content-Type: application/json

Request Body:
{
  "backupId": 123,
  "restoreType": 1,
  "restoreTarget": null,
  "remark": "恢复到备份点"
}

Response:
{
  "code": 0,
  "data": 456,  // 恢复记录ID
  "msg": "success"
}
```

#### 5.2.2 恢复记录列表

```http
GET /admin-api/iot/restore/page?pageNo=1&pageSize=10

Response:
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 456,
        "backupId": 123,
        "restoreType": 1,
        "restoreStatus": 1,
        "startTime": "2025-01-21 10:00:00",
        "endTime": "2025-01-21 10:20:00",
        "duration": 1200,
        "recordCount": 1000000
      }
    ],
    "total": 10
  }
}
```

#### 5.2.3 验证备份文件

```http
POST /admin-api/iot/backup/validate?id=123

Response:
{
  "code": 0,
  "data": {
    "valid": true,
    "backupTime": "2025-01-21 02:00:00",
    "tdengineVersion": "3.0.0",
    "databases": ["ruoyi_vue_pro"],
    "tables": ["device_message", "device_property"],
    "recordCount": 1000000,
    "fileSize": 2684354560
  }
}
```

### 5.3 备份配置接口

#### 5.3.1 创建配置

```http
POST /admin-api/iot/backup/config/create
Content-Type: application/json

Request Body:
{
  "configName": "每日全量备份",
  "enabled": true,
  "backupType": 1,
  "cronExpression": "0 0 2 * * ?",
  "retentionDays": 7,
  "maxBackupCount": 10,
  "compressionEnabled": true,
  "notifyOnFailure": true,
  "notifyEmails": "admin@example.com"
}

Response:
{
  "code": 0,
  "data": 1,
  "msg": "success"
}
```

#### 5.3.2 更新配置

```http
PUT /admin-api/iot/backup/config/update
```

#### 5.3.3 配置列表

```http
GET /admin-api/iot/backup/config/list

Response:
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "configName": "每日全量备份",
      "enabled": true,
      "backupType": 1,
      "cronExpression": "0 0 2 * * ?",
      "retentionDays": 7,
      "maxBackupCount": 10
    }
  ]
}
```

#### 5.3.4 启用/禁用配置

```http
PUT /admin-api/iot/backup/config/enable?id=1&enabled=true

Response:
{
  "code": 0,
  "data": true,
  "msg": "success"
}
```

---

## 六、核心实现要点

### 6.1 备份执行引擎

```java
/**
 * 备份执行引擎 - 核心逻辑
 */
@Service
@Slf4j
public class IotBackupExecutor {

    @Autowired
    @Qualifier("tdengine")
    private DataSource tdengineDataSource;

    /**
     * 执行全量备份
     */
    public BackupResult executeFullBackup(String backupPath) {
        log.info("[executeFullBackup][开始全量备份] path={}", backupPath);

        try {
            // 1. 创建临时目录
            File tempDir = new File(backupPath);
            FileUtil.mkdir(tempDir);

            // 2. 获取所有数据库
            List<String> databases = queryDatabases();
            log.info("[executeFullBackup][查询到数据库] count={}", databases.size());

            long totalRecordCount = 0;

            // 3. 遍历每个数据库
            for (String db : databases) {
                // 获取数据库中的所有超级表
                List<String> stables = queryStables(db);
                log.info("[executeFullBackup][数据库 {} 的超级表] count={}", db, stables.size());

                // 导出每个超级表的数据
                for (String stable : stables) {
                    long count = exportStableData(db, stable, backupPath);
                    totalRecordCount += count;
                }
            }

            // 4. 创建元数据文件
            BackupMetadata metadata = buildMetadata(databases, totalRecordCount);
            saveMetadata(backupPath, metadata);

            // 5. 压缩备份文件
            String zipPath = compressBackup(backupPath);
            long fileSize = FileUtil.size(new File(zipPath));

            // 6. 清理临时目录
            FileUtil.del(tempDir);

            log.info("[executeFullBackup][全量备份完成] zipPath={}, size={}, records={}",
                    zipPath, fileSize, totalRecordCount);

            return BackupResult.builder()
                    .filePath(zipPath)
                    .fileSize(fileSize)
                    .recordCount(totalRecordCount)
                    .build();

        } catch (Exception e) {
            log.error("[executeFullBackup][全量备份失败]", e);
            throw new ServiceException("备份失败：" + e.getMessage());
        }
    }

    /**
     * 导出超级表数据
     */
    private long exportStableData(String db, String stable, String path) {
        String sql = String.format("SELECT * FROM %s.%s", db, stable);
        String csvFile = String.format("%s/%s_%s.csv", path, db, stable);

        log.info("[exportStableData][开始导出] db={}, stable={}", db, stable);

        long recordCount = 0;

        try (Connection conn = tdengineDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {

            // 写入表头
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] headers = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                headers[i] = metaData.getColumnName(i + 1);
            }
            writer.writeNext(headers);

            // 写入数据（分批处理，避免内存溢出）
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                writer.writeNext(row);
                recordCount++;

                // 每10000条记录刷新一次
                if (recordCount % 10000 == 0) {
                    writer.flush();
                }
            }

            log.info("[exportStableData][导出完成] db={}, stable={}, records={}",
                    db, stable, recordCount);

        } catch (Exception e) {
            log.error("[exportStableData][导出失败] db={}, stable={}", db, stable, e);
            throw new ServiceException("导出数据失败：" + e.getMessage());
        }

        return recordCount;
    }

    /**
     * 查询所有数据库
     */
    private List<String> queryDatabases() {
        List<String> databases = new ArrayList<>();
        String sql = "SHOW DATABASES";

        try (Connection conn = tdengineDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dbName = rs.getString(1);
                // 过滤系统数据库
                if (!"information_schema".equals(dbName) && !"performance_schema".equals(dbName)) {
                    databases.add(dbName);
                }
            }

        } catch (Exception e) {
            log.error("[queryDatabases][查询数据库失败]", e);
            throw new ServiceException("查询数据库失败：" + e.getMessage());
        }

        return databases;
    }

    /**
     * 查询指定数据库的所有超级表
     */
    private List<String> queryStables(String database) {
        List<String> stables = new ArrayList<>();
        String sql = String.format("SHOW %s.STABLES", database);

        try (Connection conn = tdengineDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stables.add(rs.getString(1));
            }

        } catch (Exception e) {
            log.error("[queryStables][查询超级表失败] db={}", database, e);
            throw new ServiceException("查询超级表失败：" + e.getMessage());
        }

        return stables;
    }

    /**
     * 压缩备份文件
     */
    private String compressBackup(String sourcePath) {
        String zipPath = sourcePath + ".zip";

        try {
            ZipUtil.zip(sourcePath, zipPath);
            log.info("[compressBackup][压缩完成] zipPath={}", zipPath);
        } catch (Exception e) {
            log.error("[compressBackup][压缩失败]", e);
            throw new ServiceException("压缩备份文件失败：" + e.getMessage());
        }

        return zipPath;
    }
}
```

### 6.2 定时备份任务

```java
/**
 * 定时备份任务
 */
@Component
@Slf4j
public class BackupScheduleJob implements Job {

    @Autowired
    private IotBackupService backupService;

    @Autowired
    private IotBackupConfigMapper configMapper;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long configId = context.getMergedJobDataMap().getLong("configId");

        log.info("[定时备份任务][开始执行] configId={}", configId);

        // 获取配置
        IotBackupConfigDO config = configMapper.selectById(configId);
        if (config == null || !config.getEnabled()) {
            log.warn("[定时备份任务][配置不存在或已禁用] configId={}", configId);
            return;
        }

        try {
            // 执行备份
            IotBackupCreateReqVO reqVO = buildBackupRequest(config);
            Long backupId = backupService.createBackup(reqVO);

            log.info("[定时备份任务][备份成功] configId={}, backupId={}", configId, backupId);

            // 清理过期备份
            backupService.cleanExpiredBackups(config);

            // 发送成功通知
            if (config.getNotifyOnSuccess()) {
                sendNotification(config, backupId, true, null);
            }

        } catch (Exception e) {
            log.error("[定时备份任务][备份失败] configId={}", configId, e);

            // 发送失败通知
            if (config.getNotifyOnFailure()) {
                sendNotification(config, null, false, e.getMessage());
            }
        }
    }

    private IotBackupCreateReqVO buildBackupRequest(IotBackupConfigDO config) {
        IotBackupCreateReqVO reqVO = new IotBackupCreateReqVO();
        reqVO.setBackupName(generateBackupName(config));
        reqVO.setBackupType(config.getBackupType());
        reqVO.setBackupScope(config.getBackupScope());
        reqVO.setBackupMode(BackupModeEnum.AUTO.getMode());
        reqVO.setRemark("定时自动备份 - " + config.getConfigName());
        return reqVO;
    }

    private String generateBackupName(IotBackupConfigDO config) {
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMdd_HHmmss");
        return String.format("auto_backup_%s_%s", config.getId(), timestamp);
    }
}
```

### 6.3 数据恢复逻辑

```java
/**
 * 数据恢复服务实现
 */
@Service
@Slf4j
public class IotRestoreServiceImpl implements IotRestoreService {

    @Autowired
    private IotBackupRecordMapper backupMapper;

    @Autowired
    private IotRestoreRecordMapper restoreMapper;

    @Autowired
    @Qualifier("tdengine")
    private DataSource tdengineDataSource;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeRestore(IotRestoreCreateReqVO reqVO) {
        log.info("[executeRestore][开始恢复数据] reqVO={}", reqVO);

        // 1. 获取备份记录
        IotBackupRecordDO backup = backupMapper.selectById(reqVO.getBackupId());
        validateBackupFile(backup);

        // 2. 解压备份文件
        String extractPath = decompressBackup(backup.getFilePath());

        // 3. 读取元数据
        BackupMetadata metadata = readMetadata(extractPath);

        // 4. 创建恢复记录
        IotRestoreRecordDO restore = new IotRestoreRecordDO();
        restore.setBackupId(reqVO.getBackupId());
        restore.setRestoreType(reqVO.getRestoreType());
        restore.setRestoreStatus(RestoreStatusEnum.IN_PROGRESS.getStatus());
        restore.setStartTime(LocalDateTime.now());
        restoreMapper.insert(restore);

        try {
            // 5. 执行数据导入
            long recordCount = importData(extractPath, metadata, reqVO);

            // 6. 更新恢复记录
            restore.setRestoreStatus(RestoreStatusEnum.SUCCESS.getStatus());
            restore.setEndTime(LocalDateTime.now());
            restore.setDuration((int) Duration.between(restore.getStartTime(),
                    restore.getEndTime()).getSeconds());
            restore.setRecordCount(recordCount);
            restoreMapper.updateById(restore);

            log.info("[executeRestore][恢复成功] restoreId={}, records={}",
                    restore.getId(), recordCount);

            return restore.getId();

        } catch (Exception e) {
            log.error("[executeRestore][恢复失败] backupId={}", reqVO.getBackupId(), e);

            // 恢复失败，更新记录
            restore.setRestoreStatus(RestoreStatusEnum.FAILED.getStatus());
            restore.setEndTime(LocalDateTime.now());
            restore.setErrorMessage(e.getMessage());
            restoreMapper.updateById(restore);

            throw new ServiceException("数据恢复失败：" + e.getMessage());

        } finally {
            // 清理临时文件
            FileUtil.del(extractPath);
        }
    }

    /**
     * 导入数据
     */
    private long importData(String path, BackupMetadata metadata,
                           IotRestoreCreateReqVO reqVO) {
        long totalCount = 0;

        for (BackupTable table : metadata.getTables()) {
            // 跳过不需要恢复的表（选择性恢复）
            if (!shouldRestore(table, reqVO)) {
                continue;
            }

            log.info("[importData][开始导入] db={}, table={}",
                    table.getDatabase(), table.getStable());

            // 读取CSV文件
            String csvFile = String.format("%s/%s/%s.csv",
                    path, table.getDatabase(), table.getStable());

            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                String[] headers = reader.readNext();

                // 批量插入数据
                List<String[]> batch = new ArrayList<>();
                String[] row;
                while ((row = reader.readNext()) != null) {
                    batch.add(row);

                    if (batch.size() >= 1000) {
                        batchInsert(table, headers, batch);
                        totalCount += batch.size();
                        batch.clear();
                    }
                }

                // 插入剩余数据
                if (!batch.isEmpty()) {
                    batchInsert(table, headers, batch);
                    totalCount += batch.size();
                }

                log.info("[importData][导入完成] db={}, table={}, records={}",
                        table.getDatabase(), table.getStable(), totalCount);

            } catch (Exception e) {
                log.error("[importData][导入失败] table={}", table.getStable(), e);
                throw new ServiceException("导入数据失败：" + e.getMessage());
            }
        }

        return totalCount;
    }

    /**
     * 批量插入数据
     */
    private void batchInsert(BackupTable table, String[] headers, List<String[]> batch) {
        // 构建批量插入SQL
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("INSERT INTO %s.%s (", table.getDatabase(), table.getStable()));
        sql.append(String.join(",", headers));
        sql.append(") VALUES ");

        for (int i = 0; i < batch.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(");
            String[] row = batch.get(i);
            for (int j = 0; j < row.length; j++) {
                if (j > 0) sql.append(",");
                sql.append("'").append(row[j]).append("'");
            }
            sql.append(")");
        }

        try (Connection conn = tdengineDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
        } catch (Exception e) {
            log.error("[batchInsert][批量插入失败]", e);
            throw new ServiceException("批量插入失败：" + e.getMessage());
        }
    }

    /**
     * 解压备份文件
     */
    private String decompressBackup(String zipPath) {
        String extractPath = zipPath.replace(".zip", "_extract");

        try {
            ZipUtil.unzip(zipPath, extractPath);
            log.info("[decompressBackup][解压完成] extractPath={}", extractPath);
        } catch (Exception e) {
            log.error("[decompressBackup][解压失败]", e);
            throw new ServiceException("解压备份文件失败：" + e.getMessage());
        }

        return extractPath;
    }
}
```

---

## 七、文件存储方案

### 7.1 目录结构

```
/data/iot-backup/
├── backups/                      # 备份文件目录
│   ├── 20250121/                 # 按日期分类
│   │   ├── backup_001.zip        # 压缩的备份文件
│   │   ├── backup_002.zip
│   │   └── ...
│   └── 20250122/
├── temp/                         # 临时目录（备份过程中使用）
│   ├── export_xxx/               # 临时导出目录
│   └── restore_xxx/              # 临时恢复目录
└── logs/                         # 备份日志
    ├── backup_20250121.log
    └── restore_20250121.log
```

### 7.2 备份文件内部结构

```
backup_001/
├── metadata.json                 # 备份元数据
│   {
│     "backupId": 123,
│     "backupTime": "2025-01-21T10:00:00",
│     "tdengineVersion": "3.0.0",
│     "databases": ["ruoyi_vue_pro"],
│     "tables": [
│       {
│         "database": "ruoyi_vue_pro",
│         "stable": "device_message",
│         "recordCount": 1000000,
│         "fileName": "device_message.csv"
│       }
│     ]
│   }
├── ruoyi_vue_pro/               # 数据库目录
│   ├── device_message.csv       # 超级表数据
│   ├── device_property.csv
│   └── energy_realtime_data.csv
└── schemas/                      # 表结构DDL
    ├── device_message.sql
    └── device_property.sql
```

### 7.3 配置参数

```yaml
# application.yaml
yudao:
  iot:
    backup:
      storage-path: /data/iot-backup  # 备份存储路径
      temp-path: /data/iot-backup/temp  # 临时目录
      max-backup-size: 10737418240  # 最大备份文件大小 10GB
      compression-level: 6  # 压缩级别 0-9
      batch-size: 1000  # 批量插入大小
      enable-encryption: false  # 是否启用加密
```

---

## 八、配置和权限

### 8.1 权限配置

```sql
-- 备份管理权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES
-- 一级菜单
('TDengine备份管理', '', 1, 5, {IoT模块ID}, 'backup', 'database', null, 0),

-- 二级菜单 - 备份列表
('备份列表', 'iot:backup:query', 2, 1, {上级ID}, 'list', '', 'iot/backup/list', 0),
('备份详情', 'iot:backup:get', 3, 1, {上级ID}, '', '', null, 0),
('创建备份', 'iot:backup:create', 3, 2, {上级ID}, '', '', null, 0),
('删除备份', 'iot:backup:delete', 3, 3, {上级ID}, '', '', null, 0),
('下载备份', 'iot:backup:download', 3, 4, {上级ID}, '', '', null, 0),

-- 二级菜单 - 恢复管理
('恢复管理', 'iot:restore:query', 2, 2, {上级ID}, 'restore', '', 'iot/backup/restore', 0),
('执行恢复', 'iot:restore:execute', 3, 1, {上级ID}, '', '', null, 0),

-- 二级菜单 - 备份配置
('备份配置', 'iot:backup:config', 2, 3, {上级ID}, 'config', '', 'iot/backup/config', 0),
('创建配置', 'iot:backup:config:create', 3, 1, {上级ID}, '', '', null, 0),
('更新配置', 'iot:backup:config:update', 3, 2, {上级ID}, '', '', null, 0),
('删除配置', 'iot:backup:config:delete', 3, 3, {上级ID}, '', '', null, 0),
('启用配置', 'iot:backup:config:enable', 3, 4, {上级ID}, '', '', null, 0);
```

### 8.2 Quartz 定时任务配置

```java
// 在系统启动时，根据备份配置表创建定时任务
@Component
public class BackupJobInitializer implements ApplicationRunner {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private IotBackupConfigMapper configMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 查询所有启用的备份配置
        List<IotBackupConfigDO> configs = configMapper.selectList(
                new LambdaQueryWrapper<IotBackupConfigDO>()
                        .eq(IotBackupConfigDO::getEnabled, true)
        );

        // 为每个配置创建定时任务
        for (IotBackupConfigDO config : configs) {
            scheduleBackupJob(config);
        }
    }

    private void scheduleBackupJob(IotBackupConfigDO config) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(BackupScheduleJob.class)
                .withIdentity("backup_job_" + config.getId(), "backup")
                .usingJobData("configId", config.getId())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("backup_trigger_" + config.getId(), "backup")
                .withSchedule(CronScheduleBuilder.cronSchedule(config.getCronExpression()))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
```

---

## 九、实施步骤

### 第一阶段：基础功能（预计 1-2 周）

- [x] **步骤1**: 创建数据库表（备份记录、配置、恢复记录）
- [ ] **步骤2**: 实现备份Mapper和DO
- [ ] **步骤3**: 实现备份执行引擎（导出TDengine数据到CSV）
- [ ] **步骤4**: 实现手动备份Service和Controller
- [ ] **步骤5**: 实现备份文件下载功能
- [x] **步骤6**: 前端备份列表页面

### 第二阶段：恢复功能（预计 1 周）

- [ ] **步骤7**: 实现数据恢复Service（从CSV导入到TDengine）
- [ ] **步骤8**: 实现恢复Controller API
- [x] **步骤9**: 前端恢复管理页面

### 第三阶段：自动化（预计 1 周）

- [ ] **步骤10**: 实现备份配置管理
- [ ] **步骤11**: 集成Quartz定时任务
- [ ] **步骤12**: 实现自动清理过期备份
- [x] **步骤13**: 前端备份配置页面

### 第四阶段：优化和测试（预计 1 周）

- [ ] **步骤14**: 添加备份进度显示（WebSocket，可选）
- [ ] **步骤15**: 实现增量备份（可选）
- [ ] **步骤16**: 添加通知功能（邮件/站内信）
- [ ] **步骤17**: 完善单元测试和集成测试
- [ ] **步骤18**: 性能测试和优化

---

## 十、注意事项和最佳实践

### 10.1 性能优化

#### 分批导出

对于大表，使用分页查询，避免内存溢出。

```java
// 分批查询示例
long offset = 0;
int batchSize = 10000;
while (true) {
    List<IotDeviceMessageDO> batch = queryBatch(offset, batchSize);
    if (batch.isEmpty()) break;
    writeToCSV(batch);
    offset += batchSize;
}
```

#### 异步备份

使用线程池异步执行备份任务，避免阻塞主线程。

```java
@Async("backupExecutor")
public CompletableFuture<BackupResult> executeBackupAsync(Long recordId) {
    // 备份逻辑
    return CompletableFuture.completedFuture(result);
}
```

#### 压缩优化

使用适当的压缩级别（6-7），平衡速度和压缩率。

### 10.2 数据安全

#### 备份加密

敏感数据备份文件应加密存储。

```java
// 使用AES加密备份文件
encryptBackupFile(zipPath, secretKey);
```

#### 权限控制

严格控制备份和恢复操作的权限。

#### 审计日志

记录所有备份恢复操作的审计日志。

#### 验证备份

定期验证备份文件的完整性。

### 10.3 存储管理

#### 磁盘空间监控

备份前检查磁盘空间是否充足。

```java
File backupDir = new File(backupPath);
long freeSpace = backupDir.getFreeSpace();
long requiredSpace = estimateBackupSize();
if (freeSpace < requiredSpace * 1.2) {
    throw new ServiceException("磁盘空间不足");
}
```

#### 自动清理

根据保留策略自动清理过期备份。

#### 异地备份

支持将备份文件上传到OSS（阿里云/七牛云）。

### 10.4 容错处理

#### 断点续传

大文件备份支持断点续传。

#### 回滚机制

恢复失败时能够回滚到原始状态。

#### 事务管理

确保备份和恢复操作的原子性。

### 10.5 监控告警

#### 备份成功率监控

统计备份成功率，低于阈值告警。

#### 备份耗时监控

监控备份耗时，异常增长时告警。

#### 存储空间告警

磁盘使用率超过80%时告警。

---

## 十一、前端页面设计

### 11.1 备份管理页面

**页面路径**: `/iot/backup/list`

**功能**:
- 备份列表（分页）
- 创建备份（手动）
- 查看备份详情
- 下载备份文件
- 删除备份
- 执行恢复

**布局示意**:

```
┌──────────────────────────────────────────────────────┐
│  TDengine 备份管理                                    │
├──────────────────────────────────────────────────────┤
│  [创建备份]  [刷新]            搜索: [______] [查询]  │
├──────────────────────────────────────────────────────┤
│  表格:                                                │
│  ┌────┬──────────┬────────┬────────┬──────┬────────┐│
│  │ID  │备份名称  │类型    │状态    │大小  │操作    ││
│  ├────┼──────────┼────────┼────────┼──────┼────────┤│
│  │001 │backup_01 │全量备份│成功    │2.5GB │详情 下载││
│  │    │2025-01-21│        │        │      │删除 恢复││
│  ├────┼──────────┼────────┼────────┼──────┼────────┤│
│  │002 │backup_02 │表备份  │成功    │500MB │详情 下载││
│  │    │2025-01-20│        │        │      │删除 恢复││
│  └────┴──────────┴────────┴────────┴──────┴────────┘│
│  [1] [2] [3] ... [10]                                │
└──────────────────────────────────────────────────────┘
```

### 11.2 备份配置页面

**页面路径**: `/iot/backup/config`

**功能**:
- 配置列表
- 创建配置
- 编辑配置
- 删除配置
- 启用/禁用配置
- 立即执行

**布局示意**:

```
┌──────────────────────────────────────────────────────┐
│  定时备份配置                                         │
├──────────────────────────────────────────────────────┤
│  [新建配置]                                           │
├──────────────────────────────────────────────────────┤
│  配置列表:                                            │
│  ┌──────────────┬─────────┬────────┬──────────────┐ │
│  │配置名称      │Cron表达式│状态    │操作          │ │
│  ├──────────────┼─────────┼────────┼──────────────┤ │
│  │每日全量备份  │0 0 2 * *│ [开启] │编辑 删除 执行│ │
│  │每周表备份    │0 0 0 * 0│ [关闭] │编辑 删除 执行│ │
│  └──────────────┴─────────┴────────┴──────────────┘ │
└──────────────────────────────────────────────────────┘

配置表单:
┌──────────────────────────────────────────────────────┐
│  配置名称: [__________________]                       │
│  备份类型: [全量备份 ▼]                              │
│  Cron表达式: [0 0 2 * * ?]  (每天凌晨2点)           │
│  保留天数: [7] 天                                    │
│  最大备份数: [10] 个                                 │
│  启用压缩: [√]                                       │
│  失败通知: [√]  邮箱: [admin@example.com]           │
│                                                       │
│  [保存] [取消]                                       │
└──────────────────────────────────────────────────────┘
```

### 11.3 恢复记录页面

**页面路径**: `/iot/backup/restore`

**功能**:
- 恢复记录列表
- 查看恢复详情

---

## 十二、扩展功能

以下功能可作为后续优化方向：

1. **增量备份**: 只备份变更的数据（基于时间戳）
2. **备份对比**: 对比两个备份文件的差异
3. **数据迁移**: 跨TDengine实例迁移数据
4. **备份验证**: 自动验证备份文件的完整性
5. **WebSocket进度**: 实时显示备份/恢复进度
6. **多点备份**: 同时备份到本地和云存储
7. **备份报表**: 生成备份统计报表和趋势分析
8. **备份加密**: 对敏感数据进行加密存储
9. **备份压缩优化**: 使用更高效的压缩算法
10. **备份去重**: 相同数据只存储一份

---

## 附录

### A. TDengine SQL 参考

```sql
-- 查询所有数据库
SHOW DATABASES;

-- 查询指定数据库的所有超级表
SHOW database_name.STABLES;

-- 查询超级表的所有子表
SHOW database_name.TABLES LIKE 'stable_name_%';

-- 查询超级表结构
DESCRIBE database_name.stable_name;

-- 导出数据到CSV（需要TDengine 3.0+）
SELECT * FROM database_name.stable_name INTO OUTFILE '/path/to/file.csv';

-- 批量插入数据
INSERT INTO database_name.table_name VALUES
  (timestamp1, value1, value2),
  (timestamp2, value1, value2);
```

### B. 依赖库

```xml
<!-- CSV处理 -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.7.1</version>
</dependency>

<!-- 文件压缩 -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
</dependency>

<!-- Quartz定时任务 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

---

**文档维护者**: Claude
**联系方式**: 请通过项目 Issue 反馈问题
