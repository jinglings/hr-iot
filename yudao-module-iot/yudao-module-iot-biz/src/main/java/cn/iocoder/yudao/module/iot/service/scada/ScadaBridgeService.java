package cn.iocoder.yudao.module.iot.service.scada;

import cn.iocoder.yudao.module.iot.controller.admin.scada.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaControlLogDO;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SCADA 桥接服务接口
 *
 * 核心服务，用于桥接 hr-iot 数据到 SCADA/MQTT。
 * 负责：
 * - 将设备属性变化发布到 MQTT
 * - 处理来自 SCADA 的控制命令
 * - 提供设备实时数据和历史数据
 * - 管理告警
 *
 * Part of SCADA-012: Implement SCADA Bridge Service
 *
 * @author HR-IoT Team
 */
public interface ScadaBridgeService {

    // ========== MQTT 发布相关 ==========

    /**
     * 发布设备属性更新到 MQTT
     *
     * @param tenantId           租户 ID
     * @param deviceId           设备 ID
     * @param propertyIdentifier 属性标识符
     * @param value              属性值
     */
    void publishPropertyUpdate(Long tenantId, Long deviceId, String propertyIdentifier, Object value);

    /**
     * 批量发布设备属性更新到 MQTT
     *
     * @param tenantId   租户 ID
     * @param deviceId   设备 ID
     * @param properties 属性 Map (identifier -> value)
     */
    void publishPropertiesUpdate(Long tenantId, Long deviceId, Map<String, Object> properties);

    /**
     * 发布设备状态变化到 MQTT
     *
     * @param tenantId 租户 ID
     * @param deviceId 设备 ID
     * @param state    设备状态
     */
    void publishDeviceStateChange(Long tenantId, Long deviceId, Integer state);

    /**
     * 发布告警事件到 MQTT
     *
     * @param tenantId 租户 ID
     * @param alarm    告警信息
     */
    void publishAlarmEvent(Long tenantId, ScadaAlarmVO alarm);

    // ========== 控制命令相关 ==========

    /**
     * 处理来自 SCADA 的控制命令
     *
     * @param reqVO 控制命令请求
     * @return 控制命令响应
     */
    ScadaControlCommandRespVO handleControlCommand(@Valid ScadaControlCommandReqVO reqVO);

    /**
     * 发送设备服务调用命令
     *
     * @param deviceId    设备 ID
     * @param serviceName 服务名称
     * @param params      服务参数
     * @return 是否成功
     */
    boolean invokeDeviceService(Long deviceId, String serviceName, Map<String, Object> params);

    /**
     * 设置设备属性
     *
     * @param deviceId           设备 ID
     * @param propertyIdentifier 属性标识符
     * @param value              属性值
     * @return 是否成功
     */
    boolean setDeviceProperty(Long deviceId, String propertyIdentifier, Object value);

    // ========== 设备数据相关 ==========

    /**
     * 获取 SCADA 可用的设备列表
     *
     * @return 设备列表
     */
    List<ScadaDeviceVO> getScadaDevices();

    /**
     * 获取指定设备的 SCADA 信息
     *
     * @param deviceId 设备 ID
     * @return 设备信息
     */
    ScadaDeviceVO getScadaDevice(Long deviceId);

    /**
     * 获取设备实时数据
     *
     * @param deviceId 设备 ID
     * @return 实时属性数据 Map
     */
    Map<String, Object> getDeviceRealtime(Long deviceId);

    /**
     * 批量获取多个设备的实时数据
     *
     * @param deviceIds 设备 ID 列表
     * @return 设备 ID -> 实时属性数据 Map
     */
    Map<Long, Map<String, Object>> getDevicesRealtime(List<Long> deviceIds);

    /**
     * 获取设备属性历史数据
     *
     * @param reqVO 查询请求
     * @return 历史数据点列表
     */
    List<ScadaHistoryPointVO> getDeviceHistory(@Valid ScadaHistoryQueryReqVO reqVO);

    // ========== Tag 映射相关 ==========

    /**
     * 获取设备的 Tag 映射列表
     *
     * @param deviceId 设备 ID
     * @return Tag 映射列表
     */
    List<ScadaTagMappingVO> getDeviceTagMappings(Long deviceId);

    /**
     * 根据 Tag 名称获取当前值
     *
     * @param tagName Tag 名称
     * @return 当前值
     */
    Object getTagValue(String tagName);

    /**
     * 批量获取 Tag 值
     *
     * @param tagNames Tag 名称列表
     * @return Tag 名称 -> 值 Map
     */
    Map<String, Object> getTagValues(List<String> tagNames);

    // ========== 告警相关 ==========

    /**
     * 获取活动告警列表
     *
     * @return 活动告警列表
     */
    List<ScadaAlarmVO> getActiveAlarms();

    /**
     * 获取设备的活动告警
     *
     * @param deviceId 设备 ID
     * @return 活动告警列表
     */
    List<ScadaAlarmVO> getDeviceActiveAlarms(Long deviceId);

    /**
     * 确认告警
     *
     * @param alarmHistoryId 告警历史记录 ID
     * @param acknowledgedBy 确认人
     * @return 是否成功
     */
    boolean acknowledgeAlarm(Long alarmHistoryId, String acknowledgedBy);

    /**
     * 关闭告警
     *
     * @param alarmHistoryId 告警历史记录 ID
     * @param closedBy       关闭人
     * @param notes          备注
     * @return 是否成功
     */
    boolean closeAlarm(Long alarmHistoryId, String closedBy, String notes);

    /**
     * 获取告警统计
     *
     * @return 告警统计 (status -> count)
     */
    Map<Integer, Long> getAlarmStatistics();

    // ========== 控制日志相关 ==========

    /**
     * 记录控制操作日志
     *
     * @param controlLog 控制日志
     * @return 日志 ID
     */
    Long logControlOperation(IotScadaControlLogDO controlLog);

    /**
     * 获取控制操作日志统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 状态统计 (status -> count)
     */
    Map<Integer, Long> getControlLogStatistics(LocalDateTime startTime, LocalDateTime endTime);

    // ========== 仪表板相关 ==========

    /**
     * 获取可用仪表板列表
     *
     * @return 仪表板列表
     */
    List<ScadaDashboardVO> getDashboards();

    /**
     * 获取默认仪表板
     *
     * @return 默认仪表板
     */
    ScadaDashboardVO getDefaultDashboard();

    /**
     * 生成 FUXA 访问 URL
     *
     * @param dashboardId 仪表板 ID
     * @param token       JWT Token
     * @return FUXA URL
     */
    String generateFuxaUrl(String dashboardId, String token);

}
