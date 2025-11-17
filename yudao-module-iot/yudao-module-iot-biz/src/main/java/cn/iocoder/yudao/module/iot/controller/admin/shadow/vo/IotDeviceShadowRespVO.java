package cn.iocoder.yudao.module.iot.controller.admin.shadow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - IoT 设备影子 Response VO")
@Data
public class IotDeviceShadowRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long deviceId;

    @Schema(description = "期望状态（云端希望设备达到的状态）")
    private Map<String, Object> desired;

    @Schema(description = "实际状态（设备实际上报的状态）")
    private Map<String, Object> reported;

    @Schema(description = "差量状态（期望与实际的差异）")
    private Map<String, Object> delta;

    @Schema(description = "元数据（状态更新时间等）")
    private Map<String, Object> metadata;

    @Schema(description = "版本号", example = "5")
    private Integer version;

    @Schema(description = "期望状态版本号", example = "3")
    private Integer desiredVersion;

    @Schema(description = "实际状态版本号", example = "2")
    private Integer reportedVersion;

    @Schema(description = "最后期望状态更新时间")
    private LocalDateTime lastDesiredTime;

    @Schema(description = "最后实际状态上报时间")
    private LocalDateTime lastReportedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
