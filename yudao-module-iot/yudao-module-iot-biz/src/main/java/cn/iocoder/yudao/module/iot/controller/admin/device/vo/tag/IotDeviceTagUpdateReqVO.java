package cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IoT 设备标签更新 Request VO")
@Data
public class IotDeviceTagUpdateReqVO {

    @Schema(description = "标签ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "标签ID不能为空")
    private Long id;

    @Schema(description = "标签键", example = "location")
    @Size(max = 64, message = "标签键长度不能超过64个字符")
    private String tagKey;

    @Schema(description = "标签值", example = "车间A")
    @Size(max = 128, message = "标签值长度不能超过128个字符")
    private String tagValue;

    @Schema(description = "标签描述", example = "设备位置标签")
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;

    @Schema(description = "标签显示颜色", example = "#409EFF")
    @Size(max = 16, message = "颜色长度不能超过16个字符")
    private String color;

}
