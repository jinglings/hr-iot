package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment.IotEdgeModelDeploymentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment.IotEdgeModelDeploymentRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeModelDeploymentDO;
import cn.iocoder.yudao.module.iot.service.edge.deployment.IotEdgeModelDeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 边缘模型部署 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘模型部署")
@RestController
@RequestMapping("/iot/edge-model-deployment")
@Validated
public class IotEdgeModelDeploymentController {

    @Resource
    private IotEdgeModelDeploymentService deploymentService;

    @PostMapping("/deploy")
    @Operation(summary = "部署AI模型到网关")
    @Parameters({
            @Parameter(name = "modelId", description = "模型编号", required = true),
            @Parameter(name = "gatewayId", description = "网关编号", required = true)
    })
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:create')")
    public CommonResult<Long> deployModel(
            @RequestParam("modelId") Long modelId,
            @RequestParam("gatewayId") Long gatewayId) {
        return success(deploymentService.deployModel(modelId, gatewayId));
    }

    @PostMapping("/undeploy")
    @Operation(summary = "取消部署AI模型")
    @Parameter(name = "id", description = "部署记录编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:delete')")
    public CommonResult<Boolean> undeployModel(@RequestParam("id") Long id) {
        deploymentService.undeployModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型部署记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:query')")
    public CommonResult<IotEdgeModelDeploymentRespVO> getDeployment(@RequestParam("id") Long id) {
        IotEdgeModelDeploymentDO deployment = deploymentService.getDeployment(id);
        return success(BeanUtils.toBean(deployment, IotEdgeModelDeploymentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型部署记录分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:query')")
    public CommonResult<PageResult<IotEdgeModelDeploymentRespVO>> getDeploymentPage(
            @Valid IotEdgeModelDeploymentPageReqVO pageReqVO) {
        PageResult<IotEdgeModelDeploymentDO> pageResult = deploymentService.getDeploymentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEdgeModelDeploymentRespVO.class));
    }

    @PutMapping("/start")
    @Operation(summary = "启动已部署的模型")
    @Parameter(name = "id", description = "部署记录编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:update')")
    public CommonResult<Boolean> startDeployment(@RequestParam("id") Long id) {
        deploymentService.updateDeploymentStatus(id, 1);
        return success(true);
    }

    @PutMapping("/stop")
    @Operation(summary = "停止已部署的模型")
    @Parameter(name = "id", description = "部署记录编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-model-deployment:update')")
    public CommonResult<Boolean> stopDeployment(@RequestParam("id") Long id) {
        deploymentService.updateDeploymentStatus(id, 0);
        return success(true);
    }

}
