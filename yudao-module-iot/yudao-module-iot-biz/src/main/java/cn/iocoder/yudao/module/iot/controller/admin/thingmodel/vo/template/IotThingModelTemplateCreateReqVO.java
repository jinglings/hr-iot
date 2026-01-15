package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IoT 物模型模板创建 Request VO")
@Data
public class IotThingModelTemplateCreateReqVO {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度传感器")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 64, message = "模板名称长度不能超过64个字符")
    private String name;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "temp_sensor")
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 32, message = "模板编码长度不能超过32个字符")
    private String code;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "模板描述", example = "通用温湿度传感器模板")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Schema(description = "模板图标", example = "ep:thermometer")
    private String icon;

    @Schema(description = "物模型TSL定义(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "TSL定义不能为空")
    private String tsl;

}
