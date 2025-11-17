package cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 碳排放计算 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 碳排放计算 Response VO")
@Data
@Builder
public class IotEnergyCarbonEmissionRespVO {

    @Schema(description = "总能耗（kWh）", required = true, example = "1234.56")
    private BigDecimal totalConsumption;

    @Schema(description = "总碳排放量（kgCO₂）", required = true, example = "987.65")
    private BigDecimal totalCarbonEmission;

    @Schema(description = "各能源类型碳排放明细")
    private Map<String, CarbonEmissionDetail> emissionDetails;

    @Schema(description = "等效植树数量（棵）", example = "54")
    private Integer equivalentTrees;

    @Schema(description = "等效汽车行驶里程（公里）", example = "5432.1")
    private BigDecimal equivalentCarMileage;

    @Schema(description = "碳排放强度（kgCO₂/kWh）", example = "0.8")
    private BigDecimal emissionIntensity;

    @Schema(description = "统计开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间", required = true)
    private LocalDateTime endTime;

    /**
     * 碳排放明细
     */
    @Schema(description = "碳排放明细")
    @Data
    @Builder
    public static class CarbonEmissionDetail {

        @Schema(description = "能源类型名称", example = "电")
        private String energyTypeName;

        @Schema(description = "能耗量", example = "1000.00")
        private BigDecimal consumption;

        @Schema(description = "碳排放系数（kgCO₂/kWh）", example = "0.785")
        private BigDecimal emissionFactor;

        @Schema(description = "碳排放量（kgCO₂）", example = "785.00")
        private BigDecimal carbonEmission;

        @Schema(description = "占比（%）", example = "79.5")
        private BigDecimal percentage;

    }

}
