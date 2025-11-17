package cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 能源实时数据 Response VO")
@Data
public class IotEnergyDataRespVO {

    @Schema(description = "时间戳", example = "1640995200000")
    private Long ts;

    @Schema(description = "计量点ID", example = "1")
    private Long meterId;

    @Schema(description = "瞬时功率/流量", example = "100.5")
    private Double instantPower;

    @Schema(description = "累计值", example = "12345.67")
    private Double cumulativeValue;

    @Schema(description = "电压(电表)", example = "220.5")
    private Double voltage;

    @Schema(description = "电流(电表)", example = "10.2")
    private Double current;

    @Schema(description = "功率因数(电表)", example = "0.95")
    private Double powerFactor;

    @Schema(description = "数据质量(0异常1正常)", example = "1")
    private Integer dataQuality;

}
