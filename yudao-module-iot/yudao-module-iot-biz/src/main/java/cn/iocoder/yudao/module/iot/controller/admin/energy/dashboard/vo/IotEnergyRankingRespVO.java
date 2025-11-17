package cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 能耗排名 Response VO")
@Data
@Builder
public class IotEnergyRankingRespVO {

    @Schema(description = "排名", example = "1")
    private Integer rank;

    @Schema(description = "对象ID", example = "1")
    private Long objectId;

    @Schema(description = "对象名称", example = "1号楼")
    private String objectName;

    @Schema(description = "对象类型", example = "building")
    private String objectType;

    @Schema(description = "能耗值", example = "1234.56")
    private BigDecimal consumption;

    @Schema(description = "占比(%)", example = "25.6")
    private BigDecimal percentage;

}
