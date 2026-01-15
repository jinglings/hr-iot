-- =====================================================
-- IoT 物模型模板库模块 - 数据库迁移脚本
-- 创建时间: 2026-01-11
-- =====================================================

-- ---------------------------------------------------
-- 表: iot_thing_model_template_category - 物模型模板分类表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `iot_thing_model_template_category` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(64) NOT NULL COMMENT '分类名称',
    `code` varchar(32) NOT NULL COMMENT '分类编码',
    `icon` varchar(64) DEFAULT '' COMMENT '分类图标',
    `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统分类: 0=否, 1=是',
    
    -- 标准字段
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_tenant` (`code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 物模型模板分类表';

-- ---------------------------------------------------
-- 表: iot_thing_model_template - 物模型模板表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `iot_thing_model_template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` varchar(64) NOT NULL COMMENT '模板名称',
    `code` varchar(32) NOT NULL COMMENT '模板编码',
    `category_id` bigint(20) NOT NULL COMMENT '分类ID',
    `description` varchar(500) DEFAULT '' COMMENT '模板描述',
    `icon` varchar(64) DEFAULT '' COMMENT '模板图标',
    `tsl` text NOT NULL COMMENT '物模型TSL定义(JSON)',
    `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统模板: 0=否, 1=是',
    `usage_count` int(11) NOT NULL DEFAULT 0 COMMENT '使用次数',
    `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    
    -- 标准字段
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_tenant` (`code`, `tenant_id`, `deleted`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 物模型模板表';

-- ---------------------------------------------------
-- 预置分类数据 (系统分类 tenant_id = 0)
-- ---------------------------------------------------
INSERT INTO `iot_thing_model_template_category` (`name`, `code`, `icon`, `sort`, `is_system`, `tenant_id`) VALUES
('传感器', 'sensor', 'ep:odometer', 1, 1, 0),
('执行器', 'actuator', 'ep:switch', 2, 1, 0),
('仪表', 'meter', 'ep:data-line', 3, 1, 0),
('工业设备', 'industrial', 'ep:cpu', 4, 1, 0),
('环境监测', 'environment', 'ep:sunny', 5, 1, 0);

-- ---------------------------------------------------
-- 预置模板数据
-- ---------------------------------------------------

-- 1. 温湿度传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('温湿度传感器', 'temperature_humidity_sensor', 1, '通用温湿度传感器，包含温度、湿度和电池电量属性', 'ep:cold-drink', 
'{"profile":{"version":"1.0"},"properties":[{"identifier":"temperature","name":"温度","accessMode":"r","required":true,"dataType":{"type":"float","specs":{"min":-40,"max":100,"step":0.1,"unit":"°C"}}},{"identifier":"humidity","name":"湿度","accessMode":"r","required":true,"dataType":{"type":"float","specs":{"min":0,"max":100,"step":0.1,"unit":"%RH"}}},{"identifier":"battery","name":"电池电量","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":100,"unit":"%"}}}],"events":[{"identifier":"high_temp_alert","name":"高温告警","type":"alert","outputData":[{"identifier":"temperature","name":"当前温度","dataType":{"type":"float"}},{"identifier":"threshold","name":"阈值","dataType":{"type":"float"}}]},{"identifier":"low_battery","name":"低电量告警","type":"alert","outputData":[{"identifier":"battery","name":"当前电量","dataType":{"type":"int"}}]}],"services":[]}',
1, 1, 0);

-- 2. 光照传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('光照传感器', 'light_sensor', 1, '光照强度传感器', 'ep:sunny',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"illuminance","name":"光照强度","accessMode":"r","required":true,"dataType":{"type":"int","specs":{"min":0,"max":100000,"unit":"lux"}}},{"identifier":"battery","name":"电池电量","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":100,"unit":"%"}}}],"events":[],"services":[]}',
1, 2, 0);

-- 3. 烟雾传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('烟雾传感器', 'smoke_sensor', 1, '烟雾探测传感器，可触发火警告警', 'ep:warning',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"smoke_detected","name":"烟雾检测","accessMode":"r","required":true,"dataType":{"type":"bool","specs":{"0":"正常","1":"检测到烟雾"}}},{"identifier":"smoke_density","name":"烟雾浓度","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":100,"unit":"%"}}},{"identifier":"battery","name":"电池电量","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":100,"unit":"%"}}}],"events":[{"identifier":"fire_alarm","name":"火警告警","type":"alert","outputData":[{"identifier":"smoke_density","name":"烟雾浓度","dataType":{"type":"float"}}]}],"services":[{"identifier":"test_alarm","name":"测试告警","callType":"async","inputData":[],"outputData":[]}]}',
1, 3, 0);

-- 4. 门磁传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('门磁传感器', 'door_sensor', 1, '门窗开关状态检测传感器', 'ep:lock',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"door_state","name":"门窗状态","accessMode":"r","required":true,"dataType":{"type":"bool","specs":{"0":"关闭","1":"打开"}}},{"identifier":"open_count","name":"开启次数","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":999999}}},{"identifier":"battery","name":"电池电量","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":100,"unit":"%"}}}],"events":[{"identifier":"door_opened","name":"门窗打开","type":"info","outputData":[{"identifier":"timestamp","name":"时间","dataType":{"type":"date"}}]},{"identifier":"door_closed","name":"门窗关闭","type":"info","outputData":[{"identifier":"timestamp","name":"时间","dataType":{"type":"date"}}]}],"services":[]}',
1, 4, 0);

-- 5. 智能开关
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('智能开关', 'smart_switch', 2, '单路智能开关，支持远程控制', 'ep:open',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"switch","name":"开关状态","accessMode":"rw","required":true,"dataType":{"type":"bool","specs":{"0":"关","1":"开"}}},{"identifier":"power","name":"功率","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":5000,"unit":"W"}}},{"identifier":"voltage","name":"电压","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":300,"unit":"V"}}},{"identifier":"current","name":"电流","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":30,"unit":"A"}}}],"events":[{"identifier":"overload","name":"过载告警","type":"alert","outputData":[{"identifier":"power","name":"当前功率","dataType":{"type":"float"}}]}],"services":[{"identifier":"toggle","name":"切换开关","callType":"async","inputData":[],"outputData":[{"identifier":"switch","name":"新状态","dataType":{"type":"bool"}}]}]}',
1, 1, 0);

-- 6. 调光灯
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('调光灯', 'dimmable_light', 2, '可调亮度的智能灯', 'ep:sunny',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"switch","name":"开关状态","accessMode":"rw","required":true,"dataType":{"type":"bool","specs":{"0":"关","1":"开"}}},{"identifier":"brightness","name":"亮度","accessMode":"rw","dataType":{"type":"int","specs":{"min":0,"max":100,"step":1,"unit":"%"}}},{"identifier":"color_temp","name":"色温","accessMode":"rw","dataType":{"type":"int","specs":{"min":2700,"max":6500,"unit":"K"}}}],"events":[],"services":[{"identifier":"fade","name":"渐变","callType":"async","inputData":[{"identifier":"target_brightness","name":"目标亮度","dataType":{"type":"int"}},{"identifier":"duration","name":"持续时间(秒)","dataType":{"type":"int"}}],"outputData":[]}]}',
1, 2, 0);

-- 7. 智能插座
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('智能插座', 'smart_plug', 2, '智能插座，支持电量统计', 'ep:connection',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"switch","name":"开关状态","accessMode":"rw","required":true,"dataType":{"type":"bool","specs":{"0":"关","1":"开"}}},{"identifier":"power","name":"实时功率","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":3500,"unit":"W"}}},{"identifier":"energy","name":"累计电量","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":999999,"unit":"kWh"}}},{"identifier":"voltage","name":"电压","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":300,"unit":"V"}}},{"identifier":"current","name":"电流","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":20,"unit":"A"}}}],"events":[{"identifier":"overload","name":"过载告警","type":"alert","outputData":[{"identifier":"power","name":"当前功率","dataType":{"type":"float"}}]}],"services":[{"identifier":"reset_energy","name":"电量清零","callType":"async","inputData":[],"outputData":[]}]}',
1, 3, 0);

-- 8. 单相电表
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('单相电表', 'single_phase_meter', 3, '单相智能电表', 'ep:data-analysis',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"total_energy","name":"总用电量","accessMode":"r","required":true,"dataType":{"type":"float","specs":{"min":0,"max":99999999,"unit":"kWh"}}},{"identifier":"positive_energy","name":"正向有功电量","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":99999999,"unit":"kWh"}}},{"identifier":"voltage","name":"电压","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":300,"unit":"V"}}},{"identifier":"current","name":"电流","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":100,"unit":"A"}}},{"identifier":"power","name":"有功功率","accessMode":"r","dataType":{"type":"float","specs":{"min":-50000,"max":50000,"unit":"W"}}},{"identifier":"power_factor","name":"功率因数","accessMode":"r","dataType":{"type":"float","specs":{"min":-1,"max":1}}}],"events":[],"services":[{"identifier":"read_meter","name":"抄表","callType":"sync","inputData":[],"outputData":[{"identifier":"total_energy","name":"总电量","dataType":{"type":"float"}}]}]}',
1, 1, 0);

-- 9. 水表
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('水表', 'water_meter', 3, '智能水表', 'ep:cold-drink',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"total_volume","name":"累计用水量","accessMode":"r","required":true,"dataType":{"type":"float","specs":{"min":0,"max":99999999,"unit":"m³"}}},{"identifier":"flow_rate","name":"瞬时流量","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":1000,"unit":"m³/h"}}},{"identifier":"battery","name":"电池电量","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":100,"unit":"%"}}},{"identifier":"valve_state","name":"阀门状态","accessMode":"r","dataType":{"type":"bool","specs":{"0":"关闭","1":"开启"}}}],"events":[{"identifier":"leak_alarm","name":"漏水告警","type":"alert","outputData":[{"identifier":"flow_rate","name":"异常流量","dataType":{"type":"float"}}]}],"services":[{"identifier":"read_meter","name":"抄表","callType":"sync","inputData":[],"outputData":[{"identifier":"total_volume","name":"累计用量","dataType":{"type":"float"}}]},{"identifier":"control_valve","name":"阀门控制","callType":"async","inputData":[{"identifier":"action","name":"动作","dataType":{"type":"bool"}}],"outputData":[]}]}',
1, 2, 0);

-- 10. 空调控制器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('空调控制器', 'ac_controller', 4, '中央空调控制器', 'ep:cold-drink',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"power","name":"开关","accessMode":"rw","required":true,"dataType":{"type":"bool","specs":{"0":"关","1":"开"}}},{"identifier":"mode","name":"模式","accessMode":"rw","dataType":{"type":"enum","specs":{"0":"制冷","1":"制热","2":"除湿","3":"送风","4":"自动"}}},{"identifier":"target_temp","name":"设定温度","accessMode":"rw","dataType":{"type":"float","specs":{"min":16,"max":30,"step":0.5,"unit":"°C"}}},{"identifier":"current_temp","name":"当前温度","accessMode":"r","dataType":{"type":"float","specs":{"min":-10,"max":50,"unit":"°C"}}},{"identifier":"fan_speed","name":"风速","accessMode":"rw","dataType":{"type":"enum","specs":{"0":"自动","1":"低","2":"中","3":"高"}}}],"events":[],"services":[]}',
1, 1, 0);

-- 11. PM2.5传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('PM2.5传感器', 'pm25_sensor', 5, 'PM2.5空气质量传感器', 'ep:wind-power',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"pm25","name":"PM2.5","accessMode":"r","required":true,"dataType":{"type":"int","specs":{"min":0,"max":1000,"unit":"μg/m³"}}},{"identifier":"pm10","name":"PM10","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":1000,"unit":"μg/m³"}}},{"identifier":"aqi","name":"空气质量指数","accessMode":"r","dataType":{"type":"int","specs":{"min":0,"max":500}}}],"events":[{"identifier":"air_quality_alert","name":"空气质量告警","type":"alert","outputData":[{"identifier":"pm25","name":"PM2.5","dataType":{"type":"int"}},{"identifier":"level","name":"污染等级","dataType":{"type":"text"}}]}],"services":[]}',
1, 1, 0);

-- 12. CO2传感器
INSERT INTO `iot_thing_model_template` (`name`, `code`, `category_id`, `description`, `icon`, `tsl`, `is_system`, `sort`, `tenant_id`) VALUES
('CO2传感器', 'co2_sensor', 5, '二氧化碳浓度传感器', 'ep:wind-power',
'{"profile":{"version":"1.0"},"properties":[{"identifier":"co2","name":"CO2浓度","accessMode":"r","required":true,"dataType":{"type":"int","specs":{"min":0,"max":5000,"unit":"ppm"}}},{"identifier":"temperature","name":"温度","accessMode":"r","dataType":{"type":"float","specs":{"min":-10,"max":60,"unit":"°C"}}},{"identifier":"humidity","name":"湿度","accessMode":"r","dataType":{"type":"float","specs":{"min":0,"max":100,"unit":"%RH"}}}],"events":[{"identifier":"co2_high_alert","name":"CO2超标告警","type":"alert","outputData":[{"identifier":"co2","name":"当前浓度","dataType":{"type":"int"}},{"identifier":"threshold","name":"阈值","dataType":{"type":"int"}}]}],"services":[]}',
1, 2, 0);
