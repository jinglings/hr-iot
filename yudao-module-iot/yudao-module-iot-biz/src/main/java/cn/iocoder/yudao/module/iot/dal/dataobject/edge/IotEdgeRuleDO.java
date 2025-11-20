package cn.iocoder.yudao.module.iot.dal.dataobject.edge;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeRuleDeployStatusEnum;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeRuleTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 边缘规则 DO
 *
 * @author AI Assistant
 */
@TableName(value = "iot_edge_rule", autoResultMap = true)
@KeySequence("iot_edge_rule_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEdgeRuleDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 网关ID
     *
     * 关联 {@link IotEdgeGatewayDO#getId()}
     */
    private Long gatewayId;

    // ========== 规则配置 ==========

    /**
     * 规则类型
     *
     * 枚举 {@link IotEdgeRuleTypeEnum}
     */
    private String ruleType;

    /**
     * 触发器配置(JSON)
     */
    private String triggerConfig;

    /**
     * 条件配置(JSON)
     */
    private String conditionConfig;

    /**
     * 动作配置(JSON)
     */
    private String actionConfig;

    private String ruleConfig;

    // ========== 执行配置 ==========

    /**
     * 是否本地执行
     * 0=否, 1=是
     */
    private Integer executeLocal;

    /**
     * 优先级
     */
    private Integer priority;

    // ========== 状态信息 ==========

    /**
     * 状态
     * 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 部署状态
     *
     * 枚举 {@link IotEdgeRuleDeployStatusEnum}
     */
    private Integer deployStatus;

    /**
     * 部署时间
     */
    private LocalDateTime deployTime;

    /**
     * 部署错误信息
     */
    private String deployError;

    // ========== 统计信息 ==========

    /**
     * 执行次数
     */
    private Long executeCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 描述
     */
    private String description;

}
