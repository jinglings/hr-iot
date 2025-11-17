package cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 能源同比环比分析 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 能源同比环比分析 Response VO")
@Data
@Builder
public class IotEnergyComparisonAnalysisRespVO {

    @Schema(description = "当前周期能耗", required = true, example = "1234.56")
    private BigDecimal currentConsumption;

    @Schema(description = "同比周期能耗", example = "1100.00")
    private BigDecimal yoyConsumption;

    @Schema(description = "环比周期能耗", example = "1150.00")
    private BigDecimal momConsumption;

    @Schema(description = "同比增长率（%）", example = "12.23")
    private BigDecimal yoyGrowthRate;

    @Schema(description = "环比增长率（%）", example = "7.35")
    private BigDecimal momGrowthRate;

    @Schema(description = "同比增长量", example = "134.56")
    private BigDecimal yoyGrowthAmount;

    @Schema(description = "环比增长量", example = "84.56")
    private BigDecimal momGrowthAmount;

    @Schema(description = "当前周期开始时间", required = true)
    private LocalDateTime currentStartTime;

    @Schema(description = "当前周期结束时间", required = true)
    private LocalDateTime currentEndTime;

    @Schema(description = "同比周期开始时间")
    private LocalDateTime yoyStartTime;

    @Schema(description = "同比周期结束时间")
    private LocalDateTime yoyEndTime;

    @Schema(description = "环比周期开始时间")
    private LocalDateTime momStartTime;

    @Schema(description = "环比周期结束时间")
    private LocalDateTime momEndTime;

    @Schema(description = "分析周期类型", required = true, example = "day")
    private String periodType; // day, week, month, year

}
