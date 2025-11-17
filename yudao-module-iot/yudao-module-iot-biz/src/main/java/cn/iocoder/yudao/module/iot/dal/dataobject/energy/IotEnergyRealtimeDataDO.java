package cn.iocoder.yudao.module.iot.dal.dataobject.energy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 能源实时数据 DO
 *
 * 使用 TDengine 存储
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotEnergyRealtimeDataDO {

    /**
     * 时间戳
     */
    private Long ts;

    /**
     * 计量点ID
     */
    private Long meterId;

    /**
     * 瞬时功率/流量
     */
    private Double instantPower;

    /**
     * 累计值
     */
    private Double cumulativeValue;

    /**
     * 电压(电表专用)
     */
    private Double voltage;

    /**
     * 电流(电表专用)
     */
    private Double current;

    /**
     * 功率因数(电表专用)
     */
    private Double powerFactor;

    /**
     * 数据质量(0异常1正常)
     */
    private Integer dataQuality;

    // ========== TAGS 标签字段 ==========

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
     * 租户ID
     */
    private Long tenantId;

}
