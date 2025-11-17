package cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 能源数据查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyDataQueryReqVO extends PageParam {

    @Schema(description = "计量点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long meterId;

    @Schema(description = "开始时间（时间戳）", example = "1640995200000")
    private Long startTime;

    @Schema(description = "结束时间（时间戳）", example = "1641081600000")
    private Long endTime;

    @Schema(description = "数据质量(0异常1正常)", example = "1")
    private Integer dataQuality;

}
