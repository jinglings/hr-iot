package cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - IoT 能耗趋势 Response VO")
@Data
@Builder
public class IotEnergyTrendRespVO {

    @Schema(description = "时间标签列表")
    private List<String> timeLabels;

    @Schema(description = "能耗数据列表")
    private List<BigDecimal> consumptions;

    @Schema(description = "功率数据列表")
    private List<BigDecimal> powers;

}
