package cn.iocoder.yudao.module.iot.controller.admin.energy.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo.IotEnergyStatisticsPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo.IotEnergyStatisticsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Tag(name = "管理后台 - IoT 能源统计数据")
@RestController
@RequestMapping("/iot/energy/statistics")
@Validated
public class IotEnergyStatisticsController {

    @Resource
    private IotEnergyStatisticsService statisticsService;

    @GetMapping("/page")
    @Operation(summary = "获得能源统计数据分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-statistics:query')")
    public CommonResult<PageResult<IotEnergyStatisticsRespVO>> getStatisticsPage(@Valid IotEnergyStatisticsPageReqVO reqVO) {
        PageResult<IotEnergyStatisticsDO> pageResult = statisticsService.getStatisticsPage(reqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyStatisticsRespVO.class));
    }

    @GetMapping("/list/meter")
    @Operation(summary = "按计量点和周期查询统计数据")
    @Parameter(name = "meterId", description = "计量点ID", required = true, example = "1")
    @Parameter(name = "statPeriod", description = "统计周期", required = true, example = "hour")
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-statistics:query')")
    public CommonResult<List<IotEnergyStatisticsRespVO>> getStatisticsListByMeter(
            @RequestParam("meterId") Long meterId,
            @RequestParam("statPeriod") String statPeriod,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        List<IotEnergyStatisticsDO> list = statisticsService.getStatisticsListByMeterIdAndPeriod(
                meterId, statPeriod, startTime, endTime);
        return success(BeanUtils.toBean(list, IotEnergyStatisticsRespVO.class));
    }

    @GetMapping("/list/building")
    @Operation(summary = "按建筑和周期查询统计数据")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @Parameter(name = "statPeriod", description = "统计周期", required = true, example = "hour")
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-statistics:query')")
    public CommonResult<List<IotEnergyStatisticsRespVO>> getStatisticsListByBuilding(
            @RequestParam("buildingId") Long buildingId,
            @RequestParam("statPeriod") String statPeriod,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        List<IotEnergyStatisticsDO> list = statisticsService.getStatisticsListByBuildingIdAndPeriod(
                buildingId, statPeriod, startTime, endTime);
        return success(BeanUtils.toBean(list, IotEnergyStatisticsRespVO.class));
    }

    @GetMapping("/list/energy-type")
    @Operation(summary = "按能源类型和周期查询统计数据")
    @Parameter(name = "energyTypeId", description = "能源类型ID", required = true, example = "1")
    @Parameter(name = "statPeriod", description = "统计周期", required = true, example = "hour")
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-statistics:query')")
    public CommonResult<List<IotEnergyStatisticsRespVO>> getStatisticsListByEnergyType(
            @RequestParam("energyTypeId") Long energyTypeId,
            @RequestParam("statPeriod") String statPeriod,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        List<IotEnergyStatisticsDO> list = statisticsService.getStatisticsListByEnergyTypeIdAndPeriod(
                energyTypeId, statPeriod, startTime, endTime);
        return success(BeanUtils.toBean(list, IotEnergyStatisticsRespVO.class));
    }

}
