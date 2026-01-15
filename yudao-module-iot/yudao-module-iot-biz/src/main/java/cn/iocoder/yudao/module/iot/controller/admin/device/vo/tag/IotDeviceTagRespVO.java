package cn.iocoder.yudao.module.iot.controller.admin.device.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备标签 Response VO")
@Data
public class IotDeviceTagRespVO {

    @Schema(description = "标签ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "标签键", requiredMode = Schema.RequiredMode.REQUIRED, example = "location")
    private String tagKey;

    @Schema(description = "标签值", example = "车间A")
    private String tagValue;

    @Schema(description = "标签描述", example = "设备位置标签")
    private String description;

    @Schema(description = "标签显示颜色", example = "#409EFF")
    private String color;

    @Schema(description = "是否预置标签")
    private Boolean isPreset;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
