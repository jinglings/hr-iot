package cn.iocoder.yudao.module.iot.framework.scada;

import java.util.Map;

/**
 * SCADA MQTT 发布服务接口
 *
 * 提供 SCADA 系统的 MQTT 消息发布能力
 *
 * Part of SCADA-013: Configure Spring MQTT Integration
 *
 * @author HR-IoT Team
 */
public interface ScadaMqttPublisher {

    /**
     * 发布设备属性更新
     *
     * @param tenantId   租户 ID
     * @param deviceId   设备 ID
     * @param properties 属性 Map
     * @return 是否成功
     */
    boolean publishProperties(Long tenantId, Long deviceId, Map<String, Object> properties);

    /**
     * 发布设备状态变化
     *
     * @param tenantId 租户 ID
     * @param deviceId 设备 ID
     * @param state    状态
     * @return 是否成功
     */
    boolean publishDeviceState(Long tenantId, Long deviceId, Integer state);

    /**
     * 发布告警事件
     *
     * @param tenantId 租户 ID
     * @param alarm    告警数据
     * @return 是否成功
     */
    boolean publishAlarm(Long tenantId, Object alarm);

    /**
     * 发布自定义消息
     *
     * @param topic   主题
     * @param payload 消息内容
     * @return 是否成功
     */
    boolean publish(String topic, Object payload);

    /**
     * 发布自定义消息
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS 级别
     * @param retained 是否保留
     * @return 是否成功
     */
    boolean publish(String topic, Object payload, int qos, boolean retained);

    /**
     * 是否已连接
     *
     * @return 连接状态
     */
    boolean isConnected();

}
