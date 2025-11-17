package cn.iocoder.yudao.module.iot.dal.dataobject.energy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 能源统计数据 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_energy_statistics", autoResultMap = true)
@KeySequence("iot_energy_statistics_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyStatisticsDO extends TenantBaseDO {

    /**
     * 统计ID
     */
    @TableId
    private Long id;

    /**
     * 统计时间
     */
    private LocalDateTime statTime;

    /**
     * 统计周期（minute:分钟 hour:小时 day:日 month:月）
     */
    private String statPeriod;

    /**
     * 计量点ID
     */
    private Long meterId;

    /**
     * 能源类型ID
     */
    private Long energyTypeId;

    /**
     * 建筑ID
     */
    private Long buildingId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 楼层ID
     */
    private Long floorId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 起始值
     */
    private BigDecimal startValue;

    /**
     * 结束值
     */
    private BigDecimal endValue;

    /**
     * 能耗量（差值）
     */
    private BigDecimal consumption;

    /**
     * 最大值
     */
    private BigDecimal maxValue;

    /**
     * 最小值
     */
    private BigDecimal minValue;

    /**
     * 平均值
     */
    private BigDecimal avgValue;

}
