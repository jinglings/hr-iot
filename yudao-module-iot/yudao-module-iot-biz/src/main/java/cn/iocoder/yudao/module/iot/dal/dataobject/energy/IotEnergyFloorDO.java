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
 * IoT 能源管理 - 楼层 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_floor", autoResultMap = true)
@KeySequence("iot_energy_floor_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyFloorDO extends TenantBaseDO {

    /**
     * 楼层ID
     */
    @TableId
    private Long id;

    /**
     * 楼层名称
     */
    private String floorName;

    /**
     * 楼层编码
     */
    private String floorCode;

    /**
     * 所属建筑ID
     */
    private Long buildingId;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 楼层序号（负数表示地下）
     */
    private Integer floorNumber;

    /**
     * 楼层面积(平方米)
     */
    private BigDecimal area;

    /**
     * 楼层平面图URL
     */
    private String floorPlanUrl;

    /**
     * 楼层描述
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
