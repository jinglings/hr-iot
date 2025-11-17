package cn.iocoder.yudao.module.iot.controller.admin.energy.energytype;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.energytype.vo.IotEnergyTypeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyTypeDO;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 能源类型")
@RestController
@RequestMapping("/iot/energy/type")
@Validated
public class IotEnergyTypeController {

    @Resource
    private IotEnergyTypeService energyTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建能源类型")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:create')")
    public CommonResult<Long> createEnergyType(@Valid @RequestBody IotEnergyTypeSaveReqVO createReqVO) {
        return success(energyTypeService.createEnergyType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新能源类型")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:update')")
    public CommonResult<Boolean> updateEnergyType(@Valid @RequestBody IotEnergyTypeSaveReqVO updateReqVO) {
        energyTypeService.updateEnergyType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除能源类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy-type:delete')")
    public CommonResult<Boolean> deleteEnergyType(@RequestParam("id") Long id) {
        energyTypeService.deleteEnergyType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得能源类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:query')")
    public CommonResult<IotEnergyTypeRespVO> getEnergyType(@RequestParam("id") Long id) {
        IotEnergyTypeDO energyType = energyTypeService.getEnergyType(id);
        return success(BeanUtils.toBean(energyType, IotEnergyTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得能源类型分页")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:query')")
    public CommonResult<PageResult<IotEnergyTypeRespVO>> getEnergyTypePage(@Valid IotEnergyTypePageReqVO pageReqVO) {
        PageResult<IotEnergyTypeDO> pageResult = energyTypeService.getEnergyTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyTypeRespVO.class));
    }

    @GetMapping("/tree")
    @Operation(summary = "获得能源类型树形结构")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:query')")
    public CommonResult<List<IotEnergyTypeRespVO>> getEnergyTypeTree() {
        List<IotEnergyTypeRespVO> tree = energyTypeService.getEnergyTypeTree();
        return success(tree);
    }

    @GetMapping("/list-by-parent")
    @Operation(summary = "获得指定父级的能源类型列表")
    @Parameter(name = "parentId", description = "父级ID", required = true, example = "0")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:query')")
    public CommonResult<List<IotEnergyTypeRespVO>> getEnergyTypeListByParent(@RequestParam("parentId") Long parentId) {
        List<IotEnergyTypeDO> list = energyTypeService.getEnergyTypeListByParentId(parentId);
        return success(BeanUtils.toBean(list, IotEnergyTypeRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用状态的能源类型列表")
    @PreAuthorize("@ss.hasPermission('iot:energy-type:query')")
    public CommonResult<List<IotEnergyTypeRespVO>> getSimpleEnergyTypeList() {
        List<IotEnergyTypeDO> list = energyTypeService.getEnergyTypeListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, energyType ->
                new IotEnergyTypeRespVO().setId(energyType.getId()).setEnergyName(energyType.getEnergyName())));
    }

}
