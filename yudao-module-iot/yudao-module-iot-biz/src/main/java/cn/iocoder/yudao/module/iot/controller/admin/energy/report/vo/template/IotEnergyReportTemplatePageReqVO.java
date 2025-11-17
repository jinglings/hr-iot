package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 能源报表模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyReportTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称", example = "日报模板")
    private String name;

    @Schema(description = "报表类型", example = "day")
    private String reportType;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
