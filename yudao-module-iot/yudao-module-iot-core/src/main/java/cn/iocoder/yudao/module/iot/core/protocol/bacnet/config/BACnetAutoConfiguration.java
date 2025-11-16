package cn.iocoder.yudao.module.iot.core.protocol.bacnet.config;

import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetClient;
import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BACnet 协议自动配置类
 *
 * @author yudao
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BACnetProperties.class)
public class BACnetAutoConfiguration {

    /**
     * BACnet 设备管理器
     * 只有当 yudao.iot.bacnet.enabled=true 时才创建
     */
    @Bean
    @ConditionalOnProperty(prefix = "yudao.iot.bacnet", name = "enabled", havingValue = "true")
    public BACnetDeviceManager bacnetDeviceManager(BACnetProperties properties) {
        log.info("创建 BACnet 设备管理器 Bean");
        return new BACnetDeviceManager(properties);
    }

}
