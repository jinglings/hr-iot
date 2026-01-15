package cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备调试日志 Response VO")
@Data
public class IotDeviceDebugLogRespVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备标识")
    private String deviceKey;

    @Schema(description = "产品标识")
    private String productKey;

    @Schema(description = "消息方向: 1=上行, 2=下行")
    private Integer direction;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "属性/事件/服务标识符")
    private String identifier;

    @Schema(description = "消息内容(JSON)")
    private String payload;

    @Schema(description = "调用结果(JSON)")
    private String result;

    @Schema(description = "状态: 0=失败, 1=成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "响应延迟(毫秒)")
    private Integer latency;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
