package cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 边缘网关更新 Request VO")
@Data
public class IotEdgeGatewayUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "网关名称", example = "工厂1号网关")
    private String name;

    @Schema(description = "设备型号", example = "EdgeGW-2000")
    private String deviceType;

    @Schema(description = "硬件版本", example = "v1.0")
    private String hardwareVersion;

    @Schema(description = "软件版本", example = "v2.1.0")
    private String softwareVersion;

    @Schema(description = "安装位置", example = "车间A区")
    private String location;

    @Schema(description = "网关配置(JSON)")
    private String config;

    @Schema(description = "描述")
    private String description;

}
