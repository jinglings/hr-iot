package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - IoT 边缘网关创建 Request VO")
@Data
public class IotEdgeGatewayCreateReqVO {

    @Schema(description = "网关名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工厂1号网关")
    @NotBlank(message = "网关名称不能为空")
    private String name;

    @Schema(description = "网关序列号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "网关序列号不能为空")
    private String serialNumber;

    @Schema(description = "设备型号", example = "EdgeGW-2000")
    private String deviceType;

    @Schema(description = "硬件版本", example = "v1.0")
    private String hardwareVersion;

    @Schema(description = "安装位置", example = "车间A区")
    private String location;

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

}
