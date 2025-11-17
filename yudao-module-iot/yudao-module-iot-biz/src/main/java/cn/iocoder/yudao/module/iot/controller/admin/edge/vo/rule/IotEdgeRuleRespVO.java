package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 边缘规则 Response VO")
@Data
public class IotEdgeRuleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温度异常控制")
    private String name;

    @Schema(description = "网关ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long gatewayId;

    @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleType;

    @Schema(description = "触发器配置(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triggerConfig;

    @Schema(description = "条件配置(JSON)")
    private String conditionConfig;

    @Schema(description = "动作配置(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String actionConfig;

    @Schema(description = "是否本地执行: 0=否, 1=是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer executeLocal;

    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Schema(description = "状态: 0=禁用, 1=启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "部署状态: 0=未部署, 1=已部署, 2=部署失败", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer deployStatus;

    @Schema(description = "部署时间")
    private LocalDateTime deployTime;

    @Schema(description = "部署错误信息")
    private String deployError;

    @Schema(description = "执行次数", example = "100")
    private Long executeCount;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
