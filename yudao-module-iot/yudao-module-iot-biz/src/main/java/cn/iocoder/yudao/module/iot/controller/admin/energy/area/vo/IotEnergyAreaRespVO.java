package cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源区域 Response VO")
@Data
public class IotEnergyAreaRespVO {

    @Schema(description = "区域ID", example = "1")
    private Long id;

    @Schema(description = "区域名称", example = "A区")
    private String areaName;

    @Schema(description = "区域编码", example = "AREA_001")
    private String areaCode;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属建筑名称", example = "1号办公楼")
    private String buildingName;

    @Schema(description = "区域类型", example = "production")
    private String areaType;

    @Schema(description = "区域面积(平方米)", example = "1000.00")
    private BigDecimal area;

    @Schema(description = "区域描述", example = "生产车间A区")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
