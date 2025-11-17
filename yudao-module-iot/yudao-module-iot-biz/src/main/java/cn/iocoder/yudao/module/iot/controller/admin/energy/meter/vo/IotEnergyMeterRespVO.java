package cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源计量点 Response VO")
@Data
public class IotEnergyMeterRespVO {

    @Schema(description = "计量点ID", example = "1")
    private Long id;

    @Schema(description = "计量点名称", example = "1号楼总表")
    private String meterName;

    @Schema(description = "计量点编码", example = "M001")
    private String meterCode;

    @Schema(description = "能源类型ID", example = "1")
    private Long energyTypeId;

    @Schema(description = "关联设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "设备属性标识符(物模型identifier)", example = "power")
    private String deviceProperty;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "所属房间ID", example = "1")
    private Long roomId;

    @Schema(description = "计量点级别(1:一级表 2:二级表 3:三级表)", example = "1")
    private Integer meterLevel;

    @Schema(description = "父级计量点ID", example = "0")
    private Long parentId;

    @Schema(description = "计量倍率(CT/PT比)", example = "1.0000")
    private BigDecimal ratio;

    @Schema(description = "是否虚拟表", example = "false")
    private Boolean isVirtual;

    @Schema(description = "计算公式(虚拟表使用)", example = "M001+M002-M003")
    private String calcFormula;

    @Schema(description = "计量点描述", example = "1号楼电能总表")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
