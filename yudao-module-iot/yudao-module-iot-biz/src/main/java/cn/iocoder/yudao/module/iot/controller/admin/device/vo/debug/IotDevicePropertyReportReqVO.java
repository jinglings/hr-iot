package cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 设备属性上报模拟 Request VO")
@Data
public class IotDevicePropertyReportReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    @NotBlank(message = "属性标识符不能为空")
    private String identifier;

    @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "25.5")
    @NotBlank(message = "属性值不能为空")
    private String value;

}
