package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘网关分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEdgeGatewayPageReqVO extends PageParam {

    @Schema(description = "网关名称", example = "工厂1号网关")
    private String name;

    @Schema(description = "网关序列号")
    private String serialNumber;

    @Schema(description = "网关标识")
    private String gatewayKey;

    @Schema(description = "状态: 0=未激活, 1=在线, 2=离线")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
