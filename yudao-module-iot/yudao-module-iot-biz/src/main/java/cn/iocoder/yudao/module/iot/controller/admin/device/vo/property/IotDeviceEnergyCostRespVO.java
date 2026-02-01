package cn.iocoder.yudao.module.iot.controller.admin.device.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备电费计算 Response VO")
@Data
public class IotDeviceEnergyCostRespVO {

    @Schema(description = "设备编号", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "电表001")
    private String deviceName;

    @Schema(description = "设备备注名称", example = "一号楼电表")
    private String nickname;

    @Schema(description = "产品名称", example = "智能电表")
    private String productName;

    @Schema(description = "位置（从设备名称解析）", example = "B1_01B")
    private String location;

    @Schema(description = "表号（从设备名称解析）", example = "922101010083")
    private String meterNo;

    @Schema(description = "开始时间能耗读数", example = "1000.50")
    private BigDecimal startEnergy;

    @Schema(description = "开始时间能耗采集时间")
    private LocalDateTime startEnergyTime;

    @Schema(description = "结束时间能耗读数", example = "1200.80")
    private BigDecimal endEnergy;

    @Schema(description = "结束时间能耗采集时间")
    private LocalDateTime endEnergyTime;

    @Schema(description = "实际消耗（未乘倍率）", example = "200.30")
    private BigDecimal rawConsumption;

    @Schema(description = "倍率", example = "1.0000")
    private BigDecimal ratio;

    @Schema(description = "实际消耗（已乘倍率）", example = "200.30")
    private BigDecimal consumption;

    @Schema(description = "电费单价（元/度）", example = "1.0616")
    private BigDecimal unitPrice;

    @Schema(description = "电费（元）", example = "212.64")
    private BigDecimal cost;

}
