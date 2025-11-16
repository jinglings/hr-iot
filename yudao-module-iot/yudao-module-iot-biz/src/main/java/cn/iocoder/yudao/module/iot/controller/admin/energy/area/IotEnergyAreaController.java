package cn.iocoder.yudao.module.iot.controller.admin.energy.area;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.area.vo.IotEnergyAreaSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyAreaDO;
import cn.iocoder.yudao.module.iot.service.energy.area.IotEnergyAreaService;
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

@Tag(name = "管理后台 - IoT 能源区域")
@RestController
@RequestMapping("/iot/energy/area")
@Validated
public class IotEnergyAreaController {

    @Resource
    private IotEnergyAreaService areaService;

    @PostMapping("/create")
    @Operation(summary = "创建能源区域")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:create')")
    public CommonResult<Long> createArea(@Valid @RequestBody IotEnergyAreaSaveReqVO createReqVO) {
        return success(areaService.createArea(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源区域")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:update')")
    public CommonResult<Boolean> updateArea(@Valid @RequestBody IotEnergyAreaSaveReqVO updateReqVO) {
        areaService.updateArea(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-area:delete')")
    public CommonResult<Boolean> deleteArea(@RequestParam("id") Long id) {
        areaService.deleteArea(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源区域")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:query')")
    public CommonResult<IotEnergyAreaRespVO> getArea(@RequestParam("id") Long id) {
        IotEnergyAreaDO area = areaService.getArea(id);
        return success(BeanUtils.toBean(area, IotEnergyAreaRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源区域分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:query')")
    public CommonResult<PageResult<IotEnergyAreaRespVO>> getAreaPage(@Valid IotEnergyAreaPageReqVO pageReqVO) {
        PageResult<IotEnergyAreaDO> pageResult = areaService.getAreaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyAreaRespVO.class));
    }

    @GetMapping("/list-by-building")
    @Operation(summary = "获得指定建筑的能源区域列表")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:query')")
    public CommonResult<List<IotEnergyAreaRespVO>> getAreaListByBuilding(@RequestParam("buildingId") Long buildingId) {
        List<IotEnergyAreaDO> list = areaService.getAreaListByBuildingId(buildingId);
        return success(BeanUtils.toBean(list, IotEnergyAreaRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源区域列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-area:query')")
    public CommonResult<List<IotEnergyAreaRespVO>> getSimpleAreaList() {
        List<IotEnergyAreaDO> list = areaService.getAreaListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, area ->
                new IotEnergyAreaRespVO().setId(area.getId()).setAreaName(area.getAreaName())));
    }

}
