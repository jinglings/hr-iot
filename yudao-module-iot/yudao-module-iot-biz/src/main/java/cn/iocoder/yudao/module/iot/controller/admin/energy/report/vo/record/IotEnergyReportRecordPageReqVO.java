package cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源报表记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyReportRecordPageReqVO extends PageParam {

    @Schema(description = "报表名称", example = "2024年能耗日报")
    private String reportName;

    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    @Schema(description = "报表类型", example = "day")
    private String reportType;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;

}
