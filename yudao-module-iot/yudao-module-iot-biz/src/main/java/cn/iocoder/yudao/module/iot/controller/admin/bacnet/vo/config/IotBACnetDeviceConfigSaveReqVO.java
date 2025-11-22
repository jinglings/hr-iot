package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - BACnet 设备配置创建/修改 Request VO")
@Data
public class IotBACnetDeviceConfigSaveReqVO {

    @Schema(description = "配置编号（更新时必填）", example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "BACnet 设备实例号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8001")
    @NotNull(message = "BACnet 设备实例号不能为空")
    private Integer instanceNumber;

    @Schema(description = "IP 地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口", example = "47808")
    private Integer port;

    @Schema(description = "网络号", example = "0")
    private Integer networkNumber;

    @Schema(description = "MAC 地址", example = "00:11:22:33:44:55")
    private String macAddress;

    @Schema(description = "最大 APDU 长度", example = "1476")
    private Integer maxApduLength;

    @Schema(description = "分段支持（0=不支持,1=发送,2=接收,3=发送和接收）", example = "3")
    private Integer segmentationSupported;

    @Schema(description = "供应商 ID", example = "8")
    private Integer vendorId;

    @Schema(description = "是否启用轮询", example = "true")
    private Boolean pollingEnabled;

    @Schema(description = "轮询间隔（毫秒）", example = "5000")
    private Integer pollingInterval;

    @Schema(description = "读取超时（毫秒）", example = "5000")
    private Integer readTimeout;

    @Schema(description = "重试次数", example = "3")
    private Integer retryCount;

    @Schema(description = "配置状态（0=禁用,1=启用）", example = "1")
    private Integer status;

}
