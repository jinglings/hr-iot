package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 能源报表模板创建/更新 Request VO")
@Data
public class IotEnergyReportTemplateSaveReqVO {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", required = true, example = "能耗日报")
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @Schema(description = "模板描述", example = "每日能耗统计报表")
    private String description;

    @Schema(description = "报表类型", required = true, example = "day")
    @NotBlank(message = "报表类型不能为空")
    private String reportType;

    @Schema(description = "报表内容配置", required = true)
    @NotBlank(message = "报表内容配置不能为空")
    private String content;

    @Schema(description = "状态", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "定时任务cron表达式", example = "0 0 8 * * ?")
    private String cronExpression;

    @Schema(description = "接收人用户ID列表", example = "1,2,3")
    private String receiveUserIds;

    @Schema(description = "通知方式", example = "1")
    private Integer notifyType;

}
