package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * SCADA 历史数据查询 VO
 *
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 历史数据查询 Request VO")
@Data
public class ScadaHistoryQueryReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "属性标识符（多个用逗号分隔）", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature,pressure")
    private String properties;

    @Schema(description = "SCADA Tag 名称（多个用逗号分隔）", example = "Pump1_Temp,Pump1_Pressure")
    private String tagNames;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "聚合粒度(秒): 0-原始数据, 60-分钟, 3600-小时, 86400-天", example = "60")
    private Integer aggregationInterval;

    @Schema(description = "聚合函数: avg, min, max, sum, count", example = "avg")
    private String aggregationFunction;

    @Schema(description = "最大返回点数", example = "1000")
    private Integer maxPoints;

}
