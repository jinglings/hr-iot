package cn.iocoder.yudao.module.report.controller.admin.visualization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 可视化大屏 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class VisualizationDashboardBaseVO {

    @Schema(description = "大屏名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "销售数据大屏")
    @NotBlank(message = "大屏名称不能为空")
    private String name;

    @Schema(description = "大屏描述", example = "展示销售数据的可视化大屏")
    private String description;

    @Schema(description = "预览图片 URL", example = "https://www.iocoder.cn/preview.png")
    private String picUrl;

    @Schema(description = "缩略图 URL", example = "https://www.iocoder.cn/thumbnail.png")
    private String thumbnail;

    @Schema(description = "大屏配置（JSON字符串）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "大屏配置不能为空")
    private String config;

    @Schema(description = "画布宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1920")
    @NotNull(message = "画布宽度不能为空")
    private Integer width;

    @Schema(description = "画布高度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1080")
    @NotNull(message = "画布高度不能为空")
    private Integer height;

    @Schema(description = "背景颜色", example = "#0e1117")
    private String backgroundColor;

    @Schema(description = "背景图片 URL", example = "https://www.iocoder.cn/background.png")
    private String backgroundImage;

    @Schema(description = "屏幕适配模式", example = "scale")
    private String scaleMode;

    @Schema(description = "发布状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "访问密码", example = "123456")
    private String password;

    @Schema(description = "是否公开", example = "true")
    private Boolean isPublic;

    @Schema(description = "分类", example = "kpi")
    private String category;

    @Schema(description = "标签（逗号分隔）", example = "销售,数据分析")
    private String tags;

    @Schema(description = "备注", example = "用于展示销售团队的业绩数据")
    private String remark;
}
