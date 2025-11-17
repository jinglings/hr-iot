package cn.iocoder.yudao.module.iot.core.edge.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 边缘网关心跳消息 DTO
 *
 * @author AI Assistant
 */
@Data
public class EdgeGatewayHeartbeatDTO implements Serializable {

    /**
     * 网关标识
     */
    private String gatewayKey;

    /**
     * 网关序列号
     */
    private String serialNumber;

    /**
     * 状态: 0=离线, 1=在线, 2=异常
     */
    private Integer status;

    /**
     * CPU使用率 (%)
     */
    private Integer cpuUsage;

    /**
     * 内存使用率 (%)
     */
    private Integer memoryUsage;

    /**
     * 磁盘使用率 (%)
     */
    private Integer diskUsage;

    /**
     * 在线设备数
     */
    private Integer onlineDeviceCount;

    /**
     * 运行中的规则数
     */
    private Integer runningRuleCount;

    /**
     * 网关软件版本
     */
    private String version;

    /**
     * 心跳时间
     */
    private LocalDateTime heartbeatTime;

}
