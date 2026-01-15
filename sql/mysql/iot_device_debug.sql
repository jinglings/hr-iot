-- =====================================================
-- IoT 设备调试模块 - 数据库迁移脚本
-- 创建时间: 2026-01-11
-- =====================================================

-- ---------------------------------------------------
-- 表: iot_device_debug_log - 设备调试日志表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `iot_device_debug_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id` bigint(20) NOT NULL COMMENT '设备ID',
    `device_key` varchar(64) NOT NULL DEFAULT '' COMMENT '设备标识',
    `product_key` varchar(64) NOT NULL DEFAULT '' COMMENT '产品标识',
    `direction` tinyint(4) NOT NULL COMMENT '消息方向: 1=上行, 2=下行',
    `type` varchar(32) NOT NULL COMMENT '消息类型: property_report, property_set, service_invoke, event_report',
    `identifier` varchar(64) DEFAULT '' COMMENT '属性/事件/服务标识符',
    `payload` text COMMENT '消息内容(JSON)',
    `result` text COMMENT '调用结果(JSON)',
    `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0=失败, 1=成功',
    `error_message` varchar(500) DEFAULT '' COMMENT '错误信息',
    `latency` int(11) DEFAULT 0 COMMENT '响应延迟(毫秒)',
    
    -- 标准字段
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_key` (`device_key`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备调试日志表';
