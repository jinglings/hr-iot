package cn.iocoder.yudao.module.iot.dal.dataobject.scada;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SCADA 控制操作日志 DO
 *
 * 记录所有通过 SCADA 发送的控制命令
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@TableName("scada_control_log")
@KeySequence("scada_control_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotScadaControlLogDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * IoT 设备 ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 命令名称
     */
    private String commandName;

    /**
     * 命令参数 (JSON)
     */
    private String commandParams;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 执行状态: 1-成功, 0-失败
     */
    private Integer executionStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作用户 ID
     */
    private Long userId;

    /**
     * 操作用户名称
     */
    private String userName;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行耗时(毫秒)
     */
    private Integer executionTime;

    /**
     * 来源: SCADA, API, SCHEDULE
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
