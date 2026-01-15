package cn.iocoder.yudao.module.iot.controller.admin.device.vo.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备调试结果 Response VO")
@Data
public class IotDeviceDebugResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "结果数据(JSON)")
    private String data;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "响应延迟(毫秒)")
    private Integer latency;

    public static IotDeviceDebugResultVO success(String messageId, String data, Integer latency) {
        IotDeviceDebugResultVO result = new IotDeviceDebugResultVO();
        result.setSuccess(true);
        result.setMessageId(messageId);
        result.setData(data);
        result.setLatency(latency);
        return result;
    }

    public static IotDeviceDebugResultVO fail(String errorMessage) {
        IotDeviceDebugResultVO result = new IotDeviceDebugResultVO();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }

}
