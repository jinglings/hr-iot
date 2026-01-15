package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 物模型模板分类 Response VO")
@Data
public class IotThingModelTemplateCategoryRespVO {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "传感器")
    private String name;

    @Schema(description = "分类编码", example = "sensor")
    private String code;

    @Schema(description = "分类图标", example = "ep:odometer")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否系统分类")
    private Boolean isSystem;

    @Schema(description = "模板数量")
    private Integer templateCount;

}
