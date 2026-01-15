package cn.iocoder.yudao.module.iot.framework.scada;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * SCADA MQTT 发布服务实现
 *
 * Part of SCADA-013: Configure Spring MQTT Integration
 *
 * @author HR-IoT Team
 */
@Component
@ConditionalOnBean(ScadaMqttConfig.class)
@Slf4j
public class ScadaMqttPublisherImpl implements ScadaMqttPublisher {

    @Resource
    private ScadaMqttConfig mqttConfig;

    @Resource
    private ScadaMqttProperties properties;

    @Override
    public boolean publishProperties(Long tenantId, Long deviceId, Map<String, Object> propertiesData) {
        if (!isConnected()) {
            log.warn("[ScadaMqttPublisher] MQTT 未连接，无法发布属性: deviceId={}", deviceId);
            return false;
        }

        String topic = properties.buildPropertyTopic(tenantId, deviceId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", tenantId);
        payload.put("deviceId", deviceId);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("properties", propertiesData);

        return mqttConfig.publish(topic, payload);
    }

    @Override
    public boolean publishDeviceState(Long tenantId, Long deviceId, Integer state) {
        if (!isConnected()) {
            log.warn("[ScadaMqttPublisher] MQTT 未连接，无法发布状态: deviceId={}", deviceId);
            return false;
        }

        String topic = properties.buildStateTopic(tenantId, deviceId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", tenantId);
        payload.put("deviceId", deviceId);
        payload.put("state", state);
        payload.put("stateDesc", getStateDesc(state));
        payload.put("timestamp", System.currentTimeMillis());

        return mqttConfig.publish(topic, payload);
    }

    @Override
    public boolean publishAlarm(Long tenantId, Object alarm) {
        if (!isConnected()) {
            log.warn("[ScadaMqttPublisher] MQTT 未连接，无法发布告警");
            return false;
        }

        String topic = properties.buildAlarmTopic(tenantId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", tenantId);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("alarm", alarm);

        return mqttConfig.publish(topic, payload);
    }

    @Override
    public boolean publish(String topic, Object payload) {
        return mqttConfig.publish(topic, payload);
    }

    @Override
    public boolean publish(String topic, Object payload, int qos, boolean retained) {
        return mqttConfig.publish(topic, payload, qos, retained);
    }

    @Override
    public boolean isConnected() {
        return mqttConfig != null && mqttConfig.isConnected();
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

}
