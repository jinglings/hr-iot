package cn.iocoder.yudao.module.iot.controller.admin.edge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log.IotEdgeRuleLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log.IotEdgeRuleLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleLogDO;
import cn.iocoder.yudao.module.iot.service.edge.log.IotEdgeRuleLogService;
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
 * IoT 边缘规则执行日志 Controller
 *
 * @author AI Assistant
 */
@Tag(name = "管理后台 - IoT 边缘规则执行日志")
@RestController
@RequestMapping("/iot/edge-rule-log")
@Validated
public class IotEdgeRuleLogController {

    @Resource
    private IotEdgeRuleLogService ruleLogService;

    @GetMapping("/get")
    @Operation(summary = "获得规则执行日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:query')")
    public CommonResult<IotEdgeRuleLogRespVO> getRuleLog(@RequestParam("id") Long id) {
        IotEdgeRuleLogDO log = ruleLogService.getRuleLog(id);
        return success(BeanUtils.toBean(log, IotEdgeRuleLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得规则执行日志分页")
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:query')")
    public CommonResult<PageResult<IotEdgeRuleLogRespVO>> getRuleLogPage(
            @Valid IotEdgeRuleLogPageReqVO pageReqVO) {
        PageResult<IotEdgeRuleLogDO> pageResult = ruleLogService.getRuleLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEdgeRuleLogRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除规则执行日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:delete')")
    public CommonResult<Boolean> deleteRuleLog(@RequestParam("id") Long id) {
        ruleLogService.deleteRuleLog(id);
        return success(true);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空规则执行日志")
    @Parameter(name = "ruleId", description = "规则编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:edge-rule:delete')")
    public CommonResult<Boolean> clearRuleLogs(@RequestParam("ruleId") Long ruleId) {
        ruleLogService.clearRuleLogs(ruleId);
        return success(true);
    }

}
