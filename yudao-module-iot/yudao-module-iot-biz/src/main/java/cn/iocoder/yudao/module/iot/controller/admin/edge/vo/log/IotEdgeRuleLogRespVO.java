package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 边缘规则执行日志 Response VO")
@Data
public class IotEdgeRuleLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "边缘规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long ruleId;

    @Schema(description = "边缘网关编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    private Long gatewayId;

    @Schema(description = "执行时间")
    private LocalDateTime executeTime;

    @Schema(description = "执行状态: 1=成功, 2=失败", example = "1")
    private Integer executeStatus;

    @Schema(description = "执行结果", example = "规则执行成功")
    private String executeResult;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
