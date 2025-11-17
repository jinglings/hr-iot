package cn.iocoder.yudao.module.iot.core.edge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 边缘规则下发消息 DTO
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeRuleDeployDTO implements Serializable {

    /**
     * 操作类型: deploy=部署, update=更新, delete=删除
     */
    private String action;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 网关标识
     */
    private String gatewayKey;

    /**
     * 规则类型: 1=场景联动, 2=数据流转, 3=AI推理
     */
    private Integer ruleType;

    /**
     * 规则配置JSON
     */
    private String ruleConfig;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

}
