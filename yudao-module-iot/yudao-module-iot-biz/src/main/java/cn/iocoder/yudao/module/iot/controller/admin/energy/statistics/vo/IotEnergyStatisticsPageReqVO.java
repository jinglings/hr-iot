package cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 能源统计数据分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyStatisticsPageReqVO extends PageParam {

    @Schema(description = "计量点ID", example = "1")
    private Long meterId;

    @Schema(description = "能源类型ID", example = "1")
    private Long energyTypeId;

    @Schema(description = "建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "统计周期", example = "hour")
    private String statPeriod;

    @Schema(description = "统计时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] statTime;

}
