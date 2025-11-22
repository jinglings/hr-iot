package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - BACnet 属性映射配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotBACnetPropertyMappingPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1001")
    private Long deviceId;

    @Schema(description = "设备配置编号", example = "1")
    private Long deviceConfigId;

    @Schema(description = "属性标识符", example = "temperature")
    private String identifier;

    @Schema(description = "对象类型", example = "ANALOG_INPUT")
    private String objectType;

    @Schema(description = "映射状态", example = "1")
    private Integer status;

}
