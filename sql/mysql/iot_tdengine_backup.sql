-- ----------------------------
-- TDengine 备份恢复功能相关表
-- 用于管理 TDengine 时序数据库的备份、恢复和定时任务配置
-- 创建日期: 2025-01-21
-- ----------------------------

-- ----------------------------
-- 1. 备份记录表
-- 存储每次备份的详细信息，包括手动备份和定时自动备份
-- ----------------------------
DROP TABLE IF EXISTS `iot_backup_record`;
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
  INDEX `idx_backup_type` (`backup_type`),
  INDEX `idx_backup_mode` (`backup_mode`),
  INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine备份记录表';

-- ----------------------------
-- 2. 备份配置表
-- 存储定时备份的配置，支持 Cron 表达式配置定时任务
-- ----------------------------
DROP TABLE IF EXISTS `iot_backup_config`;
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
  INDEX `idx_enabled` (`enabled`),
  INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine备份配置表';

-- ----------------------------
-- 3. 恢复记录表
-- 存储数据恢复的历史记录，用于追溯和审计
-- ----------------------------
DROP TABLE IF EXISTS `iot_restore_record`;
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
  INDEX `idx_restore_status` (`restore_status`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_tenant_id` (`tenant_id`),
  CONSTRAINT `fk_restore_backup` FOREIGN KEY (`backup_id`) REFERENCES `iot_backup_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT TDengine恢复记录表';

-- ----------------------------
-- 初始化示例数据（可选）
-- ----------------------------
-- 示例：创建一个默认的每日全量备份配置
INSERT INTO `iot_backup_config` (
  `config_name`,
  `enabled`,
  `backup_type`,
  `backup_scope`,
  `cron_expression`,
  `retention_days`,
  `max_backup_count`,
  `compression_enabled`,
  `notify_on_success`,
  `notify_on_failure`,
  `notify_emails`,
  `creator`,
  `create_time`,
  `updater`,
  `update_time`,
  `deleted`,
  `tenant_id`
) VALUES (
  '每日全量备份',
  1,
  1,
  NULL,
  '0 0 2 * * ?',
  7,
  10,
  1,
  0,
  1,
  NULL,
  '1',
  NOW(),
  '1',
  NOW(),
  0,
  0
);

-- ----------------------------
-- 表结构说明
-- ----------------------------
--
-- 1. iot_backup_record (备份记录表)
--    - 记录每次备份的详细信息
--    - backup_type: 1-全量(所有数据库), 2-指定数据库, 3-指定表
--    - backup_scope: JSON 格式，例如 ["ruoyi_vue_pro"] 或 ["device_message", "device_property"]
--    - backup_mode: 1-手动触发, 2-定时任务
--    - backup_status: 0-进行中, 1-成功, 2-失败
--    - 支持按创建时间、状态、类型查询
--
-- 2. iot_backup_config (备份配置表)
--    - 定时备份任务的配置
--    - cron_expression: Quartz Cron 表达式，例如 "0 0 2 * * ?" 表示每天凌晨 2 点
--    - retention_days: 备份保留天数，超过自动删除
--    - max_backup_count: 最大保留备份数量，超过删除最旧的
--    - 支持邮件通知功能
--
-- 3. iot_restore_record (恢复记录表)
--    - 记录数据恢复的历史
--    - restore_type: 1-完整恢复(恢复所有数据), 2-选择性恢复(指定表或范围)
--    - restore_target: JSON 格式，选择性恢复时指定目标
--    - 通过外键关联备份记录表
--
-- ----------------------------
-- 使用说明
-- ----------------------------
--
-- 1. 手动备份流程：
--    - 插入备份记录（status=0, mode=1）
--    - 执行备份操作
--    - 更新备份记录（status=1/2, 文件路径、大小、耗时等）
--
-- 2. 定时备份流程：
--    - 在 iot_backup_config 中配置定时任务
--    - Quartz 定时触发备份
--    - 自动插入备份记录（mode=2）
--    - 根据 retention_days 和 max_backup_count 自动清理过期备份
--
-- 3. 数据恢复流程：
--    - 选择备份记录
--    - 插入恢复记录（status=0）
--    - 执行恢复操作
--    - 更新恢复记录（status=1/2, 耗时、条数等）
--
-- 4. 备份文件存储：
--    - 默认路径：/data/iot-backup/backups/{date}/{backup_name}.zip
--    - 文件格式：ZIP 压缩包，包含 CSV 数据文件和 metadata.json
--    - 临时目录：/data/iot-backup/temp/
--
-- 5. 权限要求：
--    - 需要配置对应的菜单权限（iot:backup:*, iot:restore:*）
--    - 建议仅授予管理员用户
--
