package cn.iocoder.yudao.module.iot.service.scada.listener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备属性变化事件
 *
 * 当设备属性发生变化时发布此事件，SCADA 监听器会接收并将数据发布到 MQTT
 *
 * Part of SCADA-015: Implement Device Property Change Listener
 *
 * @author HR-IoT Team
 */
public class DevicePropertyChangeEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 事件数据
     */
    private final PropertyChangeData data;

    /**
     * 创建设备属性变化事件
     *
     * @param source 事件源
     * @param data   事件数据
     */
    public DevicePropertyChangeEvent(Object source, PropertyChangeData data) {
        super(source);
        this.data = data;
    }

    /**
     * 获取事件数据
     */
    public PropertyChangeData getData() {
        return data;
    }

    /**
     * 属性变化数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyChangeData {

        /**
         * 租户 ID
         */
        private Long tenantId;

        /**
         * 设备 ID
         */
        private Long deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 产品 ID
         */
        private Long productId;

        /**
         * 产品 Key
         */
        private String productKey;

        /**
         * 变化的属性 (identifier -> value)
         */
        private Map<String, Object> properties;

        /**
         * 属性上报时间
         */
        private LocalDateTime reportTime;

        /**
         * 事件时间戳 (毫秒)
         */
        private Long timestamp;

        /**
         * 事件类型
         */
        private EventType eventType;

        /**
         * 设备状态 (可选，状态变化时填充)
         */
        private Integer deviceState;

    }

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 属性上报
         */
        PROPERTY_POST,

        /**
         * 属性设置响应
         */
        PROPERTY_SET_REPLY,

        /**
         * 设备上线
         */
        DEVICE_ONLINE,

        /**
         * 设备离线
         */
        DEVICE_OFFLINE,

        /**
         * 设备状态变化
         */
        STATE_CHANGE
    }

}
