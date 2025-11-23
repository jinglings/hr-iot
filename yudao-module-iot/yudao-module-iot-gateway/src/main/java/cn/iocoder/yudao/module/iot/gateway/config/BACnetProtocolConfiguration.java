package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetDeviceConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.bacnet.IotBACnetPropertyMappingMapper;
import cn.iocoder.yudao.module.iot.gateway.protocol.bacnet.IotBACnetDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.bacnet.IotBACnetMasterProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BACnet 协议配置类
 *
 * @author 芋道源码
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "yudao.iot.bacnet", name = "enabled", havingValue = "true")
public class BACnetProtocolConfiguration {

    /**
     * 创建 BACnet 主站协议 Bean
     */
    @Bean
    public IotBACnetMasterProtocol iotBACnetMasterProtocol(
            BACnetDeviceManager bacnetDeviceManager,
            IotDeviceService deviceService,
            IotDeviceMessageProducer deviceMessageProducer,
            IotBACnetDeviceConfigMapper deviceConfigMapper,
            IotBACnetPropertyMappingMapper propertyMappingMapper) {

        log.info("[iotBACnetMasterProtocol][初始化 BACnet 主站协议]");
        return new IotBACnetMasterProtocol(
                bacnetDeviceManager,
                deviceService,
                deviceMessageProducer,
                deviceConfigMapper,
                propertyMappingMapper
        );
    }

    /**
     * 创建 BACnet 下行消息订阅器 Bean
     */
    @Bean
    public IotBACnetDownstreamSubscriber iotBACnetDownstreamSubscriber(
            BACnetDeviceManager bacnetDeviceManager,
            IotBACnetMasterProtocol masterProtocol,
            IotBACnetDeviceConfigMapper deviceConfigMapper,
            IotBACnetPropertyMappingMapper propertyMappingMapper,
            IotMessageBus messageBus) {

        log.info("[iotBACnetDownstreamSubscriber][初始化 BACnet 下行消息订阅器]");
        return new IotBACnetDownstreamSubscriber(
                bacnetDeviceManager,
                masterProtocol,
                deviceConfigMapper,
                propertyMappingMapper,
                messageBus
        );
    }

}
