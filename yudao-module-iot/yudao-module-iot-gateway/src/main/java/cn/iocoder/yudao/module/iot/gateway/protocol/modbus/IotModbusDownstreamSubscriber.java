package cn.iocoder.yudao.module.iot.gateway.protocol.modbus;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.manager.IotModbusConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.router.IotModbusDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * IoT 网关 Modbus 下游订阅者：接收下行给 Modbus 设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotModbusDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotModbusMasterProtocol protocol;

    private final IotDeviceService deviceService;

    private final IotModbusConnectionManager connectionManager;

    private final IotGatewayProperties.ModbusProperties modbusProperties;

    private final IotMessageBus messageBus;

    private IotModbusDownstreamHandler downstreamHandler;

    @PostConstruct
    public void init() {
        // 初始化下游处理器
        this.downstreamHandler = new IotModbusDownstreamHandler(deviceService, connectionManager, modbusProperties);

        messageBus.register(this);
        log.info("[init][Modbus 下游订阅者初始化完成，服务器 ID: {}，Topic: {}]",
                protocol.getServerId(), getTopic());
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        try {
            downstreamHandler.handle(message);
        } catch (Exception e) {
            log.error("[onMessage][处理 Modbus 下行消息失败，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId(), e);
        }
    }

}
