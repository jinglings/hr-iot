package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - BACnet 设备发现记录 Response VO")
@Data
public class IotBACnetDiscoveryRecordRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "BACnet 设备实例号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8001")
    private Integer instanceNumber;

    @Schema(description = "设备名称", example = "AHU-01")
    private String deviceName;

    @Schema(description = "IP 地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口", example = "47808")
    private Integer port;

    @Schema(description = "供应商 ID", example = "8")
    private Integer vendorId;

    @Schema(description = "供应商名称", example = "Honeywell")
    private String vendorName;

    @Schema(description = "设备型号", example = "AHU-100")
    private String modelName;

    @Schema(description = "固件版本", example = "1.0.5")
    private String firmwareRevision;

    @Schema(description = "应用软件版本", example = "2.1.0")
    private String applicationSoftwareVersion;

    @Schema(description = "设备位置", example = "Building A - Floor 3")
    private String location;

    @Schema(description = "设备描述", example = "Air Handling Unit")
    private String description;

    @Schema(description = "对象列表（JSON）", example = "[{\"type\":\"ANALOG_INPUT\",\"instance\":0}]")
    private String objectList;

    @Schema(description = "是否已绑定", example = "false")
    private Boolean isBound;

    @Schema(description = "绑定的设备编号", example = "1001")
    private Long boundDeviceId;

    @Schema(description = "发现时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime discoveryTime;

    @Schema(description = "最后发现时间")
    private LocalDateTime lastSeenTime;

    @Schema(description = "状态（0=离线,1=在线）", example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
