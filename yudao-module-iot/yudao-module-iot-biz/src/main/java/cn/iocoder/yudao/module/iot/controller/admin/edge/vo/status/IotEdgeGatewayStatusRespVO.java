package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 边缘网关状态 Response VO")
@Data
public class IotEdgeGatewayStatusRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "边缘网关编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long gatewayId;

    @Schema(description = "状态: 0=离线, 1=在线, 2=异常", example = "1")
    private Integer status;

    @Schema(description = "CPU使用率 (%)", example = "45")
    private Integer cpuUsage;

    @Schema(description = "内存使用率 (%)", example = "60")
    private Integer memoryUsage;

    @Schema(description = "磁盘使用率 (%)", example = "70")
    private Integer diskUsage;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
