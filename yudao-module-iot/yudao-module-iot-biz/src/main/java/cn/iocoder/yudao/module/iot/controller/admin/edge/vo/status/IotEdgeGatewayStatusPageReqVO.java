package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘网关状态分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotEdgeGatewayStatusPageReqVO extends PageParam {

    @Schema(description = "网关ID", example = "1")
    private Long gatewayId;

    @Schema(description = "记录时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] recordTime;

}
