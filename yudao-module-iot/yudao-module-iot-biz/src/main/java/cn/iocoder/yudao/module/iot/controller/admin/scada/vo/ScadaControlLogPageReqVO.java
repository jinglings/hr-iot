package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * SCADA 控制日志分页查询 VO
 *
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 控制日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScadaControlLogPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称（模糊匹配）", example = "水泵")
    private String deviceName;

    @Schema(description = "命令名称", example = "setPumpStatus")
    private String commandName;

    @Schema(description = "执行状态: 1-成功, 0-失败", example = "1")
    private Integer executionStatus;

    @Schema(description = "操作用户编号", example = "1")
    private Long userId;

    @Schema(description = "操作用户名称", example = "admin")
    private String userName;

    @Schema(description = "来源: SCADA, API, SCHEDULE", example = "SCADA")
    private String source;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
