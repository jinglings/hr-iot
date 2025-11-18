package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status.IotEdgeGatewayStatusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status.IotEdgeGatewayStatusRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayStatusDO;
import cn.iocoder.yudao.module.iot.service.edge.status.IotEdgeGatewayStatusService;
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
 * IoT 边缘网关状态 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘网关状态")
@RestController
@RequestMapping("/iot/edge-gateway-status")
@Validated
public class IotEdgeGatewayStatusController {

    @Resource
    private IotEdgeGatewayStatusService gatewayStatusService;

    @GetMapping("/get")
    @Operation(summary = "获得网关状态")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<IotEdgeGatewayStatusRespVO> getGatewayStatus(@RequestParam("id") Long id) {
        IotEdgeGatewayStatusDO status = gatewayStatusService.getGatewayStatus(id);
        return success(BeanUtils.toBean(status, IotEdgeGatewayStatusRespVO.class));
    }

    @GetMapping("/get-by-gateway")
    @Operation(summary = "根据网关ID获得状态")
    @Parameter(name = "gatewayId", description = "网关编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<IotEdgeGatewayStatusRespVO> getGatewayStatusByGatewayId(
            @RequestParam("gatewayId") Long gatewayId) {
        IotEdgeGatewayStatusDO status = gatewayStatusService.getGatewayStatusByGatewayId(gatewayId);
        return success(BeanUtils.toBean(status, IotEdgeGatewayStatusRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得网关状态分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<PageResult<IotEdgeGatewayStatusRespVO>> getGatewayStatusPage(
            @Valid IotEdgeGatewayStatusPageReqVO pageReqVO) {
        PageResult<IotEdgeGatewayStatusDO> pageResult = gatewayStatusService.getGatewayStatusPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEdgeGatewayStatusRespVO.class));
    }

}
