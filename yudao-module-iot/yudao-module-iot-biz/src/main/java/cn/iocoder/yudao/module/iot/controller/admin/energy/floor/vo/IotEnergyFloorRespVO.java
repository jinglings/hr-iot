package cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源楼层 Response VO")
@Data
public class IotEnergyFloorRespVO {

    @Schema(description = "楼层ID", example = "1")
    private Long id;

    @Schema(description = "楼层名称", example = "1层")
    private String floorName;

    @Schema(description = "楼层编码", example = "FLOOR_001")
    private String floorCode;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属建筑名称", example = "1号办公楼")
    private String buildingName;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "所属区域名称", example = "A区")
    private String areaName;

    @Schema(description = "楼层序号", example = "1")
    private Integer floorNumber;

    @Schema(description = "楼层面积(平方米)", example = "800.00")
    private BigDecimal area;

    @Schema(description = "楼层平面图URL", example = "https://example.com/floor-plan.jpg")
    private String floorPlanUrl;

    @Schema(description = "楼层描述", example = "办公区1层")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
