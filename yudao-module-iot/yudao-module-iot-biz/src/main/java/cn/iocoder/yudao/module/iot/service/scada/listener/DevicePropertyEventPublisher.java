package cn.iocoder.yudao.module.iot.service.scada.listener;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.scada.listener.DevicePropertyChangeEvent.EventType;
import cn.iocoder.yudao.module.iot.service.scada.listener.DevicePropertyChangeEvent.PropertyChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备属性变化事件发布器
 *
 * 提供便捷方法用于发布设备属性变化事件
 * 在设备属性保存后调用此类的方法发布事件
 *
 * Part of SCADA-015: Implement Device Property Change Listener
 *
 * @author HR-IoT Team
 */
@Component
@Slf4j
public class DevicePropertyEventPublisher {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 发布属性上报事件
     *
     * @param device     设备信息
     * @param properties 属性数据
     * @param reportTime 上报时间
     */
    public void publishPropertyPost(IotDeviceDO device, Map<String, Object> properties, LocalDateTime reportTime) {
        publishEvent(device, properties, reportTime, EventType.PROPERTY_POST, null);
    }

    /**
     * 发布属性设置响应事件
     *
     * @param device     设备信息
     * @param properties 属性数据
     */
    public void publishPropertySetReply(IotDeviceDO device, Map<String, Object> properties) {
        publishEvent(device, properties, LocalDateTime.now(), EventType.PROPERTY_SET_REPLY, null);
    }

    /**
     * 发布设备上线事件
     *
     * @param device 设备信息
     */
    public void publishDeviceOnline(IotDeviceDO device) {
        publishEvent(device, null, LocalDateTime.now(), EventType.DEVICE_ONLINE, 1);
    }

    /**
     * 发布设备离线事件
     *
     * @param device 设备信息
     */
    public void publishDeviceOffline(IotDeviceDO device) {
        publishEvent(device, null, LocalDateTime.now(), EventType.DEVICE_OFFLINE, 2);
    }

    /**
     * 发布设备状态变化事件
     *
     * @param device 设备信息
     * @param state  新状态
     */
    public void publishStateChange(IotDeviceDO device, Integer state) {
        publishEvent(device, null, LocalDateTime.now(), EventType.STATE_CHANGE, state);
    }

    /**
     * 发布事件
     */
    private void publishEvent(IotDeviceDO device, Map<String, Object> properties,
            LocalDateTime reportTime, EventType eventType, Integer state) {
        if (device == null) {
            log.warn("[publishEvent] 设备信息为空");
            return;
        }

        try {
            PropertyChangeData data = PropertyChangeData.builder()
                    .tenantId(device.getTenantId())
                    .deviceId(device.getId())
                    .deviceName(device.getDeviceName())
                    .productId(device.getProductId())
                    .productKey(device.getProductKey())
                    .properties(properties)
                    .reportTime(reportTime)
                    .timestamp(System.currentTimeMillis())
                    .eventType(eventType)
                    .deviceState(state)
                    .build();

            DevicePropertyChangeEvent event = new DevicePropertyChangeEvent(this, data);
            eventPublisher.publishEvent(event);

            log.debug("[publishEvent] 发布事件成功: deviceId={}, eventType={}",
                    device.getId(), eventType);
        } catch (Exception e) {
            log.error("[publishEvent] 发布事件失败: deviceId={}, error={}",
                    device.getId(), e.getMessage(), e);
        }
    }

}
