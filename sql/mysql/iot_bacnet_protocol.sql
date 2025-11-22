-- ----------------------------
-- BACnet协议相关表
-- yudao-module-iot-biz
-- ----------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for iot_bacnet_device_config
-- BACnet设备配置表
-- ----------------------------
DROP TABLE IF EXISTS `iot_bacnet_device_config`;
CREATE TABLE `iot_bacnet_device_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置编号',
  `device_id` bigint NOT NULL COMMENT 'IoT设备编号，关联 iot_device.id',
  `instance_number` int NOT NULL COMMENT 'BACnet设备实例号',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'BACnet设备IP地址',
  `port` int NULL DEFAULT 47808 COMMENT 'BACnet端口，默认47808',
  `network_number` int NULL DEFAULT NULL COMMENT '网络号（用于路由）',
  `mac_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MAC地址',
  `max_apdu_length` int NULL DEFAULT 1476 COMMENT '最大APDU长度',
  `segmentation_supported` tinyint NULL DEFAULT 3 COMMENT '分段支持（0=不支持,1=发送,2=接收,3=发送和接收）',
  `vendor_id` int NULL DEFAULT NULL COMMENT '供应商ID',
  `polling_enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用轮询',
  `polling_interval` int NULL DEFAULT 5000 COMMENT '轮询间隔（毫秒）',
  `read_timeout` int NULL DEFAULT 5000 COMMENT '读取超时（毫秒）',
  `retry_count` int NULL DEFAULT 3 COMMENT '重试次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '配置状态（0=禁用,1=启用）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_id`(`device_id` ASC, `deleted` ASC) USING BTREE,
  UNIQUE INDEX `uk_instance_number`(`instance_number` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_ip_address`(`ip_address` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'BACnet设备配置表';

-- ----------------------------
-- Table structure for iot_bacnet_property_mapping
-- BACnet属性映射配置表
-- ----------------------------
DROP TABLE IF EXISTS `iot_bacnet_property_mapping`;
CREATE TABLE `iot_bacnet_property_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '映射编号',
  `device_config_id` bigint NOT NULL COMMENT 'BACnet设备配置编号，关联 iot_bacnet_device_config.id',
  `device_id` bigint NOT NULL COMMENT 'IoT设备编号，关联 iot_device.id',
  `thing_model_id` bigint NOT NULL COMMENT '物模型功能编号，关联 iot_thing_model.id',
  `identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物模型属性标识符',

  -- BACnet对象信息
  `object_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BACnet对象类型（如ANALOG_INPUT,BINARY_OUTPUT）',
  `object_instance` int NOT NULL COMMENT 'BACnet对象实例号',
  `property_identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRESENT_VALUE' COMMENT 'BACnet属性标识符（默认PRESENT_VALUE）',
  `property_array_index` int NULL DEFAULT NULL COMMENT '属性数组索引（可选）',

  -- 数据转换配置
  `data_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型（int,float,double,bool,text）',
  `unit_conversion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位转换公式（如: value * 0.1）',
  `value_mapping` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '值映射（JSON格式，用于枚举类型）',

  -- 访问控制
  `access_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'r' COMMENT '访问模式（r=只读,w=只写,rw=读写）',
  `priority` int NULL DEFAULT NULL COMMENT '写入优先级（1-16,用于可写属性）',

  -- 轮询配置
  `polling_enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用轮询',
  `cov_enabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用COV订阅（Change of Value）',

  `status` tinyint NOT NULL DEFAULT 1 COMMENT '映射状态（0=禁用,1=启用）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_object_property`(`device_id` ASC, `object_type` ASC, `object_instance` ASC, `property_identifier` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_device_config_id`(`device_config_id` ASC) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_thing_model_id`(`thing_model_id` ASC) USING BTREE,
  INDEX `idx_identifier`(`identifier` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'BACnet属性映射配置表';

-- ----------------------------
-- Table structure for iot_bacnet_discovery_record
-- BACnet设备发现记录表
-- ----------------------------
DROP TABLE IF EXISTS `iot_bacnet_discovery_record`;
CREATE TABLE `iot_bacnet_discovery_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录编号',
  `instance_number` int NOT NULL COMMENT 'BACnet设备实例号',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备名称',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `port` int NULL DEFAULT 47808 COMMENT '端口',
  `vendor_id` int NULL DEFAULT NULL COMMENT '供应商ID',
  `vendor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备型号',
  `firmware_revision` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '固件版本',
  `application_software_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用软件版本',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备位置',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备描述',
  `object_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '对象列表（JSON格式）',
  `is_bound` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已绑定到IoT设备',
  `bound_device_id` bigint NULL DEFAULT NULL COMMENT '绑定的IoT设备编号',
  `discovery_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  `last_seen_time` datetime NULL DEFAULT NULL COMMENT '最后发现时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0=离线,1=在线）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_instance_number`(`instance_number` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_is_bound`(`is_bound` ASC) USING BTREE,
  INDEX `idx_bound_device_id`(`bound_device_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'BACnet设备发现记录表';

-- ----------------------------
-- 示例数据（可选）
-- ----------------------------

-- BACnet设备配置示例
-- INSERT INTO `iot_bacnet_device_config` (`device_id`, `instance_number`, `ip_address`, `port`, `polling_interval`, `status`, `tenant_id`)
-- VALUES (1001, 8001, '192.168.1.100', 47808, 5000, 1, 1);

-- BACnet属性映射示例（温度传感器）
-- INSERT INTO `iot_bacnet_property_mapping` (`device_config_id`, `device_id`, `thing_model_id`, `identifier`, `object_type`, `object_instance`, `property_identifier`, `data_type`, `access_mode`, `status`, `tenant_id`)
-- VALUES (1, 1001, 1, 'temperature', 'ANALOG_INPUT', 0, 'PRESENT_VALUE', 'float', 'r', 1, 1);

-- BACnet属性映射示例（开关控制）
-- INSERT INTO `iot_bacnet_property_mapping` (`device_config_id`, `device_id`, `thing_model_id`, `identifier`, `object_type`, `object_instance`, `property_identifier`, `data_type`, `access_mode`, `priority`, `status`, `tenant_id`)
-- VALUES (1, 1001, 2, 'switch_status', 'BINARY_OUTPUT', 1, 'PRESENT_VALUE', 'bool', 'rw', 8, 1, 1);
