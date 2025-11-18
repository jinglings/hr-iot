package cn.iocoder.yudao.module.iot.core.edge.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 边缘规则执行结果上报 DTO
 *
 * @author AI Assistant
 */
@Data
public class EdgeRuleExecuteResultDTO implements Serializable {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 网关标识
     */
    private String gatewayKey;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行状态: 1=成功, 2=失败
     */
    private Integer executeStatus;

    /**
     * 执行结果描述
     */
    private String executeResult;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 触发数据JSON
     */
    private String triggerData;

}
