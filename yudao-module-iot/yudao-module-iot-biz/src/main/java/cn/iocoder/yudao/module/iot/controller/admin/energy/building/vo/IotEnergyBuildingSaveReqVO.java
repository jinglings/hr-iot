package cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 能源建筑新增/修改 Request VO")
@Data
public class IotEnergyBuildingSaveReqVO {

    @Schema(description = "建筑ID", example = "1")
    private Long id;

    @Schema(description = "建筑名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号办公楼")
    @NotEmpty(message = "建筑名称不能为空")
    private String buildingName;

    @Schema(description = "建筑编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BUILDING_001")
    @NotEmpty(message = "建筑编码不能为空")
    private String buildingCode;

    @Schema(description = "建筑类型", example = "office")
    private String buildingType;

    @Schema(description = "建筑地址", example = "园区东侧")
    private String address;

    @Schema(description = "建筑面积(平方米)", example = "5000.00")
    private BigDecimal area;

    @Schema(description = "经度", example = "120.123456")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "30.123456")
    private BigDecimal latitude;

    @Schema(description = "建筑图片URL", example = "https://example.com/building.jpg")
    private String imageUrl;

    @Schema(description = "建筑描述", example = "主办公楼")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
