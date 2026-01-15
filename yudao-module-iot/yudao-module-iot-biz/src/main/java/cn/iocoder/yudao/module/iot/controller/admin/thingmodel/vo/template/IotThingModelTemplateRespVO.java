package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 物模型模板 Response VO")
@Data
public class IotThingModelTemplateRespVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度传感器")
    private String name;

    @Schema(description = "模板编码", example = "temp_sensor")
    private String code;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "传感器")
    private String categoryName;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "模板图标")
    private String icon;

    @Schema(description = "物模型TSL定义(JSON)")
    private String tsl;

    @Schema(description = "是否系统模板")
    private Boolean isSystem;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
