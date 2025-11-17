package cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "管理后台 - IoT 能耗总览 Response VO")
@Data
@Builder
public class IotEnergyOverviewRespVO {

    @Schema(description = "今日能耗总量", example = "1234.56")
    private BigDecimal todayConsumption;

    @Schema(description = "今日能耗（分项）")
    private Map<String, BigDecimal> todayConsumptionByType;

    @Schema(description = "本月能耗总量", example = "12345.67")
    private BigDecimal monthConsumption;

    @Schema(description = "本月能耗（分项）")
    private Map<String, BigDecimal> monthConsumptionByType;

    @Schema(description = "实时总功率", example = "123.45")
    private BigDecimal realtimePower;

    @Schema(description = "日同比增长率(%)", example = "5.6")
    private BigDecimal dayYoyRate;

    @Schema(description = "日环比增长率(%)", example = "2.3")
    private BigDecimal dayMomRate;

    @Schema(description = "月同比增长率(%)", example = "8.9")
    private BigDecimal monthYoyRate;

    @Schema(description = "月环比增长率(%)", example = "3.2")
    private BigDecimal monthMomRate;

    @Schema(description = "在线设备数", example = "85")
    private Integer onlineDeviceCount;

    @Schema(description = "设备总数", example = "100")
    private Integer totalDeviceCount;

    @Schema(description = "告警数量", example = "3")
    private Integer alertCount;

}
