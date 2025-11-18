package cn.iocoder.yudao.module.iot.controller.admin.shadow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - IoT 设备影子变更历史 Response VO")
@Data
public class IotDeviceShadowHistoryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long deviceId;

    @Schema(description = "影子ID", example = "1001")
    private Long shadowId;

    @Schema(description = "变更类型", example = "DESIRED")
    private String changeType;

    @Schema(description = "变更前状态")
    private Map<String, Object> beforeState;

    @Schema(description = "变更后状态")
    private Map<String, Object> afterState;

    @Schema(description = "差量状态")
    private Map<String, Object> deltaState;

    @Schema(description = "版本号", example = "5")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
