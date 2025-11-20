package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 边缘规则执行日志 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_rule_log", autoResultMap = true)
@KeySequence("iot_edge_rule_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeRuleLogDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 规则ID
     *
     * 关联 {@link IotEdgeRuleDO#getId()}
     */
    private Long ruleId;

    /**
     * 网关ID
     *
     * 关联 {@link IotEdgeGatewayDO#getId()}
     */
    private Long gatewayId;

    // ========== 执行信息 ==========

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行结果
     * 0=失败, 1=成功
     */
    private Integer executeResult;

    /**
     * 执行耗时(ms)
     */
    private Integer executeDuration;

    /**
     * 错误信息
     */
    private String errorMessage;

    // ========== 触发信息 ==========

    /**
     * 触发数据(JSON)
     */
    private String triggerData;

    /**
     * 执行动作数据(JSON)
     */
    private String actionData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private Integer executeStatus;

}
