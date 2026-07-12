package cn.iocoder.yudao.module.iot.framework.freeze;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 设备属性【数据冻结检测】配置类
 *
 * @author HR-IoT Team
 */
@Configuration
@EnableConfigurationProperties(IotPropertyFreezeProperties.class)
public class IotPropertyFreezeConfiguration {
}
