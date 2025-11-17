package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 边缘规则创建 Request VO")
@Data
public class IotEdgeRuleCreateReqVO {

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温度异常控制")
    @NotBlank(message = "规则名称不能为空")
    private String name;

    @Schema(description = "网关ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "网关ID不能为空")
    private Long gatewayId;

    @Schema(description = "规则类型: SCENE, DATA_FLOW, AI_INFERENCE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @Schema(description = "触发器配置(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "触发器配置不能为空")
    private String triggerConfig;

    @Schema(description = "条件配置(JSON)")
    private String conditionConfig;

    @Schema(description = "动作配置(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "动作配置不能为空")
    private String actionConfig;

    @Schema(description = "是否本地执行: 0=否, 1=是", example = "1")
    private Integer executeLocal;

    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Schema(description = "描述")
    private String description;

}
