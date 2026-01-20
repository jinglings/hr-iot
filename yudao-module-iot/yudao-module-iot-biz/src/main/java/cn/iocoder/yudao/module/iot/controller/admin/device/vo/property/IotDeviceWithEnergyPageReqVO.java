package cn.iocoder.yudao.module.iot.controller.admin.device.vo.property;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 设备能耗分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceWithEnergyPageReqVO extends PageParam {

    @Schema(description = "设备名称", example = "电表001")
    private String deviceName;

    @Schema(description = "备注名称", example = "一号楼电表")
    private String nickname;

    @Schema(description = "产品编号", example = "1024")
    private Long productId;

    @Schema(description = "设备状态", example = "1")
    private Integer state;

    @Schema(description = "设备分组编号", example = "1")
    private Long groupId;

}
