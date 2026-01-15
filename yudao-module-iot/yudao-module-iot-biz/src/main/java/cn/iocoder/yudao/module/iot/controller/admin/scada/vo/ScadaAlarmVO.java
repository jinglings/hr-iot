package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCADA 告警 VO
 *
 * 用于展示 SCADA 界面中的告警信息
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 告警 Response VO")
@Data
public class ScadaAlarmVO {

    @Schema(description = "告警记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "告警配置编号", example = "100")
    private Long alarmId;

    @Schema(description = "告警名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "水泵压力过高")
    private String alarmName;

    @Schema(description = "告警类型: HIGH, HIGHHIGH, LOW, LOWLOW, BOOL", example = "HIGH")
    private String alarmType;

    @Schema(description = "关联的 Tag ID", example = "tag_001")
    private String tagId;

    @Schema(description = "关联的 Tag 名称", example = "Pump1_Pressure")
    private String tagName;

    @Schema(description = "设备编号", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "水泵1号")
    private String deviceName;

    @Schema(description = "触发时的值", example = "15.5")
    private String tagValue;

    @Schema(description = "阈值", example = "10.0")
    private String thresholdValue;

    @Schema(description = "单位", example = "MPa")
    private String unit;

    @Schema(description = "优先级: 1-低, 2-中, 3-高, 4-紧急", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer priority;

    @Schema(description = "优先级描述", example = "高")
    private String priorityDesc;

    @Schema(description = "告警消息", example = "水泵1号压力超过上限")
    private String message;

    @Schema(description = "状态: 1-活动, 2-已确认, 3-已恢复, 4-已关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "活动")
    private String statusDesc;

    @Schema(description = "触发时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime triggeredAt;

    @Schema(description = "持续时间(秒)", example = "120")
    private Long durationSeconds;

    @Schema(description = "确认时间")
    private LocalDateTime acknowledgedAt;

    @Schema(description = "确认人", example = "张三")
    private String acknowledgedBy;

    @Schema(description = "恢复时间")
    private LocalDateTime recoveredAt;

    @Schema(description = "关闭时间")
    private LocalDateTime closedAt;

    @Schema(description = "关闭人", example = "李四")
    private String closedBy;

    @Schema(description = "备注", example = "已通知维修人员")
    private String notes;

    @Schema(description = "是否需要确认", example = "true")
    private Boolean ackRequired;

}
