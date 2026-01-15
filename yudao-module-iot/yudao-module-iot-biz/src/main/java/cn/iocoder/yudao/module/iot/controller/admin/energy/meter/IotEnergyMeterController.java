package cn.iocoder.yudao.module.iot.controller.admin.energy.meter;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.meter.vo.IotEnergyMeterSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyTypeDO;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.floor.IotEnergyFloorService;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import cn.iocoder.yudao.module.iot.service.energy.room.IotEnergyRoomService;
import cn.iocoder.yudao.module.iot.service.energy.energytype.IotEnergyTypeService;
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

@Tag(name = "管理后台 - IoT 能源计量点")
@RestController
@RequestMapping("/iot/energy/meter")
@Validated
public class IotEnergyMeterController {

    @Resource
    private IotEnergyMeterService meterService;

    @Resource
    private IotEnergyTypeService energyTypeService;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Resource
    private IotEnergyFloorService floorService;

    @Resource
    private IotEnergyRoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "创建能源计量点")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:create')")
    public CommonResult<Long> createMeter(@Valid @RequestBody IotEnergyMeterSaveReqVO createReqVO) {
        return success(meterService.createMeter(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源计量点")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:update')")
    public CommonResult<Boolean> updateMeter(@Valid @RequestBody IotEnergyMeterSaveReqVO updateReqVO) {
        meterService.updateMeter(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源计量点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:delete')")
    public CommonResult<Boolean> deleteMeter(@RequestParam("id") Long id) {
        meterService.deleteMeter(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源计量点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<IotEnergyMeterRespVO> getMeter(@RequestParam("id") Long id) {
        IotEnergyMeterDO meter = meterService.getMeter(id);
        return success(BeanUtils.toBean(meter, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源计量点分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<PageResult<IotEnergyMeterRespVO>> getMeterPage(@Valid IotEnergyMeterPageReqVO pageReqVO) {
        PageResult<IotEnergyMeterDO> pageResult = meterService.getMeterPage(pageReqVO);
        PageResult<IotEnergyMeterRespVO> respPageResult = BeanUtils.toBean(pageResult, IotEnergyMeterRespVO.class);

        // 批量获取能源类型信息
        List<Long> energyTypeIds = convertList(respPageResult.getList(), IotEnergyMeterRespVO::getEnergyTypeId);
        Map<Long, String> energyTypeNameMap = energyTypeIds.isEmpty() ? Map.of() :
                energyTypeService.getEnergyTypeList(energyTypeIds).stream()
                        .collect(Collectors.toMap(IotEnergyTypeDO::getId, IotEnergyTypeDO::getEnergyName));

        // 批量获取建筑信息
        List<Long> buildingIds = convertList(respPageResult.getList(), IotEnergyMeterRespVO::getBuildingId);
        Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Map.of() :
                buildingService.getBuildingList(buildingIds).stream()
                        .collect(Collectors.toMap(IotEnergyBuildingDO::getId, IotEnergyBuildingDO::getBuildingName));

        // 批量获取区域信息
        List<Long> areaIds = convertList(respPageResult.getList(), IotEnergyMeterRespVO::getAreaId);
        Map<Long, String> areaNameMap = areaIds.isEmpty() ? Map.of() :
                areaService.getAreaList(areaIds).stream()
                        .collect(Collectors.toMap(IotEnergyAreaDO::getId, IotEnergyAreaDO::getAreaName));

        // 批量获取楼层信息
        List<Long> floorIds = convertList(respPageResult.getList(), IotEnergyMeterRespVO::getFloorId);
        Map<Long, String> floorNameMap = floorIds.isEmpty() ? Map.of() :
                floorService.getFloorList(floorIds).stream()
                        .collect(Collectors.toMap(IotEnergyFloorDO::getId, IotEnergyFloorDO::getFloorName));

        // 批量获取房间信息
        List<Long> roomIds = convertList(respPageResult.getList(), IotEnergyMeterRespVO::getRoomId);
        Map<Long, String> roomNameMap = roomIds.isEmpty() ? Map.of() :
                roomService.getRoomList(roomIds).stream()
                        .collect(Collectors.toMap(IotEnergyRoomDO::getId, IotEnergyRoomDO::getRoomName));

        // 填充所有名称字段
        respPageResult.getList().forEach(meter -> {
            if (meter.getEnergyTypeId() != null) {
                meter.setEnergyTypeName(energyTypeNameMap.get(meter.getEnergyTypeId()));
            }
            if (meter.getBuildingId() != null) {
                meter.setBuildingName(buildingNameMap.get(meter.getBuildingId()));
            }
            if (meter.getAreaId() != null) {
                meter.setAreaName(areaNameMap.get(meter.getAreaId()));
            }
            if (meter.getFloorId() != null) {
                meter.setFloorName(floorNameMap.get(meter.getFloorId()));
            }
            if (meter.getRoomId() != null) {
                meter.setRoomName(roomNameMap.get(meter.getRoomId()));
            }
        });

        return success(respPageResult);
    }

    @GetMapping("/list-by-energy-type")
    @Operation(summary = "获得指定能源类型的计量点列表")
    @Parameter(name = "energyTypeId", description = "能源类型ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByEnergyType(@RequestParam("energyTypeId") Long energyTypeId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByEnergyTypeId(energyTypeId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-device")
    @Operation(summary = "获得指定设备的计量点列表")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByDevice(@RequestParam("deviceId") Long deviceId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByDeviceId(deviceId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-building-id")
    @Operation(summary = "获得指定建筑的计量点列表")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByBuilding(@RequestParam("buildingId") Long buildingId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByBuildingId(buildingId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-area-id")
    @Operation(summary = "获得指定区域的计量点列表")
    @Parameter(name = "areaId", description = "区域ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByArea(@RequestParam("areaId") Long areaId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByAreaId(areaId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-floor-id")
    @Operation(summary = "获得指定楼层的计量点列表")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByFloor(@RequestParam("floorId") Long floorId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByFloorId(floorId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-room-id")
    @Operation(summary = "获得指定房间的计量点列表")
    @Parameter(name = "roomId", description = "房间ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByRoom(@RequestParam("roomId") Long roomId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByRoomId(roomId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/list-by-parent")
    @Operation(summary = "获得指定父级的计量点列表")
    @Parameter(name = "parentId", description = "父级ID", required = true, example = "0")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getMeterListByParent(@RequestParam("parentId") Long parentId) {
        List<IotEnergyMeterDO> list = meterService.getMeterListByParentId(parentId);
        return success(BeanUtils.toBean(list, IotEnergyMeterRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源计量点列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-meter:query')")
    public CommonResult<List<IotEnergyMeterRespVO>> getSimpleMeterList() {
        List<IotEnergyMeterDO> list = meterService.getMeterListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, meter ->
                new IotEnergyMeterRespVO().setId(meter.getId()).setMeterName(meter.getMeterName())));
    }

}
