package cn.iocoder.yudao.module.iot.controller.admin.energy.building;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.vo.IotEnergySpaceTreeNodeVO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 能源建筑")
@RestController
@RequestMapping("/iot/energy/building")
@Validated
public class IotEnergyBuildingController {

    @Resource
    private IotEnergyBuildingService buildingService;

    @Resource
    private IotEnergyAreaService areaService;

    @Resource
    private IotEnergyFloorService floorService;

    @Resource
    private IotEnergyRoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "创建能源建筑")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:create')")
    public CommonResult<Long> createBuilding(@Valid @RequestBody IotEnergyBuildingSaveReqVO createReqVO) {
        return success(buildingService.createBuilding(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源建筑")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:update')")
    public CommonResult<Boolean> updateBuilding(@Valid @RequestBody IotEnergyBuildingSaveReqVO updateReqVO) {
        buildingService.updateBuilding(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源建筑")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-building:delete')")
    public CommonResult<Boolean> deleteBuilding(@RequestParam("id") Long id) {
        buildingService.deleteBuilding(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源建筑")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:query')")
    public CommonResult<IotEnergyBuildingRespVO> getBuilding(@RequestParam("id") Long id) {
        IotEnergyBuildingDO building = buildingService.getBuilding(id);
        return success(BeanUtils.toBean(building, IotEnergyBuildingRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源建筑分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:query')")
    public CommonResult<PageResult<IotEnergyBuildingRespVO>> getBuildingPage(@Valid IotEnergyBuildingPageReqVO pageReqVO) {
        PageResult<IotEnergyBuildingDO> pageResult = buildingService.getBuildingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyBuildingRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源建筑列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:query')")
    public CommonResult<List<IotEnergyBuildingRespVO>> getSimpleBuildingList() {
        List<IotEnergyBuildingDO> list = buildingService.getBuildingListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, building ->
                new IotEnergyBuildingRespVO().setId(building.getId()).setBuildingName(building.getBuildingName())));
    }

    @GetMapping("/space-tree")
    @Operation(summary = "获得能源空间树形结构")
    @PreAuthorize("@ss.hasPermission('iot:energy-building:query')")
    public CommonResult<List<IotEnergySpaceTreeNodeVO>> getSpaceTree() {
        // 1. 获取所有启用的建筑
        List<IotEnergyBuildingDO> buildings = buildingService.getBuildingListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 2. 构建树形结构
        List<IotEnergySpaceTreeNodeVO> tree = new ArrayList<>();
        for (IotEnergyBuildingDO building : buildings) {
            IotEnergySpaceTreeNodeVO buildingNode = new IotEnergySpaceTreeNodeVO();
            buildingNode.setId(building.getId());
            buildingNode.setName(building.getBuildingName());
            buildingNode.setCode(building.getBuildingCode());
            buildingNode.setType("building");
            buildingNode.setParentId(0L);
            buildingNode.setSort(building.getSort());
            buildingNode.setStatus(building.getStatus());

            // 2.1 获取该建筑下的所有区域
            List<IotEnergyAreaDO> areas = areaService.getAreaListByBuildingId(building.getId());
            List<IotEnergySpaceTreeNodeVO> areaNodes = new ArrayList<>();
            for (IotEnergyAreaDO area : areas) {
                if (area.getStatus() != CommonStatusEnum.ENABLE.getStatus()) continue;

                IotEnergySpaceTreeNodeVO areaNode = new IotEnergySpaceTreeNodeVO();
                areaNode.setId(area.getId());
                areaNode.setName(area.getAreaName());
                areaNode.setCode(area.getAreaCode());
                areaNode.setType("area");
                areaNode.setParentId(building.getId());
                areaNode.setSort(area.getSort());
                areaNode.setStatus(area.getStatus());

                // 2.1.1 获取该区域下的所有楼层
                List<IotEnergyFloorDO> floors = floorService.getFloorListByAreaId(area.getId());
                List<IotEnergySpaceTreeNodeVO> floorNodes = buildFloorNodes(floors, area.getId());
                areaNode.setChildren(floorNodes);

                areaNodes.add(areaNode);
            }

            // 2.2 获取该建筑下没有指定区域的楼层
            List<IotEnergyFloorDO> buildingFloors = floorService.getFloorListByBuildingId(building.getId())
                    .stream()
                    .filter(floor -> floor.getAreaId() == null && floor.getStatus() == CommonStatusEnum.ENABLE.getStatus())
                    .collect(Collectors.toList());
            List<IotEnergySpaceTreeNodeVO> floorNodes = buildFloorNodes(buildingFloors, building.getId());

            // 2.3 合并区域节点和楼层节点
            List<IotEnergySpaceTreeNodeVO> children = new ArrayList<>();
            children.addAll(areaNodes);
            children.addAll(floorNodes);
            buildingNode.setChildren(children);

            tree.add(buildingNode);
        }

        return success(tree);
    }

    /**
     * 构建楼层节点
     */
    private List<IotEnergySpaceTreeNodeVO> buildFloorNodes(List<IotEnergyFloorDO> floors, Long parentId) {
        List<IotEnergySpaceTreeNodeVO> floorNodes = new ArrayList<>();
        for (IotEnergyFloorDO floor : floors) {
            if (floor.getStatus() != CommonStatusEnum.ENABLE.getStatus()) continue;

            IotEnergySpaceTreeNodeVO floorNode = new IotEnergySpaceTreeNodeVO();
            floorNode.setId(floor.getId());
            floorNode.setName(floor.getFloorName());
            floorNode.setCode(floor.getFloorCode());
            floorNode.setType("floor");
            floorNode.setParentId(parentId);
            floorNode.setSort(floor.getSort());
            floorNode.setStatus(floor.getStatus());

            // 获取该楼层下的所有房间
            List<IotEnergyRoomDO> rooms = roomService.getRoomListByFloorId(floor.getId());
            List<IotEnergySpaceTreeNodeVO> roomNodes = new ArrayList<>();
            for (IotEnergyRoomDO room : rooms) {
                if (room.getStatus() != CommonStatusEnum.ENABLE.getStatus()) continue;

                IotEnergySpaceTreeNodeVO roomNode = new IotEnergySpaceTreeNodeVO();
                roomNode.setId(room.getId());
                roomNode.setName(room.getRoomName());
                roomNode.setCode(room.getRoomCode());
                roomNode.setType("room");
                roomNode.setParentId(floor.getId());
                roomNode.setSort(room.getSort());
                roomNode.setStatus(room.getStatus());
                roomNode.setChildren(new ArrayList<>());

                roomNodes.add(roomNode);
            }
            floorNode.setChildren(roomNodes);

            floorNodes.add(floorNode);
        }
        return floorNodes;
    }

}
