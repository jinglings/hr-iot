package cn.iocoder.yudao.module.iot.controller.admin.device.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备能耗 Response VO")
@Data
public class IotDeviceWithEnergyRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "电表001")
    private String deviceName;

    @Schema(description = "设备备注名称", example = "一号楼电表")
    private String nickname;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "产品名称", example = "智能电表")
    private String productName;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer state;

    @Schema(description = "能耗值(energy属性)", example = "1234.56")
    private BigDecimal energy;

    @Schema(description = "能耗更新时间")
    private LocalDateTime energyUpdateTime;

    @Schema(description = "设备位置的纬度", example = "30.123456")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度", example = "120.123456")
    private BigDecimal longitude;

    @Schema(description = "最后上线时间")
    private LocalDateTime onlineTime;

    @Schema(description = "最后离线时间")
    private LocalDateTime offlineTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
