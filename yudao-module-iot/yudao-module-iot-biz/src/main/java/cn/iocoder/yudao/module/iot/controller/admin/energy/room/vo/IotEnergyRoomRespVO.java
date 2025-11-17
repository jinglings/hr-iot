package cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 能源房间 Response VO")
@Data
public class IotEnergyRoomRespVO {

    @Schema(description = "房间ID", example = "1")
    private Long id;

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

    @Schema(description = "房间用途", example = "办公")
    private String roomUsage;

    @Schema(description = "房间面积(平方米)", example = "50.00")
    private BigDecimal area;

    @Schema(description = "平面图X坐标", example = "100")
    private Integer positionX;

    @Schema(description = "平面图Y坐标", example = "200")
    private Integer positionY;

    @Schema(description = "房间描述", example = "部门经理办公室")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
