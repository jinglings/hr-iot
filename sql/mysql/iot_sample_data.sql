-- =============================================
-- IoT 模块示例数据
-- 包含产品、设备、物模型等完整示例数据
-- =============================================

SET NAMES utf8mb4;

-- =============================================
-- 1. 产品分类示例数据
-- =============================================

-- 清理已有分类数据(可选)
-- DELETE FROM iot_product_category WHERE id >= 1;

-- 插入产品分类
INSERT INTO `iot_product_category` (`id`, `name`, `sort`, `status`, `description`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, '环境监测传感器', 1, 0, '用于监测环境参数的各类传感器', 'admin', NOW(), 'admin', NOW(), 0),
(2, '智能家居设备', 2, 0, '家庭自动化和智能控制设备', 'admin', NOW(), 'admin', NOW(), 0),
(3, '工业设备', 3, 0, '工业生产和监控设备', 'admin', NOW(), 'admin', NOW(), 0),
(4, '能源管理设备', 4, 0, '能源监测和管理设备', 'admin', NOW(), 'admin', NOW(), 0),
(5, '智慧农业设备', 5, 0, '农业监测和控制设备', 'admin', NOW(), 'admin', NOW(), 0);

-- =============================================
-- 2. 产品示例数据
-- =============================================

-- 清理已有产品数据(可选)
-- DELETE FROM iot_product WHERE id >= 1001;

-- 插入产品
INSERT INTO `iot_product` (`id`, `name`, `product_key`, `category_id`, `icon`, `pic_url`, `description`, `status`, `device_type`, `net_type`, `location_type`, `codec_type`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(1001, '智能温湿度传感器', 'a1TempHumi001', 1, NULL, NULL, '高精度温湿度传感器,支持实时监测环境温湿度', 1, 1, 1, 3, 'alink', 'admin', NOW(), 'admin', NOW(), 0, 1),
(1002, '智能LED灯泡', 'a1SmartLight02', 2, NULL, NULL, '支持调光调色的智能灯泡,可远程控制', 1, 1, 1, 3, 'alink', 'admin', NOW(), 'admin', NOW(), 0, 1),
(1003, '智能电表', 'a1PowerMeter03', 4, NULL, NULL, '三相智能电表,支持电能计量和监测', 1, 1, 3, 3, 'modbus', 'admin', NOW(), 'admin', NOW(), 0, 1),
(1004, '工业网关', 'a1Gateway0004', 3, NULL, NULL, '工业级物联网网关,支持多种协议转换', 1, 2, 3, 3, 'alink', 'admin', NOW(), 'admin', NOW(), 0, 1),
(1005, '土壤监测站', 'a1SoilMonit05', 5, NULL, NULL, '多参数土壤环境监测站,支持土壤温湿度、PH、EC等参数监测', 1, 1, 5, 1, 'alink', 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =============================================
-- 3. 物模型示例数据 - 智能温湿度传感器
-- =============================================

-- 清理已有物模型数据(可选)
-- DELETE FROM iot_thing_model WHERE product_id = 1001;

-- 温湿度传感器 - 属性: 温度
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2001, 'temperature', '温度', '环境温度', 1001, 'a1TempHumi001', 1,
'{"identifier":"temperature","name":"温度","accessMode":"r","dataType":"float","dataSpecs":{"min":-40,"max":100,"step":0.1,"unit":"℃","unitName":"摄氏度"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 属性: 湿度
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2002, 'humidity', '湿度', '环境湿度', 1001, 'a1TempHumi001', 1,
'{"identifier":"humidity","name":"湿度","accessMode":"r","dataType":"float","dataSpecs":{"min":0,"max":100,"step":0.1,"unit":"%RH","unitName":"相对湿度"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 属性: 电池电量
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2003, 'battery_level', '电池电量', '设备电池电量百分比', 1001, 'a1TempHumi001', 1,
'{"identifier":"battery_level","name":"电池电量","accessMode":"r","dataType":"int","dataSpecs":{"min":0,"max":100,"step":1,"unit":"%","unitName":"百分比"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 属性: 采样间隔
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2004, 'sampling_interval', '采样间隔', '数据采样间隔时间', 1001, 'a1TempHumi001', 1,
'{"identifier":"sampling_interval","name":"采样间隔","accessMode":"rw","dataType":"int","dataSpecs":{"min":1,"max":3600,"step":1,"unit":"s","unitName":"秒"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 事件: 高温告警
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2005, 'high_temp_alert', '高温告警', '温度超过阈值时触发告警', 1001, 'a1TempHumi001', 3,
NULL,
'{"identifier":"high_temp_alert","name":"高温告警","type":"alert","outputParams":[{"identifier":"current_temp","name":"当前温度","dataType":"float"},{"identifier":"threshold","name":"阈值温度","dataType":"float"},{"identifier":"location","name":"设备位置","dataType":"text"}]}',
NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 事件: 低电量告警
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2006, 'low_battery_alert', '低电量告警', '电池电量低于20%时触发告警', 1001, 'a1TempHumi001', 3,
NULL,
'{"identifier":"low_battery_alert","name":"低电量告警","type":"alert","outputParams":[{"identifier":"current_level","name":"当前电量","dataType":"int"},{"identifier":"threshold","name":"阈值","dataType":"int"}]}',
NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 服务: 校准传感器
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2007, 'calibrate', '校准传感器', '校准温湿度传感器', 1001, 'a1TempHumi001', 2,
NULL, NULL,
'{"identifier":"calibrate","name":"校准传感器","callType":"sync","inputParams":[{"identifier":"temp_offset","name":"温度偏移量","dataType":"float"},{"identifier":"humi_offset","name":"湿度偏移量","dataType":"float"}],"outputParams":[{"identifier":"result","name":"校准结果","dataType":"bool"}]}',
'admin', NOW(), 'admin', NOW(), 0);

-- 温湿度传感器 - 服务: 重启设备
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2008, 'reboot', '重启设备', '重启温湿度传感器', 1001, 'a1TempHumi001', 2,
NULL, NULL,
'{"identifier":"reboot","name":"重启设备","callType":"async","inputParams":[{"identifier":"delay_seconds","name":"延迟秒数","dataType":"int"}],"outputParams":[]}',
'admin', NOW(), 'admin', NOW(), 0);

-- =============================================
-- 4. 物模型示例数据 - 智能LED灯泡
-- =============================================

-- 智能LED灯泡 - 属性: 电源开关
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(3001, 'power_switch', '电源开关', '灯泡电源开关', 1002, 'a1SmartLight02', 1,
'{"identifier":"power_switch","name":"电源开关","accessMode":"rw","dataType":"bool","dataSpecs":{"0":"关闭","1":"开启"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能LED灯泡 - 属性: 亮度
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(3002, 'brightness', '亮度', '灯泡亮度', 1002, 'a1SmartLight02', 1,
'{"identifier":"brightness","name":"亮度","accessMode":"rw","dataType":"int","dataSpecs":{"min":0,"max":100,"step":1,"unit":"%","unitName":"百分比"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能LED灯泡 - 属性: 色温
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(3003, 'color_temp', '色温', '灯泡色温', 1002, 'a1SmartLight02', 1,
'{"identifier":"color_temp","name":"色温","accessMode":"rw","dataType":"int","dataSpecs":{"min":2700,"max":6500,"step":100,"unit":"K","unitName":"开尔文"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能LED灯泡 - 属性: RGB颜色
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(3004, 'rgb_color', 'RGB颜色', '灯泡RGB颜色', 1002, 'a1SmartLight02', 1,
'{"identifier":"rgb_color","name":"RGB颜色","accessMode":"rw","dataType":"struct","dataSpecs":{"specs":[{"identifier":"r","name":"红色","dataType":"int","min":0,"max":255},{"identifier":"g","name":"绿色","dataType":"int","min":0,"max":255},{"identifier":"b","name":"蓝色","dataType":"int","min":0,"max":255}]}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能LED灯泡 - 服务: 设置场景
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(3005, 'set_scene', '设置场景', '设置灯光场景模式', 1002, 'a1SmartLight02', 2,
NULL, NULL,
'{"identifier":"set_scene","name":"设置场景","callType":"async","inputParams":[{"identifier":"scene_mode","name":"场景模式","dataType":"enum","dataSpecs":{"0":"阅读模式","1":"观影模式","2":"休息模式","3":"派对模式"}}],"outputParams":[]}',
'admin', NOW(), 'admin', NOW(), 0);

-- =============================================
-- 5. 物模型示例数据 - 智能电表
-- =============================================

-- 智能电表 - 属性: 电压
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(4001, 'voltage', '电压', '当前电压', 1003, 'a1PowerMeter03', 1,
'{"identifier":"voltage","name":"电压","accessMode":"r","dataType":"float","dataSpecs":{"min":0,"max":500,"step":0.1,"unit":"V","unitName":"伏特"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能电表 - 属性: 电流
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(4002, 'current', '电流', '当前电流', 1003, 'a1PowerMeter03', 1,
'{"identifier":"current","name":"电流","accessMode":"r","dataType":"float","dataSpecs":{"min":0,"max":100,"step":0.01,"unit":"A","unitName":"安培"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能电表 - 属性: 功率
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(4003, 'power', '功率', '当前功率', 1003, 'a1PowerMeter03', 1,
'{"identifier":"power","name":"功率","accessMode":"r","dataType":"float","dataSpecs":{"min":0,"max":50000,"step":0.1,"unit":"W","unitName":"瓦特"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能电表 - 属性: 电能
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(4004, 'energy', '电能', '累计电能', 1003, 'a1PowerMeter03', 1,
'{"identifier":"energy","name":"电能","accessMode":"r","dataType":"double","dataSpecs":{"min":0,"max":999999999,"step":0.01,"unit":"kWh","unitName":"千瓦时"}}',
NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- 智能电表 - 事件: 过载告警
INSERT INTO `iot_thing_model` (`id`, `identifier`, `name`, `description`, `product_id`, `product_key`, `type`, `property`, `event`, `service`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(4005, 'overload_alert', '过载告警', '功率超过额定值时触发', 1003, 'a1PowerMeter03', 3,
NULL,
'{"identifier":"overload_alert","name":"过载告警","type":"alert","outputParams":[{"identifier":"current_power","name":"当前功率","dataType":"float"},{"identifier":"rated_power","name":"额定功率","dataType":"float"}]}',
NULL, 'admin', NOW(), 'admin', NOW(), 0);

-- =============================================
-- 6. 设备分组示例数据
-- =============================================

-- 清理已有分组数据(可选)
-- DELETE FROM iot_device_group WHERE id >= 1;

INSERT INTO `iot_device_group` (`id`, `name`, `status`, `description`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, '一号厂房', 0, '一号厂房所有设备', 'admin', NOW(), 'admin', NOW(), 0),
(2, '二号厂房', 0, '二号厂房所有设备', 'admin', NOW(), 'admin', NOW(), 0),
(3, '办公区域', 0, '办公区域设备', 'admin', NOW(), 'admin', NOW(), 0),
(4, '仓储区域', 0, '仓储区域设备', 'admin', NOW(), 'admin', NOW(), 0),
(5, '测试设备组', 0, '用于测试的设备分组', 'admin', NOW(), 'admin', NOW(), 0);

-- =============================================
-- 7. 设备示例数据
-- =============================================

-- 清理已有设备数据(可选)
-- DELETE FROM iot_device WHERE id >= 10001;

-- 温湿度传感器设备
INSERT INTO `iot_device` (`id`, `device_name`, `nickname`, `serial_number`, `pic_url`, `group_ids`, `product_id`, `product_key`, `device_type`, `gateway_id`, `state`, `online_time`, `offline_time`, `active_time`, `ip`, `firmware_id`, `device_secret`, `auth_type`, `location_type`, `latitude`, `longitude`, `area_id`, `address`, `config`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(10001, 'temp_sensor_001', '办公室1温湿度传感器', 'SN2024010001', NULL, '[3]', 1001, 'a1TempHumi001', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.101', NULL, 'a1b2c3d4e5f6g7h8i9j0', 'device_secret', 3, 39.904200, 116.407400, NULL, '北京市朝阳区xxx大厦5层办公室1', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10002, 'temp_sensor_002', '办公室2温湿度传感器', 'SN2024010002', NULL, '[3]', 1001, 'a1TempHumi001', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.102', NULL, 'k1l2m3n4o5p6q7r8s9t0', 'device_secret', 3, 39.904300, 116.407500, NULL, '北京市朝阳区xxx大厦5层办公室2', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10003, 'temp_sensor_003', '车间1温湿度传感器', 'SN2024010003', NULL, '[1]', 1001, 'a1TempHumi001', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.103', NULL, 'u1v2w3x4y5z6a7b8c9d0', 'device_secret', 3, 39.905000, 116.408000, NULL, '北京市朝阳区xxx工业园一号厂房', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10004, 'temp_sensor_004', '车间2温湿度传感器', 'SN2024010004', NULL, '[2]', 1001, 'a1TempHumi001', 1, NULL, 2, NOW(), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), '192.168.1.104', NULL, 'e1f2g3h4i5j6k7l8m9n0', 'device_secret', 3, 39.905100, 116.408100, NULL, '北京市朝阳区xxx工业园二号厂房', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10005, 'temp_sensor_005', '仓库温湿度传感器', 'SN2024010005', NULL, '[4]', 1001, 'a1TempHumi001', 1, NULL, 0, NULL, NULL, NULL, NULL, NULL, 'o1p2q3r4s5t6u7v8w9x0', 'device_secret', 3, 39.906000, 116.409000, NULL, '北京市朝阳区xxx仓储中心', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 智能LED灯泡设备
INSERT INTO `iot_device` (`id`, `device_name`, `nickname`, `serial_number`, `pic_url`, `group_ids`, `product_id`, `product_key`, `device_type`, `gateway_id`, `state`, `online_time`, `offline_time`, `active_time`, `ip`, `firmware_id`, `device_secret`, `auth_type`, `location_type`, `latitude`, `longitude`, `area_id`, `address`, `config`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(10006, 'light_001', '办公室1主灯', 'SN2024020001', NULL, '[3]', 1002, 'a1SmartLight02', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.201', NULL, 'y1z2a3b4c5d6e7f8g9h0', 'device_secret', 3, 39.904200, 116.407400, NULL, '北京市朝阳区xxx大厦5层办公室1', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10007, 'light_002', '办公室2主灯', 'SN2024020002', NULL, '[3]', 1002, 'a1SmartLight02', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.202', NULL, 'i1j2k3l4m5n6o7p8q9r0', 'device_secret', 3, 39.904300, 116.407500, NULL, '北京市朝阳区xxx大厦5层办公室2', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(10008, 'light_003', '会议室主灯', 'SN2024020003', NULL, '[3]', 1002, 'a1SmartLight02', 1, NULL, 2, NOW(), DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), '192.168.1.203', NULL, 's1t2u3v4w5x6y7z8a9b0', 'device_secret', 3, 39.904400, 116.407600, NULL, '北京市朝阳区xxx大厦5层会议室', NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 智能电表设备
INSERT INTO `iot_device` (`id`, `device_name`, `nickname`, `serial_number`, `pic_url`, `group_ids`, `product_id`, `product_key`, `device_type`, `gateway_id`, `state`, `online_time`, `offline_time`, `active_time`, `ip`, `firmware_id`, `device_secret`, `auth_type`, `location_type`, `latitude`, `longitude`, `area_id`, `address`, `config`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(10009, 'meter_001', '一号厂房总电表', 'SN2024030001', NULL, '[1]', 1003, 'a1PowerMeter03', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.301', NULL, 'c1d2e3f4g5h6i7j8k9l0', 'device_secret', 3, 39.905000, 116.408000, NULL, '北京市朝阳区xxx工业园一号厂房配电室', '{"sampling_interval":60}', 'admin', NOW(), 'admin', NOW(), 0, 1),
(10010, 'meter_002', '二号厂房总电表', 'SN2024030002', NULL, '[2]', 1003, 'a1PowerMeter03', 1, NULL, 1, NOW(), NULL, NOW(), '192.168.1.302', NULL, 'm1n2o3p4q5r6s7t8u9v0', 'device_secret', 3, 39.905100, 116.408100, NULL, '北京市朝阳区xxx工业园二号厂房配电室', '{"sampling_interval":60}', 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =============================================
-- 8. 设备分组关联说明
-- =============================================

-- 注意: 设备的 group_ids 字段存储的是 JSON 数组格式
-- 例如: [1,3] 表示设备同时属于分组1和分组3
-- 查询某个分组下的所有设备可以使用:
-- SELECT * FROM iot_device WHERE JSON_CONTAINS(group_ids, '1');

-- =============================================
-- 9. 数据验证查询
-- =============================================

-- 查看产品统计
SELECT
    p.id,
    p.name AS product_name,
    p.product_key,
    p.status,
    pc.name AS category_name,
    COUNT(d.id) AS device_count
FROM iot_product p
LEFT JOIN iot_product_category pc ON p.category_id = pc.id
LEFT JOIN iot_device d ON p.id = d.product_id AND d.deleted = 0
WHERE p.deleted = 0
GROUP BY p.id, p.name, p.product_key, p.status, pc.name
ORDER BY p.id;

-- 查看设备状态统计
SELECT
    p.name AS product_name,
    d.state,
    CASE d.state
        WHEN 0 THEN '未激活'
        WHEN 1 THEN '在线'
        WHEN 2 THEN '离线'
        ELSE '未知'
    END AS state_name,
    COUNT(*) AS device_count
FROM iot_device d
LEFT JOIN iot_product p ON d.product_id = p.id
WHERE d.deleted = 0
GROUP BY p.name, d.state
ORDER BY p.name, d.state;

-- 查看物模型统计
SELECT
    p.name AS product_name,
    tm.type,
    CASE tm.type
        WHEN 1 THEN '属性'
        WHEN 2 THEN '服务'
        WHEN 3 THEN '事件'
        ELSE '未知'
    END AS type_name,
    COUNT(*) AS model_count
FROM iot_thing_model tm
LEFT JOIN iot_product p ON tm.product_id = p.id
WHERE tm.deleted = 0
GROUP BY p.name, tm.type
ORDER BY p.name, tm.type;

-- 查看设备分组统计
SELECT
    dg.id,
    dg.name AS group_name,
    COUNT(d.id) AS device_count
FROM iot_device_group dg
LEFT JOIN iot_device d ON JSON_CONTAINS(d.group_ids, CAST(dg.id AS CHAR))
WHERE dg.deleted = 0
GROUP BY dg.id, dg.name
ORDER BY dg.id;

-- =============================================
-- 10. 使用说明
-- =============================================

/*
本示例数据包含:

1. 5个产品分类
2. 5个产品:
   - 智能温湿度传感器 (已发布)
   - 智能LED灯泡 (已发布)
   - 智能电表 (已发布)
   - 工业网关 (已发布)
   - 土壤监测站 (已发布)

3. 完整的物模型定义(属性、事件、服务):
   - 温湿度传感器: 4个属性 + 2个事件 + 2个服务
   - 智能LED灯泡: 4个属性 + 1个服务
   - 智能电表: 4个属性 + 1个事件

4. 5个设备分组
5. 10个设备实例:
   - 5个温湿度传感器(3个在线,1个离线,1个未激活)
   - 3个智能LED灯泡(2个在线,1个离线)
   - 2个智能电表(2个在线)

导入说明:
1. 确保已执行 iot_tables.sql 创建表结构
2. 直接执行本文件导入示例数据
3. 使用文件末尾的查询语句验证数据

清理数据:
如需清理示例数据,请按以下顺序执行:
DELETE FROM iot_device WHERE id >= 10001;
DELETE FROM iot_thing_model WHERE id >= 2001;
DELETE FROM iot_product WHERE id >= 1001;
DELETE FROM iot_device_group WHERE id >= 1;
DELETE FROM iot_product_category WHERE id >= 1;

注意:
- 所有设备的 device_secret 都是示例数据,实际使用时应该是随机生成的密钥
- 设备的 IP 地址、经纬度等数据都是示例数据
- tenant_id 默认为 1,根据实际多租户场景调整
*/
