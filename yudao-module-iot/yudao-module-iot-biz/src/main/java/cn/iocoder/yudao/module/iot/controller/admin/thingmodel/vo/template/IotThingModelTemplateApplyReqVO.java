package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 物模型模板应用 Request VO")
@Data
public class IotThingModelTemplateApplyReqVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "是否覆盖现有物模型", example = "false")
    private Boolean overwrite;

}
