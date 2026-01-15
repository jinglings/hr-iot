package cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - IoT 设备标签批量绑定 Request VO")
@Data
public class IotDeviceTagBatchBindReqVO {

    @Schema(description = "设备ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "设备ID列表不能为空")
    private List<Long> deviceIds;

    @Schema(description = "标签ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "标签ID列表不能为空")
    private List<Long> tagIds;

}
