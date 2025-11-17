package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘规则日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotEdgeRuleLogPageReqVO extends PageParam {

    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    @Schema(description = "网关ID", example = "1")
    private Long gatewayId;

    @Schema(description = "执行结果: 0=失败, 1=成功")
    private Integer executeResult;

    @Schema(description = "执行时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] executeTime;

}
