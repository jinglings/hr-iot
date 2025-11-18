package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 边缘模型部署 Response VO")
@Data
public class IotEdgeModelDeploymentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "AI模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long modelId;

    @Schema(description = "边缘网关编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    private Long gatewayId;

    @Schema(description = "部署状态: 1=部署中, 2=部署成功, 3=部署失败", example = "2")
    private Integer deployStatus;

    @Schema(description = "部署时间")
    private LocalDateTime deployTime;

    @Schema(description = "状态: 0=停止, 1=运行", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
