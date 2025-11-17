package cn.iocoder.yudao.module.iot.controller.admin.shadow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - IoT 设备影子更新实际状态 Request VO")
@Data
public class IotDeviceShadowUpdateReportedReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "实际状态（设备上报的属性）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "实际状态不能为空")
    private Map<String, Object> reported;

    @Schema(description = "版本号（用于并发控制）", example = "5")
    private Integer version;

}
