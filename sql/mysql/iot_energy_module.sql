-- ----------------------------
-- 能源管理模块数据库脚本
-- Author: Claude
-- Date: 2025-11-16
-- Description: 创建能源管理模块所需的所有数据表
-- ----------------------------

-- ----------------------------
-- 1. 建筑表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_building` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '建筑ID',
  `building_name` varchar(100) NOT NULL COMMENT '建筑名称',
  `building_code` varchar(50) NOT NULL COMMENT '建筑编码',
  `building_type` varchar(20) DEFAULT NULL COMMENT '建筑类型(office:办公楼,factory:厂房,dormitory:宿舍,warehouse:仓库,other:其他)',
  `address` varchar(255) DEFAULT NULL COMMENT '建筑地址',
  `area` decimal(10,2) DEFAULT NULL COMMENT '建筑面积(平方米)',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `image_url` varchar(500) DEFAULT NULL COMMENT '建筑图片URL',
  `description` text COMMENT '建筑描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_code` (`building_code`,`tenant_id`,`deleted`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-建筑表';

-- ----------------------------
-- 2. 区域表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_area` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `area_name` varchar(100) NOT NULL COMMENT '区域名称',
  `area_code` varchar(50) NOT NULL COMMENT '区域编码',
  `building_id` bigint(20) NOT NULL COMMENT '所属建筑ID',
  `area_type` varchar(20) DEFAULT NULL COMMENT '区域类型(production:生产区,office:办公区,public:公共区,storage:仓储区,other:其他)',
  `area` decimal(10,2) DEFAULT NULL COMMENT '区域面积(平方米)',
  `description` text COMMENT '区域描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`,`tenant_id`,`deleted`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-区域表';

-- ----------------------------
-- 3. 楼层表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_floor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `floor_name` varchar(100) NOT NULL COMMENT '楼层名称',
  `floor_code` varchar(50) NOT NULL COMMENT '楼层编码',
  `building_id` bigint(20) NOT NULL COMMENT '所属建筑ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `floor_number` int(11) NOT NULL COMMENT '楼层序号(-2,-1,1,2,3...负数表示地下)',
  `area` decimal(10,2) DEFAULT NULL COMMENT '楼层面积(平方米)',
  `floor_plan_url` varchar(500) DEFAULT NULL COMMENT '楼层平面图URL',
  `description` text COMMENT '楼层描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_floor_code` (`floor_code`,`tenant_id`,`deleted`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-楼层表';

-- ----------------------------
-- 4. 房间表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  `room_name` varchar(100) NOT NULL COMMENT '房间名称',
  `room_code` varchar(50) NOT NULL COMMENT '房间编码',
  `building_id` bigint(20) NOT NULL COMMENT '所属建筑ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `floor_id` bigint(20) NOT NULL COMMENT '所属楼层ID',
  `room_type` varchar(20) DEFAULT NULL COMMENT '房间类型(meeting:会议室,office:办公室,server:机房,warehouse:仓库,workshop:车间,other:其他)',
  `room_usage` varchar(50) DEFAULT NULL COMMENT '房间用途',
  `area` decimal(10,2) DEFAULT NULL COMMENT '房间面积(平方米)',
  `position_x` int(11) DEFAULT NULL COMMENT '平面图X坐标',
  `position_y` int(11) DEFAULT NULL COMMENT '平面图Y坐标',
  `description` text COMMENT '房间描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`,`tenant_id`,`deleted`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-房间表';

-- ----------------------------
-- 5. 能源类型表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '能源类型ID',
  `energy_name` varchar(50) NOT NULL COMMENT '能源名称',
  `energy_code` varchar(30) NOT NULL COMMENT '能源编码',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级能源ID(0表示顶级)',
  `unit` varchar(20) NOT NULL COMMENT '计量单位(kWh,m³,GJ,kg等)',
  `coal_conversion_factor` decimal(10,4) DEFAULT NULL COMMENT '折标煤系数(kgce)',
  `carbon_emission_factor` decimal(10,4) DEFAULT NULL COMMENT '碳排放系数(kgCO₂)',
  `icon` varchar(100) DEFAULT NULL COMMENT '能源图标',
  `color` varchar(20) DEFAULT NULL COMMENT '图表颜色(十六进制)',
  `description` text COMMENT '能源描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_energy_code` (`energy_code`,`tenant_id`,`deleted`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-能源类型表';

-- ----------------------------
-- 6. 计量点表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_meter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '计量点ID',
  `meter_name` varchar(100) NOT NULL COMMENT '计量点名称',
  `meter_code` varchar(50) NOT NULL COMMENT '计量点编码',
  `energy_type_id` bigint(20) NOT NULL COMMENT '能源类型ID',
  `device_id` bigint(20) DEFAULT NULL COMMENT '关联设备ID',
  `device_property` varchar(100) DEFAULT NULL COMMENT '设备属性标识符(物模型identifier)',
  `building_id` bigint(20) DEFAULT NULL COMMENT '所属建筑ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '所属楼层ID',
  `room_id` bigint(20) DEFAULT NULL COMMENT '所属房间ID',
  `meter_level` tinyint(4) DEFAULT '1' COMMENT '计量点级别(1:一级表 2:二级表 3:三级表)',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级计量点ID(0表示顶级)',
  `ratio` decimal(10,4) DEFAULT '1.0000' COMMENT '计量倍率(CT/PT比)',
  `is_virtual` bit(1) DEFAULT b'0' COMMENT '是否虚拟表(通过其他表计算)',
  `calc_formula` varchar(500) DEFAULT NULL COMMENT '计算公式(虚拟表使用,如:M001+M002-M003)',
  `description` text COMMENT '计量点描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meter_code` (`meter_code`,`tenant_id`,`deleted`),
  KEY `idx_energy_type_id` (`energy_type_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-计量点表';

-- ----------------------------
-- 7. 能耗统计表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_time` datetime NOT NULL COMMENT '统计时间',
  `stat_period` varchar(10) NOT NULL COMMENT '统计周期(minute:分钟 hour:小时 day:日 month:月)',
  `meter_id` bigint(20) NOT NULL COMMENT '计量点ID',
  `energy_type_id` bigint(20) NOT NULL COMMENT '能源类型ID',
  `building_id` bigint(20) DEFAULT NULL COMMENT '建筑ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '区域ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '楼层ID',
  `room_id` bigint(20) DEFAULT NULL COMMENT '房间ID',
  `start_value` decimal(15,4) DEFAULT NULL COMMENT '起始值',
  `end_value` decimal(15,4) DEFAULT NULL COMMENT '结束值',
  `consumption` decimal(15,4) DEFAULT NULL COMMENT '能耗量(差值)',
  `max_value` decimal(15,4) DEFAULT NULL COMMENT '最大值',
  `min_value` decimal(15,4) DEFAULT NULL COMMENT '最小值',
  `avg_value` decimal(15,4) DEFAULT NULL COMMENT '平均值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat` (`meter_id`,`stat_time`,`stat_period`,`tenant_id`),
  KEY `idx_stat_time` (`stat_time`),
  KEY `idx_stat_period` (`stat_period`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_energy_type_id` (`energy_type_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-能耗统计表';

-- ----------------------------
-- 8. 告警规则表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_alert_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `alert_type` varchar(20) NOT NULL COMMENT '告警类型(threshold:能耗超标,mutation:能耗突变,offline:设备离线,abnormal:数据异常)',
  `object_type` varchar(20) NOT NULL COMMENT '监控对象类型(building:建筑,area:区域,floor:楼层,room:房间,meter:计量点)',
  `object_id` bigint(20) DEFAULT NULL COMMENT '监控对象ID',
  `energy_type_id` bigint(20) DEFAULT NULL COMMENT '能源类型ID',
  `threshold_config` text COMMENT '阈值配置(JSON格式)',
  `alert_level` varchar(10) DEFAULT 'warning' COMMENT '告警级别(info:提示,warning:警告,error:严重)',
  `alert_method` varchar(100) DEFAULT NULL COMMENT '告警方式(system:系统通知,sms:短信,email:邮件,dingtalk:钉钉,wechat:企业微信)',
  `receivers` varchar(500) DEFAULT NULL COMMENT '接收人(多个用逗号分隔)',
  `effective_time` varchar(100) DEFAULT NULL COMMENT '生效时间段(如:08:00-18:00)',
  `description` text COMMENT '规则描述',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`,`tenant_id`,`deleted`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_object_type` (`object_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-告警规则表';

-- ----------------------------
-- 9. 告警记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_alert_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `rule_id` bigint(20) NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `alert_type` varchar(20) NOT NULL COMMENT '告警类型',
  `alert_level` varchar(10) DEFAULT 'warning' COMMENT '告警级别',
  `object_type` varchar(20) NOT NULL COMMENT '告警对象类型',
  `object_id` bigint(20) DEFAULT NULL COMMENT '告警对象ID',
  `object_name` varchar(100) DEFAULT NULL COMMENT '告警对象名称',
  `alert_content` text COMMENT '告警内容',
  `alert_value` decimal(15,4) DEFAULT NULL COMMENT '告警值',
  `threshold_value` decimal(15,4) DEFAULT NULL COMMENT '阈值',
  `alert_time` datetime NOT NULL COMMENT '告警时间',
  `alert_status` varchar(20) DEFAULT 'pending' COMMENT '处理状态(pending:待处理,processing:处理中,resolved:已处理,ignored:已忽略)',
  `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` text COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_alert_time` (`alert_time`),
  KEY `idx_alert_status` (`alert_status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-告警记录表';

-- ----------------------------
-- 10. 报表定时任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_energy_report_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `report_type` varchar(20) NOT NULL COMMENT '报表类型(daily:日报,weekly:周报,monthly:月报,yearly:年报,custom:自定义)',
  `report_config` text COMMENT '报表配置(JSON格式)',
  `schedule_type` varchar(20) NOT NULL COMMENT '执行周期(daily:每日,weekly:每周,monthly:每月)',
  `schedule_time` varchar(50) DEFAULT NULL COMMENT '执行时间(如:09:00)',
  `receivers` varchar(500) DEFAULT NULL COMMENT '接收人邮箱(多个用逗号分隔)',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_type` (`schedule_type`),
  KEY `idx_next_execute_time` (`next_execute_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源管理-报表定时任务表';

-- ----------------------------
-- 初始化能源类型数据
-- ----------------------------
INSERT INTO `iot_energy_type` (`id`, `energy_name`, `energy_code`, `parent_id`, `unit`, `coal_conversion_factor`, `carbon_emission_factor`, `icon`, `color`, `description`, `sort`, `status`, `creator`, `tenant_id`) VALUES
(1, '电能', 'ELECTRIC', 0, 'kWh', 0.1229, 0.7850, 'electric', '#1890ff', '电力能源', 1, 1, 'system', 0),
(2, '总用电', 'ELECTRIC_TOTAL', 1, 'kWh', 0.1229, 0.7850, 'electric', '#1890ff', '总用电量', 1, 1, 'system', 0),
(3, '照明用电', 'ELECTRIC_LIGHTING', 1, 'kWh', 0.1229, 0.7850, 'electric', '#52c41a', '照明用电', 2, 1, 'system', 0),
(4, '空调用电', 'ELECTRIC_HVAC', 1, 'kWh', 0.1229, 0.7850, 'electric', '#faad14', '空调用电', 3, 1, 'system', 0),
(5, '动力用电', 'ELECTRIC_POWER', 1, 'kWh', 0.1229, 0.7850, 'electric', '#f5222d', '动力设备用电', 4, 1, 'system', 0),
(6, '特殊用电', 'ELECTRIC_SPECIAL', 1, 'kWh', 0.1229, 0.7850, 'electric', '#722ed1', '特殊用电', 5, 1, 'system', 0),
(7, '水能', 'WATER', 0, 'm³', 0.0857, 0.9100, 'water', '#13c2c2', '水资源', 2, 1, 'system', 0),
(8, '自来水', 'WATER_TAP', 7, 'm³', 0.0857, 0.9100, 'water', '#13c2c2', '自来水', 1, 1, 'system', 0),
(9, '中水', 'WATER_RECLAIMED', 7, 'm³', 0.0857, 0.9100, 'water', '#36cfc9', '中水回用', 2, 1, 'system', 0),
(10, '热水', 'WATER_HOT', 7, 'm³', 0.0857, 0.9100, 'water', '#ff7a45', '热水', 3, 1, 'system', 0),
(11, '燃气', 'GAS', 0, 'm³', 1.3300, 2.1600, 'gas', '#fa8c16', '燃气能源', 3, 1, 'system', 0),
(12, '天然气', 'GAS_NATURAL', 11, 'm³', 1.3300, 2.1600, 'gas', '#fa8c16', '天然气', 1, 1, 'system', 0),
(13, '液化气', 'GAS_LPG', 11, 'kg', 1.7140, 3.1010, 'gas', '#ff9c6e', '液化石油气', 2, 1, 'system', 0),
(14, '热能', 'HEAT', 0, 'GJ', 34.1200, 110.0000, 'heat', '#ff4d4f', '热能', 4, 1, 'system', 0),
(15, '蒸汽', 'HEAT_STEAM', 14, 'GJ', 34.1200, 110.0000, 'heat', '#ff4d4f', '蒸汽', 1, 1, 'system', 0),
(16, '热水', 'HEAT_WATER', 14, 'GJ', 34.1200, 110.0000, 'heat', '#ff7875', '热水', 2, 1, 'system', 0),
(17, '冷能', 'COOLING', 0, 'GJ', 34.1200, 0.0000, 'cooling', '#096dd9', '冷能', 5, 1, 'system', 0),
(18, '冷冻水', 'COOLING_WATER', 17, 'GJ', 34.1200, 0.0000, 'cooling', '#096dd9', '冷冻水', 1, 1, 'system', 0);

-- ----------------------------
-- 初始化示例建筑数据
-- ----------------------------
INSERT INTO `iot_energy_building` (`id`, `building_name`, `building_code`, `building_type`, `address`, `area`, `description`, `sort`, `status`, `creator`, `tenant_id`) VALUES
(1, '1号办公楼', 'BUILDING_001', 'office', '园区东侧', 5000.00, '主办公楼', 1, 1, 'admin', 0),
(2, '2号厂房', 'BUILDING_002', 'factory', '园区西侧', 8000.00, '生产车间', 2, 1, 'admin', 0),
(3, '3号宿舍楼', 'BUILDING_003', 'dormitory', '园区南侧', 3000.00, '员工宿舍', 3, 1, 'admin', 0);

-- ----------------------------
-- TDengine时序数据库初始化脚本
-- 注意：需要单独在TDengine中执行
-- ----------------------------
-- CREATE DATABASE IF NOT EXISTS energy_db;
-- USE energy_db;
--
-- CREATE STABLE IF NOT EXISTS energy_realtime_data (
--   ts TIMESTAMP,
--   meter_id BIGINT,
--   instant_power DOUBLE,
--   cumulative_value DOUBLE,
--   voltage DOUBLE,
--   current DOUBLE,
--   power_factor DOUBLE,
--   data_quality TINYINT
-- ) TAGS (
--   energy_type_id BIGINT,
--   building_id BIGINT,
--   area_id BIGINT,
--   floor_id BIGINT,
--   room_id BIGINT,
--   tenant_id BIGINT
-- );
