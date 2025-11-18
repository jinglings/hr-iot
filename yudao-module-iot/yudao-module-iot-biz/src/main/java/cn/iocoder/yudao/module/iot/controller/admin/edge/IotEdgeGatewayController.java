package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeGatewayConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.service.edge.gateway.IotEdgeGatewayService;
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
 * IoT 边缘网关 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘网关")
@RestController
@RequestMapping("/iot/edge-gateway")
@Validated
public class IotEdgeGatewayController {

    @Resource
    private IotEdgeGatewayService edgeGatewayService;

    @PostMapping("/create")
    @Operation(summary = "创建边缘网关")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:create')")
    public CommonResult<Long> createGateway(@Valid @RequestBody IotEdgeGatewayCreateReqVO createReqVO) {
        return success(edgeGatewayService.createGateway(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新边缘网关")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:update')")
    public CommonResult<Boolean> updateGateway(@Valid @RequestBody IotEdgeGatewayUpdateReqVO updateReqVO) {
        edgeGatewayService.updateGateway(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除边缘网关")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:delete')")
    public CommonResult<Boolean> deleteGateway(@RequestParam("id") Long id) {
        edgeGatewayService.deleteGateway(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得边缘网关")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<IotEdgeGatewayRespVO> getGateway(@RequestParam("id") Long id) {
        IotEdgeGatewayDO gateway = edgeGatewayService.getGateway(id);
        return success(IotEdgeGatewayConvert.INSTANCE.convert(gateway));
    }

    @GetMapping("/page")
    @Operation(summary = "获得边缘网关分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<PageResult<IotEdgeGatewayRespVO>> getGatewayPage(@Valid IotEdgeGatewayPageReqVO pageReqVO) {
        PageResult<IotEdgeGatewayDO> pageResult = edgeGatewayService.getGatewayPage(pageReqVO);
        return success(IotEdgeGatewayConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/enable")
    @Operation(summary = "启用边缘网关")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:update')")
    public CommonResult<Boolean> enableGateway(@RequestParam("id") Long id) {
        edgeGatewayService.updateGatewayStatus(id, 1);
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "禁用边缘网关")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:update')")
    public CommonResult<Boolean> disableGateway(@RequestParam("id") Long id) {
        edgeGatewayService.updateGatewayStatus(id, 0);
        return success(true);
    }

    @GetMapping("/get-by-key")
    @Operation(summary = "通过网关标识获取网关信息")
    @Parameter(name = "gatewayKey", description = "网关标识", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-gateway:query')")
    public CommonResult<IotEdgeGatewayRespVO> getGatewayByKey(@RequestParam("gatewayKey") String gatewayKey) {
        IotEdgeGatewayDO gateway = edgeGatewayService.getGatewayByKey(gatewayKey);
        return success(IotEdgeGatewayConvert.INSTANCE.convert(gateway));
    }

}
