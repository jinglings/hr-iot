package cn.iocoder.yudao.module.iot.controller.admin.energy.data;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo.IotEnergyDataQueryReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.data.vo.IotEnergyDataRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.service.energy.data.IotEnergyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 能源数据")
@RestController
@RequestMapping("/iot/energy/data")
@Validated
public class IotEnergyDataController {

    @Resource
    private IotEnergyDataService energyDataService;

    @GetMapping("/realtime/page")
    @Operation(summary = "获得能源实时数据分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<PageResult<IotEnergyDataRespVO>> getRealtimeDataPage(@Valid IotEnergyDataQueryReqVO reqVO) {
        PageResult<IotEnergyRealtimeDataDO> pageResult = energyDataService.getRealtimeDataPage(reqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyDataRespVO.class));
    }

    @GetMapping("/realtime/latest")
    @Operation(summary = "获得计量点最新数据")
    @Parameter(name = "meterId", description = "计量点ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<IotEnergyDataRespVO> getLatestData(@RequestParam("meterId") Long meterId) {
        IotEnergyRealtimeDataDO data = energyDataService.getLatestDataByMeterId(meterId);
        return success(BeanUtils.toBean(data, IotEnergyDataRespVO.class));
    }

    @GetMapping("/realtime/list")
    @Operation(summary = "获得计量点时间范围内的数据")
    @Parameter(name = "meterId", description = "计量点ID", required = true, example = "1")
    @Parameter(name = "startTime", description = "开始时间", required = true, example = "1640995200000")
    @Parameter(name = "endTime", description = "结束时间", required = true, example = "1641081600000")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<List<IotEnergyDataRespVO>> getDataList(@RequestParam("meterId") Long meterId,
                                                                @RequestParam("startTime") Long startTime,
                                                                @RequestParam("endTime") Long endTime) {
        List<IotEnergyRealtimeDataDO> list = energyDataService.getDataListByMeterIdAndTimeRange(meterId, startTime, endTime);
        return success(BeanUtils.toBean(list, IotEnergyDataRespVO.class));
    }

    @GetMapping("/aggregate")
    @Operation(summary = "按时间范围统计能耗数据")
    @Parameter(name = "meterId", description = "计量点ID", required = true, example = "1")
    @Parameter(name = "startTime", description = "开始时间", required = true, example = "1640995200000")
    @Parameter(name = "endTime", description = "结束时间", required = true, example = "1641081600000")
    @Parameter(name = "interval", description = "时间间隔(1m/1h/1d)", required = true, example = "1h")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<List<Map<String, Object>>> getAggregateData(@RequestParam("meterId") Long meterId,
                                                                      @RequestParam("startTime") Long startTime,
                                                                      @RequestParam("endTime") Long endTime,
                                                                      @RequestParam("interval") String interval) {
        List<Map<String, Object>> data = energyDataService.getAggregateDataByTimeRange(meterId, startTime, endTime, interval);
        return success(data);
    }

    @GetMapping("/stats/building")
    @Operation(summary = "按建筑统计能耗数据")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @Parameter(name = "startTime", description = "开始时间", example = "1640995200000")
    @Parameter(name = "endTime", description = "结束时间", example = "1641081600000")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<Map<String, Object>> getEnergyStatsByBuilding(@RequestParam("buildingId") Long buildingId,
                                                                        @RequestParam(value = "startTime", required = false) Long startTime,
                                                                        @RequestParam(value = "endTime", required = false) Long endTime) {
        Map<String, Object> stats = energyDataService.getEnergyStatsByBuilding(buildingId, startTime, endTime);
        return success(stats);
    }

    @GetMapping("/stats/type")
    @Operation(summary = "按能源类型统计能耗数据")
    @Parameter(name = "energyTypeId", description = "能源类型ID", required = true, example = "1")
    @Parameter(name = "startTime", description = "开始时间", example = "1640995200000")
    @Parameter(name = "endTime", description = "结束时间", example = "1641081600000")
    @PreAuthorize("@ss.hasPermission('iot:energy-data:query')")
    public CommonResult<List<Map<String, Object>>> getEnergyStatsByType(@RequestParam("energyTypeId") Long energyTypeId,
                                                                          @RequestParam(value = "startTime", required = false) Long startTime,
                                                                          @RequestParam(value = "endTime", required = false) Long endTime) {
        List<Map<String, Object>> stats = energyDataService.getEnergyStatsByType(energyTypeId, startTime, endTime);
        return success(stats);
    }

}
