package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源报表记录 Response VO")
@Data
public class IotEnergyReportRecordRespVO {

    @Schema(description = "记录ID", required = true, example = "1")
    private Long id;

    @Schema(description = "报表模板ID", required = true, example = "1")
    private Long templateId;

    @Schema(description = "报表名称", required = true, example = "2024年1月能耗日报")
    private String reportName;

    @Schema(description = "报表类型", required = true, example = "day")
    private String reportType;

    @Schema(description = "统计开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "报表数据", required = true)
    private String reportData;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "生成状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "生成方式", required = true, example = "1")
    private Integer generateType;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
