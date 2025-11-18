package cn.iocoder.yudao.module.report.controller.admin.visualization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 可视化大屏创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VisualizationDashboardCreateReqVO extends VisualizationDashboardBaseVO {
}
