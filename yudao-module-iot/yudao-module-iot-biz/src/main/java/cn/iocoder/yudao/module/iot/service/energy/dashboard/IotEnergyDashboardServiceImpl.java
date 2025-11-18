package cn.iocoder.yudao.module.iot.service.energy.dashboard;

import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyRankingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyTrendRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import cn.iocoder.yudao.module.iot.service.energy.energytype.IotEnergyTypeService;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IoT 能源数据可视化 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyDashboardServiceImpl implements IotEnergyDashboardService {

    @Resource
    private IotEnergyStatisticsService statisticsService;

    @Resource
    private IotEnergyMeterService meterService;

    @Resource
    private IotEnergyTypeService energyTypeService;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Override
    public IotEnergyOverviewRespVO getEnergyOverview() {
        try {
            // 今日时间范围
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

            // 昨日时间范围
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime yesterdayStart = LocalDateTime.of(yesterday, LocalTime.MIN);
            LocalDateTime yesterdayEnd = LocalDateTime.of(yesterday, LocalTime.MAX);

            // 去年同日时间范围
            LocalDate lastYearToday = today.minusYears(1);
            LocalDateTime lastYearStart = LocalDateTime.of(lastYearToday, LocalTime.MIN);
            LocalDateTime lastYearEnd = LocalDateTime.of(lastYearToday, LocalTime.MAX);

            // 本月时间范围
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDateTime thisMonthStart = LocalDateTime.of(monthStart, LocalTime.MIN);
            LocalDateTime thisMonthEnd = LocalDateTime.now();

            // 上月时间范围
            LocalDate lastMonthStartDate = monthStart.minusMonths(1);
            LocalDate lastMonthEndDate = monthStart.minusDays(1);
            LocalDateTime lastMonthStart = LocalDateTime.of(lastMonthStartDate, LocalTime.MIN);
            LocalDateTime lastMonthEnd = LocalDateTime.of(lastMonthEndDate, LocalTime.MAX);

            // 去年同月时间范围
            LocalDate lastYearMonthStartDate = monthStart.minusYears(1);
            LocalDate lastYearMonthEndDate = lastYearMonthStartDate.plusMonths(1).minusDays(1);
            LocalDateTime lastYearMonthStart = LocalDateTime.of(lastYearMonthStartDate, LocalTime.MIN);
            LocalDateTime lastYearMonthEnd = LocalDateTime.of(lastYearMonthEndDate, LocalTime.MAX);

            // 获取今日能耗
            List<IotEnergyStatisticsDO> todayStats = statisticsService.getStatisticsByTimeRange(
                    todayStart, todayEnd, "day", null, null);
            BigDecimal todayConsumption = calculateTotalConsumption(todayStats);
            Map<String, BigDecimal> todayConsumptionByType = calculateConsumptionByType(todayStats);

            // 获取昨日能耗
            List<IotEnergyStatisticsDO> yesterdayStats = statisticsService.getStatisticsByTimeRange(
                    yesterdayStart, yesterdayEnd, "day", null, null);
            BigDecimal yesterdayConsumption = calculateTotalConsumption(yesterdayStats);

            // 获取去年同日能耗
            List<IotEnergyStatisticsDO> lastYearStats = statisticsService.getStatisticsByTimeRange(
                    lastYearStart, lastYearEnd, "day", null, null);
            BigDecimal lastYearConsumption = calculateTotalConsumption(lastYearStats);

            // 获取本月能耗
            List<IotEnergyStatisticsDO> thisMonthStats = statisticsService.getStatisticsByTimeRange(
                    thisMonthStart, thisMonthEnd, "day", null, null);
            BigDecimal monthConsumption = calculateTotalConsumption(thisMonthStats);
            Map<String, BigDecimal> monthConsumptionByType = calculateConsumptionByType(thisMonthStats);

            // 获取上月能耗
            List<IotEnergyStatisticsDO> lastMonthStats = statisticsService.getStatisticsByTimeRange(
                    lastMonthStart, lastMonthEnd, "day", null, null);
            BigDecimal lastMonthConsumption = calculateTotalConsumption(lastMonthStats);

            // 获取去年同月能耗
            List<IotEnergyStatisticsDO> lastYearMonthStats = statisticsService.getStatisticsByTimeRange(
                    lastYearMonthStart, lastYearMonthEnd, "day", null, null);
            BigDecimal lastYearMonthConsumption = calculateTotalConsumption(lastYearMonthStats);

            // 计算增长率
            BigDecimal dayYoyRate = calculateGrowthRate(todayConsumption, lastYearConsumption);
            BigDecimal dayMomRate = calculateGrowthRate(todayConsumption, yesterdayConsumption);
            BigDecimal monthYoyRate = calculateGrowthRate(monthConsumption, lastYearMonthConsumption);
            BigDecimal monthMomRate = calculateGrowthRate(monthConsumption, lastMonthConsumption);

            // 获取实时总功率（从最新的统计数据中获取）
            BigDecimal realtimePower = calculateRealtimePower();

            // 获取设备统计信息
            Integer totalDeviceCount = meterService.getTotalMeterCount();
            Integer onlineDeviceCount = meterService.getOnlineMeterCount();

            // TODO: 获取告警数量（待告警模块实现后集成）
            Integer alertCount = 0;

            return IotEnergyOverviewRespVO.builder()
                    .todayConsumption(todayConsumption)
                    .todayConsumptionByType(todayConsumptionByType)
                    .monthConsumption(monthConsumption)
                    .monthConsumptionByType(monthConsumptionByType)
                    .realtimePower(realtimePower)
                    .dayYoyRate(dayYoyRate)
                    .dayMomRate(dayMomRate)
                    .monthYoyRate(monthYoyRate)
                    .monthMomRate(monthMomRate)
                    .onlineDeviceCount(onlineDeviceCount)
                    .totalDeviceCount(totalDeviceCount)
                    .alertCount(alertCount)
                    .build();

        } catch (Exception e) {
            log.error("[getEnergyOverview][获取能耗总览失败]", e);
            // 返回空数据
            return IotEnergyOverviewRespVO.builder()
                    .todayConsumption(BigDecimal.ZERO)
                    .todayConsumptionByType(new HashMap<>())
                    .monthConsumption(BigDecimal.ZERO)
                    .monthConsumptionByType(new HashMap<>())
                    .realtimePower(BigDecimal.ZERO)
                    .dayYoyRate(BigDecimal.ZERO)
                    .dayMomRate(BigDecimal.ZERO)
                    .monthYoyRate(BigDecimal.ZERO)
                    .monthMomRate(BigDecimal.ZERO)
                    .onlineDeviceCount(0)
                    .totalDeviceCount(0)
                    .alertCount(0)
                    .build();
        }
    }

    @Override
    public List<IotEnergyRankingRespVO> getEnergyRanking(String objectType, LocalDateTime startTime,
                                                          LocalDateTime endTime, Integer topN) {
        try {
            List<IotEnergyStatisticsDO> stats = statisticsService.getStatisticsByTimeRange(
                    startTime, endTime, "day", null, null);

            // 按对象类型分组统计
            Map<Long, BigDecimal> consumptionMap = new HashMap<>();
            Map<Long, String> nameMap = new HashMap<>();

            for (IotEnergyStatisticsDO stat : stats) {
                Long objectId = null;
                String objectName = null;

                // 根据对象类型获取对应的ID和名称
                if ("building".equals(objectType)) {
                    objectId = stat.getBuildingId();
                    if (objectId != null) {
                        objectName = buildingService.getBuilding(objectId).getName();
                    }
                }
                // TODO: 支持其他对象类型（area/floor/room）

                if (objectId != null) {
                    consumptionMap.merge(objectId, stat.getConsumption(), BigDecimal::add);
                    nameMap.put(objectId, objectName);
                }
            }

            // 计算总能耗
            BigDecimal totalConsumption = consumptionMap.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 转换为排名列表
            List<IotEnergyRankingRespVO> rankings = new ArrayList<>();
            for (Map.Entry<Long, BigDecimal> entry : consumptionMap.entrySet()) {
                BigDecimal consumption = entry.getValue();
                BigDecimal percentage = totalConsumption.compareTo(BigDecimal.ZERO) > 0
                        ? consumption.divide(totalConsumption, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;

                rankings.add(IotEnergyRankingRespVO.builder()
                        .objectId(entry.getKey())
                        .objectName(nameMap.get(entry.getKey()))
                        .objectType(objectType)
                        .consumption(consumption)
                        .percentage(percentage)
                        .build());
            }

            // 按能耗降序排序
            rankings.sort((a, b) -> b.getConsumption().compareTo(a.getConsumption()));

            // 设置排名
            for (int i = 0; i < rankings.size(); i++) {
                rankings.get(i).setRank(i + 1);
            }

            // 取前N名
            if (topN != null && topN > 0 && rankings.size() > topN) {
                return rankings.subList(0, topN);
            }

            return rankings;

        } catch (Exception e) {
            log.error("[getEnergyRanking][获取能耗排名失败]", e);
            return new ArrayList<>();
        }
    }

    @Override
    public IotEnergyTrendRespVO getEnergyTrend(String period, Long buildingId, Long energyTypeId) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime;
            String statPeriod;
            DateTimeFormatter formatter;

            // 根据周期确定时间范围和统计粒度
            switch (period) {
                case "24h":
                    startTime = endTime.minusHours(24);
                    statPeriod = "hour";
                    formatter = DateTimeFormatter.ofPattern("HH:00");
                    break;
                case "7d":
                    startTime = endTime.minusDays(7);
                    statPeriod = "day";
                    formatter = DateTimeFormatter.ofPattern("MM-dd");
                    break;
                case "30d":
                    startTime = endTime.minusDays(30);
                    statPeriod = "day";
                    formatter = DateTimeFormatter.ofPattern("MM-dd");
                    break;
                case "12m":
                    startTime = endTime.minusMonths(12);
                    statPeriod = "month";
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                    break;
                default:
                    startTime = endTime.minusDays(7);
                    statPeriod = "day";
                    formatter = DateTimeFormatter.ofPattern("MM-dd");
            }

            // 查询统计数据
            List<IotEnergyStatisticsDO> stats = statisticsService.getStatisticsByTimeRange(
                    startTime, endTime, statPeriod, buildingId, energyTypeId);

            // 按时间分组
            Map<String, List<IotEnergyStatisticsDO>> groupedByTime = stats.stream()
                    .collect(Collectors.groupingBy(stat -> stat.getStatTime().format(formatter)));

            // 生成时间标签列表和数据列表
            List<String> timeLabels = new ArrayList<>();
            List<BigDecimal> consumptions = new ArrayList<>();
            List<BigDecimal> powers = new ArrayList<>();

            // 按时间排序
            List<String> sortedTimes = new ArrayList<>(groupedByTime.keySet());
            sortedTimes.sort(String::compareTo);

            for (String time : sortedTimes) {
                List<IotEnergyStatisticsDO> timeStats = groupedByTime.get(time);
                BigDecimal consumption = timeStats.stream()
                        .map(IotEnergyStatisticsDO::getConsumption)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgPower = timeStats.stream()
                        .map(IotEnergyStatisticsDO::getAvgValue)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                timeLabels.add(time);
                consumptions.add(consumption);
                powers.add(avgPower);
            }

            return IotEnergyTrendRespVO.builder()
                    .timeLabels(timeLabels)
                    .consumptions(consumptions)
                    .powers(powers)
                    .build();

        } catch (Exception e) {
            log.error("[getEnergyTrend][获取能耗趋势失败]", e);
            return IotEnergyTrendRespVO.builder()
                    .timeLabels(new ArrayList<>())
                    .consumptions(new ArrayList<>())
                    .powers(new ArrayList<>())
                    .build();
        }
    }

    @Override
    public Map<String, Object> getEnergyBreakdown(LocalDateTime startTime, LocalDateTime endTime, Long buildingId) {
        try {
            List<IotEnergyStatisticsDO> stats = statisticsService.getStatisticsByTimeRange(
                    startTime, endTime, "day", buildingId, null);

            // 按能源类型分组统计
            Map<Long, BigDecimal> consumptionByTypeId = stats.stream()
                    .filter(stat -> stat.getEnergyTypeId() != null)
                    .collect(Collectors.groupingBy(
                            IotEnergyStatisticsDO::getEnergyTypeId,
                            Collectors.reducing(BigDecimal.ZERO,
                                    IotEnergyStatisticsDO::getConsumption,
                                    BigDecimal::add)
                    ));

            // 转换为名称映射
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> breakdown = new ArrayList<>();
            BigDecimal totalConsumption = BigDecimal.ZERO;

            for (Map.Entry<Long, BigDecimal> entry : consumptionByTypeId.entrySet()) {
                String typeName = energyTypeService.getEnergyType(entry.getKey()).getName();
                BigDecimal consumption = entry.getValue();
                totalConsumption = totalConsumption.add(consumption);

                Map<String, Object> item = new HashMap<>();
                item.put("energyType", typeName);
                item.put("consumption", consumption);
                breakdown.add(item);
            }

            // 计算百分比
            for (Map<String, Object> item : breakdown) {
                BigDecimal consumption = (BigDecimal) item.get("consumption");
                BigDecimal percentage = totalConsumption.compareTo(BigDecimal.ZERO) > 0
                        ? consumption.divide(totalConsumption, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                item.put("percentage", percentage);
            }

            // 按能耗降序排序
            breakdown.sort((a, b) -> {
                BigDecimal aValue = (BigDecimal) a.get("consumption");
                BigDecimal bValue = (BigDecimal) b.get("consumption");
                return bValue.compareTo(aValue);
            });

            result.put("breakdown", breakdown);
            result.put("totalConsumption", totalConsumption);

            return result;

        } catch (Exception e) {
            log.error("[getEnergyBreakdown][获取分项能耗失败]", e);
            Map<String, Object> result = new HashMap<>();
            result.put("breakdown", new ArrayList<>());
            result.put("totalConsumption", BigDecimal.ZERO);
            return result;
        }
    }

    /**
     * 计算总能耗
     */
    private BigDecimal calculateTotalConsumption(List<IotEnergyStatisticsDO> stats) {
        return stats.stream()
                .map(IotEnergyStatisticsDO::getConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 按能源类型统计能耗
     */
    private Map<String, BigDecimal> calculateConsumptionByType(List<IotEnergyStatisticsDO> stats) {
        Map<Long, BigDecimal> consumptionByTypeId = stats.stream()
                .filter(stat -> stat.getEnergyTypeId() != null)
                .collect(Collectors.groupingBy(
                        IotEnergyStatisticsDO::getEnergyTypeId,
                        Collectors.reducing(BigDecimal.ZERO,
                                IotEnergyStatisticsDO::getConsumption,
                                BigDecimal::add)
                ));

        // 转换为名称映射
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : consumptionByTypeId.entrySet()) {
            try {
                String typeName = energyTypeService.getEnergyType(entry.getKey()).getName();
                result.put(typeName, entry.getValue());
            } catch (Exception e) {
                log.warn("[calculateConsumptionByType][获取能源类型名称失败] energyTypeId={}", entry.getKey());
            }
        }
        return result;
    }

    /**
     * 计算增长率（百分比）
     */
    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算实时总功率
     */
    private BigDecimal calculateRealtimePower() {
        // 从最近的统计数据中获取平均功率
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourAgo = now.minusHours(1);

        List<IotEnergyStatisticsDO> recentStats = statisticsService.getStatisticsByTimeRange(
                hourAgo, now, "minute", null, null);

        return recentStats.stream()
                .map(IotEnergyStatisticsDO::getAvgValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
