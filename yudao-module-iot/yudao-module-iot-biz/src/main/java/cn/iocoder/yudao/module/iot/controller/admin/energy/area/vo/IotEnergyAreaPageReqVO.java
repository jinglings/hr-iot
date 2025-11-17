package cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 能源区域分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyAreaPageReqVO extends PageParam {

    @Schema(description = "区域名称", example = "A区")
    private String areaName;

    @Schema(description = "区域编码", example = "AREA_001")
    private String areaCode;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "区域类型", example = "production")
    private String areaType;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
