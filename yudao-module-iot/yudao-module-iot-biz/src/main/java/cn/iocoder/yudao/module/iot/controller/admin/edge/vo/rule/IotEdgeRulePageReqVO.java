package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotEdgeRulePageReqVO extends PageParam {

    @Schema(description = "规则名称", example = "温度异常控制")
    private String name;

    @Schema(description = "网关ID", example = "1")
    private Long gatewayId;

    @Schema(description = "规则类型: SCENE, DATA_FLOW, AI_INFERENCE")
    private String ruleType;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "部署状态: 0=未部署, 1=已部署, 2=部署失败")
    private Integer deployStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
