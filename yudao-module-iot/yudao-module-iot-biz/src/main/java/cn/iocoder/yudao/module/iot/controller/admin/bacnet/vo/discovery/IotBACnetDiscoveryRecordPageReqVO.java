package cn.iocoder.yudao.module.iot.controller.admin.bacnet.vo.discovery;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - BACnet 设备发现记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotBACnetDiscoveryRecordPageReqVO extends PageParam {

    @Schema(description = "BACnet 设备实例号", example = "8001")
    private Integer instanceNumber;

    @Schema(description = "设备名称", example = "AHU-01")
    private String deviceName;

    @Schema(description = "IP 地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "是否已绑定", example = "false")
    private Boolean isBound;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "发现时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] discoveryTime;

}
