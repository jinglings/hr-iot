package cn.iocoder.yudao.module.iot.service.energy.analysis;

import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyCarbonEmissionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyComparisonAnalysisRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyEfficiencyRespVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 能源分析 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyAnalysisService {

    /**
     * 获取同比环比分析
     *
     * @param periodType 周期类型：day-日，week-周，month-月，year-年
     * @param currentStartTime 当前周期开始时间
     * @param currentEndTime 当前周期结束时间
     * @param buildingId 建筑ID（可选）
     * @param energyTypeId 能源类型ID（可选）
     * @return 同比环比分析结果
     */
    IotEnergyComparisonAnalysisRespVO getComparisonAnalysis(String periodType,
                                                             LocalDateTime currentStartTime,
                                                             LocalDateTime currentEndTime,
                                                             Long buildingId,
                                                             Long energyTypeId);

    /**
     * 获取能效指标评估
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param buildingId 建筑ID（可选）
     * @param buildingArea 建筑面积（平方米）
     * @param peopleCount 人数
     * @param outputValue 产值（万元）
     * @return 能效指标评估结果
     */
    IotEnergyEfficiencyRespVO getEfficiencyAnalysis(LocalDateTime startTime,
                                                     LocalDateTime endTime,
                                                     Long buildingId,
                                                     BigDecimal buildingArea,
                                                     Integer peopleCount,
                                                     BigDecimal outputValue);

    /**
     * 获取碳排放计算
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param buildingId 建筑ID（可选）
     * @return 碳排放计算结果
     */
    IotEnergyCarbonEmissionRespVO getCarbonEmissionAnalysis(LocalDateTime startTime,
                                                             LocalDateTime endTime,
                                                             Long buildingId);

}
