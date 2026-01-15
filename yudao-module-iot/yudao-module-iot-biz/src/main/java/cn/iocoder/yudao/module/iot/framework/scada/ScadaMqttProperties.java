package cn.iocoder.yudao.module.iot.framework.scada;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * SCADA MQTT 配置属性
 *
 * 用于配置 SCADA 模块与 MQTT Broker 的连接参数
 *
 * Part of SCADA-013: Configure Spring MQTT Integration
 *
 * @author HR-IoT Team
 */
@ConfigurationProperties(prefix = "scada.mqtt")
@Data
@Validated
public class ScadaMqttProperties {

    /**
     * 是否启用 SCADA MQTT 集成
     */
    private boolean enabled = true;

    /**
     * MQTT Broker URL
     * 格式: tcp://host:port 或 ssl://host:port
     */
    @NotBlank(message = "MQTT Broker URL 不能为空")
    private String brokerUrl = "tcp://localhost:1883";

    /**
     * 客户端 ID 前缀
     * 完整客户端 ID = prefix + "-" + UUID
     */
    private String clientIdPrefix = "hr-iot-scada";

    /**
     * 用户名 (可选)
     */
    private String username;

    /**
     * 密码 (可选)
     */
    private String password;

    /**
     * 是否使用 SSL/TLS
     */
    private boolean ssl = false;

    /**
     * 连接超时时间 (秒)
     */
    private int connectionTimeout = 30;

    /**
     * 保持活动间隔 (秒)
     */
    private int keepAliveInterval = 60;

    /**
     * 是否自动重连
     */
    private boolean automaticReconnect = true;

    /**
     * 重连间隔 (毫秒)
     */
    private int reconnectInterval = 5000;

    /**
     * 最大重连尝试次数 (0 = 无限)
     */
    private int maxReconnectAttempts = 0;

    /**
     * 是否清除会话
     */
    private boolean cleanSession = true;

    /**
     * 默认 QoS 级别 (0, 1, 2)
     */
    private int defaultQos = 1;

    /**
     * 是否保留消息
     */
    private boolean defaultRetained = false;

    /**
     * SCADA 主题前缀
     */
    private String topicPrefix = "scada";

    /**
     * 控制命令主题模板
     * 占位符: {tenantId}, {deviceId}
     */
    private String controlTopicTemplate = "scada/{tenantId}/device/{deviceId}/control";

    /**
     * 属性更新主题模板
     */
    private String propertyTopicTemplate = "scada/{tenantId}/device/{deviceId}/properties";

    /**
     * 状态变化主题模板
     */
    private String stateTopicTemplate = "scada/{tenantId}/device/{deviceId}/state";

    /**
     * 告警主题模板
     */
    private String alarmTopicTemplate = "scada/{tenantId}/alarm";

    /**
     * 订阅控制命令的主题通配符
     */
    private String controlSubscribeTopic = "scada/+/device/+/control/#";

    /**
     * 发布线程池大小
     */
    private int publisherThreadPoolSize = 4;

    /**
     * 订阅者线程池大小
     */
    private int subscriberThreadPoolSize = 2;

    /**
     * 消息队列大小
     */
    private int messageQueueSize = 1000;

    /**
     * 完成超时时间 (毫秒)
     */
    private int completionTimeout = 30000;

    /**
     * 构建控制主题
     */
    public String buildControlTopic(Long tenantId, Long deviceId) {
        return controlTopicTemplate
                .replace("{tenantId}", String.valueOf(tenantId))
                .replace("{deviceId}", String.valueOf(deviceId));
    }

    /**
     * 构建属性主题
     */
    public String buildPropertyTopic(Long tenantId, Long deviceId) {
        return propertyTopicTemplate
                .replace("{tenantId}", String.valueOf(tenantId))
                .replace("{deviceId}", String.valueOf(deviceId));
    }

    /**
     * 构建状态主题
     */
    public String buildStateTopic(Long tenantId, Long deviceId) {
        return stateTopicTemplate
                .replace("{tenantId}", String.valueOf(tenantId))
                .replace("{deviceId}", String.valueOf(deviceId));
    }

    /**
     * 构建告警主题
     */
    public String buildAlarmTopic(Long tenantId) {
        return alarmTopicTemplate
                .replace("{tenantId}", String.valueOf(tenantId));
    }

}
