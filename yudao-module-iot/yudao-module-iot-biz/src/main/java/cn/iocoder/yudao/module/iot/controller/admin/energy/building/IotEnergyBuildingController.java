package cn.iocoder.yudao.module.iot.controller.admin.energy.building;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.building.vo.IotEnergyBuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 能源建筑")
@RestController
@RequestMapping("/iot/energy/building")
@Validated
public class IotEnergyBuildingController {

    @Resource
    private IotEnergyBuildingService buildingService;

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

}
