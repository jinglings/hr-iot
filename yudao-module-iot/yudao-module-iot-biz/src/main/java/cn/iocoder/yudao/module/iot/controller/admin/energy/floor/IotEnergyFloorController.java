package cn.iocoder.yudao.module.iot.controller.admin.energy.floor;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.floor.vo.IotEnergyFloorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.floor.IotEnergyFloorService;
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
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 能源楼层")
@RestController
@RequestMapping("/iot/energy/floor")
@Validated
public class IotEnergyFloorController {

    @Resource
    private IotEnergyFloorService floorService;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @PostMapping("/create")
    @Operation(summary = "创建能源楼层")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:create')")
    public CommonResult<Long> createFloor(@Valid @RequestBody IotEnergyFloorSaveReqVO createReqVO) {
        return success(floorService.createFloor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源楼层")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:update')")
    public CommonResult<Boolean> updateFloor(@Valid @RequestBody IotEnergyFloorSaveReqVO updateReqVO) {
        floorService.updateFloor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源楼层")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:delete')")
    public CommonResult<Boolean> deleteFloor(@RequestParam("id") Long id) {
        floorService.deleteFloor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源楼层")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:query')")
    public CommonResult<IotEnergyFloorRespVO> getFloor(@RequestParam("id") Long id) {
        IotEnergyFloorDO floor = floorService.getFloor(id);
        return success(BeanUtils.toBean(floor, IotEnergyFloorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源楼层分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:query')")
    public CommonResult<PageResult<IotEnergyFloorRespVO>> getFloorPage(@Valid IotEnergyFloorPageReqVO pageReqVO) {
        PageResult<IotEnergyFloorDO> pageResult = floorService.getFloorPage(pageReqVO);
        PageResult<IotEnergyFloorRespVO> respPageResult = BeanUtils.toBean(pageResult, IotEnergyFloorRespVO.class);

        // 批量获取建筑信息
        List<Long> buildingIds = convertList(respPageResult.getList(), IotEnergyFloorRespVO::getBuildingId);
        Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Map.of() :
                buildingService.getBuildingList(buildingIds).stream()
                        .collect(Collectors.toMap(IotEnergyBuildingDO::getId, IotEnergyBuildingDO::getBuildingName));

        // 批量获取区域信息
        List<Long> areaIds = convertList(respPageResult.getList(), IotEnergyFloorRespVO::getAreaId);
        Map<Long, String> areaNameMap = areaIds.isEmpty() ? Map.of() :
                areaService.getAreaList(areaIds).stream()
                        .collect(Collectors.toMap(IotEnergyAreaDO::getId, IotEnergyAreaDO::getAreaName));

        // 填充建筑名称和区域名称
        respPageResult.getList().forEach(floor -> {
            if (floor.getBuildingId() != null) {
                floor.setBuildingName(buildingNameMap.get(floor.getBuildingId()));
            }
            if (floor.getAreaId() != null) {
                floor.setAreaName(areaNameMap.get(floor.getAreaId()));
            }
        });

        return success(respPageResult);
    }

    @GetMapping("/list-by-building-id")
    @Operation(summary = "获得指定建筑的能源楼层列表")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:query')")
    public CommonResult<List<IotEnergyFloorRespVO>> getFloorListByBuilding(@RequestParam("buildingId") Long buildingId) {
        List<IotEnergyFloorDO> list = floorService.getFloorListByBuildingId(buildingId);
        return success(BeanUtils.toBean(list, IotEnergyFloorRespVO.class));
    }

    @GetMapping("/list-by-area-id")
    @Operation(summary = "获得指定区域的能源楼层列表")
    @Parameter(name = "areaId", description = "区域ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:query')")
    public CommonResult<List<IotEnergyFloorRespVO>> getFloorListByArea(@RequestParam("areaId") Long areaId) {
        List<IotEnergyFloorDO> list = floorService.getFloorListByAreaId(areaId);
        return success(BeanUtils.toBean(list, IotEnergyFloorRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源楼层列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-floor:query')")
    public CommonResult<List<IotEnergyFloorRespVO>> getSimpleFloorList() {
        List<IotEnergyFloorDO> list = floorService.getFloorListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, floor ->
                new IotEnergyFloorRespVO().setId(floor.getId()).setFloorName(floor.getFloorName())));
    }

}
