package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCADA 历史数据点 VO
 *
 * 用于展示设备属性的历史数据趋势
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 历史数据点 Response VO")
@Data
public class ScadaHistoryPointVO {

    @Schema(description = "时间戳", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime timestamp;

    @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "25.5")
    private Object value;

    @Schema(description = "属性名称", example = "temperature")
    private String property;

    @Schema(description = "单位", example = "℃")
    private String unit;

    @Schema(description = "数据质量: 0-GOOD, 1-BAD, 2-UNCERTAIN", example = "0")
    private Integer quality;

    @Schema(description = "设备编号", example = "1")
    private Long deviceId;

    @Schema(description = "SCADA Tag 名称", example = "Pump1_Temperature")
    private String tagName;

}
