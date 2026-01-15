-- SCADA Database Migration - All in One
-- =============================================================
-- This script contains all SCADA-related table definitions
-- Run this script to set up the complete SCADA database schema
-- 
-- Part of SCADA-016: Create Database Migration Scripts
-- 
-- Tables included:
-- 1. scada_dashboard - 仪表板配置
-- 2. scada_view - 视图配置
-- 3. scada_device - FUXA 设备配置
-- 4. scada_setting - 全局设置
-- 5. scada_tag_mapping - Tag 映射
-- 6. scada_control_log - 控制操作日志
-- 7. scada_alarm - 告警配置
-- 8. scada_alarm_history - 告警历史
-- =============================================================

-- ============================================================
-- 1. SCADA Dashboard Table
-- ============================================================
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
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 仪表板配置表';

-- ============================================================
-- 2. SCADA View Table
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `view_id` varchar(100) NOT NULL COMMENT 'FUXA 视图 ID (UUID)',
  `dashboard_id` bigint(20) DEFAULT NULL COMMENT '所属仪表板 ID',
  `name` varchar(255) NOT NULL COMMENT '视图名称',
  `config_json` longtext NOT NULL COMMENT 'FUXA view configuration (JSON)',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_view_id` (`tenant_id`, `view_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_dashboard_id` (`dashboard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 视图配置表';

-- ============================================================
-- 3. SCADA Tag Mapping Table
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_tag_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_name` varchar(255) NOT NULL COMMENT 'SCADA Tag 名称',
  `tag_id` varchar(100) DEFAULT NULL COMMENT 'FUXA 内部 Tag ID',
  `device_id` bigint(20) NOT NULL COMMENT 'IoT 设备 ID',
  `property_identifier` varchar(100) NOT NULL COMMENT 'IoT 属性标识符',
  `data_type` varchar(50) NOT NULL DEFAULT 'number' COMMENT '数据类型',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `scale_factor` decimal(20,6) DEFAULT '1' COMMENT '缩放因子',
  `offset` decimal(20,6) DEFAULT '0' COMMENT '偏移量',
  `min_value` decimal(20,6) DEFAULT NULL COMMENT '最小值',
  `max_value` decimal(20,6) DEFAULT NULL COMMENT '最大值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_tag_name` (`tenant_id`, `tag_name`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA Tag 映射表';

-- ============================================================
-- 4. SCADA Control Log Table
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_control_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` bigint(20) NOT NULL COMMENT 'IoT 设备 ID',
  `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `command_name` varchar(100) NOT NULL COMMENT '命令名称/服务名称',
  `command_params` text COMMENT '命令参数 (JSON)',
  `old_value` varchar(255) DEFAULT NULL COMMENT '操作前的值',
  `new_value` varchar(255) DEFAULT NULL COMMENT '操作后的值',
  `execution_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '执行状态: 0-失败, 1-成功',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户 ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '操作用户名',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端 IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `execution_time` int(11) DEFAULT NULL COMMENT '执行耗时 (毫秒)',
  `source` varchar(50) DEFAULT 'SCADA' COMMENT '来源: SCADA, API, MQTT',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_execution_status` (`execution_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 控制操作日志表';

-- ============================================================
-- 5. SCADA Alarm Configuration Table
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_alarm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alarm_name` varchar(255) NOT NULL COMMENT '告警名称',
  `tag_id` varchar(100) NOT NULL COMMENT '关联的 Tag ID',
  `alarm_type` varchar(50) NOT NULL COMMENT '告警类型: HIGH, LOW, HIHI, LOLO, BOOL, DEVIATION',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `trigger_value` decimal(20,6) DEFAULT NULL COMMENT '触发值',
  `hysteresis` decimal(20,6) DEFAULT '0' COMMENT '滞后值',
  `delay_on` int(11) DEFAULT '0' COMMENT '触发延迟 (毫秒)',
  `delay_off` int(11) DEFAULT '0' COMMENT '恢复延迟 (毫秒)',
  `priority` int(11) NOT NULL DEFAULT '2' COMMENT '优先级: 1-低, 2-中, 3-高, 4-紧急',
  `message` varchar(500) DEFAULT NULL COMMENT '告警消息模板',
  `ack_required` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否需要确认',
  `config_json` text COMMENT '扩展配置 (JSON)',
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

-- ============================================================
-- 6. SCADA Alarm History Table
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_alarm_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alarm_id` bigint(20) NOT NULL COMMENT '告警配置 ID',
  `alarm_name` varchar(255) NOT NULL COMMENT '告警名称',
  `tag_id` varchar(100) NOT NULL COMMENT 'Tag ID',
  `tag_value` varchar(255) DEFAULT NULL COMMENT '触发时的值',
  `priority` int(11) NOT NULL DEFAULT '2' COMMENT '优先级',
  `message` varchar(500) DEFAULT NULL COMMENT '告警消息',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态: 1-活动, 2-已确认, 3-已恢复, 4-已关闭',
  `triggered_at` datetime NOT NULL COMMENT '触发时间',
  `acknowledged_at` datetime DEFAULT NULL COMMENT '确认时间',
  `acknowledged_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `closed_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `closed_by` varchar(64) DEFAULT NULL COMMENT '关闭人',
  `notes` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_alarm_id` (`alarm_id`),
  KEY `idx_status` (`status`),
  KEY `idx_triggered_at` (`triggered_at`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 告警历史记录表';

-- ============================================================
-- Sample Data for Testing
-- ============================================================

-- Sample Dashboard
INSERT INTO `scada_dashboard` (`dashboard_id`, `name`, `description`, `dashboard_type`, `config_json`, `is_default`, `tenant_id`) VALUES
('demo-water-system', '供水系统监控', '供水系统 SCADA 仪表板', 'water', '{}', b'1', 1),
('demo-hvac-system', '暖通空调监控', 'HVAC 系统 SCADA 仪表板', 'hvac', '{}', b'0', 1);

-- Sample Tag Mappings
INSERT INTO `scada_tag_mapping` (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `description`, `tenant_id`) VALUES
('Pump1_Status', 'tag_pump_001', 1, 'status', 'boolean', NULL, '1号水泵状态', 1),
('Pump1_Speed', 'tag_pump_001_speed', 1, 'speed', 'number', 'RPM', '1号水泵转速', 1),
('Tank1_Level', 'tag_tank_001', 2, 'level', 'number', '%', '1号水箱液位', 1),
('Temp_Sensor1', 'tag_temp_001', 3, 'temperature', 'number', '°C', '温度传感器1', 1),
('Pressure_Sensor1', 'tag_pressure_001', 4, 'pressure', 'number', 'PSI', '压力传感器1', 1);

-- Sample Alarm Configurations
INSERT INTO `scada_alarm` (`alarm_name`, `tag_id`, `alarm_type`, `trigger_value`, `priority`, `message`, `tenant_id`) VALUES
('水箱液位过高', 'tag_tank_001', 'HIGH', 90.00, 3, '水箱液位超过 90%', 1),
('水箱液位过低', 'tag_tank_001', 'LOW', 10.00, 3, '水箱液位低于 10%', 1),
('温度过高', 'tag_temp_001', 'HIGH', 80.00, 3, '温度超过 80°C', 1),
('压力异常', 'tag_pressure_001', 'HIHI', 150.00, 4, '压力严重超标', 1);

-- ============================================================
-- Post-Migration Verification
-- ============================================================
-- Run these queries to verify the migration:
-- SELECT COUNT(*) FROM scada_dashboard;
-- SELECT COUNT(*) FROM scada_tag_mapping;
-- SELECT COUNT(*) FROM scada_alarm;
-- SHOW TABLES LIKE 'scada%';
