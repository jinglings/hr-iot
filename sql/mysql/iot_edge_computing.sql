-- ----------------------------
-- IoT 边缘计算模块表结构
-- Edge Computing Module
-- ----------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for iot_edge_gateway
-- 边缘网关表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_gateway`;
CREATE TABLE `iot_edge_gateway` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网关名称',
  `serial_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网关序列号',
  `gateway_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网关标识',
  `gateway_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网关密钥',

  -- 设备信息
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备型号',
  `hardware_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '硬件版本',
  `software_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '软件版本',

  -- 网络信息
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `mac_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MAC地址',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '安装位置',

  -- 状态信息
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未激活, 1=在线, 2=离线',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后在线时间',
  `last_offline_time` datetime NULL DEFAULT NULL COMMENT '最后离线时间',
  `active_time` datetime NULL DEFAULT NULL COMMENT '激活时间',

  -- 资源信息
  `cpu_cores` int NULL DEFAULT NULL COMMENT 'CPU核心数',
  `memory_total` int NULL DEFAULT NULL COMMENT '总内存(MB)',
  `disk_total` int NULL DEFAULT NULL COMMENT '总磁盘(GB)',

  -- 配置信息
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '网关配置(JSON)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',

  -- 标准字段
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_serial_number`(`serial_number` ASC) USING BTREE,
  UNIQUE INDEX `uk_gateway_key`(`gateway_key` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘网关表';

-- ----------------------------
-- Table structure for iot_edge_gateway_status
-- 边缘网关状态表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_gateway_status`;
CREATE TABLE `iot_edge_gateway_status` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gateway_id` bigint NOT NULL COMMENT '网关ID',

  -- 资源使用情况
  `cpu_usage` decimal(5,2) NULL DEFAULT NULL COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) NULL DEFAULT NULL COMMENT '内存使用率(%)',
  `disk_usage` decimal(5,2) NULL DEFAULT NULL COMMENT '磁盘使用率(%)',
  `network_upload_speed` int NULL DEFAULT NULL COMMENT '网络上传速度(KB/s)',
  `network_download_speed` int NULL DEFAULT NULL COMMENT '网络下载速度(KB/s)',

  -- 设备统计
  `online_device_count` int DEFAULT 0 COMMENT '在线设备数',
  `total_device_count` int DEFAULT 0 COMMENT '总设备数',

  -- 规则统计
  `active_rule_count` int DEFAULT 0 COMMENT '活跃规则数',
  `rule_execute_count` int DEFAULT 0 COMMENT '规则执行次数',

  -- 数据统计
  `data_receive_count` bigint DEFAULT 0 COMMENT '接收数据条数',
  `data_upload_count` bigint DEFAULT 0 COMMENT '上传数据条数',

  -- 时间信息
  `record_time` datetime NOT NULL COMMENT '记录时间',

  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gateway_id`(`gateway_id` ASC) USING BTREE,
  INDEX `idx_record_time`(`record_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘网关状态表';

-- ----------------------------
-- Table structure for iot_edge_rule
-- 边缘规则表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_rule`;
CREATE TABLE `iot_edge_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `gateway_id` bigint NOT NULL COMMENT '网关ID',

  -- 规则配置
  `rule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则类型: SCENE, DATA_FLOW, AI_INFERENCE',
  `trigger_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器配置(JSON)',
  `condition_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '条件配置(JSON)',
  `action_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作配置(JSON)',

  -- 执行配置
  `execute_local` tinyint NOT NULL DEFAULT 1 COMMENT '是否本地执行: 0=否, 1=是',
  `priority` int DEFAULT 0 COMMENT '优先级',

  -- 状态信息
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
  `deploy_status` tinyint NOT NULL DEFAULT 0 COMMENT '部署状态: 0=未部署, 1=已部署, 2=部署失败',
  `deploy_time` datetime NULL DEFAULT NULL COMMENT '部署时间',
  `deploy_error` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部署错误信息',

  -- 统计信息
  `execute_count` bigint DEFAULT 0 COMMENT '执行次数',
  `last_execute_time` datetime NULL DEFAULT NULL COMMENT '最后执行时间',

  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',

  -- 标准字段
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gateway_id`(`gateway_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_deploy_status`(`deploy_status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘规则表';

-- ----------------------------
-- Table structure for iot_edge_ai_model
-- AI模型表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_ai_model`;
CREATE TABLE `iot_edge_ai_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型版本',

  -- 模型信息
  `model_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型类型: IMAGE_CLASSIFICATION, OBJECT_DETECTION, ANOMALY_DETECTION',
  `model_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型格式: ONNX, TENSORFLOW_LITE, PYTORCH',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型文件URL',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MD5',

  -- 运行要求
  `min_memory` int NULL DEFAULT NULL COMMENT '最小内存要求(MB)',
  `min_cpu_cores` int NULL DEFAULT NULL COMMENT '最小CPU核心数',
  `gpu_required` tinyint DEFAULT 0 COMMENT '是否需要GPU: 0=否, 1=是',

  -- 应用场景
  `application_scene` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用场景',
  `input_format` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '输入格式',
  `output_format` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '输出格式',

  -- 状态信息
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',

  -- 标准字段
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_model_type`(`model_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘 AI 模型表';

-- ----------------------------
-- Table structure for iot_edge_model_deployment
-- 模型部署记录表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_model_deployment`;
CREATE TABLE `iot_edge_model_deployment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id` bigint NOT NULL COMMENT '模型ID',
  `gateway_id` bigint NOT NULL COMMENT '网关ID',

  -- 部署信息
  `deploy_status` tinyint NOT NULL DEFAULT 0 COMMENT '部署状态: 0=未部署, 1=已部署, 2=部署失败',
  `deploy_time` datetime NULL DEFAULT NULL COMMENT '部署时间',
  `deploy_error` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部署错误信息',

  -- 运行信息
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0=停止, 1=运行中',
  `start_time` datetime NULL DEFAULT NULL COMMENT '启动时间',
  `stop_time` datetime NULL DEFAULT NULL COMMENT '停止时间',

  -- 性能统计
  `inference_count` bigint DEFAULT 0 COMMENT '推理次数',
  `avg_inference_time` int NULL DEFAULT NULL COMMENT '平均推理时间(ms)',
  `last_inference_time` datetime NULL DEFAULT NULL COMMENT '最后推理时间',

  -- 标准字段
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',

  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_model_id`(`model_id` ASC) USING BTREE,
  INDEX `idx_gateway_id`(`gateway_id` ASC) USING BTREE,
  INDEX `idx_deploy_status`(`deploy_status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘模型部署记录表';

-- ----------------------------
-- Table structure for iot_edge_rule_log
-- 边缘规则执行日志表
-- ----------------------------
DROP TABLE IF EXISTS `iot_edge_rule_log`;
CREATE TABLE `iot_edge_rule_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `gateway_id` bigint NOT NULL COMMENT '网关ID',

  -- 执行信息
  `execute_time` datetime NOT NULL COMMENT '执行时间',
  `execute_result` tinyint NOT NULL COMMENT '执行结果: 0=失败, 1=成功',
  `execute_duration` int NULL DEFAULT NULL COMMENT '执行耗时(ms)',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',

  -- 触发信息
  `trigger_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '触发数据(JSON)',
  `action_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行动作数据(JSON)',

  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE,
  INDEX `idx_gateway_id`(`gateway_id` ASC) USING BTREE,
  INDEX `idx_execute_time`(`execute_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 边缘规则执行日志表';
