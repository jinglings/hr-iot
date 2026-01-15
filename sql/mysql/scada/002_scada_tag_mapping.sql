-- SCADA Tag Mapping Table
-- Maps SCADA tags to IoT device properties
-- Part of SCADA-008: Replace SQLite with MySQL

CREATE TABLE IF NOT EXISTS `scada_tag_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_name` varchar(255) NOT NULL COMMENT 'SCADA tag 名称 (e.g., Pump1_Status)',
  `tag_id` varchar(100) NOT NULL COMMENT 'FUXA 内部 tag ID',
  `device_id` bigint(20) DEFAULT NULL COMMENT 'IoT 平台设备 ID',
  `property_identifier` varchar(100) DEFAULT NULL COMMENT '物模型属性标识符',
  `data_type` varchar(50) NOT NULL DEFAULT 'number' COMMENT '数据类型: number, string, boolean, object',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `scale_factor` decimal(10,4) DEFAULT '1.0000' COMMENT '缩放系数',
  `offset` decimal(10,4) DEFAULT '0.0000' COMMENT '偏移量',
  `min_value` decimal(20,6) DEFAULT NULL COMMENT '最小值',
  `max_value` decimal(20,6) DEFAULT NULL COMMENT '最大值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_tag_name` (`tenant_id`, `tag_name`),
  UNIQUE KEY `uk_tenant_tag_id` (`tenant_id`, `tag_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_property_identifier` (`property_identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA Tag 映射表';

-- SCADA Alarm Configuration Table
CREATE TABLE IF NOT EXISTS `scada_alarm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alarm_name` varchar(255) NOT NULL COMMENT '告警名称',
  `tag_id` varchar(100) DEFAULT NULL COMMENT '关联的 tag ID',
  `alarm_type` varchar(50) NOT NULL DEFAULT 'HIGH' COMMENT '告警类型: HIGH, HIGHHIGH, LOW, LOWLOW, BOOL, etc.',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `trigger_value` decimal(20,6) DEFAULT NULL COMMENT '触发值',
  `hysteresis` decimal(10,4) DEFAULT '0.0000' COMMENT '滞后值',
  `delay_on` int(11) DEFAULT '0' COMMENT '触发延迟(秒)',
  `delay_off` int(11) DEFAULT '0' COMMENT '恢复延迟(秒)',
  `priority` tinyint(4) NOT NULL DEFAULT '2' COMMENT '优先级: 1-低, 2-中, 3-高, 4-紧急',
  `message` varchar(500) DEFAULT NULL COMMENT '告警消息',
  `ack_required` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否需要确认',
  `config_json` text COMMENT '扩展配置 (JSON)',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_alarm_name` (`tenant_id`, `alarm_name`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 告警配置表';

-- SCADA Notification Configuration Table
CREATE TABLE IF NOT EXISTS `scada_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notification_id` varchar(100) NOT NULL COMMENT 'FUXA 通知 ID',
  `name` varchar(255) NOT NULL COMMENT '通知名称',
  `notification_type` varchar(50) NOT NULL DEFAULT 'email' COMMENT '通知类型: email, sms, webhook, etc.',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `recipients_json` text COMMENT '接收人配置 (JSON)',
  `trigger_config_json` text COMMENT '触发条件配置 (JSON)',
  `template` text COMMENT '消息模板',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_notification_id` (`tenant_id`, `notification_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 通知配置表';

-- SCADA Script Table
CREATE TABLE IF NOT EXISTS `scada_script` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `script_id` varchar(100) NOT NULL COMMENT 'FUXA 脚本 ID',
  `name` varchar(255) NOT NULL COMMENT '脚本名称',
  `script_type` varchar(50) NOT NULL DEFAULT 'javascript' COMMENT '脚本类型',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `script_code` longtext COMMENT '脚本代码',
  `trigger_config_json` text COMMENT '触发配置 (JSON)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_script_id` (`tenant_id`, `script_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 脚本表';

-- SCADA Report Configuration Table
CREATE TABLE IF NOT EXISTS `scada_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `report_id` varchar(100) NOT NULL COMMENT 'FUXA 报表 ID',
  `name` varchar(255) NOT NULL COMMENT '报表名称',
  `report_type` varchar(50) NOT NULL DEFAULT 'custom' COMMENT '报表类型',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `config_json` longtext COMMENT '报表配置 (JSON)',
  `schedule_json` text COMMENT '调度配置 (JSON)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_report_id` (`tenant_id`, `report_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 报表配置表';

-- SCADA Text/Label Configuration Table
CREATE TABLE IF NOT EXISTS `scada_text` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `text_id` varchar(100) NOT NULL COMMENT 'FUXA 文本 ID',
  `name` varchar(255) NOT NULL COMMENT '文本名称',
  `config_json` text COMMENT '文本配置 (JSON)',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_text_id` (`tenant_id`, `text_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 文本配置表';

-- SCADA Map Location Table
CREATE TABLE IF NOT EXISTS `scada_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `location_id` varchar(100) NOT NULL COMMENT 'FUXA 位置 ID',
  `name` varchar(255) NOT NULL COMMENT '位置名称',
  `config_json` text COMMENT '位置配置 (JSON)',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_location_id` (`tenant_id`, `location_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 地图位置表';
