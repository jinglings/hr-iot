package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 物模型模板列表查询 Request VO")
@Data
public class IotThingModelTemplateListReqVO {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "模板名称", example = "温湿度")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "是否系统模板")
    private Boolean isSystem;

}
