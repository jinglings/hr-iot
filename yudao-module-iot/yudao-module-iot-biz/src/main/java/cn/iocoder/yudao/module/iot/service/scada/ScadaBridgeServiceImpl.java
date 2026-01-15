package cn.iocoder.yudao.module.iot.service.scada;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.scada.vo.*;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.*;
import cn.iocoder.yudao.module.iot.dal.mysql.scada.*;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SCADA 桥接服务实现
 *
 * Part of SCADA-012: Implement SCADA Bridge Service
 *
 * @author HR-IoT Team
 */
@Service
@Validated
@Slf4j
public class ScadaBridgeServiceImpl implements ScadaBridgeService {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDevicePropertyService devicePropertyService;

    @Resource
    private IotDeviceMessageService deviceMessageService;

    @Resource
    private IotScadaDashboardMapper dashboardMapper;

    @Resource
    private IotScadaTagMappingMapper tagMappingMapper;

    @Resource
    private IotScadaControlLogMapper controlLogMapper;

    @Resource
    private IotScadaAlarmMapper alarmMapper;

    @Resource
    private IotScadaAlarmHistoryMapper alarmHistoryMapper;

    @Value("${scada.fuxa.base-url:http://localhost:1881}")
    private String fuxaBaseUrl;

    @Value("${scada.mqtt.topic-prefix:iot}")
    private String mqttTopicPrefix;

    // ========== MQTT 发布相关 ==========

    @Override
    public void publishPropertyUpdate(Long tenantId, Long deviceId, String propertyIdentifier, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(propertyIdentifier, value);
        publishPropertiesUpdate(tenantId, deviceId, properties);
    }

    @Override
    public void publishPropertiesUpdate(Long tenantId, Long deviceId, Map<String, Object> properties) {
        if (CollUtil.isEmpty(properties)) {
            return;
        }

        try {
            // 获取设备信息
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                log.warn("[publishPropertiesUpdate] 设备不存在: deviceId={}", deviceId);
                return;
            }

            // 构建 MQTT 消息
            String topic = buildMqttTopic(tenantId, deviceId, "properties");
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", deviceId);
            payload.put("deviceName", device.getDeviceName());
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("properties", properties);

            // 发布消息 - 这里使用设备消息服务
            IotDeviceMessage message = new IotDeviceMessage();
            message.setDeviceId(deviceId);
            message.setData(payload);
            deviceMessageService.sendDeviceMessage(message, device);

            log.debug("[publishPropertiesUpdate] 发布属性更新: topic={}, properties={}", topic, properties);
        } catch (Exception e) {
            log.error("[publishPropertiesUpdate] 发布属性更新失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    @Override
    public void publishDeviceStateChange(Long tenantId, Long deviceId, Integer state) {
        try {
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                return;
            }

            String topic = buildMqttTopic(tenantId, deviceId, "state");
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", deviceId);
            payload.put("deviceName", device.getDeviceName());
            payload.put("state", state);
            payload.put("timestamp", System.currentTimeMillis());

            IotDeviceMessage message = new IotDeviceMessage();
            message.setDeviceId(deviceId);
            message.setData(payload);
            deviceMessageService.sendDeviceMessage(message, device);

            log.debug("[publishDeviceStateChange] 发布状态变化: deviceId={}, state={}", deviceId, state);
        } catch (Exception e) {
            log.error("[publishDeviceStateChange] 发布状态变化失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    @Override
    public void publishAlarmEvent(Long tenantId, ScadaAlarmVO alarm) {
        try {
            String topic = buildMqttTopic(tenantId, null, "alarm");
            Map<String, Object> payload = new HashMap<>();
            payload.put("alarmId", alarm.getId());
            payload.put("alarmName", alarm.getAlarmName());
            payload.put("deviceId", alarm.getDeviceId());
            payload.put("priority", alarm.getPriority());
            payload.put("status", alarm.getStatus());
            payload.put("message", alarm.getMessage());
            payload.put("triggeredAt", alarm.getTriggeredAt());
            payload.put("timestamp", System.currentTimeMillis());

            log.info("[publishAlarmEvent] 发布告警事件: alarmName={}, priority={}",
                    alarm.getAlarmName(), alarm.getPriority());
        } catch (Exception e) {
            log.error("[publishAlarmEvent] 发布告警事件失败: error={}", e.getMessage(), e);
        }
    }

    // ========== 控制命令相关 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScadaControlCommandRespVO handleControlCommand(ScadaControlCommandReqVO reqVO) {
        long startTime = System.currentTimeMillis();
        ScadaControlCommandRespVO respVO = new ScadaControlCommandRespVO();
        respVO.setDeviceId(reqVO.getDeviceId());
        respVO.setCommandName(reqVO.getCommandName());

        IotScadaControlLogDO controlLog = new IotScadaControlLogDO();
        controlLog.setDeviceId(reqVO.getDeviceId());
        controlLog.setCommandName(reqVO.getCommandName());
        controlLog.setCommandParams(JsonUtils.toJsonString(reqVO.getParams()));
        controlLog.setOldValue(reqVO.getOldValue() != null ? reqVO.getOldValue().toString() : null);
        controlLog.setNewValue(reqVO.getNewValue() != null ? reqVO.getNewValue().toString() : null);
        controlLog.setSource(StrUtil.isNotBlank(reqVO.getSource()) ? reqVO.getSource() : "SCADA");
        controlLog.setUserId(SecurityFrameworkUtils.getLoginUserId());
        controlLog.setUserName(SecurityFrameworkUtils.getLoginUserNickname());
        controlLog.setTenantId(TenantContextHolder.getTenantId());
        controlLog.setCreateTime(LocalDateTime.now());

        try {
            // 获取设备
            IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
            respVO.setDeviceName(device.getDeviceName());
            controlLog.setDeviceName(device.getDeviceName());

            // 执行控制命令
            boolean success;
            if (reqVO.getParams() != null && !reqVO.getParams().isEmpty()) {
                // 服务调用
                success = invokeDeviceService(reqVO.getDeviceId(), reqVO.getCommandName(), reqVO.getParams());
            } else if (reqVO.getNewValue() != null) {
                // 属性设置
                success = setDeviceProperty(reqVO.getDeviceId(), reqVO.getCommandName(), reqVO.getNewValue());
            } else {
                success = invokeDeviceService(reqVO.getDeviceId(), reqVO.getCommandName(), new HashMap<>());
            }

            respVO.setSuccess(success);
            respVO.setOldValue(reqVO.getOldValue());
            respVO.setNewValue(reqVO.getNewValue());
            controlLog.setExecutionStatus(success ? 1 : 0);

            if (!success) {
                respVO.setErrorMessage("命令执行失败");
                controlLog.setErrorMessage("命令执行失败");
            }
        } catch (Exception e) {
            log.error("[handleControlCommand] 控制命令执行异常: deviceId={}, command={}, error={}",
                    reqVO.getDeviceId(), reqVO.getCommandName(), e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage(e.getMessage());
            controlLog.setExecutionStatus(0);
            controlLog.setErrorMessage(e.getMessage());
        }

        // 记录执行时间
        int executionTime = (int) (System.currentTimeMillis() - startTime);
        respVO.setExecutionTime(executionTime);
        respVO.setExecuteTime(LocalDateTime.now());
        controlLog.setExecutionTime(executionTime);

        // 保存控制日志
        controlLogMapper.insert(controlLog);
        respVO.setLogId(controlLog.getId());

        return respVO;
    }

    @Override
    public boolean invokeDeviceService(Long deviceId, String serviceName, Map<String, Object> params) {
        try {
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                log.warn("[invokeDeviceService] 设备不存在: deviceId={}", deviceId);
                return false;
            }

            // 构建服务调用消息
            IotDeviceMessage message = new IotDeviceMessage();
            message.setDeviceId(deviceId);
            message.setMethod("thing.service." + serviceName);
            message.setData(params);

            deviceMessageService.sendDeviceMessage(message, device);
            log.info("[invokeDeviceService] 发送服务调用: deviceId={}, service={}", deviceId, serviceName);
            return true;
        } catch (Exception e) {
            log.error("[invokeDeviceService] 服务调用失败: deviceId={}, service={}, error={}",
                    deviceId, serviceName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setDeviceProperty(Long deviceId, String propertyIdentifier, Object value) {
        try {
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                log.warn("[setDeviceProperty] 设备不存在: deviceId={}", deviceId);
                return false;
            }

            // 构建属性设置消息
            Map<String, Object> properties = new HashMap<>();
            properties.put(propertyIdentifier, value);

            IotDeviceMessage message = new IotDeviceMessage();
            message.setDeviceId(deviceId);
            message.setMethod("thing.property.set");
            message.setData(properties);

            deviceMessageService.sendDeviceMessage(message, device);
            log.info("[setDeviceProperty] 发送属性设置: deviceId={}, property={}, value={}",
                    deviceId, propertyIdentifier, value);
            return true;
        } catch (Exception e) {
            log.error("[setDeviceProperty] 属性设置失败: deviceId={}, property={}, error={}",
                    deviceId, propertyIdentifier, e.getMessage(), e);
            return false;
        }
    }

    // ========== 设备数据相关 ==========

    @Override
    public List<ScadaDeviceVO> getScadaDevices() {
        // 获取当前租户的所有在线设备
        List<IotDeviceDO> devices = deviceService.getDeviceListByState(1); // 在线状态
        return devices.stream()
                .map(this::convertToScadaDeviceVO)
                .collect(Collectors.toList());
    }

    @Override
    public ScadaDeviceVO getScadaDevice(Long deviceId) {
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            return null;
        }
        return convertToScadaDeviceVO(device);
    }

    @Override
    public Map<String, Object> getDeviceRealtime(Long deviceId) {
        Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(deviceId);
        Map<String, Object> result = new HashMap<>();
        if (properties != null) {
            properties.forEach((key, prop) -> {
                if (prop != null && prop.getValue() != null) {
                    result.put(key, prop.getValue());
                }
            });
        }
        return result;
    }

    @Override
    public Map<Long, Map<String, Object>> getDevicesRealtime(List<Long> deviceIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Long deviceId : deviceIds) {
            result.put(deviceId, getDeviceRealtime(deviceId));
        }
        return result;
    }

    @Override
    public List<ScadaHistoryPointVO> getDeviceHistory(ScadaHistoryQueryReqVO reqVO) {
        // TODO: 实现历史数据查询，需要集成时序数据库
        log.info("[getDeviceHistory] 查询历史数据: deviceId={}, properties={}, startTime={}, endTime={}",
                reqVO.getDeviceId(), reqVO.getProperties(), reqVO.getStartTime(), reqVO.getEndTime());
        return new ArrayList<>();
    }

    // ========== Tag 映射相关 ==========

    @Override
    public List<ScadaTagMappingVO> getDeviceTagMappings(Long deviceId) {
        List<IotScadaTagMappingDO> mappings = tagMappingMapper.selectListByDeviceId(deviceId);
        return mappings.stream()
                .map(this::convertToTagMappingVO)
                .collect(Collectors.toList());
    }

    @Override
    public Object getTagValue(String tagName) {
        IotScadaTagMappingDO mapping = tagMappingMapper.selectByTagName(tagName);
        if (mapping == null || mapping.getDeviceId() == null) {
            return null;
        }

        Map<String, Object> realtime = getDeviceRealtime(mapping.getDeviceId());
        return realtime.get(mapping.getPropertyIdentifier());
    }

    @Override
    public Map<String, Object> getTagValues(List<String> tagNames) {
        Map<String, Object> result = new HashMap<>();
        List<IotScadaTagMappingDO> mappings = tagMappingMapper.selectListByTagNames(tagNames);

        // 按设备分组
        Map<Long, List<IotScadaTagMappingDO>> deviceMappings = mappings.stream()
                .filter(m -> m.getDeviceId() != null)
                .collect(Collectors.groupingBy(IotScadaTagMappingDO::getDeviceId));

        // 批量获取设备实时数据
        for (Map.Entry<Long, List<IotScadaTagMappingDO>> entry : deviceMappings.entrySet()) {
            Map<String, Object> deviceRealtime = getDeviceRealtime(entry.getKey());
            for (IotScadaTagMappingDO mapping : entry.getValue()) {
                Object value = deviceRealtime.get(mapping.getPropertyIdentifier());
                result.put(mapping.getTagName(), value);
            }
        }
        return result;
    }

    // ========== 告警相关 ==========

    @Override
    public List<ScadaAlarmVO> getActiveAlarms() {
        List<IotScadaAlarmHistoryDO> alarms = alarmHistoryMapper.selectActiveAlarms();
        return alarms.stream()
                .map(this::convertToAlarmVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScadaAlarmVO> getDeviceActiveAlarms(Long deviceId) {
        // 获取设备关联的 Tag
        List<IotScadaTagMappingDO> mappings = tagMappingMapper.selectListByDeviceId(deviceId);
        if (CollUtil.isEmpty(mappings)) {
            return new ArrayList<>();
        }

        List<String> tagIds = mappings.stream()
                .map(IotScadaTagMappingDO::getTagId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(tagIds)) {
            return new ArrayList<>();
        }

        // 查询这些 Tag 的活动告警
        List<IotScadaAlarmHistoryDO> alarms = alarmHistoryMapper.selectActiveAlarms();
        return alarms.stream()
                .filter(a -> tagIds.contains(a.getTagId()))
                .map(this::convertToAlarmVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean acknowledgeAlarm(Long alarmHistoryId, String acknowledgedBy) {
        int updated = alarmHistoryMapper.acknowledgeAlarm(alarmHistoryId, acknowledgedBy);
        if (updated > 0) {
            log.info("[acknowledgeAlarm] 告警已确认: id={}, by={}", alarmHistoryId, acknowledgedBy);
        }
        return updated > 0;
    }

    @Override
    public boolean closeAlarm(Long alarmHistoryId, String closedBy, String notes) {
        int updated = alarmHistoryMapper.closeAlarm(alarmHistoryId, closedBy, notes);
        if (updated > 0) {
            log.info("[closeAlarm] 告警已关闭: id={}, by={}", alarmHistoryId, closedBy);
        }
        return updated > 0;
    }

    @Override
    public Map<Integer, Long> getAlarmStatistics() {
        return alarmHistoryMapper.selectCountGroupByStatus();
    }

    // ========== 控制日志相关 ==========

    @Override
    public Long logControlOperation(IotScadaControlLogDO controlLog) {
        controlLog.setCreateTime(LocalDateTime.now());
        controlLog.setTenantId(TenantContextHolder.getTenantId());
        controlLogMapper.insert(controlLog);
        return controlLog.getId();
    }

    @Override
    public Map<Integer, Long> getControlLogStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return controlLogMapper.selectCountGroupByStatus(startTime, endTime);
    }

    // ========== 仪表板相关 ==========

    @Override
    public List<ScadaDashboardVO> getDashboards() {
        List<IotScadaDashboardDO> dashboards = dashboardMapper.selectListByStatus(1);
        return dashboards.stream()
                .map(this::convertToDashboardVO)
                .collect(Collectors.toList());
    }

    @Override
    public ScadaDashboardVO getDefaultDashboard() {
        IotScadaDashboardDO dashboard = dashboardMapper.selectDefault();
        if (dashboard == null) {
            // 如果没有默认仪表板，返回第一个
            List<IotScadaDashboardDO> dashboards = dashboardMapper.selectListByStatus(1);
            if (!dashboards.isEmpty()) {
                dashboard = dashboards.get(0);
            }
        }
        return dashboard != null ? convertToDashboardVO(dashboard) : null;
    }

    @Override
    public String generateFuxaUrl(String dashboardId, String token) {
        if (StrUtil.isBlank(dashboardId)) {
            return fuxaBaseUrl;
        }
        // 构建带认证的 FUXA URL
        return String.format("%s/view/%s?token=%s", fuxaBaseUrl, dashboardId, token);
    }

    // ========== 私有方法 ==========

    /**
     * 构建 MQTT 主题
     */
    private String buildMqttTopic(Long tenantId, Long deviceId, String suffix) {
        if (deviceId != null) {
            return String.format("%s/%d/device/%d/%s", mqttTopicPrefix, tenantId, deviceId, suffix);
        }
        return String.format("%s/%d/%s", mqttTopicPrefix, tenantId, suffix);
    }

    /**
     * 转换为 ScadaDeviceVO
     */
    private ScadaDeviceVO convertToScadaDeviceVO(IotDeviceDO device) {
        ScadaDeviceVO vo = new ScadaDeviceVO();
        vo.setDeviceId(device.getId());
        vo.setDeviceName(device.getDeviceName());
        vo.setNickname(device.getNickname());
        vo.setProductId(device.getProductId());
        vo.setProductKey(device.getProductKey());
        vo.setDeviceType(device.getDeviceType());
        vo.setState(device.getState());
        vo.setStateDesc(getStateDesc(device.getState()));
        vo.setPicUrl(device.getPicUrl());
        vo.setLastOnlineTime(device.getOnlineTime());
        vo.setLastOfflineTime(device.getOfflineTime());
        vo.setControllable(true);

        if (device.getLatitude() != null) {
            vo.setLatitude(device.getLatitude().doubleValue());
        }
        if (device.getLongitude() != null) {
            vo.setLongitude(device.getLongitude().doubleValue());
        }
        vo.setAddress(device.getAddress());

        // 获取实时属性
        try {
            vo.setProperties(getDeviceRealtime(device.getId()));
        } catch (Exception e) {
            log.warn("[convertToScadaDeviceVO] 获取实时属性失败: deviceId={}", device.getId());
            vo.setProperties(new HashMap<>());
        }

        // 获取活动告警数量
        List<ScadaAlarmVO> alarms = getDeviceActiveAlarms(device.getId());
        vo.setActiveAlarmCount(alarms.size());
        vo.setHasActiveAlarm(!alarms.isEmpty());

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStateDesc(Integer state) {
        if (state == null) {
            return "未知";
        }
        switch (state) {
            case 0:
                return "未激活";
            case 1:
                return "在线";
            case 2:
                return "离线";
            case 3:
                return "禁用";
            default:
                return "未知";
        }
    }

    /**
     * 转换为 TagMappingVO
     */
    private ScadaTagMappingVO convertToTagMappingVO(IotScadaTagMappingDO mapping) {
        ScadaTagMappingVO vo = new ScadaTagMappingVO();
        vo.setId(mapping.getId());
        vo.setTagName(mapping.getTagName());
        vo.setTagId(mapping.getTagId());
        vo.setDeviceId(mapping.getDeviceId());
        vo.setPropertyIdentifier(mapping.getPropertyIdentifier());
        vo.setDataType(mapping.getDataType());
        vo.setUnit(mapping.getUnit());
        vo.setScaleFactor(mapping.getScaleFactor());
        vo.setOffset(mapping.getOffset());
        vo.setMinValue(mapping.getMinValue());
        vo.setMaxValue(mapping.getMaxValue());
        vo.setDescription(mapping.getDescription());

        // 获取当前值
        if (mapping.getDeviceId() != null && StrUtil.isNotBlank(mapping.getPropertyIdentifier())) {
            try {
                Map<String, Object> realtime = getDeviceRealtime(mapping.getDeviceId());
                vo.setCurrentValue(realtime.get(mapping.getPropertyIdentifier()));
            } catch (Exception e) {
                log.debug("[convertToTagMappingVO] 获取当前值失败: tagName={}", mapping.getTagName());
            }
        }

        return vo;
    }

    /**
     * 转换为 AlarmVO
     */
    private ScadaAlarmVO convertToAlarmVO(IotScadaAlarmHistoryDO alarm) {
        ScadaAlarmVO vo = new ScadaAlarmVO();
        vo.setId(alarm.getId());
        vo.setAlarmId(alarm.getAlarmId());
        vo.setAlarmName(alarm.getAlarmName());
        vo.setTagId(alarm.getTagId());
        vo.setTagValue(alarm.getTagValue());
        vo.setPriority(alarm.getPriority());
        vo.setPriorityDesc(getPriorityDesc(alarm.getPriority()));
        vo.setMessage(alarm.getMessage());
        vo.setStatus(alarm.getStatus());
        vo.setStatusDesc(getAlarmStatusDesc(alarm.getStatus()));
        vo.setTriggeredAt(alarm.getTriggeredAt());
        vo.setAcknowledgedAt(alarm.getAcknowledgedAt());
        vo.setAcknowledgedBy(alarm.getAcknowledgedBy());
        vo.setRecoveredAt(alarm.getRecoveredAt());
        vo.setClosedAt(alarm.getClosedAt());
        vo.setClosedBy(alarm.getClosedBy());
        vo.setNotes(alarm.getNotes());

        // 计算持续时间
        if (alarm.getTriggeredAt() != null) {
            LocalDateTime endTime = alarm.getRecoveredAt() != null ? alarm.getRecoveredAt() : LocalDateTime.now();
            vo.setDurationSeconds(java.time.Duration.between(alarm.getTriggeredAt(), endTime).getSeconds());
        }

        return vo;
    }

    /**
     * 获取优先级描述
     */
    private String getPriorityDesc(Integer priority) {
        if (priority == null) {
            return "未知";
        }
        switch (priority) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "未知";
        }
    }

    /**
     * 获取告警状态描述
     */
    private String getAlarmStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "活动";
            case 2:
                return "已确认";
            case 3:
                return "已恢复";
            case 4:
                return "已关闭";
            default:
                return "未知";
        }
    }

    /**
     * 转换为 DashboardVO
     */
    private ScadaDashboardVO convertToDashboardVO(IotScadaDashboardDO dashboard) {
        ScadaDashboardVO vo = new ScadaDashboardVO();
        vo.setId(dashboard.getId());
        vo.setDashboardId(dashboard.getDashboardId());
        vo.setName(dashboard.getName());
        vo.setDescription(dashboard.getDescription());
        vo.setDashboardType(dashboard.getDashboardType());
        vo.setDashboardTypeDesc(getDashboardTypeDesc(dashboard.getDashboardType()));
        vo.setThumbnailUrl(dashboard.getThumbnailUrl());
        vo.setIsDefault(dashboard.getIsDefault());
        vo.setSortOrder(dashboard.getSortOrder());
        vo.setStatus(dashboard.getStatus());
        vo.setStatusDesc(dashboard.getStatus() == 1 ? "启用" : "禁用");
        vo.setFuxaUrl(generateFuxaUrl(dashboard.getDashboardId(), null));
        vo.setCreateTime(dashboard.getCreateTime());
        vo.setUpdateTime(dashboard.getUpdateTime());
        vo.setCreator(dashboard.getCreator());
        return vo;
    }

    /**
     * 获取仪表板类型描述
     */
    private String getDashboardTypeDesc(String type) {
        if (StrUtil.isBlank(type)) {
            return "自定义";
        }
        switch (type) {
            case "water":
                return "供水系统";
            case "hvac":
                return "暖通空调";
            case "energy":
                return "能源管理";
            case "custom":
                return "自定义";
            default:
                return type;
        }
    }

}
