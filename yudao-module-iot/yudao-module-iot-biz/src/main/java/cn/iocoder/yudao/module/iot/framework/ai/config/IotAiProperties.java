package cn.iocoder.yudao.module.iot.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IoT AI 配置属性
 */
@Data
@ConfigurationProperties(prefix = "yudao.iot.ai")
public class IotAiProperties {

    /**
     * 是否启用 AI 助手
     */
    private boolean enabled = false;

    /**
     * LLM API 基础 URL (OpenAI 兼容格式)
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * 最大 Token 数
     */
    private int maxTokens = 4096;

    /**
     * 温度参数 (0-1, 越低越确定性)
     */
    private double temperature = 0.3;

}
