package cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 设备调试日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceDebugLogPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1024")
    private Long deviceId;

    @Schema(description = "消息方向: 1=上行, 2=下行")
    private Integer direction;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "状态: 0=失败, 1=成功")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
