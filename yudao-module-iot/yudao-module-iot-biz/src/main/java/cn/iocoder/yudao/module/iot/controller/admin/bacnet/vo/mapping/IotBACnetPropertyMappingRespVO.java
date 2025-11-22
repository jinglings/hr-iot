package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - BACnet 属性映射配置 Response VO")
@Data
public class IotBACnetPropertyMappingRespVO {

    @Schema(description = "映射编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceConfigId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long deviceId;

    @Schema(description = "物模型功能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long thingModelId;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    private String identifier;

    @Schema(description = "对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ANALOG_INPUT")
    private String objectType;

    @Schema(description = "对象实例号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer objectInstance;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRESENT_VALUE")
    private String propertyIdentifier;

    @Schema(description = "属性数组索引", example = "0")
    private Integer propertyArrayIndex;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "float")
    private String dataType;

    @Schema(description = "单位转换公式", example = "value * 0.1")
    private String unitConversion;

    @Schema(description = "值映射", example = "{\"0\":\"关闭\",\"1\":\"开启\"}")
    private String valueMapping;

    @Schema(description = "访问模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "rw")
    private String accessMode;

    @Schema(description = "写入优先级", example = "8")
    private Integer priority;

    @Schema(description = "是否启用轮询", example = "true")
    private Boolean pollingEnabled;

    @Schema(description = "是否启用 COV", example = "false")
    private Boolean covEnabled;

    @Schema(description = "映射状态", example = "1")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
