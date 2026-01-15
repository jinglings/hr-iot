package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * SCADA Tag 映射 VO
 *
 * 用于配置和查看 SCADA Tag 与设备属性的映射关系
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA Tag 映射 Response VO")
@Data
public class ScadaTagMappingVO {

    @Schema(description = "映射编号", example = "1")
    private Long id;

    @Schema(description = "SCADA Tag 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Pump1_Status")
    @NotBlank(message = "Tag 名称不能为空")
    private String tagName;

    @Schema(description = "FUXA 内部 Tag ID", example = "tag_001")
    private String tagId;

    @Schema(description = "IoT 设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "设备名称", example = "水泵1号")
    private String deviceName;

    @Schema(description = "物模型属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "status")
    @NotBlank(message = "属性标识符不能为空")
    private String propertyIdentifier;

    @Schema(description = "属性名称", example = "运行状态")
    private String propertyName;

    @Schema(description = "数据类型: number, string, boolean, object", example = "boolean")
    private String dataType;

    @Schema(description = "单位", example = "")
    private String unit;

    @Schema(description = "缩放系数", example = "1.0")
    private BigDecimal scaleFactor;

    @Schema(description = "偏移量", example = "0.0")
    private BigDecimal offset;

    @Schema(description = "最小值", example = "0")
    private BigDecimal minValue;

    @Schema(description = "最大值", example = "100")
    private BigDecimal maxValue;

    @Schema(description = "描述", example = "水泵运行状态")
    private String description;

    @Schema(description = "当前值", example = "true")
    private Object currentValue;

    @Schema(description = "更新时间", example = "2026-01-03T12:00:00")
    private String lastUpdateTime;

}
