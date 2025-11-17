package cn.iocoder.yudao.module.iot.service.energy.statistics;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo.IotEnergyStatisticsPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.IotEnergyStatisticsMapper;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotEnergyRealtimeDataMapper;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 能源统计数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyStatisticsServiceImpl implements IotEnergyStatisticsService {

    @Resource
    private IotEnergyStatisticsMapper statisticsMapper;

    @Resource
    private IotEnergyRealtimeDataMapper realtimeDataMapper;

    @Resource
    private IotEnergyMeterService meterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStatistics(IotEnergyStatisticsDO statistics) {
        // 检查是否已存在，如果存在则更新，否则插入
        IotEnergyStatisticsDO existingStats = statisticsMapper.selectOneByMeterIdAndTime(
                statistics.getMeterId(), statistics.getStatPeriod(), statistics.getStatTime());

        if (existingStats != null) {
            statistics.setId(existingStats.getId());
            statisticsMapper.updateById(statistics);
        } else {
            statisticsMapper.insert(statistics);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStatisticsBatch(List<IotEnergyStatisticsDO> statisticsList) {
        for (IotEnergyStatisticsDO statistics : statisticsList) {
            saveStatistics(statistics);
        }
    }

    @Override
    public PageResult<IotEnergyStatisticsDO> getStatisticsPage(IotEnergyStatisticsPageReqVO reqVO) {
        return statisticsMapper.selectPage(reqVO);
    }

    @Override
    public List<IotEnergyStatisticsDO> getStatisticsListByMeterIdAndPeriod(Long meterId, String statPeriod,
                                                                            LocalDateTime startTime, LocalDateTime endTime) {
        return statisticsMapper.selectListByMeterIdAndPeriod(meterId, statPeriod, startTime, endTime);
    }

    @Override
    public List<IotEnergyStatisticsDO> getStatisticsListByBuildingIdAndPeriod(Long buildingId, String statPeriod,
                                                                               LocalDateTime startTime, LocalDateTime endTime) {
        return statisticsMapper.selectListByBuildingIdAndPeriod(buildingId, statPeriod, startTime, endTime);
    }

    @Override
    public List<IotEnergyStatisticsDO> getStatisticsListByEnergyTypeIdAndPeriod(Long energyTypeId, String statPeriod,
                                                                                 LocalDateTime startTime, LocalDateTime endTime) {
        return statisticsMapper.selectListByEnergyTypeIdAndPeriod(energyTypeId, statPeriod, startTime, endTime);
    }

    @Override
    public void aggregateMinuteData(Long meterId, LocalDateTime statTime) {
        aggregateData(meterId, statTime, "minute", 1);
    }

    @Override
    public void aggregateHourData(Long meterId, LocalDateTime statTime) {
        aggregateData(meterId, statTime, "hour", 60);
    }

    @Override
    public void aggregateDayData(Long meterId, LocalDateTime statTime) {
        aggregateData(meterId, statTime, "day", 24 * 60);
    }

    @Override
    public void aggregateMonthData(Long meterId, LocalDateTime statTime) {
        aggregateData(meterId, statTime, "month", 30 * 24 * 60);
    }

    /**
     * 聚合数据通用方法
     *
     * @param meterId 计量点ID
     * @param statTime 统计时间
     * @param statPeriod 统计周期
     * @param minutes 时间范围（分钟）
     */
    private void aggregateData(Long meterId, LocalDateTime statTime, String statPeriod, int minutes) {
        try {
            // 1. 获取计量点信息
            IotEnergyMeterDO meter = meterService.getMeter(meterId);
            if (meter == null) {
                log.warn("[aggregateData][计量点({})不存在]", meterId);
                return;
            }

            // 2. 如果是虚拟表，暂时跳过（虚拟表的聚合逻辑在虚拟表计算Service中处理）
            if (Boolean.TRUE.equals(meter.getIsVirtual())) {
                return;
            }

            // 3. 计算时间范围
            LocalDateTime startTime = getStartTime(statTime, statPeriod);
            LocalDateTime endTime = getEndTime(statTime, statPeriod);

            // 4. 从TDengine查询该时间段的数据
            Long startTs = LocalDateTimeUtil.toEpochMilli(startTime);
            Long endTs = LocalDateTimeUtil.toEpochMilli(endTime);

            List<IotEnergyRealtimeDataDO> dataList = realtimeDataMapper.selectListByMeterIdAndTimeRange(
                    meterId, startTs, endTs);

            if (dataList.isEmpty()) {
                log.debug("[aggregateData][计量点({})在时间段({} - {})没有数据]", meterId, startTime, endTime);
                return;
            }

            // 5. 计算统计值
            BigDecimal startValue = null;
            BigDecimal endValue = null;
            BigDecimal maxValue = null;
            BigDecimal minValue = null;
            BigDecimal sumValue = BigDecimal.ZERO;
            int count = 0;

            for (IotEnergyRealtimeDataDO data : dataList) {
                if (data.getCumulativeValue() != null) {
                    BigDecimal value = BigDecimal.valueOf(data.getCumulativeValue());

                    // 起始值（第一个数据）
                    if (startValue == null) {
                        startValue = value;
                    }

                    // 结束值（最后一个数据）
                    endValue = value;

                    // 最大值
                    if (maxValue == null || value.compareTo(maxValue) > 0) {
                        maxValue = value;
                    }

                    // 最小值
                    if (minValue == null || value.compareTo(minValue) < 0) {
                        minValue = value;
                    }

                    // 累计求和（用于计算平均值）
                    sumValue = sumValue.add(value);
                    count++;
                }
            }

            // 6. 计算能耗量（结束值 - 起始值）
            BigDecimal consumption = null;
            if (startValue != null && endValue != null) {
                consumption = endValue.subtract(startValue);
            }

            // 7. 计算平均值
            BigDecimal avgValue = null;
            if (count > 0) {
                avgValue = sumValue.divide(BigDecimal.valueOf(count), 4, BigDecimal.ROUND_HALF_UP);
            }

            // 8. 构建统计数据对象
            IotEnergyStatisticsDO statistics = IotEnergyStatisticsDO.builder()
                    .statTime(statTime)
                    .statPeriod(statPeriod)
                    .meterId(meterId)
                    .energyTypeId(meter.getEnergyTypeId())
                    .buildingId(meter.getBuildingId())
                    .areaId(meter.getAreaId())
                    .floorId(meter.getFloorId())
                    .roomId(meter.getRoomId())
                    .startValue(startValue)
                    .endValue(endValue)
                    .consumption(consumption)
                    .maxValue(maxValue)
                    .minValue(minValue)
                    .avgValue(avgValue)
                    .build();

            // 9. 保存统计数据
            saveStatistics(statistics);

            log.debug("[aggregateData][成功聚合计量点({})的{}数据，时间：{}]", meterId, statPeriod, statTime);
        } catch (Exception e) {
            log.error("[aggregateData][聚合计量点({})的{}数据失败，时间：{}]", meterId, statPeriod, statTime, e);
        }
    }

    /**
     * 获取统计开始时间
     */
    private LocalDateTime getStartTime(LocalDateTime statTime, String statPeriod) {
        switch (statPeriod) {
            case "minute":
                return statTime.withSecond(0).withNano(0);
            case "hour":
                return statTime.withMinute(0).withSecond(0).withNano(0);
            case "day":
                return statTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "month":
                return statTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                return statTime;
        }
    }

    /**
     * 获取统计结束时间
     */
    private LocalDateTime getEndTime(LocalDateTime statTime, String statPeriod) {
        LocalDateTime startTime = getStartTime(statTime, statPeriod);
        switch (statPeriod) {
            case "minute":
                return startTime.plusMinutes(1).minusSeconds(1);
            case "hour":
                return startTime.plusHours(1).minusSeconds(1);
            case "day":
                return startTime.plusDays(1).minusSeconds(1);
            case "month":
                return startTime.plusMonths(1).minusSeconds(1);
            default:
                return statTime;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteStatisticsByTimeBefore(LocalDateTime statTime) {
        return statisticsMapper.deleteByStatTimeBefore(statTime);
    }

}
