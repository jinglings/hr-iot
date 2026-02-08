package cn.iocoder.yudao.module.iot.framework.ai.config;

import cn.iocoder.yudao.module.iot.framework.ai.client.IotAiClient;
import cn.iocoder.yudao.module.iot.framework.ai.client.IotAiClientImpl;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiToolExecutor;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiToolRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IoT AI 自动配置
 */
@Configuration
@ConditionalOnProperty(prefix = "yudao.iot.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(IotAiProperties.class)
public class IotAiAutoConfiguration {

    @Bean
    public IotAiClient iotAiClient(IotAiProperties properties) {
        return new IotAiClientImpl(properties);
    }

    @Bean
    public IotAiToolRegistry iotAiToolRegistry() {
        return new IotAiToolRegistry();
    }

    @Bean
    public IotAiToolExecutor iotAiToolExecutor(IotAiToolRegistry toolRegistry) {
        return new IotAiToolExecutor(toolRegistry);
    }
}
