package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - BACnet 设备绑定 Request VO")
@Data
public class IotBACnetDiscoveryBindReqVO {

    @Schema(description = "发现记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发现记录编号不能为空")
    private Long discoveryRecordId;

    @Schema(description = "设备编号（绑定到已有设备）", example = "1001")
    private Long deviceId;

    @Schema(description = "设备名称（创建新设备）", example = "BACnet-AHU-01")
    private String deviceName;

    @Schema(description = "产品编号（创建新设备时必填）", example = "10")
    private Long productId;

    @Schema(description = "是否启用智能映射", example = "true")
    private Boolean enableSmartMapping;

}
