package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源报表生成 Request VO")
@Data
public class IotEnergyReportGenerateReqVO {

    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    @Schema(description = "报表名称", required = true, example = "2024年1月能耗日报")
    @NotBlank(message = "报表名称不能为空")
    private String reportName;

    @Schema(description = "报表类型", required = true, example = "day")
    @NotBlank(message = "报表类型不能为空")
    private String reportType;

    @Schema(description = "统计开始时间", required = true)
    @NotNull(message = "统计开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间", required = true)
    @NotNull(message = "统计结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "能源类型ID", example = "1")
    private Long energyTypeId;

    @Schema(description = "导出格式", required = true, example = "excel")
    @NotBlank(message = "导出格式不能为空")
    private String exportFormat; // excel, pdf

}
