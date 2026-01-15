-- SCADA Alarm Configuration and History Tables
-- Part of SCADA-016: Create Database Migration Scripts
-- 
-- Tables:
-- 1. scada_alarm - 告警配置表
-- 2. scada_alarm_history - 告警历史记录表
--

-- ============================================================
-- SCADA Alarm Configuration Table
-- 告警配置表，定义告警规则
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_alarm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alarm_name` varchar(255) NOT NULL COMMENT '告警名称',
  `tag_id` varchar(100) NOT NULL COMMENT '关联的 Tag ID',
  `alarm_type` varchar(50) NOT NULL COMMENT '告警类型: HIGH, LOW, HIHI, LOLO, BOOL, DEVIATION',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  
  -- Trigger configuration
  `trigger_value` decimal(20,6) DEFAULT NULL COMMENT '触发值',
  `hysteresis` decimal(20,6) DEFAULT '0' COMMENT '滞后值 (防止抖动)',
  `delay_on` int(11) DEFAULT '0' COMMENT '触发延迟 (毫秒)',
  `delay_off` int(11) DEFAULT '0' COMMENT '恢复延迟 (毫秒)',
  
  -- Alarm details
  `priority` int(11) NOT NULL DEFAULT '2' COMMENT '优先级: 1-低, 2-中, 3-高, 4-紧急',
  `message` varchar(500) DEFAULT NULL COMMENT '告警消息模板',
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
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 告警配置表';

-- ============================================================
-- SCADA Alarm History Table
-- 告警历史记录表，记录告警事件
-- ============================================================
CREATE TABLE IF NOT EXISTS `scada_alarm_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alarm_id` bigint(20) NOT NULL COMMENT '告警配置 ID',
  `alarm_name` varchar(255) NOT NULL COMMENT '告警名称 (冗余，便于查询)',
  `tag_id` varchar(100) NOT NULL COMMENT 'Tag ID',
  `tag_value` varchar(255) DEFAULT NULL COMMENT '触发时的 Tag 值',
  
  -- Alarm details
  `priority` int(11) NOT NULL DEFAULT '2' COMMENT '优先级',
  `message` varchar(500) DEFAULT NULL COMMENT '告警消息',
  
  -- Status: 1-活动, 2-已确认, 3-已恢复, 4-已关闭
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态',
  
  -- Timeline
  `triggered_at` datetime NOT NULL COMMENT '触发时间',
  `acknowledged_at` datetime DEFAULT NULL COMMENT '确认时间',
  `acknowledged_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `closed_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `closed_by` varchar(64) DEFAULT NULL COMMENT '关闭人',
  `notes` varchar(500) DEFAULT NULL COMMENT '处理备注',
  
  -- Standard fields
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',

  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_alarm_id` (`alarm_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_priority` (`priority`),
  KEY `idx_status` (`status`),
  KEY `idx_triggered_at` (`triggered_at`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_triggered` (`tenant_id`, `triggered_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SCADA 告警历史记录表';

-- ============================================================
-- Sample data for testing
-- ============================================================
INSERT INTO `scada_alarm` (`alarm_name`, `tag_id`, `alarm_type`, `trigger_value`, `priority`, `message`, `tenant_id`) VALUES
('温度过高告警', 'tag_temp_001', 'HIGH', 80.00, 3, '温度超过 80°C，当前值: ${value}°C', 1),
('温度过低告警', 'tag_temp_001', 'LOW', 10.00, 2, '温度低于 10°C，当前值: ${value}°C', 1),
('压力异常告警', 'tag_pressure_001', 'HIHI', 150.00, 4, '压力严重超标，当前值: ${value} PSI', 1),
('水泵故障告警', 'tag_pump_status_001', 'BOOL', 0, 3, '水泵 1 发生故障', 1),
('流量偏差告警', 'tag_flow_001', 'DEVIATION', 20.00, 2, '流量偏差超过 20%', 1);
