package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeAiModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import cn.iocoder.yudao.module.iot.service.edge.aimodel.IotEdgeAiModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 边缘AI模型 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘AI模型")
@RestController
@RequestMapping("/iot/edge-ai-model")
@Validated
public class IotEdgeAiModelController {

    @Resource
    private IotEdgeAiModelService edgeAiModelService;

    @PostMapping("/create")
    @Operation(summary = "创建AI模型")
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:create')")
    public CommonResult<Long> createModel(@Valid @RequestBody IotEdgeAiModelCreateReqVO createReqVO) {
        return success(edgeAiModelService.createModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新AI模型")
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody IotEdgeAiModelUpdateReqVO updateReqVO) {
        edgeAiModelService.updateModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除AI模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") Long id) {
        edgeAiModelService.deleteModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得AI模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:query')")
    public CommonResult<IotEdgeAiModelRespVO> getModel(@RequestParam("id") Long id) {
        IotEdgeAiModelDO model = edgeAiModelService.getModel(id);
        return success(IotEdgeAiModelConvert.INSTANCE.convert(model));
    }

    @GetMapping("/page")
    @Operation(summary = "获得AI模型分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:query')")
    public CommonResult<PageResult<IotEdgeAiModelRespVO>> getModelPage(@Valid IotEdgeAiModelPageReqVO pageReqVO) {
        PageResult<IotEdgeAiModelDO> pageResult = edgeAiModelService.getModelPage(pageReqVO);
        return success(IotEdgeAiModelConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/enable")
    @Operation(summary = "启用AI模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:update')")
    public CommonResult<Boolean> enableModel(@RequestParam("id") Long id) {
        IotEdgeAiModelDO model = edgeAiModelService.getModel(id);
        if (model != null) {
            IotEdgeAiModelUpdateReqVO updateReqVO = new IotEdgeAiModelUpdateReqVO();
            updateReqVO.setId(id);
            updateReqVO.setStatus(1);
            edgeAiModelService.updateModel(updateReqVO);
        }
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "禁用AI模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-ai-model:update')")
    public CommonResult<Boolean> disableModel(@RequestParam("id") Long id) {
        IotEdgeAiModelDO model = edgeAiModelService.getModel(id);
        if (model != null) {
            IotEdgeAiModelUpdateReqVO updateReqVO = new IotEdgeAiModelUpdateReqVO();
            updateReqVO.setId(id);
            updateReqVO.setStatus(0);
            edgeAiModelService.updateModel(updateReqVO);
        }
        return success(true);
    }

}
