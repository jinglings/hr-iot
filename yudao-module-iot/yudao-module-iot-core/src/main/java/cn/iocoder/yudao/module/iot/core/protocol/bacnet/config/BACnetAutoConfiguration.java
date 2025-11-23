package cn.iocoder.yudao.module.iot.core.protocol.bacnet.config;

import cn.iocoder.yudao.module.iot.core.protocol.bacnet.core.BACnetDeviceManager;
import lombok.extern.slf4j.Slf4j;
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
     * 注意: 该Bean总是会被创建，但是否启用由BACnetDeviceManager内部的enabled属性控制
     * 这样可以避免其他依赖BACnetDeviceManager的Bean找不到依赖的问题
     */
    @Bean
    public BACnetDeviceManager bacnetDeviceManager(BACnetProperties properties) {
        log.info("[bacnetDeviceManager][创建 BACnet 设备管理器 Bean, enabled={}]", properties.getEnabled());
        return new BACnetDeviceManager(properties);
    }

}
