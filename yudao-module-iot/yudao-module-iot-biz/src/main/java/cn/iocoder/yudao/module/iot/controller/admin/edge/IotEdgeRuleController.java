package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeRuleConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;
import cn.iocoder.yudao.module.iot.service.edge.rule.IotEdgeRuleService;
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
 * IoT 边缘规则 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘规则")
@RestController
@RequestMapping("/iot/edge-rule")
@Validated
public class IotEdgeRuleController {

    @Resource
    private IotEdgeRuleService edgeRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建边缘规则")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:create')")
    public CommonResult<Long> createRule(@Valid @RequestBody IotEdgeRuleCreateReqVO createReqVO) {
        return success(edgeRuleService.createRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新边缘规则")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:update')")
    public CommonResult<Boolean> updateRule(@Valid @RequestBody IotEdgeRuleUpdateReqVO updateReqVO) {
        edgeRuleService.updateRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除边缘规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:delete')")
    public CommonResult<Boolean> deleteRule(@RequestParam("id") Long id) {
        edgeRuleService.deleteRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得边缘规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:query')")
    public CommonResult<IotEdgeRuleRespVO> getRule(@RequestParam("id") Long id) {
        IotEdgeRuleDO rule = edgeRuleService.getRule(id);
        return success(IotEdgeRuleConvert.INSTANCE.convert(rule));
    }

    @GetMapping("/page")
    @Operation(summary = "获得边缘规则分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:query')")
    public CommonResult<PageResult<IotEdgeRuleRespVO>> getRulePage(@Valid IotEdgeRulePageReqVO pageReqVO) {
        PageResult<IotEdgeRuleDO> pageResult = edgeRuleService.getRulePage(pageReqVO);
        return success(IotEdgeRuleConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署边缘规则到网关")
    @Parameter(name = "id", description = "规则编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:deploy')")
    public CommonResult<Boolean> deployRule(@RequestParam("id") Long id) {
        edgeRuleService.deployRule(id);
        return success(true);
    }

    @PostMapping("/undeploy")
    @Operation(summary = "取消部署边缘规则")
    @Parameter(name = "id", description = "规则编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:deploy')")
    public CommonResult<Boolean> undeployRule(@RequestParam("id") Long id) {
        edgeRuleService.undeployRule(id);
        return success(true);
    }

    @PutMapping("/enable")
    @Operation(summary = "启用边缘规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:update')")
    public CommonResult<Boolean> enableRule(@RequestParam("id") Long id) {
        edgeRuleService.updateRuleStatus(id, 1);
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "禁用边缘规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:update')")
    public CommonResult<Boolean> disableRule(@RequestParam("id") Long id) {
        edgeRuleService.updateRuleStatus(id, 0);
        return success(true);
    }

}
