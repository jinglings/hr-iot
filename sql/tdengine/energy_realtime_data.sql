-- =====================================================
-- TDengine 能源实时数据表创建脚本
-- =====================================================
-- 功能说明:
-- 1. 创建能源实时数据超级表(Super Table)
-- 2. 用于存储电表、水表、气表等能源计量设备的实时数据
-- 3. 支持按计量点(meter_id)自动创建子表
--
-- 使用方式:
-- 1. 连接到 TDengine 数据库
-- 2. 创建或使用数据库: USE your_database_name;
-- 3. 执行本脚本
-- =====================================================

-- 创建数据库(如果不存在) - 根据实际情况调整数据库名称
-- CREATE DATABASE IF NOT EXISTS iot_energy KEEP 3650 DURATION 10 BUFFER 16;
-- USE iot_energy;

-- =====================================================
-- 1. 创建能源实时数据超级表
-- =====================================================
CREATE STABLE IF NOT EXISTS energy_realtime_data (

    ts               TIMESTAMP,


    meter_id         BIGINT,
    instant_power    DOUBLE,
    cumulative_value DOUBLE,
    voltage          DOUBLE,
    current          DOUBLE,
    power_factor     DOUBLE,
    data_quality     TINYINT
) TAGS (

    energy_type_id   BIGINT,
    building_id      BIGINT,
    area_id          BIGINT,
    floor_id         BIGINT,
    room_id          BIGINT,
    tenant_id        BIGINT
);

-- =====================================================
-- 2. 查看超级表是否创建成功
-- =====================================================
SHOW STABLES LIKE 'energy_realtime_data';

-- =====================================================
-- 3. 查看超级表结构
-- =====================================================
DESCRIBE energy_realtime_data;

-- =====================================================
-- 4. 子表创建说明
-- =====================================================
-- 子表会在插入数据时自动创建，命名规则: energy_realtime_data_{meterId}
-- 例如: INSERT INTO energy_realtime_data_1
--       USING energy_realtime_data
--       TAGS (1, 1001, 1, 1, 101, 1)
--       VALUES (NOW, 1, 5.5, 100.5, 220.0, 25.0, 0.95, 1);

-- =====================================================
-- 5. 常用查询示例
-- =====================================================

-- 查询某个计量点的最新数据
-- SELECT * FROM energy_realtime_data_1 ORDER BY ts DESC LIMIT 1;

-- 查询某个建筑的所有数据
-- SELECT * FROM energy_realtime_data WHERE building_id = 1001 AND ts >= NOW - 1h;

-- 查询某种能源类型的统计数据
-- SELECT AVG(instant_power), MAX(instant_power), MIN(instant_power)
-- FROM energy_realtime_data
-- WHERE energy_type_id = 1 AND ts >= NOW - 1d
-- INTERVAL(1h);

-- 查询某个租户的能耗统计
-- SELECT SUM(cumulative_value) AS total_consumption
-- FROM energy_realtime_data
-- WHERE tenant_id = 1 AND ts >= NOW - 1d;

-- =====================================================
-- 6. 数据保留策略说明
-- =====================================================
-- TDengine 会根据数据库的 KEEP 参数自动删除过期数据
-- 建议根据业务需求设置合理的保留期限:
-- - 实时数据: 保留 7-30 天
-- - 统计数据: 通过定时任务聚合到统计表，保留更长时间
--
-- 修改数据库保留期限:
-- ALTER DATABASE iot_energy KEEP 30;

-- =====================================================
-- 7. 索引优化建议
-- =====================================================
-- TDengine 会自动为时间戳和标签创建索引
-- 无需手动创建额外索引
-- 查询时应尽量使用标签字段进行过滤以提高性能

-- =====================================================
-- 8. 常见问题处理
-- =====================================================

-- 问题1: 超级表已存在且结构不同
-- 解决: 删除超级表并重新创建 (注意: 会删除所有子表和数据)
-- DROP STABLE IF EXISTS energy_realtime_data;

-- 问题2: 查看所有子表
-- SELECT TBNAME FROM energy_realtime_data;

-- 问题3: 查看某个子表的详细信息
-- SELECT * FROM energy_realtime_data WHERE TBNAME = 'energy_realtime_data_1';

-- 问题4: 删除特定的子表
-- DROP TABLE IF EXISTS energy_realtime_data_1;

-- =====================================================
-- 脚本结束
-- =====================================================
