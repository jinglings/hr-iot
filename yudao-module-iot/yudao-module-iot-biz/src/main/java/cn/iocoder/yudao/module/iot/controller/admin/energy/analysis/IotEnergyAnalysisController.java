package cn.iocoder.yudao.module.iot.controller.admin.energy.analysis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyCarbonEmissionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyComparisonAnalysisRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.analysis.vo.IotEnergyEfficiencyRespVO;
import cn.iocoder.yudao.module.iot.service.energy.analysis.IotEnergyAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 能源分析
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 能源分析")
@RestController
@RequestMapping("/iot/energy/analysis")
@Validated
public class IotEnergyAnalysisController {

    @Resource
    private IotEnergyAnalysisService analysisService;

    @GetMapping("/comparison")
    @Operation(summary = "获取同比环比分析")
    @PreAuthorize("@ss.hasPermission('iot:energy:analysis:query')")
    public CommonResult<IotEnergyComparisonAnalysisRespVO> getComparisonAnalysis(
            @Parameter(description = "周期类型", required = true, example = "day")
            @RequestParam("periodType") String periodType,
            @Parameter(description = "当前周期开始时间", required = true)
            @RequestParam("currentStartTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime currentStartTime,
            @Parameter(description = "当前周期结束时间", required = true)
            @RequestParam("currentEndTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime currentEndTime,
            @Parameter(description = "建筑ID", example = "1")
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @Parameter(description = "能源类型ID", example = "1")
            @RequestParam(value = "energyTypeId", required = false) Long energyTypeId) {

        IotEnergyComparisonAnalysisRespVO result = analysisService.getComparisonAnalysis(
                periodType, currentStartTime, currentEndTime, buildingId, energyTypeId);
        return success(result);
    }

    @GetMapping("/efficiency")
    @Operation(summary = "获取能效指标评估")
    @PreAuthorize("@ss.hasPermission('iot:energy:analysis:query')")
    public CommonResult<IotEnergyEfficiencyRespVO> getEfficiencyAnalysis(
            @Parameter(description = "统计开始时间", required = true)
            @RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "统计结束时间", required = true)
            @RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "建筑ID", example = "1")
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @Parameter(description = "建筑面积（平方米）", example = "5000.00")
            @RequestParam(value = "buildingArea", required = false) BigDecimal buildingArea,
            @Parameter(description = "人数", example = "200")
            @RequestParam(value = "peopleCount", required = false) Integer peopleCount,
            @Parameter(description = "产值（万元）", example = "1000.00")
            @RequestParam(value = "outputValue", required = false) BigDecimal outputValue) {

        IotEnergyEfficiencyRespVO result = analysisService.getEfficiencyAnalysis(
                startTime, endTime, buildingId, buildingArea, peopleCount, outputValue);
        return success(result);
    }

    @GetMapping("/carbon-emission")
    @Operation(summary = "获取碳排放计算")
    @PreAuthorize("@ss.hasPermission('iot:energy:analysis:query')")
    public CommonResult<IotEnergyCarbonEmissionRespVO> getCarbonEmissionAnalysis(
            @Parameter(description = "统计开始时间", required = true)
            @RequestParam("startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "统计结束时间", required = true)
            @RequestParam("endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "建筑ID", example = "1")
            @RequestParam(value = "buildingId", required = false) Long buildingId) {

        IotEnergyCarbonEmissionRespVO result = analysisService.getCarbonEmissionAnalysis(
                startTime, endTime, buildingId);
        return success(result);
    }

}
