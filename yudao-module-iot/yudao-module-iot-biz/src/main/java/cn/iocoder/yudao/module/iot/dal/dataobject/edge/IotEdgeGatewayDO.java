package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeGatewayStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 边缘网关 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_gateway", autoResultMap = true)
@KeySequence("iot_edge_gateway_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeGatewayDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 网关名称
     */
    private String name;

    /**
     * 网关序列号
     */
    private String serialNumber;

    /**
     * 网关标识
     */
    private String gatewayKey;

    /**
     * 网关密钥
     */
    private String gatewaySecret;

    // ========== 设备信息 ==========

    /**
     * 设备型号
     */
    private String deviceType;

    /**
     * 硬件版本
     */
    private String hardwareVersion;

    /**
     * 软件版本
     */
    private String softwareVersion;

    // ========== 网络信息 ==========

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 安装位置
     */
    private String location;

    // ========== 状态信息 ==========

    /**
     * 状态
     *
     * 枚举 {@link IotEdgeGatewayStatusEnum}
     */
    private Integer status;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后离线时间
     */
    private LocalDateTime lastOfflineTime;

    /**
     * 激活时间
     */
    private LocalDateTime activeTime;

    // ========== 资源信息 ==========

    /**
     * CPU核心数
     */
    private Integer cpuCores;

    /**
     * 总内存(MB)
     */
    private Integer memoryTotal;

    /**
     * 总磁盘(GB)
     */
    private Integer diskTotal;

    // ========== 配置信息 ==========

    /**
     * 网关配置(JSON)
     */
    private String config;

    /**
     * 描述
     */
    private String description;

}
