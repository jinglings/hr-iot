package cn.iocoder.yudao.module.iot.controller.admin.shadow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - IoT 设备影子更新期望状态 Request VO")
@Data
public class IotDeviceShadowUpdateDesiredReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "期望状态（要更新的属性）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "期望状态不能为空")
    private Map<String, Object> desired;

    @Schema(description = "版本号（用于并发控制）", example = "5")
    private Integer version;

}
