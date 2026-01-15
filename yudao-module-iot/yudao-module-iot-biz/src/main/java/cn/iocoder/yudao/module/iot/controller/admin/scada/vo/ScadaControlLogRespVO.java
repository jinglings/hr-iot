package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCADA 控制日志 VO
 *
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 控制日志 Response VO")
@Data
public class ScadaControlLogRespVO {

    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "水泵1号")
    private String deviceName;

    @Schema(description = "命令名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "setPumpStatus")
    private String commandName;

    @Schema(description = "命令参数 (JSON)", example = "{\"status\": true}")
    private String commandParams;

    @Schema(description = "旧值", example = "false")
    private String oldValue;

    @Schema(description = "新值", example = "true")
    private String newValue;

    @Schema(description = "执行状态: 1-成功, 0-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer executionStatus;

    @Schema(description = "执行状态描述", example = "成功")
    private String executionStatusDesc;

    @Schema(description = "错误信息", example = "")
    private String errorMessage;

    @Schema(description = "操作用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "操作用户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String userName;

    @Schema(description = "客户端 IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "执行耗时(毫秒)", example = "150")
    private Integer executionTime;

    @Schema(description = "来源: SCADA, API, SCHEDULE", example = "SCADA")
    private String source;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
