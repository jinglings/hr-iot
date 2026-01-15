package cn.iocoder.yudao.module.iot.controller.admin.energy.room;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.room.vo.IotEnergyRoomSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyFloorDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRoomDO;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import cn.iocoder.yudao.module.iot.service.energy.floor.IotEnergyFloorService;
import cn.iocoder.yudao.module.iot.service.energy.room.IotEnergyRoomService;
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

@Tag(name = "管理后台 - IoT 能源房间")
@RestController
@RequestMapping("/iot/energy/room")
@Validated
public class IotEnergyRoomController {

    @Resource
    private IotEnergyRoomService roomService;

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Resource
    private IotEnergyFloorService floorService;

    @PostMapping("/create")
    @Operation(summary = "创建能源房间")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:create')")
    public CommonResult<Long> createRoom(@Valid @RequestBody IotEnergyRoomSaveReqVO createReqVO) {
        return success(roomService.createRoom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源房间")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:update')")
    public CommonResult<Boolean> updateRoom(@Valid @RequestBody IotEnergyRoomSaveReqVO updateReqVO) {
        roomService.updateRoom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源房间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-room:delete')")
    public CommonResult<Boolean> deleteRoom(@RequestParam("id") Long id) {
        roomService.deleteRoom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源房间")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<IotEnergyRoomRespVO> getRoom(@RequestParam("id") Long id) {
        IotEnergyRoomDO room = roomService.getRoom(id);
        return success(BeanUtils.toBean(room, IotEnergyRoomRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源房间分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<PageResult<IotEnergyRoomRespVO>> getRoomPage(@Valid IotEnergyRoomPageReqVO pageReqVO) {
        PageResult<IotEnergyRoomDO> pageResult = roomService.getRoomPage(pageReqVO);
        PageResult<IotEnergyRoomRespVO> respPageResult = BeanUtils.toBean(pageResult, IotEnergyRoomRespVO.class);

        // 批量获取建筑信息
        List<Long> buildingIds = convertList(respPageResult.getList(), IotEnergyRoomRespVO::getBuildingId);
        Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Map.of() :
                buildingService.getBuildingList(buildingIds).stream()
                        .collect(Collectors.toMap(IotEnergyBuildingDO::getId, IotEnergyBuildingDO::getBuildingName));

        // 批量获取区域信息
        List<Long> areaIds = convertList(respPageResult.getList(), IotEnergyRoomRespVO::getAreaId);
        Map<Long, String> areaNameMap = areaIds.isEmpty() ? Map.of() :
                areaService.getAreaList(areaIds).stream()
                        .collect(Collectors.toMap(IotEnergyAreaDO::getId, IotEnergyAreaDO::getAreaName));

        // 批量获取楼层信息
        List<Long> floorIds = convertList(respPageResult.getList(), IotEnergyRoomRespVO::getFloorId);
        Map<Long, String> floorNameMap = floorIds.isEmpty() ? Map.of() :
                floorService.getFloorList(floorIds).stream()
                        .collect(Collectors.toMap(IotEnergyFloorDO::getId, IotEnergyFloorDO::getFloorName));

        // 填充建筑名称、区域名称和楼层名称
        respPageResult.getList().forEach(room -> {
            if (room.getBuildingId() != null) {
                room.setBuildingName(buildingNameMap.get(room.getBuildingId()));
            }
            if (room.getAreaId() != null) {
                room.setAreaName(areaNameMap.get(room.getAreaId()));
            }
            if (room.getFloorId() != null) {
                room.setFloorName(floorNameMap.get(room.getFloorId()));
            }
        });

        return success(respPageResult);
    }

    @GetMapping("/list-by-floor-id")
    @Operation(summary = "获得指定楼层的能源房间列表")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<List<IotEnergyRoomRespVO>> getRoomListByFloor(@RequestParam("floorId") Long floorId) {
        List<IotEnergyRoomDO> list = roomService.getRoomListByFloorId(floorId);
        return success(BeanUtils.toBean(list, IotEnergyRoomRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源房间列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-room:query')")
    public CommonResult<List<IotEnergyRoomRespVO>> getSimpleRoomList() {
        List<IotEnergyRoomDO> list = roomService.getRoomListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, room ->
                new IotEnergyRoomRespVO().setId(room.getId()).setRoomName(room.getRoomName())));
    }

}
