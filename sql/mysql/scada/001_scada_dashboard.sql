-- SCADA Dashboard Configuration Table
-- Storage for FUXA dashboard configurations with multi-tenancy support
-- Part of SCADA-008: Replace SQLite with MySQL

CREATE TABLE IF NOT EXISTS `scada_dashboard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dashboard_id` varchar(100) NOT NULL COMMENT 'FUXA 仪表板 ID (UUID)',
  `name` varchar(255) NOT NULL COMMENT '仪表板名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `dashboard_type` varchar(50) NOT NULL DEFAULT 'custom' COMMENT '类型: water, hvac, energy, custom',
  `config_json` longtext NOT NULL COMMENT 'FUXA dashboard configuration (JSON)',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图 URL',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认仪表板',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_dashboard_id` (`tenant_id`, `dashboard_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_dashboard_type` (`dashboard_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 仪表板配置表';

-- SCADA View Table (individual views within dashboards)
CREATE TABLE IF NOT EXISTS `scada_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `view_id` varchar(100) NOT NULL COMMENT 'FUXA 视图 ID (UUID)',
  `dashboard_id` bigint(20) DEFAULT NULL COMMENT '所属仪表板 ID (可选)',
  `name` varchar(255) NOT NULL COMMENT '视图名称',
  `config_json` longtext NOT NULL COMMENT 'FUXA view configuration (JSON)',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_view_id` (`tenant_id`, `view_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_dashboard_id` (`dashboard_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 视图配置表';

-- SCADA Device Configuration Table
CREATE TABLE IF NOT EXISTS `scada_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(100) NOT NULL COMMENT 'FUXA 设备 ID',
  `name` varchar(255) NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) NOT NULL COMMENT '设备类型: MQTTclient, HrIotMQTT, OPCUA, etc.',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `property_json` text COMMENT '设备属性配置 (JSON)',
  `tags_json` longtext COMMENT '设备标签配置 (JSON)',
  `polling_interval` int(11) DEFAULT '3000' COMMENT '轮询间隔(毫秒)',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_device_id` (`tenant_id`, `device_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 设备配置表';

-- SCADA Device Security Table (for storing sensitive credentials)
CREATE TABLE IF NOT EXISTS `scada_device_security` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(100) NOT NULL COMMENT 'FUXA 设备 ID',
  `security_json` text COMMENT '安全配置 (加密存储)',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_device_id` (`tenant_id`, `device_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 设备安全配置表';

-- SCADA General Settings Table
CREATE TABLE IF NOT EXISTS `scada_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `setting_key` varchar(100) NOT NULL COMMENT '配置键',
  `setting_value` longtext COMMENT '配置值 (JSON)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',

  -- Standard audit fields
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_setting_key` (`tenant_id`, `setting_key`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 通用配置表';
