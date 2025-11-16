package cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 能源房间分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotEnergyRoomPageReqVO extends PageParam {

    @Schema(description = "房间名称", example = "101办公室")
    private String roomName;

    @Schema(description = "房间编码", example = "ROOM_001")
    private String roomCode;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "房间类型", example = "office")
    private String roomType;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
