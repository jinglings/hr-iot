-- =====================================================
-- IoT 设备标签模块 - 数据库迁移脚本
-- 创建时间: 2026-01-11
-- =====================================================

-- ---------------------------------------------------
-- 表: iot_device_tag - 设备标签定义表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `iot_device_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `tag_key` varchar(64) NOT NULL COMMENT '标签键',
    `tag_value` varchar(128) DEFAULT '' COMMENT '标签值',
    `description` varchar(255) DEFAULT '' COMMENT '标签描述',
    `color` varchar(16) DEFAULT '#409EFF' COMMENT '标签显示颜色',
    `is_preset` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否预置标签: 0=否, 1=是',
    `usage_count` int(11) NOT NULL DEFAULT 0 COMMENT '使用次数',
    
    -- 标准字段
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_key_value_tenant` (`tag_key`, `tag_value`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备标签表';

-- ---------------------------------------------------
-- 表: iot_device_tag_relation - 设备标签关联表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `iot_device_tag_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `device_id` bigint(20) NOT NULL COMMENT '设备ID',
    `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
    
    -- 标准字段
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_tag_tenant` (`device_id`, `tag_id`, `tenant_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_tag_id` (`tag_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备标签关联表';

-- ---------------------------------------------------
-- 预置标签数据
-- ---------------------------------------------------
INSERT INTO `iot_device_tag` (`tag_key`, `tag_value`, `description`, `color`, `is_preset`, `tenant_id`) VALUES
-- 位置标签
('location', '车间A', '设备位置-车间A', '#409EFF', 1, 0),
('location', '车间B', '设备位置-车间B', '#409EFF', 1, 0),
('location', '办公室', '设备位置-办公室', '#409EFF', 1, 0),
('location', '机房', '设备位置-机房', '#409EFF', 1, 0),
-- 环境标签
('environment', '生产环境', '生产环境设备', '#67C23A', 1, 0),
('environment', '测试环境', '测试环境设备', '#E6A23C', 1, 0),
('environment', '开发环境', '开发环境设备', '#909399', 1, 0),
-- 优先级标签
('priority', 'high', '高优先级设备', '#F56C6C', 1, 0),
('priority', 'medium', '中优先级设备', '#E6A23C', 1, 0),
('priority', 'low', '低优先级设备', '#909399', 1, 0),
-- 用途标签
('usage', '监控', '监控类设备', '#409EFF', 1, 0),
('usage', '控制', '控制类设备', '#67C23A', 1, 0),
('usage', '计量', '计量类设备', '#E6A23C', 1, 0);
