package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.config;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - BACnet 设备配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotBACnetDeviceConfigPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1001")
    private Long deviceId;

    @Schema(description = "BACnet 设备实例号", example = "8001")
    private Integer instanceNumber;

    @Schema(description = "IP 地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "配置状态", example = "1")
    private Integer status;

}
