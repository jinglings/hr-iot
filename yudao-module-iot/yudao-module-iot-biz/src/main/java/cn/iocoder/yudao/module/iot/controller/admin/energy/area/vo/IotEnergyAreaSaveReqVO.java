package cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 能源区域新增/修改 Request VO")
@Data
public class IotEnergyAreaSaveReqVO {

    @Schema(description = "区域ID", example = "1")
    private Long id;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A区")
    @NotEmpty(message = "区域名称不能为空")
    private String areaName;

    @Schema(description = "区域编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "AREA_001")
    @NotEmpty(message = "区域编码不能为空")
    private String areaCode;

    @Schema(description = "所属建筑ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属建筑不能为空")
    private Long buildingId;

    @Schema(description = "区域类型", example = "production")
    private String areaType;

    @Schema(description = "区域面积(平方米)", example = "1000.00")
    private BigDecimal area;

    @Schema(description = "区域描述", example = "生产车间A区")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
