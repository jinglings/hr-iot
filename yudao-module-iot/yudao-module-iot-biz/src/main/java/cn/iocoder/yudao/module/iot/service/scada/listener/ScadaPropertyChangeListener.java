package cn.iocoder.yudao.module.iot.service.scada.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.scada.IotScadaTagMappingDO;
import cn.iocoder.yudao.module.iot.dal.mysql.scada.IotScadaTagMappingMapper;
import cn.iocoder.yudao.module.iot.framework.scada.ScadaMqttPublisher;
import cn.iocoder.yudao.module.iot.service.scada.listener.DevicePropertyChangeEvent.EventType;
import cn.iocoder.yudao.module.iot.service.scada.listener.DevicePropertyChangeEvent.PropertyChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SCADA 属性变化监听器
 *
 * 监听设备属性变化事件，将数据转换为 SCADA Tag 格式并发布到 MQTT
 *
 * Part of SCADA-015: Implement Device Property Change Listener
 *
 * @author HR-IoT Team
 */
@Component
@ConditionalOnBean(ScadaMqttPublisher.class)
@Slf4j
public class ScadaPropertyChangeListener {

    @Resource
    private ScadaMqttPublisher mqttPublisher;

    @Resource
    private IotScadaTagMappingMapper tagMappingMapper;

    /**
     * 处理设备属性变化事件
     *
     * @param event 属性变化事件
     */
    @EventListener
    @Async("scadaEventExecutor")
    public void onPropertyChange(DevicePropertyChangeEvent event) {
        PropertyChangeData data = event.getData();
        if (data == null || data.getDeviceId() == null) {
            log.warn("[onPropertyChange] 事件数据为空");
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 根据事件类型处理
            switch (data.getEventType()) {
                case PROPERTY_POST:
                case PROPERTY_SET_REPLY:
                    handlePropertyUpdate(data);
                    break;
                case DEVICE_ONLINE:
                case DEVICE_OFFLINE:
                case STATE_CHANGE:
                    handleStateChange(data);
                    break;
                default:
                    log.warn("[onPropertyChange] 未知事件类型: {}", data.getEventType());
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 100) {
                log.warn("[onPropertyChange] 处理时间过长: {}ms, deviceId={}", elapsed, data.getDeviceId());
            } else {
                log.debug("[onPropertyChange] 处理完成: {}ms, deviceId={}", elapsed, data.getDeviceId());
            }
        } catch (Exception e) {
            log.error("[onPropertyChange] 处理事件失败: deviceId={}, error={}",
                    data.getDeviceId(), e.getMessage(), e);
        }
    }

    /**
     * 处理属性更新
     */
    private void handlePropertyUpdate(PropertyChangeData data) {
        if (CollUtil.isEmpty(data.getProperties())) {
            return;
        }

        // 1. 获取设备的 Tag 映射
        List<IotScadaTagMappingDO> tagMappings = tagMappingMapper.selectListByDeviceId(data.getDeviceId());

        // 2. 转换属性为 SCADA Tag 格式
        Map<String, Object> scadaProperties = new HashMap<>();
        Map<String, Object> tagValues = new HashMap<>();

        for (Map.Entry<String, Object> entry : data.getProperties().entrySet()) {
            String propertyIdentifier = entry.getKey();
            Object value = entry.getValue();

            // 查找对应的 Tag 映射
            IotScadaTagMappingDO tagMapping = CollUtil.findOne(tagMappings,
                    m -> propertyIdentifier.equals(m.getPropertyIdentifier()));

            if (tagMapping != null) {
                // 应用缩放和偏移
                Object transformedValue = transformValue(value, tagMapping);
                tagValues.put(tagMapping.getTagName(), transformedValue);
            }

            // 同时发布原始属性
            scadaProperties.put(propertyIdentifier, value);
        }

        // 3. 发布到 MQTT
        if (!scadaProperties.isEmpty()) {
            boolean published = mqttPublisher.publishProperties(
                    data.getTenantId(),
                    data.getDeviceId(),
                    scadaProperties);

            if (published) {
                log.debug("[handlePropertyUpdate] 发布属性成功: deviceId={}, properties={}",
                        data.getDeviceId(), scadaProperties.keySet());
            }
        }

        // 4. 发布 Tag 值到专门的 Tag 主题（如果有映射）
        if (!tagValues.isEmpty()) {
            publishTagValues(data.getTenantId(), tagValues);
        }
    }

    /**
     * 处理设备状态变化
     */
    private void handleStateChange(PropertyChangeData data) {
        Integer state = data.getDeviceState();
        if (state == null) {
            // 根据事件类型推断状态
            if (data.getEventType() == EventType.DEVICE_ONLINE) {
                state = 1; // 在线
            } else if (data.getEventType() == EventType.DEVICE_OFFLINE) {
                state = 2; // 离线
            }
        }

        if (state != null) {
            boolean published = mqttPublisher.publishDeviceState(
                    data.getTenantId(),
                    data.getDeviceId(),
                    state);

            if (published) {
                log.info("[handleStateChange] 发布状态变化成功: deviceId={}, state={}",
                        data.getDeviceId(), state);
            }
        }
    }

    /**
     * 发布 Tag 值
     */
    private void publishTagValues(Long tenantId, Map<String, Object> tagValues) {
        String topic = String.format("scada/%d/tags", tenantId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", tenantId);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("tags", tagValues);

        mqttPublisher.publish(topic, payload);
        log.debug("[publishTagValues] 发布 Tag 值成功: tenantId={}, tags={}", tenantId, tagValues.keySet());
    }

    /**
     * 转换属性值（应用缩放因子和偏移量）
     *
     * @param value      原始值
     * @param tagMapping Tag 映射配置
     * @return 转换后的值
     */
    private Object transformValue(Object value, IotScadaTagMappingDO tagMapping) {
        if (value == null) {
            return null;
        }

        // 如果没有配置缩放或偏移，直接返回原值
        BigDecimal scaleFactor = tagMapping.getScaleFactor();
        BigDecimal offset = tagMapping.getOffset();

        if ((scaleFactor == null || scaleFactor.compareTo(BigDecimal.ONE) == 0) &&
                (offset == null || offset.compareTo(BigDecimal.ZERO) == 0)) {
            return value;
        }

        // 尝试转换为数值进行计算
        try {
            BigDecimal numValue;
            if (value instanceof Number) {
                numValue = new BigDecimal(value.toString());
            } else if (value instanceof String) {
                numValue = new BigDecimal((String) value);
            } else {
                // 非数值类型，直接返回
                return value;
            }

            // 应用公式: result = value * scaleFactor + offset
            if (scaleFactor != null && scaleFactor.compareTo(BigDecimal.ONE) != 0) {
                numValue = numValue.multiply(scaleFactor);
            }
            if (offset != null && offset.compareTo(BigDecimal.ZERO) != 0) {
                numValue = numValue.add(offset);
            }

            // 根据原始类型返回适当的类型
            if (value instanceof Integer) {
                return numValue.intValue();
            } else if (value instanceof Long) {
                return numValue.longValue();
            } else if (value instanceof Float) {
                return numValue.floatValue();
            } else if (value instanceof Double) {
                return numValue.doubleValue();
            } else {
                return numValue;
            }
        } catch (NumberFormatException e) {
            log.warn("[transformValue] 值转换失败: value={}, error={}", value, e.getMessage());
            return value;
        }
    }

}
