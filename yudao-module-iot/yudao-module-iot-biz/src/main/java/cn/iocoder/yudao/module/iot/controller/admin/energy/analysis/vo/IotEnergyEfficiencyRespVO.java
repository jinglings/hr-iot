package cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 能效指标评估 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 能效指标评估 Response VO")
@Data
@Builder
public class IotEnergyEfficiencyRespVO {

    @Schema(description = "总能耗", required = true, example = "1234.56")
    private BigDecimal totalConsumption;

    @Schema(description = "建筑面积（平方米）", example = "5000.00")
    private BigDecimal buildingArea;

    @Schema(description = "单位面积能耗（kWh/m²）", example = "0.25")
    private BigDecimal consumptionPerArea;

    @Schema(description = "人数", example = "200")
    private Integer peopleCount;

    @Schema(description = "人均能耗（kWh/人）", example = "6.17")
    private BigDecimal consumptionPerPerson;

    @Schema(description = "产值（万元）", example = "1000.00")
    private BigDecimal outputValue;

    @Schema(description = "万元产值能耗（kWh/万元）", example = "1.23")
    private BigDecimal consumptionPerOutputValue;

    @Schema(description = "能效等级", example = "A")
    private String efficiencyLevel; // A, B, C, D, E

    @Schema(description = "能效评分（0-100）", example = "85.5")
    private BigDecimal efficiencyScore;

    @Schema(description = "节能潜力（%）", example = "15.2")
    private BigDecimal savingPotential;

    @Schema(description = "能效建议")
    private String suggestions;

    @Schema(description = "统计开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间", required = true)
    private LocalDateTime endTime;

}
