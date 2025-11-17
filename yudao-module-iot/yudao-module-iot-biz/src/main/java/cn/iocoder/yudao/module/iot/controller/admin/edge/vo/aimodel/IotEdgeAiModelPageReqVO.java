package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 边缘AI模型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotEdgeAiModelPageReqVO extends PageParam {

    @Schema(description = "模型名称", example = "质量检测模型")
    private String name;

    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "模型格式")
    private String modelFormat;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
