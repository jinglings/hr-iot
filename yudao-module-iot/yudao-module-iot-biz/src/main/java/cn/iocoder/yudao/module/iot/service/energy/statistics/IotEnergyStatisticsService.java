package cn.iocoder.yudao.module.iot.service.energy.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo.IotEnergyStatisticsPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 能源统计数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyStatisticsService {

    /**
     * 保存统计数据
     *
     * @param statistics 统计数据
     */
    void saveStatistics(IotEnergyStatisticsDO statistics);

    /**
     * 批量保存统计数据
     *
     * @param statisticsList 统计数据列表
     */
    void saveStatisticsBatch(List<IotEnergyStatisticsDO> statisticsList);

    /**
     * 获得统计数据分页
     *
     * @param reqVO 查询条件
     * @return 统计数据分页
     */
    PageResult<IotEnergyStatisticsDO> getStatisticsPage(IotEnergyStatisticsPageReqVO reqVO);

    /**
     * 按计量点和周期查询统计数据
     *
     * @param meterId 计量点ID
     * @param statPeriod 统计周期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    List<IotEnergyStatisticsDO> getStatisticsListByMeterIdAndPeriod(Long meterId, String statPeriod,
                                                                     LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按建筑和周期查询统计数据
     *
     * @param buildingId 建筑ID
     * @param statPeriod 统计周期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    List<IotEnergyStatisticsDO> getStatisticsListByBuildingIdAndPeriod(Long buildingId, String statPeriod,
                                                                        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按能源类型和周期查询统计数据
     *
     * @param energyTypeId 能源类型ID
     * @param statPeriod 统计周期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    List<IotEnergyStatisticsDO> getStatisticsListByEnergyTypeIdAndPeriod(Long energyTypeId, String statPeriod,
                                                                          LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 聚合计量点的实时数据（分钟级）
     *
     * @param meterId 计量点ID
     * @param statTime 统计时间
     */
    void aggregateMinuteData(Long meterId, LocalDateTime statTime);

    /**
     * 聚合计量点的实时数据（小时级）
     *
     * @param meterId 计量点ID
     * @param statTime 统计时间
     */
    void aggregateHourData(Long meterId, LocalDateTime statTime);

    /**
     * 聚合计量点的实时数据（日级）
     *
     * @param meterId 计量点ID
     * @param statTime 统计时间
     */
    void aggregateDayData(Long meterId, LocalDateTime statTime);

    /**
     * 聚合计量点的实时数据（月级）
     *
     * @param meterId 计量点ID
     * @param statTime 统计时间
     */
    void aggregateMonthData(Long meterId, LocalDateTime statTime);

    /**
     * 删除指定时间之前的统计数据
     *
     * @param statTime 统计时间
     * @return 删除数量
     */
    int deleteStatisticsByTimeBefore(LocalDateTime statTime);

    /**
     * 按时间范围查询统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param statPeriod 统计周期（可选）
     * @param buildingId 建筑ID（可选）
     * @param energyTypeId 能源类型ID（可选）
     * @return 统计数据列表
     */
    List<IotEnergyStatisticsDO> getStatisticsByTimeRange(LocalDateTime startTime, LocalDateTime endTime,
                                                          String statPeriod, Long buildingId, Long energyTypeId);

}
