-- ----------------------------
-- BACnet测试数据 - 一号厂房总电表
-- 设备ID: 10009
-- 产品: a1PowerMeter03 (电表)
-- 物模型: energy (电能)
-- ----------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- 1. BACnet设备配置
-- ----------------------------
INSERT INTO `iot_bacnet_device_config` (
  `id`,
  `device_id`,
  `instance_number`,
  `ip_address`,
  `port`,
  `network_number`,
  `mac_address`,
  `max_apdu_length`,
  `segmentation_supported`,
  `vendor_id`,
  `polling_enabled`,
  `polling_interval`,
  `read_timeout`,
  `retry_count`,
  `status`,
  `creator`,
  `create_time`,
  `updater`,
  `update_time`,
  `deleted`,
  `tenant_id`
) VALUES (
  1001,                          -- id
  10009,                         -- device_id: 对应 meter_001 设备
  300001,                        -- instance_number: BACnet设备实例号
  '192.168.1.301',               -- ip_address: 与设备IP相同
  47808,                         -- port: BACnet标准端口
  NULL,                          -- network_number: 无需路由
  NULL,                          -- mac_address: 可选
  1476,                          -- max_apdu_length: 默认最大APDU长度
  3,                             -- segmentation_supported: 支持发送和接收分段
  120,                           -- vendor_id: 示例供应商ID（可根据实际修改）
  b'1',                          -- polling_enabled: 启用轮询
  5000,                          -- polling_interval: 5秒轮询一次
  5000,                          -- read_timeout: 5秒超时
  3,                             -- retry_count: 重试3次
  1,                             -- status: 启用
  'admin',                       -- creator
  '2025-11-21 06:52:09',         -- create_time
  NULL,                          -- updater
  '2025-11-21 06:52:09',         -- update_time
  b'0',                          -- deleted
  1                              -- tenant_id
);

-- ----------------------------
-- 2. BACnet属性映射 - 电能 (energy)
-- ----------------------------
INSERT INTO `iot_bacnet_property_mapping` (
  `id`,
  `device_config_id`,
  `device_id`,
  `thing_model_id`,
  `identifier`,
  `object_type`,
  `object_instance`,
  `property_identifier`,
  `property_array_index`,
  `data_type`,
  `unit_conversion`,
  `value_mapping`,
  `access_mode`,
  `priority`,
  `polling_enabled`,
  `cov_enabled`,
  `status`,
  `sort`,
  `creator`,
  `create_time`,
  `updater`,
  `update_time`,
  `deleted`,
  `tenant_id`
) VALUES (
  5001,                          -- id
  1001,                          -- device_config_id: 关联上面的设备配置
  10009,                         -- device_id: meter_001
  4004,                          -- thing_model_id: 对应 energy 物模型
  'energy',                      -- identifier: 物模型标识符
  'ANALOG_INPUT',                -- object_type: 模拟量输入（用于读取累计电能）
  0,                             -- object_instance: BACnet对象实例号
  'PRESENT_VALUE',               -- property_identifier: 当前值
  NULL,                          -- property_array_index: 无需数组索引
  'double',                      -- data_type: 双精度浮点数
  NULL,                          -- unit_conversion: 无需转换（已是kWh）
  NULL,                          -- value_mapping: 无需值映射
  'r',                           -- access_mode: 只读
  NULL,                          -- priority: 只读属性无需优先级
  b'1',                          -- polling_enabled: 启用轮询
  b'0',                          -- cov_enabled: 不启用COV（对于累计值，轮询更合适）
  1,                             -- status: 启用
  1,                             -- sort: 排序
  'admin',                       -- creator
  '2025-11-21 07:20:04',         -- create_time
  NULL,                          -- updater
  '2025-11-21 07:20:04',         -- update_time
  b'0',                          -- deleted
  1                              -- tenant_id
);

-- ----------------------------
-- 3. 可选：更多电表常见属性映射示例
-- ----------------------------

-- 电压 (voltage) - 假设物模型ID为4005
-- INSERT INTO `iot_bacnet_property_mapping` (
--   `device_config_id`, `device_id`, `thing_model_id`, `identifier`,
--   `object_type`, `object_instance`, `property_identifier`,
--   `data_type`, `access_mode`, `polling_enabled`, `status`, `sort`, `tenant_id`
-- ) VALUES (
--   1001, 10009, 4005, 'voltage',
--   'ANALOG_INPUT', 1, 'PRESENT_VALUE',
--   'double', 'r', b'1', 1, 2, 1
-- );

-- 电流 (current) - 假设物模型ID为4006
-- INSERT INTO `iot_bacnet_property_mapping` (
--   `device_config_id`, `device_id`, `thing_model_id`, `identifier`,
--   `object_type`, `object_instance`, `property_identifier`,
--   `data_type`, `access_mode`, `polling_enabled`, `status`, `sort`, `tenant_id`
-- ) VALUES (
--   1001, 10009, 4006, 'current',
--   'ANALOG_INPUT', 2, 'PRESENT_VALUE',
--   'double', 'r', b'1', 1, 3, 1
-- );

-- 功率 (power) - 假设物模型ID为4007
-- INSERT INTO `iot_bacnet_property_mapping` (
--   `device_config_id`, `device_id`, `thing_model_id`, `identifier`,
--   `object_type`, `object_instance`, `property_identifier`,
--   `data_type`, `access_mode`, `polling_enabled`, `status`, `sort`, `tenant_id`
-- ) VALUES (
--   1001, 10009, 4007, 'power',
--   'ANALOG_INPUT', 3, 'PRESENT_VALUE',
--   'double', 'r', b'1', 1, 4, 1
-- );

-- ----------------------------
-- 说明
-- ----------------------------
-- 1. BACnet设备配置 (iot_bacnet_device_config):
--    - instance_number: 300001 - BACnet设备在网络中的唯一实例号
--    - ip_address: 192.168.1.301 - 电表的IP地址
--    - polling_interval: 5000ms - 每5秒读取一次数据
--    - vendor_id: 120 - 示例供应商ID，需根据实际电表厂商修改
--
-- 2. BACnet属性映射 (iot_bacnet_property_mapping):
--    - object_type: ANALOG_INPUT - 模拟量输入对象，用于读取数值型数据
--    - object_instance: 0 - 该BACnet设备内的对象实例号（通常从0开始）
--    - property_identifier: PRESENT_VALUE - BACnet标准属性，表示当前值
--    - data_type: double - 与物模型定义一致
--    - access_mode: r - 只读，电能是累计值，不可写
--    - polling_enabled: true - 启用轮询，定期读取电能数据
--
-- 3. 使用场景:
--    - 系统会根据 polling_interval (5秒) 自动轮询读取电表的 energy 值
--    - 读取的数据会通过物模型标识符 'energy' 映射到对应的物模型属性
--    - 数据单位为 kWh (千瓦时)，无需单位转换
--
-- 4. 测试步骤:
--    a. 执行本SQL文件插入测试数据
--    b. 确保BACnet设备配置正确（IP、端口、实例号）
--    c. 启动系统，BACnet网关会自动连接设备
--    d. 查看设备数据上报，验证 energy 值是否正确读取
--
-- 5. 注意事项:
--    - instance_number (300001) 必须与实际BACnet电表的设备实例号一致
--    - object_instance (0) 必须与电表内部的对象实例号一致
--    - 如果电表有其他属性（电压、电流、功率等），可参考注释部分添加更多映射
--    - vendor_id 可通过BACnet设备发现功能获取实际值
