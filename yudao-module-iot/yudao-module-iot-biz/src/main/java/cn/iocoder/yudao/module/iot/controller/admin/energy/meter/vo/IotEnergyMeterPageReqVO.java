package cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 能源计量点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyMeterPageReqVO extends PageParam {

    @Schema(description = "计量点名称", example = "1号楼总表")
    private String meterName;

    @Schema(description = "计量点编码", example = "M001")
    private String meterCode;

    @Schema(description = "能源类型ID", example = "1")
    private Long energyTypeId;

    @Schema(description = "关联设备ID", example = "1")
    private Long deviceId;

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

    @Schema(description = "是否虚拟表", example = "false")
    private Boolean isVirtual;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
