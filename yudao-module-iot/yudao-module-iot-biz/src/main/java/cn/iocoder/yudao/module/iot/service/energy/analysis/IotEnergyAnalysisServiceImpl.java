package cn.iocoder.yudao.module.iot.service.energy.analysis;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyCarbonEmissionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyComparisonAnalysisRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyEfficiencyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import cn.iocoder.yudao.module.iot.service.energy.energytype.IotEnergyTypeService;
import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 能源分析 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyAnalysisServiceImpl implements IotEnergyAnalysisService {

    @Resource
    private IotEnergyStatisticsService statisticsService;

    @Resource
    private IotEnergyTypeService energyTypeService;

    // 碳排放系数配置（kgCO₂/kWh）
    private static final Map<String, BigDecimal> CARBON_EMISSION_FACTORS = new HashMap<>();

    static {
        // 电力碳排放系数（中国电网平均值）
        CARBON_EMISSION_FACTORS.put("电", new BigDecimal("0.785"));
        // 天然气碳排放系数
        CARBON_EMISSION_FACTORS.put("天然气", new BigDecimal("0.215"));
        // 煤炭碳排放系数
        CARBON_EMISSION_FACTORS.put("煤", new BigDecimal("0.329"));
        // 汽油碳排放系数
        CARBON_EMISSION_FACTORS.put("汽油", new BigDecimal("2.296"));
        // 柴油碳排放系数
        CARBON_EMISSION_FACTORS.put("柴油", new BigDecimal("2.661"));
        // 其他能源默认系数
        CARBON_EMISSION_FACTORS.put("default", new BigDecimal("0.5"));
    }

    @Override
    public IotEnergyComparisonAnalysisRespVO getComparisonAnalysis(String periodType,
                                                                     LocalDateTime currentStartTime,
                                                                     LocalDateTime currentEndTime,
                                                                     Long buildingId,
                                                                     Long energyTypeId) {
        try {
            // 1. 计算同比和环比时间范围
            LocalDateTime[] yoyPeriod = calculateYoyPeriod(periodType, currentStartTime, currentEndTime);
            LocalDateTime[] momPeriod = calculateMomPeriod(periodType, currentStartTime, currentEndTime);

            // 2. 获取统计周期
            String statPeriod = getStatPeriodByPeriodType(periodType);

            // 3. 查询当前周期数据
            List<IotEnergyStatisticsDO> currentStats = statisticsService.getStatisticsByTimeRange(
                    currentStartTime, currentEndTime, statPeriod, buildingId, energyTypeId);
            BigDecimal currentConsumption = calculateTotalConsumption(currentStats);

            // 4. 查询同比周期数据
            List<IotEnergyStatisticsDO> yoyStats = statisticsService.getStatisticsByTimeRange(
                    yoyPeriod[0], yoyPeriod[1], statPeriod, buildingId, energyTypeId);
            BigDecimal yoyConsumption = calculateTotalConsumption(yoyStats);

            // 5. 查询环比周期数据
            List<IotEnergyStatisticsDO> momStats = statisticsService.getStatisticsByTimeRange(
                    momPeriod[0], momPeriod[1], statPeriod, buildingId, energyTypeId);
            BigDecimal momConsumption = calculateTotalConsumption(momStats);

            // 6. 计算同比增长率和增长量
            BigDecimal yoyGrowthRate = calculateGrowthRate(currentConsumption, yoyConsumption);
            BigDecimal yoyGrowthAmount = currentConsumption.subtract(yoyConsumption);

            // 7. 计算环比增长率和增长量
            BigDecimal momGrowthRate = calculateGrowthRate(currentConsumption, momConsumption);
            BigDecimal momGrowthAmount = currentConsumption.subtract(momConsumption);

            return IotEnergyComparisonAnalysisRespVO.builder()
                    .currentConsumption(currentConsumption)
                    .yoyConsumption(yoyConsumption)
                    .momConsumption(momConsumption)
                    .yoyGrowthRate(yoyGrowthRate)
                    .momGrowthRate(momGrowthRate)
                    .yoyGrowthAmount(yoyGrowthAmount)
                    .momGrowthAmount(momGrowthAmount)
                    .currentStartTime(currentStartTime)
                    .currentEndTime(currentEndTime)
                    .yoyStartTime(yoyPeriod[0])
                    .yoyEndTime(yoyPeriod[1])
                    .momStartTime(momPeriod[0])
                    .momEndTime(momPeriod[1])
                    .periodType(periodType)
                    .build();

        } catch (Exception e) {
            log.error("[getComparisonAnalysis][同比环比分析失败]", e);
            throw new RuntimeException("同比环比分析失败", e);
        }
    }

    @Override
    public IotEnergyEfficiencyRespVO getEfficiencyAnalysis(LocalDateTime startTime,
                                                            LocalDateTime endTime,
                                                            Long buildingId,
                                                            BigDecimal buildingArea,
                                                            Integer peopleCount,
                                                            BigDecimal outputValue) {
        try {
            // 1. 查询总能耗
            List<IotEnergyStatisticsDO> stats = statisticsService.getStatisticsByTimeRange(
                    startTime, endTime, "day", buildingId, null);
            BigDecimal totalConsumption = calculateTotalConsumption(stats);

            // 2. 计算单位面积能耗
            BigDecimal consumptionPerArea = BigDecimal.ZERO;
            if (buildingArea != null && buildingArea.compareTo(BigDecimal.ZERO) > 0) {
                consumptionPerArea = totalConsumption.divide(buildingArea, 2, RoundingMode.HALF_UP);
            }

            // 3. 计算人均能耗
            BigDecimal consumptionPerPerson = BigDecimal.ZERO;
            if (peopleCount != null && peopleCount > 0) {
                consumptionPerPerson = totalConsumption.divide(new BigDecimal(peopleCount), 2, RoundingMode.HALF_UP);
            }

            // 4. 计算万元产值能耗
            BigDecimal consumptionPerOutputValue = BigDecimal.ZERO;
            if (outputValue != null && outputValue.compareTo(BigDecimal.ZERO) > 0) {
                consumptionPerOutputValue = totalConsumption.divide(outputValue, 2, RoundingMode.HALF_UP);
            }

            // 5. 评估能效等级和评分
            String efficiencyLevel = evaluateEfficiencyLevel(consumptionPerArea);
            BigDecimal efficiencyScore = calculateEfficiencyScore(consumptionPerArea);

            // 6. 计算节能潜力
            BigDecimal savingPotential = calculateSavingPotential(consumptionPerArea);

            // 7. 生成能效建议
            String suggestions = generateEfficiencySuggestions(efficiencyLevel, consumptionPerArea);

            return IotEnergyEfficiencyRespVO.builder()
                    .totalConsumption(totalConsumption)
                    .buildingArea(buildingArea)
                    .consumptionPerArea(consumptionPerArea)
                    .peopleCount(peopleCount)
                    .consumptionPerPerson(consumptionPerPerson)
                    .outputValue(outputValue)
                    .consumptionPerOutputValue(consumptionPerOutputValue)
                    .efficiencyLevel(efficiencyLevel)
                    .efficiencyScore(efficiencyScore)
                    .savingPotential(savingPotential)
                    .suggestions(suggestions)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (Exception e) {
            log.error("[getEfficiencyAnalysis][能效分析失败]", e);
            throw new RuntimeException("能效分析失败", e);
        }
    }

    @Override
    public IotEnergyCarbonEmissionRespVO getCarbonEmissionAnalysis(LocalDateTime startTime,
                                                                     LocalDateTime endTime,
                                                                     Long buildingId) {
        try {
            // 1. 查询能耗统计数据
            List<IotEnergyStatisticsDO> stats = statisticsService.getStatisticsByTimeRange(
                    startTime, endTime, "day", buildingId, null);

            // 2. 按能源类型分组统计
            Map<Long, BigDecimal> consumptionByTypeId = new HashMap<>();
            for (IotEnergyStatisticsDO stat : stats) {
                if (stat.getEnergyTypeId() != null) {
                    consumptionByTypeId.merge(stat.getEnergyTypeId(), stat.getConsumption(), BigDecimal::add);
                }
            }

            // 3. 计算各能源类型碳排放
            Map<String, IotEnergyCarbonEmissionRespVO.CarbonEmissionDetail> emissionDetails = new HashMap<>();
            BigDecimal totalConsumption = BigDecimal.ZERO;
            BigDecimal totalCarbonEmission = BigDecimal.ZERO;

            for (Map.Entry<Long, BigDecimal> entry : consumptionByTypeId.entrySet()) {
                String energyTypeName = energyTypeService.getEnergyType(entry.getKey()).getName();
                BigDecimal consumption = entry.getValue();
                BigDecimal emissionFactor = getCarbonEmissionFactor(energyTypeName);
                BigDecimal carbonEmission = consumption.multiply(emissionFactor);

                totalConsumption = totalConsumption.add(consumption);
                totalCarbonEmission = totalCarbonEmission.add(carbonEmission);

                emissionDetails.put(energyTypeName, IotEnergyCarbonEmissionRespVO.CarbonEmissionDetail.builder()
                        .energyTypeName(energyTypeName)
                        .consumption(consumption)
                        .emissionFactor(emissionFactor)
                        .carbonEmission(carbonEmission)
                        .build());
            }

            // 4. 计算占比
            for (IotEnergyCarbonEmissionRespVO.CarbonEmissionDetail detail : emissionDetails.values()) {
                BigDecimal percentage = totalCarbonEmission.compareTo(BigDecimal.ZERO) > 0
                        ? detail.getCarbonEmission().divide(totalCarbonEmission, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                detail.setPercentage(percentage);
            }

            // 5. 计算等效指标
            // 一棵树一年吸收约18.3kgCO₂
            Integer equivalentTrees = totalCarbonEmission.divide(new BigDecimal("18.3"), 0, RoundingMode.HALF_UP).intValue();

            // 汽车每公里排放约0.182kgCO₂
            BigDecimal equivalentCarMileage = totalCarbonEmission.divide(new BigDecimal("0.182"), 1, RoundingMode.HALF_UP);

            // 6. 计算碳排放强度
            BigDecimal emissionIntensity = totalConsumption.compareTo(BigDecimal.ZERO) > 0
                    ? totalCarbonEmission.divide(totalConsumption, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return IotEnergyCarbonEmissionRespVO.builder()
                    .totalConsumption(totalConsumption)
                    .totalCarbonEmission(totalCarbonEmission)
                    .emissionDetails(emissionDetails)
                    .equivalentTrees(equivalentTrees)
                    .equivalentCarMileage(equivalentCarMileage)
                    .emissionIntensity(emissionIntensity)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

        } catch (Exception e) {
            log.error("[getCarbonEmissionAnalysis][碳排放分析失败]", e);
            throw new RuntimeException("碳排放分析失败", e);
        }
    }

    /**
     * 计算同比周期
     */
    private LocalDateTime[] calculateYoyPeriod(String periodType, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime yoyStartTime;
        LocalDateTime yoyEndTime;

        switch (periodType) {
            case "day":
                yoyStartTime = startTime.minusYears(1);
                yoyEndTime = endTime.minusYears(1);
                break;
            case "week":
                yoyStartTime = startTime.minusYears(1);
                yoyEndTime = endTime.minusYears(1);
                break;
            case "month":
                yoyStartTime = startTime.minusYears(1);
                yoyEndTime = endTime.minusYears(1);
                break;
            case "year":
                yoyStartTime = startTime.minusYears(1);
                yoyEndTime = endTime.minusYears(1);
                break;
            default:
                yoyStartTime = startTime.minusYears(1);
                yoyEndTime = endTime.minusYears(1);
        }

        return new LocalDateTime[]{yoyStartTime, yoyEndTime};
    }

    /**
     * 计算环比周期
     */
    private LocalDateTime[] calculateMomPeriod(String periodType, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime momStartTime;
        LocalDateTime momEndTime;

        switch (periodType) {
            case "day":
                momStartTime = startTime.minusDays(1);
                momEndTime = endTime.minusDays(1);
                break;
            case "week":
                momStartTime = startTime.minusWeeks(1);
                momEndTime = endTime.minusWeeks(1);
                break;
            case "month":
                momStartTime = startTime.minusMonths(1);
                momEndTime = endTime.minusMonths(1);
                break;
            case "year":
                momStartTime = startTime.minusYears(1);
                momEndTime = endTime.minusYears(1);
                break;
            default:
                momStartTime = startTime.minusDays(1);
                momEndTime = endTime.minusDays(1);
        }

        return new LocalDateTime[]{momStartTime, momEndTime};
    }

    /**
     * 根据周期类型获取统计粒度
     */
    private String getStatPeriodByPeriodType(String periodType) {
        switch (periodType) {
            case "day":
                return "hour";
            case "week":
            case "month":
                return "day";
            case "year":
                return "month";
            default:
                return "day";
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
     * 计算增长率
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
     * 评估能效等级
     * 基于单位面积能耗（kWh/m²）
     */
    private String evaluateEfficiencyLevel(BigDecimal consumptionPerArea) {
        if (consumptionPerArea.compareTo(new BigDecimal("0.15")) <= 0) {
            return "A"; // 优秀
        } else if (consumptionPerArea.compareTo(new BigDecimal("0.25")) <= 0) {
            return "B"; // 良好
        } else if (consumptionPerArea.compareTo(new BigDecimal("0.35")) <= 0) {
            return "C"; // 一般
        } else if (consumptionPerArea.compareTo(new BigDecimal("0.45")) <= 0) {
            return "D"; // 较差
        } else {
            return "E"; // 差
        }
    }

    /**
     * 计算能效评分（0-100）
     */
    private BigDecimal calculateEfficiencyScore(BigDecimal consumptionPerArea) {
        // 基准值：0.15 kWh/m² 为满分
        BigDecimal baseValue = new BigDecimal("0.15");
        if (consumptionPerArea.compareTo(baseValue) <= 0) {
            return new BigDecimal("100");
        }

        // 每增加0.05，扣10分
        BigDecimal excess = consumptionPerArea.subtract(baseValue);
        BigDecimal deduction = excess.divide(new BigDecimal("0.05"), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("10"));

        BigDecimal score = new BigDecimal("100").subtract(deduction);
        return score.max(BigDecimal.ZERO);
    }

    /**
     * 计算节能潜力
     */
    private BigDecimal calculateSavingPotential(BigDecimal consumptionPerArea) {
        // 行业标杆值：0.15 kWh/m²
        BigDecimal benchmarkValue = new BigDecimal("0.15");
        if (consumptionPerArea.compareTo(benchmarkValue) <= 0) {
            return BigDecimal.ZERO;
        }

        return consumptionPerArea.subtract(benchmarkValue)
                .divide(consumptionPerArea, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 生成能效建议
     */
    private String generateEfficiencySuggestions(String efficiencyLevel, BigDecimal consumptionPerArea) {
        StringBuilder suggestions = new StringBuilder();

        switch (efficiencyLevel) {
            case "A":
                suggestions.append("能效表现优秀，请继续保持。");
                break;
            case "B":
                suggestions.append("能效表现良好。建议优化照明系统，采用LED灯具。");
                break;
            case "C":
                suggestions.append("能效一般。建议：1. 优化空调使用策略；2. 加强能源监控；3. 定期设备维护。");
                break;
            case "D":
                suggestions.append("能效较差。建议：1. 更换老旧设备；2. 安装节能设备；3. 加强员工节能意识培训；4. 实施能源管理制度。");
                break;
            case "E":
                suggestions.append("能效较差，急需改善。建议：1. 全面能源审计；2. 制定节能改造计划；3. 引入能源管理系统；4. 考虑可再生能源。");
                break;
        }

        return suggestions.toString();
    }

    /**
     * 获取碳排放系数
     */
    private BigDecimal getCarbonEmissionFactor(String energyTypeName) {
        return CARBON_EMISSION_FACTORS.getOrDefault(energyTypeName,
                CARBON_EMISSION_FACTORS.get("default"));
    }

}
