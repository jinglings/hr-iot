package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Map;

/**
 * SCADA 控制命令请求 VO
 *
 * 用于发送设备控制命令
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 控制命令 Request VO")
@Data
public class ScadaControlCommandReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "命令名称/服务标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "setPumpStatus")
    @NotBlank(message = "命令名称不能为空")
    private String commandName;

    @Schema(description = "命令参数", example = "{\"status\": true, \"speed\": 50}")
    private Map<String, Object> params;

    @Schema(description = "是否同步执行（等待设备响应）", example = "false")
    private Boolean sync;

    @Schema(description = "超时时间(秒)", example = "30")
    private Integer timeout;

    @Schema(description = "来源标识", example = "SCADA")
    private String source;

    @Schema(description = "SCADA Tag 名称（可选，用于记录）", example = "Pump1_Start")
    private String tagName;

    @Schema(description = "期望的新值（用于简单属性设置）", example = "true")
    private Object newValue;

    @Schema(description = "当前旧值（用于审计记录）", example = "false")
    private Object oldValue;

}
