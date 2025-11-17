package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘模型部署分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotEdgeModelDeploymentPageReqVO extends PageParam {

    @Schema(description = "模型ID", example = "1")
    private Long modelId;

    @Schema(description = "网关ID", example = "1")
    private Long gatewayId;

    @Schema(description = "部署状态")
    private Integer deployStatus;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
