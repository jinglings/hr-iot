package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 边缘网关 Response VO")
@Data
public class IotEdgeGatewayRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "网关名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工厂1号网关")
    private String name;

    @Schema(description = "网关序列号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serialNumber;

    @Schema(description = "网关标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gatewayKey;

    @Schema(description = "网关密钥")
    private String gatewaySecret;

    @Schema(description = "设备型号", example = "EdgeGW-2000")
    private String deviceType;

    @Schema(description = "硬件版本", example = "v1.0")
    private String hardwareVersion;

    @Schema(description = "软件版本", example = "v2.1.0")
    private String softwareVersion;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "MAC地址", example = "00:1B:44:11:3A:B7")
    private String macAddress;

    @Schema(description = "安装位置", example = "车间A区")
    private String location;

    @Schema(description = "状态: 0=未激活, 1=在线, 2=离线", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "CPU核心数", example = "4")
    private Integer cpuCores;

    @Schema(description = "总内存(MB)", example = "8192")
    private Integer memoryTotal;

    @Schema(description = "总磁盘(GB)", example = "500")
    private Integer diskTotal;

    @Schema(description = "网关配置(JSON)")
    private String config;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
