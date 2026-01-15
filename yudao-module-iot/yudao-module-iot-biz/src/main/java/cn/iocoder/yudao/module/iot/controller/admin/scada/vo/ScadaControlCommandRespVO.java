package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCADA 控制命令响应 VO
 *
 * 返回控制命令执行结果
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 控制命令 Response VO")
@Data
public class ScadaControlCommandRespVO {

    @Schema(description = "命令日志编号", example = "1")
    private Long logId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "水泵1号")
    private String deviceName;

    @Schema(description = "命令名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "setPumpStatus")
    private String commandName;

    @Schema(description = "执行状态: true-成功, false-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;

    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "旧值", example = "false")
    private Object oldValue;

    @Schema(description = "新值", example = "true")
    private Object newValue;

    @Schema(description = "执行耗时(毫秒)", example = "150")
    private Integer executionTime;

    @Schema(description = "执行时间")
    private LocalDateTime executeTime;

    @Schema(description = "设备响应数据")
    private Object response;

}
