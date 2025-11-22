package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * iot 错误码枚举类
 * <p>
 * iot 系统，使用 1-050-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 产品相关 1-050-001-000 ============
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_001_000, "产品不存在");
    ErrorCode PRODUCT_KEY_EXISTS = new ErrorCode(1_050_001_001, "产品标识已经存在");
    ErrorCode PRODUCT_STATUS_NOT_DELETE = new ErrorCode(1_050_001_002, "产品状是发布状态，不允许删除");
    ErrorCode PRODUCT_STATUS_NOT_ALLOW_THING_MODEL = new ErrorCode(1_050_001_003, "产品状是发布状态，不允许操作物模型");
    ErrorCode PRODUCT_DELETE_FAIL_HAS_DEVICE = new ErrorCode(1_050_001_004, "产品下存在设备，不允许删除");

    // ========== 产品物模型 1-050-002-000 ============
    ErrorCode THING_MODEL_NOT_EXISTS = new ErrorCode(1_050_002_000, "产品物模型不存在");
    ErrorCode THING_MODEL_EXISTS_BY_PRODUCT_KEY = new ErrorCode(1_050_002_001, "ProductKey 对应的产品物模型已存在");
    ErrorCode THING_MODEL_IDENTIFIER_EXISTS = new ErrorCode(1_050_002_002, "存在重复的功能标识符。");
    ErrorCode THING_MODEL_NAME_EXISTS = new ErrorCode(1_050_002_003, "存在重复的功能名称。");
    ErrorCode THING_MODEL_IDENTIFIER_INVALID = new ErrorCode(1_050_002_003, "产品物模型标识无效");

    // ========== 设备 1-050-003-000 ============
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_003_000, "设备不存在");
    ErrorCode DEVICE_NAME_EXISTS = new ErrorCode(1_050_003_001, "设备名称在同一产品下必须唯一");
    ErrorCode DEVICE_HAS_CHILDREN = new ErrorCode(1_050_003_002, "有子设备，不允许删除");
    ErrorCode DEVICE_KEY_EXISTS = new ErrorCode(1_050_003_003, "设备标识已经存在");
    ErrorCode DEVICE_GATEWAY_NOT_EXISTS = new ErrorCode(1_050_003_004, "网关设备不存在");
    ErrorCode DEVICE_NOT_GATEWAY = new ErrorCode(1_050_003_005, "设备不是网关设备");
    ErrorCode DEVICE_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_050_003_006, "导入设备数据不能为空！");
    ErrorCode DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL = new ErrorCode(1_050_003_007, "下行设备消息失败，原因：设备未连接网关");
    ErrorCode DEVICE_SERIAL_NUMBER_EXISTS = new ErrorCode(1_050_003_008, "设备序列号已存在，序列号必须全局唯一");

    // ========== 产品分类 1-050-004-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_050_004_000, "产品分类不存在");

    // ========== 设备分组 1-050-005-000 ==========
    ErrorCode DEVICE_GROUP_NOT_EXISTS = new ErrorCode(1_050_005_000, "设备分组不存在");
    ErrorCode DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS = new ErrorCode(1_050_005_001, "设备分组下存在设备，不允许删除");

    // ========== OTA 固件相关 1-050-008-000 ==========

    ErrorCode OTA_FIRMWARE_NOT_EXISTS = new ErrorCode(1_050_008_000, "固件信息不存在");
    ErrorCode OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE = new ErrorCode(1_050_008_001, "产品版本号重复");

    // ========== OTA 升级任务相关 1-050-008-100 ==========

    ErrorCode OTA_TASK_NOT_EXISTS = new ErrorCode(1_050_008_100, "升级任务不存在");
    ErrorCode OTA_TASK_CREATE_FAIL_NAME_DUPLICATE = new ErrorCode(1_050_008_101, "创建 OTA 任务失败，原因：任务名称重复");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_FIRMWARE_EXISTS = new ErrorCode(1_050_008_102,
            "创建 OTA 任务失败，原因：设备({})已经是该固件版本");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_OTA_IN_PROCESS = new ErrorCode(1_050_008_102,
            "创建 OTA 任务失败，原因：设备({})已经在升级中...");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_EMPTY = new ErrorCode(1_050_008_103, "创建 OTA 任务失败，原因：没有可升级的设备");
    ErrorCode OTA_TASK_CANCEL_FAIL_STATUS_END = new ErrorCode(1_050_008_104, "取消 OTA 任务失败，原因：任务状态不是进行中");

    // ========== OTA 升级任务记录相关 1-050-008-200 ==========

    ErrorCode OTA_TASK_RECORD_NOT_EXISTS = new ErrorCode(1_050_008_200, "升级记录不存在");
    ErrorCode OTA_TASK_RECORD_CANCEL_FAIL_STATUS_ERROR = new ErrorCode(1_050_008_201, "取消 OTA 升级记录失败，原因：记录状态不是进行中");
    ErrorCode OTA_TASK_RECORD_UPDATE_PROGRESS_FAIL_NO_EXISTS = new ErrorCode(1_050_008_202, "更新 OTA 升级记录进度失败，原因：该设备没有进行中的升级记录");

    // ========== IoT 数据流转规则 1-050-010-000 ==========
    ErrorCode DATA_RULE_NOT_EXISTS = new ErrorCode(1_050_010_000, "数据流转规则不存在");

    // ========== IoT 数据流转目的 1-050-011-000 ==========
    ErrorCode DATA_SINK_NOT_EXISTS = new ErrorCode(1_050_011_000, "数据桥梁不存在");
    ErrorCode DATA_SINK_DELETE_FAIL_USED_BY_RULE = new ErrorCode(1_050_011_001, "数据流转目的正在被数据流转规则使用，无法删除");

    // ========== IoT 场景联动 1-050-012-000 ==========
    ErrorCode RULE_SCENE_NOT_EXISTS = new ErrorCode(1_050_012_000, "场景联动不存在");

    // ========== IoT 告警配置 1-050-013-000 ==========
    ErrorCode ALERT_CONFIG_NOT_EXISTS = new ErrorCode(1_050_013_000, "IoT 告警配置不存在");

    // ========== IoT 告警记录 1-050-014-000 ==========
    ErrorCode ALERT_RECORD_NOT_EXISTS = new ErrorCode(1_050_014_000, "IoT 告警记录不存在");

    // ========== 能源管理 - 建筑 1-050-020-000 ==========
    ErrorCode ENERGY_BUILDING_NOT_EXISTS = new ErrorCode(1_050_020_000, "能源建筑不存在");
    ErrorCode ENERGY_BUILDING_CODE_EXISTS = new ErrorCode(1_050_020_001, "建筑编码已存在");
    ErrorCode ENERGY_BUILDING_DELETE_FAIL_HAS_AREA = new ErrorCode(1_050_020_002, "建筑下存在区域，不允许删除");

    // ========== 能源管理 - 区域 1-050-021-000 ==========
    ErrorCode ENERGY_AREA_NOT_EXISTS = new ErrorCode(1_050_021_000, "能源区域不存在");
    ErrorCode ENERGY_AREA_CODE_EXISTS = new ErrorCode(1_050_021_001, "区域编码已存在");

    // ========== 能源管理 - 楼层 1-050-022-000 ==========
    ErrorCode ENERGY_FLOOR_NOT_EXISTS = new ErrorCode(1_050_022_000, "能源楼层不存在");
    ErrorCode ENERGY_FLOOR_CODE_EXISTS = new ErrorCode(1_050_022_001, "楼层编码已存在");

    // ========== 能源管理 - 房间 1-050-023-000 ==========
    ErrorCode ENERGY_ROOM_NOT_EXISTS = new ErrorCode(1_050_023_000, "能源房间不存在");
    ErrorCode ENERGY_ROOM_CODE_EXISTS = new ErrorCode(1_050_023_001, "房间编码已存在");

    // ========== 能源管理 - 能源类型 1-050-024-000 ==========
    ErrorCode ENERGY_TYPE_NOT_EXISTS = new ErrorCode(1_050_024_000, "能源类型不存在");
    ErrorCode ENERGY_TYPE_CODE_EXISTS = new ErrorCode(1_050_024_001, "能源类型编码已存在");
    ErrorCode ENERGY_TYPE_PARENT_ERROR = new ErrorCode(1_050_024_002, "父级能源类型不合法");
    ErrorCode ENERGY_TYPE_EXITS_CHILDREN = new ErrorCode(1_050_024_003, "能源类型存在子类型，不允许删除");

    // ========== 能源管理 - 计量点 1-050-025-000 ==========
    ErrorCode ENERGY_METER_NOT_EXISTS = new ErrorCode(1_050_025_000, "能源计量点不存在");
    ErrorCode ENERGY_METER_CODE_EXISTS = new ErrorCode(1_050_025_001, "计量点编码已存在");
    ErrorCode ENERGY_METER_VIRTUAL_FORMULA_REQUIRED = new ErrorCode(1_050_025_002, "虚拟表必须填写计算公式");
    ErrorCode ENERGY_METER_DEVICE_REQUIRED = new ErrorCode(1_050_025_003, "非虚拟表必须绑定设备");
    ErrorCode ENERGY_METER_PARENT_ERROR = new ErrorCode(1_050_025_004, "父级计量点不合法");
    ErrorCode ENERGY_METER_EXITS_CHILDREN = new ErrorCode(1_050_025_005, "计量点存在子计量点，不允许删除");

    // ========== 能源管理 - 报表模板 1-050-026-000 ==========
    ErrorCode ENERGY_REPORT_TEMPLATE_NOT_EXISTS = new ErrorCode(1_050_026_000, "能源报表模板不存在");

    // ========== 能源管理 - 报表记录 1-050-027-000 ==========
    ErrorCode ENERGY_REPORT_RECORD_NOT_EXISTS = new ErrorCode(1_050_027_000, "能源报表记录不存在");

    // ========== 边缘计算 - 边缘网关 1-050-030-000 ==========
    ErrorCode EDGE_GATEWAY_NOT_EXISTS = new ErrorCode(1_050_030_000, "边缘网关不存在");
    ErrorCode EDGE_GATEWAY_SERIAL_NUMBER_EXISTS = new ErrorCode(1_050_030_001, "边缘网关序列号已存在");
    ErrorCode EDGE_GATEWAY_KEY_EXISTS = new ErrorCode(1_050_030_002, "边缘网关标识已存在");
    ErrorCode EDGE_GATEWAY_DELETE_FAIL_HAS_RULE = new ErrorCode(1_050_030_003, "边缘网关下存在规则，不允许删除");
    ErrorCode EDGE_GATEWAY_OFFLINE = new ErrorCode(1_050_030_004, "边缘网关离线");
    ErrorCode EDGE_GATEWAY_NOT_ONLINE = new ErrorCode(1_050_030_005, "边缘网关未上线");

    // ========== 边缘计算 - 边缘规则 1-050-031-000 ==========
    ErrorCode EDGE_RULE_NOT_EXISTS = new ErrorCode(1_050_031_000, "边缘规则不存在");
    ErrorCode EDGE_RULE_DEPLOY_FAILED = new ErrorCode(1_050_031_001, "边缘规则部署失败");
    ErrorCode EDGE_RULE_ALREADY_DEPLOYED = new ErrorCode(1_050_031_002, "边缘规则已部署");

    // ========== 边缘计算 - AI模型 1-050-032-000 ==========
    ErrorCode EDGE_AI_MODEL_NOT_EXISTS = new ErrorCode(1_050_032_000, "AI模型不存在");
    ErrorCode EDGE_AI_MODEL_VERSION_EXISTS = new ErrorCode(1_050_032_001, "AI模型版本已存在");
    ErrorCode EDGE_AI_MODEL_DELETE_FAIL_HAS_DEPLOYMENT = new ErrorCode(1_050_032_002, "AI模型存在部署记录，不允许删除");
    ErrorCode EDGE_AI_MODEL_NOT_ENABLED = new ErrorCode(1_050_032_003, "AI模型未启用");

    // ========== 边缘计算 - 模型部署 1-050-033-000 ==========
    ErrorCode EDGE_MODEL_DEPLOYMENT_NOT_EXISTS = new ErrorCode(1_050_033_000, "模型部署记录不存在");
    ErrorCode EDGE_MODEL_DEPLOYMENT_ALREADY_EXISTS = new ErrorCode(1_050_033_001, "模型已在该网关部署");
    ErrorCode EDGE_MODEL_DEPLOYMENT_FAILED = new ErrorCode(1_050_033_002, "模型部署失败");

    // ========== BACnet 协议 - 设备配置 1-050-040-000 ==========
    ErrorCode BACNET_DEVICE_CONFIG_NOT_EXISTS = new ErrorCode(1_050_040_000, "BACnet 设备配置不存在");
    ErrorCode BACNET_DEVICE_CONFIG_EXISTS = new ErrorCode(1_050_040_001, "设备已存在 BACnet 配置");
    ErrorCode BACNET_INSTANCE_NUMBER_EXISTS = new ErrorCode(1_050_040_002, "BACnet 设备实例号已被使用");

    // ========== BACnet 协议 - 属性映射 1-050-041-000 ==========
    ErrorCode BACNET_PROPERTY_MAPPING_NOT_EXISTS = new ErrorCode(1_050_041_000, "BACnet 属性映射不存在");
    ErrorCode BACNET_PROPERTY_MAPPING_EXISTS = new ErrorCode(1_050_041_001, "BACnet 属性映射已存在");

    // ========== BACnet 协议 - 设备发现 1-050-042-000 ==========
    ErrorCode BACNET_DISCOVERY_RECORD_NOT_EXISTS = new ErrorCode(1_050_042_000, "BACnet 设备发现记录不存在");
    ErrorCode BACNET_DISCOVERY_DEVICE_NOT_FOUND = new ErrorCode(1_050_042_001, "未找到 BACnet 设备");
    ErrorCode BACNET_DISCOVERY_ALREADY_BOUND = new ErrorCode(1_050_042_002, "BACnet 设备已绑定");
    ErrorCode BACNET_BIND_PARAM_ERROR = new ErrorCode(1_050_042_003, "绑定参数错误，请选择设备或填写设备信息");

}