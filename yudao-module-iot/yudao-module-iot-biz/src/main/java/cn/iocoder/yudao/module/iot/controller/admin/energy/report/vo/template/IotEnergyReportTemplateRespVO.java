package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源报表模板 Response VO")
@Data
public class IotEnergyReportTemplateRespVO {

    @Schema(description = "模板ID", required = true, example = "1")
    private Long id;

    @Schema(description = "模板名称", required = true, example = "能耗日报")
    private String name;

    @Schema(description = "模板描述", example = "每日能耗统计报表")
    private String description;

    @Schema(description = "报表类型", required = true, example = "day")
    private String reportType;

    @Schema(description = "报表内容配置", required = true)
    private String content;

    @Schema(description = "状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "定时任务cron表达式", example = "0 0 8 * * ?")
    private String cronExpression;

    @Schema(description = "接收人用户ID列表", example = "1,2,3")
    private String receiveUserIds;

    @Schema(description = "通知方式", example = "1")
    private Integer notifyType;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
