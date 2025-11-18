-- 设备影子表
CREATE TABLE IF NOT EXISTS `iot_device_shadow` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `desired_state` json DEFAULT NULL COMMENT '期望状态（云端下发）',
    `reported_state` json DEFAULT NULL COMMENT '实际状态（设备上报）',
    `metadata` json DEFAULT NULL COMMENT '元数据（状态更新时间等）',
    `version` int NOT NULL DEFAULT 1 COMMENT '版本号（用于并发控制）',
    `desired_version` int DEFAULT NULL COMMENT '期望状态版本号',
    `reported_version` int DEFAULT NULL COMMENT '实际状态版本号',
    `last_desired_time` datetime DEFAULT NULL COMMENT '最后期望状态更新时间',
    `last_reported_time` datetime DEFAULT NULL COMMENT '最后实际状态更新时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备影子表';

-- 设备影子变更历史表（可选，用于审计）
CREATE TABLE IF NOT EXISTS `iot_device_shadow_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `shadow_id` bigint NOT NULL COMMENT '影子ID',
    `change_type` varchar(20) NOT NULL COMMENT '变更类型: DESIRED, REPORTED, DELTA',
    `before_state` json DEFAULT NULL COMMENT '变更前状态',
    `after_state` json DEFAULT NULL COMMENT '变更后状态',
    `delta_state` json DEFAULT NULL COMMENT '差量状态',
    `version` int NOT NULL COMMENT '版本号',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_shadow_id` (`shadow_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备影子变更历史表';
