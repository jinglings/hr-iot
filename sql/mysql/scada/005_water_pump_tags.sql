-- ============================================================
-- SCADA Tag Mapping for Water Pump System
-- Part of SCADA-026: Configure Tag Mappings for Water System
-- 
-- This script creates sample tag mappings for a water pump system
-- demonstration dashboard with 4 pumps, pressure/temperature sensors,
-- flow meters, and control valves.
-- ============================================================

-- Set tenant ID (adjust as needed for your environment)
SET @tenant_id = 1;

-- ============================================================
-- 1. Pump 1 Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Pump1_Status',     'pump1_status',     1, 'status',     'boolean', NULL,   1, 0, 0, 1, '1号水泵运行状态: true=运行, false=停止', @tenant_id),
  ('Pump1_Mode',       'pump1_mode',       1, 'mode',       'string',  NULL,   1, 0, NULL, NULL, '1号水泵运行模式: AUTO/MANUAL', @tenant_id),
  ('Pump1_Frequency',  'pump1_freq',       1, 'frequency',  'number',  'Hz',   1, 0, 0, 60, '1号水泵电机频率', @tenant_id),
  ('Pump1_Current',    'pump1_current',    1, 'current',    'number',  'A',    1, 0, 0, 100, '1号水泵电机电流', @tenant_id),
  ('Pump1_Power',      'pump1_power',      1, 'power',      'number',  'kW',   1, 0, 0, 50, '1号水泵功率', @tenant_id),
  ('Pump1_Runtime',    'pump1_runtime',    1, 'runtime',    'number',  'h',    1, 0, 0, 99999, '1号水泵累计运行时间', @tenant_id),
  ('Pump1_Alarm',      'pump1_alarm',      1, 'alarm',      'boolean', NULL,   1, 0, 0, 1, '1号水泵告警状态', @tenant_id);

-- ============================================================
-- 2. Pump 2 Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Pump2_Status',     'pump2_status',     2, 'status',     'boolean', NULL,   1, 0, 0, 1, '2号水泵运行状态', @tenant_id),
  ('Pump2_Mode',       'pump2_mode',       2, 'mode',       'string',  NULL,   1, 0, NULL, NULL, '2号水泵运行模式', @tenant_id),
  ('Pump2_Frequency',  'pump2_freq',       2, 'frequency',  'number',  'Hz',   1, 0, 0, 60, '2号水泵电机频率', @tenant_id),
  ('Pump2_Current',    'pump2_current',    2, 'current',    'number',  'A',    1, 0, 0, 100, '2号水泵电机电流', @tenant_id),
  ('Pump2_Power',      'pump2_power',      2, 'power',      'number',  'kW',   1, 0, 0, 50, '2号水泵功率', @tenant_id),
  ('Pump2_Runtime',    'pump2_runtime',    2, 'runtime',    'number',  'h',    1, 0, 0, 99999, '2号水泵累计运行时间', @tenant_id),
  ('Pump2_Alarm',      'pump2_alarm',      2, 'alarm',      'boolean', NULL,   1, 0, 0, 1, '2号水泵告警状态', @tenant_id);

-- ============================================================
-- 3. Pump 3 Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Pump3_Status',     'pump3_status',     3, 'status',     'boolean', NULL,   1, 0, 0, 1, '3号水泵运行状态', @tenant_id),
  ('Pump3_Mode',       'pump3_mode',       3, 'mode',       'string',  NULL,   1, 0, NULL, NULL, '3号水泵运行模式', @tenant_id),
  ('Pump3_Frequency',  'pump3_freq',       3, 'frequency',  'number',  'Hz',   1, 0, 0, 60, '3号水泵电机频率', @tenant_id),
  ('Pump3_Current',    'pump3_current',    3, 'current',    'number',  'A',    1, 0, 0, 100, '3号水泵电机电流', @tenant_id),
  ('Pump3_Power',      'pump3_power',      3, 'power',      'number',  'kW',   1, 0, 0, 50, '3号水泵功率', @tenant_id),
  ('Pump3_Runtime',    'pump3_runtime',    3, 'runtime',    'number',  'h',    1, 0, 0, 99999, '3号水泵累计运行时间', @tenant_id),
  ('Pump3_Alarm',      'pump3_alarm',      3, 'alarm',      'boolean', NULL,   1, 0, 0, 1, '3号水泵告警状态', @tenant_id);

-- ============================================================
-- 4. Pump 4 Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Pump4_Status',     'pump4_status',     4, 'status',     'boolean', NULL,   1, 0, 0, 1, '4号水泵运行状态', @tenant_id),
  ('Pump4_Mode',       'pump4_mode',       4, 'mode',       'string',  NULL,   1, 0, NULL, NULL, '4号水泵运行模式', @tenant_id),
  ('Pump4_Frequency',  'pump4_freq',       4, 'frequency',  'number',  'Hz',   1, 0, 0, 60, '4号水泵电机频率', @tenant_id),
  ('Pump4_Current',    'pump4_current',    4, 'current',    'number',  'A',    1, 0, 0, 100, '4号水泵电机电流', @tenant_id),
  ('Pump4_Power',      'pump4_power',      4, 'power',      'number',  'kW',   1, 0, 0, 50, '4号水泵功率', @tenant_id),
  ('Pump4_Runtime',    'pump4_runtime',    4, 'runtime',    'number',  'h',    1, 0, 0, 99999, '4号水泵累计运行时间', @tenant_id),
  ('Pump4_Alarm',      'pump4_alarm',      4, 'alarm',      'boolean', NULL,   1, 0, 0, 1, '4号水泵告警状态', @tenant_id);

-- ============================================================
-- 5. Pressure Sensors Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Pressure_Inlet',     'pressure_inlet',    10, 'pressure', 'number', 'MPa', 1, 0, 0, 2.0, '进水压力', @tenant_id),
  ('Pressure_Outlet',    'pressure_outlet',   11, 'pressure', 'number', 'MPa', 1, 0, 0, 2.0, '出水压力', @tenant_id),
  ('Pressure_Supply',    'pressure_supply',   12, 'pressure', 'number', 'MPa', 1, 0, 0, 2.0, '供水压力', @tenant_id),
  ('Pressure_Return',    'pressure_return',   13, 'pressure', 'number', 'MPa', 1, 0, 0, 2.0, '回水压力', @tenant_id),
  ('Pressure_Diff',      'pressure_diff',     NULL, NULL,     'number', 'MPa', 1, 0, -1, 1, '压差（计算值）', @tenant_id);

-- ============================================================
-- 6. Temperature Sensors Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Temp_Inlet',         'temp_inlet',        20, 'temperature', 'number', '℃', 1, 0, 0, 100, '进水温度', @tenant_id),
  ('Temp_Outlet',        'temp_outlet',       21, 'temperature', 'number', '℃', 1, 0, 0, 100, '出水温度', @tenant_id),
  ('Temp_Supply',        'temp_supply',       22, 'temperature', 'number', '℃', 1, 0, 0, 100, '供水温度', @tenant_id),
  ('Temp_Return',        'temp_return',       23, 'temperature', 'number', '℃', 1, 0, 0, 100, '回水温度', @tenant_id),
  ('Temp_Ambient',       'temp_ambient',      24, 'temperature', 'number', '℃', 1, 0, -20, 50, '环境温度', @tenant_id);

-- ============================================================
-- 7. Flow Meters Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Flow_Inlet',         'flow_inlet',        30, 'flowRate',      'number', 'm³/h', 1, 0, 0, 1000, '进水流量', @tenant_id),
  ('Flow_Outlet',        'flow_outlet',       31, 'flowRate',      'number', 'm³/h', 1, 0, 0, 1000, '出水流量', @tenant_id),
  ('Flow_Total_Inlet',   'flow_total_inlet',  30, 'totalFlow',     'number', 'm³',   1, 0, 0, 999999999, '进水累计流量', @tenant_id),
  ('Flow_Total_Outlet',  'flow_total_outlet', 31, 'totalFlow',     'number', 'm³',   1, 0, 0, 999999999, '出水累计流量', @tenant_id);

-- ============================================================
-- 8. Valves Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Valve1_Status',      'valve1_status',     40, 'status',   'boolean', NULL, 1, 0, 0, 1, '阀门1状态: true=开, false=关', @tenant_id),
  ('Valve1_Position',    'valve1_position',   40, 'position', 'number',  '%',  1, 0, 0, 100, '阀门1开度', @tenant_id),
  ('Valve2_Status',      'valve2_status',     41, 'status',   'boolean', NULL, 1, 0, 0, 1, '阀门2状态', @tenant_id),
  ('Valve2_Position',    'valve2_position',   41, 'position', 'number',  '%',  1, 0, 0, 100, '阀门2开度', @tenant_id),
  ('Valve3_Status',      'valve3_status',     42, 'status',   'boolean', NULL, 1, 0, 0, 1, '阀门3状态', @tenant_id),
  ('Valve3_Position',    'valve3_position',   42, 'position', 'number',  '%',  1, 0, 0, 100, '阀门3开度', @tenant_id),
  ('Valve4_Status',      'valve4_status',     43, 'status',   'boolean', NULL, 1, 0, 0, 1, '阀门4状态', @tenant_id),
  ('Valve4_Position',    'valve4_position',   43, 'position', 'number',  '%',  1, 0, 0, 100, '阀门4开度', @tenant_id);

-- ============================================================
-- 9. Tank Level Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Tank1_Level',        'tank1_level',       50, 'level',    'number', '%',  1, 0, 0, 100, '水箱1液位', @tenant_id),
  ('Tank1_Volume',       'tank1_volume',      50, 'volume',   'number', 'm³', 1, 0, 0, 1000, '水箱1容量', @tenant_id),
  ('Tank2_Level',        'tank2_level',       51, 'level',    'number', '%',  1, 0, 0, 100, '水箱2液位', @tenant_id),
  ('Tank2_Volume',       'tank2_volume',      51, 'volume',   'number', 'm³', 1, 0, 0, 1000, '水箱2容量', @tenant_id),
  ('Tank_Level_High',    'tank_level_high',   NULL, NULL,     'boolean', NULL, 1, 0, 0, 1, '水箱高液位报警', @tenant_id),
  ('Tank_Level_Low',     'tank_level_low',    NULL, NULL,     'boolean', NULL, 1, 0, 0, 1, '水箱低液位报警', @tenant_id);

-- ============================================================
-- 10. Heat Exchanger Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('HX_Hot_Inlet_Temp',   'hx_hot_inlet',    60, 'hotInletTemp',    'number', '℃', 1, 0, 0, 100, '换热器热侧进口温度', @tenant_id),
  ('HX_Hot_Outlet_Temp',  'hx_hot_outlet',   60, 'hotOutletTemp',   'number', '℃', 1, 0, 0, 100, '换热器热侧出口温度', @tenant_id),
  ('HX_Cold_Inlet_Temp',  'hx_cold_inlet',   60, 'coldInletTemp',   'number', '℃', 1, 0, 0, 100, '换热器冷侧进口温度', @tenant_id),
  ('HX_Cold_Outlet_Temp', 'hx_cold_outlet',  60, 'coldOutletTemp',  'number', '℃', 1, 0, 0, 100, '换热器冷侧出口温度', @tenant_id),
  ('HX_Efficiency',       'hx_efficiency',   60, 'efficiency',      'number', '%',  1, 0, 0, 100, '换热效率', @tenant_id);

-- ============================================================
-- 11. System Status Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('System_Mode',         'system_mode',      NULL, NULL, 'string',  NULL,  1, 0, NULL, NULL, '系统模式: AUTO/MANUAL/OFF', @tenant_id),
  ('System_Status',       'system_status',    NULL, NULL, 'string',  NULL,  1, 0, NULL, NULL, '系统状态: RUNNING/STOPPED/ALARM', @tenant_id),
  ('Total_Power',         'total_power',      NULL, NULL, 'number',  'kW',  1, 0, 0, 500, '系统总功率', @tenant_id),
  ('Total_Flow',          'total_flow',       NULL, NULL, 'number',  'm³/h', 1, 0, 0, 2000, '系统总流量', @tenant_id),
  ('Active_Pump_Count',   'active_pumps',     NULL, NULL, 'number',  NULL,  1, 0, 0, 4, '运行中的水泵数量', @tenant_id),
  ('Alarm_Count',         'alarm_count',      NULL, NULL, 'number',  NULL,  1, 0, 0, 100, '当前告警数量', @tenant_id);

-- ============================================================
-- 12. Energy Monitoring Tags
-- ============================================================
INSERT INTO `scada_tag_mapping` 
  (`tag_name`, `tag_id`, `device_id`, `property_identifier`, `data_type`, `unit`, `scale_factor`, `offset`, `min_value`, `max_value`, `description`, `tenant_id`)
VALUES
  ('Energy_Today',        'energy_today',     NULL, NULL, 'number', 'kWh',  1, 0, 0, 99999, '今日用电量', @tenant_id),
  ('Energy_Month',        'energy_month',     NULL, NULL, 'number', 'kWh',  1, 0, 0, 999999, '本月用电量', @tenant_id),
  ('Energy_Total',        'energy_total',     NULL, NULL, 'number', 'kWh',  1, 0, 0, 99999999, '累计用电量', @tenant_id),
  ('Water_Today',         'water_today',      NULL, NULL, 'number', 'm³',   1, 0, 0, 99999, '今日用水量', @tenant_id),
  ('Water_Month',         'water_month',      NULL, NULL, 'number', 'm³',   1, 0, 0, 999999, '本月用水量', @tenant_id),
  ('Water_Total',         'water_total',      NULL, NULL, 'number', 'm³',   1, 0, 0, 99999999, '累计用水量', @tenant_id);

-- ============================================================
-- Summary: Total Tags Created
-- ============================================================
-- Pump 1-4:        28 tags (7 per pump)
-- Pressure:         5 tags
-- Temperature:      5 tags
-- Flow:             4 tags
-- Valves:           8 tags
-- Tanks:            6 tags
-- Heat Exchanger:   5 tags
-- System Status:    6 tags
-- Energy:           6 tags
-- ---------------------
-- Total:           73 tags
-- ============================================================

SELECT 
  COUNT(*) as total_tags,
  SUM(CASE WHEN data_type = 'number' THEN 1 ELSE 0 END) as number_tags,
  SUM(CASE WHEN data_type = 'boolean' THEN 1 ELSE 0 END) as boolean_tags,
  SUM(CASE WHEN data_type = 'string' THEN 1 ELSE 0 END) as string_tags
FROM scada_tag_mapping 
WHERE tenant_id = @tenant_id AND deleted = 0;
