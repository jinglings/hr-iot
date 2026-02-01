package cn.iocoder.yudao.module.iot.core.protocol.bacnet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * BACnet 协议配置属性
 *
 * @author yudao
 */
@Data
@ConfigurationProperties(prefix = "yudao.iot.bacnet")
public class BACnetProperties {

    /**
     * 是否启用 BACnet 协议
     */
    private Boolean enabled = false;

    /**
     * BACnet 设备 ID
     */
    private Integer deviceId = 1234;

    /**
     * BACnet 本地设备名称
     */
    private String localDeviceName = "YudaoIoT-BACnet-Device";

    /**
     * BACnet 监听端口
     */
    private Integer port = 47808;

    /**
     * 本地绑定地址
     */
    private String localBindAddress = "0.0.0.0";

    /**
     * 广播地址
     */
    private String broadcastAddress = "255.255.255.255";

    /**
     * 网络前缀长度（子网掩码位数，如 24 表示 255.255.255.0）
     */
    private Integer networkPrefixLength = 24;

    /**
     * 设备发现超时时间（毫秒）
     */
    private Long discoveryTimeout = 5000L;

    /**
     * 设备通信超时时间（毫秒）
     */
    private Long communicationTimeout = 3000L;

    /**
     * 传输层超时时间（毫秒）
     */
    private Integer timeout = 5000;

    /**
     * 最大并发请求数
     */
    private Integer maxConcurrentRequests = 10;

    /**
     * 是否启用轮询采集（默认关闭，避免无设备环境报错）
     */
    private Boolean pollingEnabled = false;

}
