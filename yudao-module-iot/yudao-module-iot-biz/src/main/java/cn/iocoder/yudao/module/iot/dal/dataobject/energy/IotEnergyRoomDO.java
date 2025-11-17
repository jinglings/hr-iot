package cn.iocoder.yudao.module.iot.dal.dataobject.energy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * IoT 能源管理 - 房间 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_room", autoResultMap = true)
@KeySequence("iot_energy_room_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyRoomDO extends TenantBaseDO {

    /**
     * 房间ID
     */
    @TableId
    private Long id;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间编码
     */
    private String roomCode;

    /**
     * 所属建筑ID
     */
    private Long buildingId;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 所属楼层ID
     */
    private Long floorId;

    /**
     * 房间类型
     *
     * meeting:会议室, office:办公室, server:机房, warehouse:仓库, workshop:车间, other:其他
     */
    private String roomType;

    /**
     * 房间用途
     */
    private String roomUsage;

    /**
     * 房间面积(平方米)
     */
    private BigDecimal area;

    /**
     * 平面图X坐标
     */
    private Integer positionX;

    /**
     * 平面图Y坐标
     */
    private Integer positionY;

    /**
     * 房间描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     *
     * 0:停用 1:启用
     */
    private Integer status;

}
