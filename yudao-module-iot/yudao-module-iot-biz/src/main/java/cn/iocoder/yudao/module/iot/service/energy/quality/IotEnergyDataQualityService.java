package cn.iocoder.yudao.module.iot.service.energy.quality;

import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;

/**
 * IoT 能源数据质量监控 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyDataQualityService {

    /**
     * 检测数据质量
     *
     * @param data 能源实时数据
     * @return 数据质量（0:异常 1:正常）
     */
    Integer detectDataQuality(IotEnergyRealtimeDataDO data);

    /**
     * 检测数据是否异常
     *
     * @param meterId 计量点ID
     * @param currentValue 当前值
     * @return 是否异常
     */
    boolean isAbnormalValue(Long meterId, Double currentValue);

    /**
     * 检测数据是否突变
     *
     * @param meterId 计量点ID
     * @param currentValue 当前值
     * @return 是否突变
     */
    boolean isMutationValue(Long meterId, Double currentValue);

}
