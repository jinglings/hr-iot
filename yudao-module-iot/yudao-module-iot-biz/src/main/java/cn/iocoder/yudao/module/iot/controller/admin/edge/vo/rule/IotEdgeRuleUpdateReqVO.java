package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 边缘规则更新 Request VO")
@Data
public class IotEdgeRuleUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "规则名称", example = "温度异常控制")
    private String name;

    @Schema(description = "触发器配置(JSON)")
    private String triggerConfig;

    @Schema(description = "条件配置(JSON)")
    private String conditionConfig;

    @Schema(description = "动作配置(JSON)")
    private String actionConfig;

    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Schema(description = "描述")
    private String description;

}
