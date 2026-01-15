package cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 设备服务调用 Request VO")
@Data
public class IotDeviceServiceInvokeReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "服务标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "toggle")
    @NotBlank(message = "服务标识符不能为空")
    private String identifier;

    @Schema(description = "输入参数(JSON)", example = "{\"duration\": 10}")
    private String inputParams;

}
