package cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 设备标签分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceTagPageReqVO extends PageParam {

    @Schema(description = "标签键", example = "location")
    private String tagKey;

    @Schema(description = "标签值", example = "车间A")
    private String tagValue;

    @Schema(description = "是否预置标签")
    private Boolean isPreset;

}
