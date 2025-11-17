package cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源统计数据 Response VO")
@Data
public class IotEnergyStatisticsRespVO {

    @Schema(description = "统计ID", example = "1")
    private Long id;

    @Schema(description = "统计时间", example = "2024-01-01 00:00:00")
    private LocalDateTime statTime;

    @Schema(description = "统计周期", example = "hour")
    private String statPeriod;

    @Schema(description = "计量点ID", example = "1")
    private Long meterId;

    @Schema(description = "能源类型ID", example = "1")
    private Long energyTypeId;

    @Schema(description = "建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @Schema(description = "起始值", example = "100.00")
    private BigDecimal startValue;

    @Schema(description = "结束值", example = "120.00")
    private BigDecimal endValue;

    @Schema(description = "能耗量", example = "20.00")
    private BigDecimal consumption;

    @Schema(description = "最大值", example = "125.00")
    private BigDecimal maxValue;

    @Schema(description = "最小值", example = "95.00")
    private BigDecimal minValue;

    @Schema(description = "平均值", example = "110.00")
    private BigDecimal avgValue;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
