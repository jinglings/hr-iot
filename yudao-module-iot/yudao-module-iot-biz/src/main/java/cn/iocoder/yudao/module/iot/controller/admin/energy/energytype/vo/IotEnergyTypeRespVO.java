package cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IoT 能源类型 Response VO")
@Data
public class IotEnergyTypeRespVO {

    @Schema(description = "能源类型ID", example = "1")
    private Long id;

    @Schema(description = "能源名称", example = "电能")
    private String energyName;

    @Schema(description = "能源编码", example = "ELECTRIC")
    private String energyCode;

    @Schema(description = "父级能源ID", example = "0")
    private Long parentId;

    @Schema(description = "计量单位", example = "kWh")
    private String unit;

    @Schema(description = "折标煤系数(kgce)", example = "0.1229")
    private BigDecimal coalConversionFactor;

    @Schema(description = "碳排放系数(kgCO₂)", example = "0.7850")
    private BigDecimal carbonEmissionFactor;

    @Schema(description = "能源图标", example = "electric")
    private String icon;

    @Schema(description = "图表颜色(十六进制)", example = "#1890ff")
    private String color;

    @Schema(description = "能源描述", example = "电力能源")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子节点列表")
    private List<IotEnergyTypeRespVO> children;

}
