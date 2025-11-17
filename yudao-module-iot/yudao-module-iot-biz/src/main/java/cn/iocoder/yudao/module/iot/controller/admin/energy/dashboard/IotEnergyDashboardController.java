package cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyOverviewRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyRankingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.dashboard.vo.IotEnergyTrendRespVO;
import cn.iocoder.yudao.module.iot.service.energy.dashboard.IotEnergyDashboardService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 能源数据可视化
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 能源数据可视化")
@RestController
@RequestMapping("/iot/energy/dashboard")
@Validated
public class IotEnergyDashboardController {

    @Resource
    private IotEnergyDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "获取能耗总览数据")
    @PreAuthorize("@ss.hasPermission('iot:energy:dashboard:query')")
    public CommonResult<IotEnergyOverviewRespVO> getEnergyOverview() {
        IotEnergyOverviewRespVO overview = dashboardService.getEnergyOverview();
        return success(overview);
    }

    @GetMapping("/ranking")
    @Operation(summary = "获取能耗排名")
    @PreAuthorize("@ss.hasPermission('iot:energy:dashboard:query')")
    public CommonResult<List<IotEnergyRankingRespVO>> getEnergyRanking(
            @Parameter(description = "对象类型", required = true, example = "building")
            @RequestParam(value = "objectType") String objectType,
            @Parameter(description = "开始时间", required = true)
            @RequestParam(value = "startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam(value = "endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "取前N名", example = "10")
            @RequestParam(value = "topN", required = false, defaultValue = "10") Integer topN) {
        List<IotEnergyRankingRespVO> ranking = dashboardService.getEnergyRanking(objectType, startTime, endTime, topN);
        return success(ranking);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取能耗趋势数据")
    @PreAuthorize("@ss.hasPermission('iot:energy:dashboard:query')")
    public CommonResult<IotEnergyTrendRespVO> getEnergyTrend(
            @Parameter(description = "周期", required = true, example = "24h")
            @RequestParam(value = "period") String period,
            @Parameter(description = "建筑ID", example = "1")
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @Parameter(description = "能源类型ID", example = "1")
            @RequestParam(value = "energyTypeId", required = false) Long energyTypeId) {
        IotEnergyTrendRespVO trend = dashboardService.getEnergyTrend(period, buildingId, energyTypeId);
        return success(trend);
    }

    @GetMapping("/breakdown")
    @Operation(summary = "获取分项能耗数据")
    @PreAuthorize("@ss.hasPermission('iot:energy:dashboard:query')")
    public CommonResult<Map<String, Object>> getEnergyBreakdown(
            @Parameter(description = "开始时间", required = true)
            @RequestParam(value = "startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam(value = "endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "建筑ID", example = "1")
            @RequestParam(value = "buildingId", required = false) Long buildingId) {
        Map<String, Object> breakdown = dashboardService.getEnergyBreakdown(startTime, endTime, buildingId);
        return success(breakdown);
    }

}
