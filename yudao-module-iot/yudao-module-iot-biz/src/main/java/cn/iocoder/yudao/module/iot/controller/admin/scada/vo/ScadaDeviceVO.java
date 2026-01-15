package cn.iocoder.yudao.module.iot.controller.admin.scada.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SCADA 设备 VO
 *
 * 用于展示 SCADA 界面中的设备信息和实时数据
 * Part of SCADA-010: Create SCADA Data Models
 *
 * @author HR-IoT Team
 */
@Schema(description = "管理后台 - SCADA 设备 Response VO")
@Data
public class ScadaDeviceVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "水泵1号")
    private String deviceName;

    @Schema(description = "设备备注名称", example = "一号供水站水泵")
    private String nickname;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long productId;

    @Schema(description = "产品名称", example = "智能水泵")
    private String productName;

    @Schema(description = "产品标识", example = "smart_pump")
    private String productKey;

    @Schema(description = "设备类型: 1-直连设备, 2-网关子设备, 3-网关设备", example = "1")
    private Integer deviceType;

    @Schema(description = "设备状态: 0-未激活, 1-在线, 2-离线, 3-禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer state;

    @Schema(description = "状态描述", example = "在线")
    private String stateDesc;

    @Schema(description = "设备图片 URL", example = "https://example.com/pump.png")
    private String picUrl;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "设备属性实时值", example = "{\"temperature\": 25.5, \"pressure\": 1.2}")
    private Map<String, Object> properties;

    @Schema(description = "是否有活动告警", example = "false")
    private Boolean hasActiveAlarm;

    @Schema(description = "活动告警数量", example = "0")
    private Integer activeAlarmCount;

    @Schema(description = "是否可控制", example = "true")
    private Boolean controllable;

    @Schema(description = "设备位置纬度", example = "31.2304")
    private Double latitude;

    @Schema(description = "设备位置经度", example = "121.4737")
    private Double longitude;

    @Schema(description = "设备地址", example = "上海市浦东新区")
    private String address;

}
