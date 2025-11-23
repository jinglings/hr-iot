package cn.iocoder.yudao.module.iot.service.energy.dashboard;

import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyRankingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyTrendRespVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT 能源数据可视化 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyDashboardService {

    /**
     * 获取能耗总览数据
     *
     * @return 总览数据
     */
    IotEnergyOverviewRespVO getEnergyOverview();

    /**
     * 获取能耗排名
     *
     * @param type 对象类型（building-建筑，meter-计量点）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param topN 取前N名
     * @return 排名列表
     */
    List<IotEnergyRankingRespVO> getEnergyRanking(String type, LocalDateTime startTime,
                                                   LocalDateTime endTime, Integer topN);

    /**
     * 获取能耗趋势数据
     *
     * @param period 周期（24h/7d/30d/12m）
     * @param buildingId 建筑ID（可选）
     * @param energyTypeId 能源类型ID（可选）
     * @return 趋势数据
     */
    IotEnergyTrendRespVO getEnergyTrend(String period, Long buildingId, Long energyTypeId);

    /**
     * 获取分项能耗数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param buildingId 建筑ID（可选）
     * @return 分项能耗数据（能源类型名称 -> 能耗值）
     */
    Map<String, Object> getEnergyBreakdown(LocalDateTime startTime, LocalDateTime endTime, Long buildingId);

}
