package cn.iocoder.yudao.module.iot.service.energy.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo.IotEnergyDataQueryReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;

import java.util.List;
import java.util.Map;

/**
 * IoT 能源数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyDataService {

    /**
     * 保存能源实时数据
     *
     * @param data 能源实时数据
     */
    void saveRealtimeData(IotEnergyRealtimeDataDO data);

    /**
     * 批量保存能源实时数据
     *
     * @param dataList 能源实时数据列表
     */
    void saveRealtimeDataBatch(List<IotEnergyRealtimeDataDO> dataList);

    /**
     * 获得能源实时数据分页
     *
     * @param reqVO 查询条件
     * @return 能源实时数据分页
     */
    PageResult<IotEnergyRealtimeDataDO> getRealtimeDataPage(IotEnergyDataQueryReqVO reqVO);

    /**
     * 查询计量点的最新数据
     *
     * @param meterId 计量点ID
     * @return 最新数据
     */
    IotEnergyRealtimeDataDO getLatestDataByMeterId(Long meterId);

    /**
     * 查询计量点在指定时间范围内的数据
     *
     * @param meterId 计量点ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据列表
     */
    List<IotEnergyRealtimeDataDO> getDataListByMeterIdAndTimeRange(Long meterId, Long startTime, Long endTime);

    /**
     * 按时间范围统计能耗数据（聚合）
     *
     * @param meterId 计量点ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（如：1m, 1h, 1d）
     * @return 聚合数据列表
     */
    List<Map<String, Object>> getAggregateDataByTimeRange(Long meterId, Long startTime, Long endTime, String interval);

    /**
     * 按建筑统计能耗数据
     *
     * @param buildingId 建筑ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> getEnergyStatsByBuilding(Long buildingId, Long startTime, Long endTime);

    /**
     * 按能源类型统计能耗数据
     *
     * @param energyTypeId 能源类型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    List<Map<String, Object>> getEnergyStatsByType(Long energyTypeId, Long startTime, Long endTime);

}
