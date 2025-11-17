package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 边缘网关状态 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_gateway_status", autoResultMap = true)
@KeySequence("iot_edge_gateway_status_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeGatewayStatusDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 网关ID
     *
     * 关联 {@link IotEdgeGatewayDO#getId()}
     */
    private Long gatewayId;

    // ========== 资源使用情况 ==========

    /**
     * CPU使用率(%)
     */
    private BigDecimal cpuUsage;

    /**
     * 内存使用率(%)
     */
    private BigDecimal memoryUsage;

    /**
     * 磁盘使用率(%)
     */
    private BigDecimal diskUsage;

    /**
     * 网络上传速度(KB/s)
     */
    private Integer networkUploadSpeed;

    /**
     * 网络下载速度(KB/s)
     */
    private Integer networkDownloadSpeed;

    // ========== 设备统计 ==========

    /**
     * 在线设备数
     */
    private Integer onlineDeviceCount;

    /**
     * 总设备数
     */
    private Integer totalDeviceCount;

    // ========== 规则统计 ==========

    /**
     * 活跃规则数
     */
    private Integer activeRuleCount;

    /**
     * 规则执行次数
     */
    private Integer ruleExecuteCount;

    // ========== 数据统计 ==========

    /**
     * 接收数据条数
     */
    private Long dataReceiveCount;

    /**
     * 上传数据条数
     */
    private Long dataUploadCount;

    // ========== 时间信息 ==========

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

}
