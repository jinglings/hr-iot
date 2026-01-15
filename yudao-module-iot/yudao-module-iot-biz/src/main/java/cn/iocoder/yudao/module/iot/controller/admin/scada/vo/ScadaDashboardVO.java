package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCADA 仪表板 VO
 *
 * 用于展示 SCADA 仪表板列表和详情
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 仪表板 Response VO")
@Data
public class ScadaDashboardVO {

    @Schema(description = "仪表板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "FUXA 仪表板 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "uuid-xxx")
    private String dashboardId;

    @Schema(description = "仪表板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "供水系统总览")
    private String name;

    @Schema(description = "描述", example = "显示供水系统的整体运行状态")
    private String description;

    @Schema(description = "仪表板类型: water, hvac, energy, custom", example = "water")
    private String dashboardType;

    @Schema(description = "类型描述", example = "供水系统")
    private String dashboardTypeDesc;

    @Schema(description = "缩略图 URL", example = "https://example.com/thumb.png")
    private String thumbnailUrl;

    @Schema(description = "是否默认仪表板", example = "true")
    private Boolean isDefault;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "FUXA 访问 URL", example = "http://fuxa:1881/view/uuid-xxx")
    private String fuxaUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者", example = "admin")
    private String creator;

}
