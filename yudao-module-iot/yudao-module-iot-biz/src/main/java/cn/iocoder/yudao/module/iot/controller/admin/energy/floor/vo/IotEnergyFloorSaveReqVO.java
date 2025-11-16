package cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 能源楼层新增/修改 Request VO")
@Data
public class IotEnergyFloorSaveReqVO {

    @Schema(description = "楼层ID", example = "1")
    private Long id;

    @Schema(description = "楼层名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1层")
    @NotEmpty(message = "楼层名称不能为空")
    private String floorName;

    @Schema(description = "楼层编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "FLOOR_001")
    @NotEmpty(message = "楼层编码不能为空")
    private String floorCode;

    @Schema(description = "所属建筑ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属建筑不能为空")
    private Long buildingId;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "楼层序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "楼层序号不能为空")
    private Integer floorNumber;

    @Schema(description = "楼层面积(平方米)", example = "800.00")
    private BigDecimal area;

    @Schema(description = "楼层平面图URL", example = "https://example.com/floor-plan.jpg")
    private String floorPlanUrl;

    @Schema(description = "楼层描述", example = "办公区1层")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
