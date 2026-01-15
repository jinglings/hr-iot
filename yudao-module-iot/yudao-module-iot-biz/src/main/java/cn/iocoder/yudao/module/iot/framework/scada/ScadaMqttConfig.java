package cn.iocoder.yudao.module.iot.framework.scada;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * SCADA MQTT 配置类
 *
 * 配置 MQTT 客户端，支持发布和订阅功能
 *
 * Part of SCADA-013: Configure Spring MQTT Integration
 *
 * @author HR-IoT Team
 */
@Configuration
@EnableConfigurationProperties(ScadaMqttProperties.class)
@ConditionalOnProperty(prefix = "scada.mqtt", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class ScadaMqttConfig implements InitializingBean, DisposableBean {

    @Resource
    private ScadaMqttProperties properties;

    private MqttAsyncClient mqttClient;
    private ExecutorService publisherExecutor;
    private ExecutorService subscriberExecutor;
    private final Map<String, BiConsumer<String, byte[]>> topicHandlers = new ConcurrentHashMap<>();
    private volatile boolean connected = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!properties.isEnabled()) {
            log.info("[ScadaMqttConfig] SCADA MQTT 集成已禁用");
            return;
        }

        // 初始化线程池
        publisherExecutor = new ThreadPoolExecutor(
                properties.getPublisherThreadPoolSize(),
                properties.getPublisherThreadPoolSize(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getMessageQueueSize()),
                r -> new Thread(r, "scada-mqtt-publisher"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        subscriberExecutor = new ThreadPoolExecutor(
                properties.getSubscriberThreadPoolSize(),
                properties.getSubscriberThreadPoolSize(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getMessageQueueSize()),
                r -> new Thread(r, "scada-mqtt-subscriber"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 连接 MQTT Broker
        connect();
    }

    /**
     * 连接到 MQTT Broker
     */
    private void connect() {
        try {
            String clientId = properties.getClientIdPrefix() + "-" + UUID.randomUUID().toString().substring(0, 8);

            mqttClient = new MqttAsyncClient(
                    properties.getBrokerUrl(),
                    clientId,
                    new MemoryPersistence());

            // 配置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(properties.isCleanSession());
            options.setConnectionTimeout(properties.getConnectionTimeout());
            options.setKeepAliveInterval(properties.getKeepAliveInterval());
            options.setAutomaticReconnect(properties.isAutomaticReconnect());

            if (StrUtil.isNotBlank(properties.getUsername())) {
                options.setUserName(properties.getUsername());
            }
            if (StrUtil.isNotBlank(properties.getPassword())) {
                options.setPassword(properties.getPassword().toCharArray());
            }

            // 设置回调
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;
                    log.warn("[ScadaMqttConfig] MQTT 连接断开: {}", cause.getMessage());
                    if (properties.isAutomaticReconnect()) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleMessage(topic, message.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.debug("[ScadaMqttConfig] 消息投递完成: messageId={}", token.getMessageId());
                }
            });

            // 异步连接
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connected = true;
                    log.info("[ScadaMqttConfig] MQTT 连接成功: broker={}, clientId={}",
                            properties.getBrokerUrl(), clientId);
                    // 订阅控制命令主题
                    subscribeToControlTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    connected = false;
                    log.error("[ScadaMqttConfig] MQTT 连接失败: broker={}, error={}",
                            properties.getBrokerUrl(), exception.getMessage());
                    if (properties.isAutomaticReconnect()) {
                        scheduleReconnect();
                    }
                }
            });

        } catch (MqttException e) {
            log.error("[ScadaMqttConfig] MQTT 客户端初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 订阅控制命令主题
     */
    private void subscribeToControlTopics() {
        if (mqttClient == null || !connected) {
            return;
        }

        try {
            String controlTopic = properties.getControlSubscribeTopic();
            mqttClient.subscribe(controlTopic, properties.getDefaultQos(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("[ScadaMqttConfig] 订阅控制主题成功: {}", controlTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    log.error("[ScadaMqttConfig] 订阅控制主题失败: {}, error={}",
                            controlTopic, exception.getMessage());
                }
            });
        } catch (MqttException e) {
            log.error("[ScadaMqttConfig] 订阅失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理接收到的消息
     */
    private void handleMessage(String topic, byte[] payload) {
        subscriberExecutor.submit(() -> {
            try {
                log.debug("[ScadaMqttConfig] 收到消息: topic={}, size={}", topic, payload.length);

                // 查找匹配的处理器
                for (Map.Entry<String, BiConsumer<String, byte[]>> entry : topicHandlers.entrySet()) {
                    if (topicMatches(entry.getKey(), topic)) {
                        entry.getValue().accept(topic, payload);
                        return;
                    }
                }

                // 默认处理：解析控制命令
                if (topic.contains("/control")) {
                    handleControlMessage(topic, payload);
                }
            } catch (Exception e) {
                log.error("[ScadaMqttConfig] 处理消息异常: topic={}, error={}", topic, e.getMessage(), e);
            }
        });
    }

    /**
     * 处理控制命令消息
     */
    private void handleControlMessage(String topic, byte[] payload) {
        try {
            String payloadStr = new String(payload, StandardCharsets.UTF_8);
            log.info("[ScadaMqttConfig] 收到控制命令: topic={}, payload={}", topic, payloadStr);

            // 解析主题获取租户ID和设备ID
            // 格式: scada/{tenantId}/device/{deviceId}/control
            String[] parts = topic.split("/");
            if (parts.length >= 4) {
                Long tenantId = Long.parseLong(parts[1]);
                Long deviceId = Long.parseLong(parts[3]);

                // TODO: 调用 ScadaBridgeService 处理控制命令
                log.info("[ScadaMqttConfig] 解析控制命令: tenantId={}, deviceId={}", tenantId, deviceId);
            }
        } catch (Exception e) {
            log.error("[ScadaMqttConfig] 处理控制命令失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查主题是否匹配（支持通配符）
     */
    private boolean topicMatches(String pattern, String topic) {
        String regex = pattern.replace("+", "[^/]+").replace("#", ".*");
        return topic.matches(regex);
    }

    /**
     * 调度重连
     */
    private void scheduleReconnect() {
        CompletableFuture.delayedExecutor(properties.getReconnectInterval(), TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (!connected && properties.isAutomaticReconnect()) {
                        log.info("[ScadaMqttConfig] 尝试重新连接 MQTT...");
                        connect();
                    }
                });
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param payload 消息内容
     * @return 是否成功
     */
    public boolean publish(String topic, Object payload) {
        return publish(topic, payload, properties.getDefaultQos(), properties.isDefaultRetained());
    }

    /**
     * 发布消息
     *
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      QoS 级别
     * @param retained 是否保留
     * @return 是否成功
     */
    public boolean publish(String topic, Object payload, int qos, boolean retained) {
        if (!connected || mqttClient == null) {
            log.warn("[ScadaMqttConfig] MQTT 未连接，无法发布消息: topic={}", topic);
            return false;
        }

        publisherExecutor.submit(() -> {
            try {
                byte[] bytes;
                if (payload instanceof byte[]) {
                    bytes = (byte[]) payload;
                } else if (payload instanceof String) {
                    bytes = ((String) payload).getBytes(StandardCharsets.UTF_8);
                } else {
                    bytes = JsonUtils.toJsonString(payload).getBytes(StandardCharsets.UTF_8);
                }

                MqttMessage message = new MqttMessage(bytes);
                message.setQos(qos);
                message.setRetained(retained);

                mqttClient.publish(topic, message, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        log.debug("[ScadaMqttConfig] 发布消息成功: topic={}", topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        log.error("[ScadaMqttConfig] 发布消息失败: topic={}, error={}",
                                topic, exception.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("[ScadaMqttConfig] 发布消息异常: topic={}, error={}", topic, e.getMessage(), e);
            }
        });

        return true;
    }

    /**
     * 注册主题处理器
     *
     * @param topicPattern 主题模式（支持 + 和 # 通配符）
     * @param handler      处理器
     */
    public void registerHandler(String topicPattern, BiConsumer<String, byte[]> handler) {
        topicHandlers.put(topicPattern, handler);
        log.info("[ScadaMqttConfig] 注册主题处理器: {}", topicPattern);
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }

    @Override
    public void destroy() throws Exception {
        log.info("[ScadaMqttConfig] 正在关闭 MQTT 连接...");

        connected = false;

        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                log.warn("[ScadaMqttConfig] 关闭 MQTT 连接异常: {}", e.getMessage());
            }
        }

        if (publisherExecutor != null) {
            publisherExecutor.shutdown();
        }
        if (subscriberExecutor != null) {
            subscriberExecutor.shutdown();
        }

        log.info("[ScadaMqttConfig] MQTT 连接已关闭");
    }

}
