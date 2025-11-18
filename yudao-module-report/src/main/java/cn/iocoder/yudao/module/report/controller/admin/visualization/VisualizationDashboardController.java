package cn.iocoder.yudao.module.report.controller.admin.visualization;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.visualization.vo.*;
import cn.iocoder.yudao.module.report.convert.visualization.VisualizationDashboardConvert;
import cn.iocoder.yudao.module.report.dal.dataobject.visualization.VisualizationDashboardDO;
import cn.iocoder.yudao.module.report.service.visualization.VisualizationDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 可视化大屏 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 可视化大屏")
@RestController
@RequestMapping("/report/visualization/dashboard")
@Validated
public class VisualizationDashboardController {

    @Resource
    private VisualizationDashboardService dashboardService;

    @PostMapping("/create")
    @Operation(summary = "创建可视化大屏")
    @PreAuthorize("@ss.hasPermission('report:visualization:create')")
    public CommonResult<Long> createDashboard(@Valid @RequestBody VisualizationDashboardCreateReqVO createReqVO) {
        return success(dashboardService.createDashboard(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新可视化大屏")
    @PreAuthorize("@ss.hasPermission('report:visualization:update')")
    public CommonResult<Boolean> updateDashboard(@Valid @RequestBody VisualizationDashboardUpdateReqVO updateReqVO) {
        dashboardService.updateDashboard(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除可视化大屏")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:visualization:delete')")
    public CommonResult<Boolean> deleteDashboard(@RequestParam("id") Long id) {
        dashboardService.deleteDashboard(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得可视化大屏")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:visualization:query')")
    public CommonResult<VisualizationDashboardRespVO> getDashboard(@RequestParam("id") Long id) {
        VisualizationDashboardDO dashboard = dashboardService.getDashboard(id);
        return success(VisualizationDashboardConvert.INSTANCE.convert(dashboard));
    }

    @GetMapping("/page")
    @Operation(summary = "获得可视化大屏分页")
    @PreAuthorize("@ss.hasPermission('report:visualization:query')")
    public CommonResult<PageResult<VisualizationDashboardRespVO>> getDashboardPage(@Valid VisualizationDashboardPageReqVO pageVO) {
        PageResult<VisualizationDashboardDO> pageResult = dashboardService.getDashboardPage(
                pageVO, getLoginUserId());
        return success(VisualizationDashboardConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/public-page")
    @Operation(summary = "获得公开的可视化大屏分页")
    public CommonResult<PageResult<VisualizationDashboardRespVO>> getPublicDashboardPage(@Valid VisualizationDashboardPageReqVO pageVO) {
        PageResult<VisualizationDashboardDO> pageResult = dashboardService.getPublicDashboardPage(pageVO);
        return success(VisualizationDashboardConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/copy")
    @Operation(summary = "复制可视化大屏")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:visualization:create')")
    public CommonResult<Long> copyDashboard(@RequestParam("id") Long id) {
        return success(dashboardService.copyDashboard(id));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新可视化大屏状态")
    @PreAuthorize("@ss.hasPermission('report:visualization:update')")
    public CommonResult<Boolean> updateDashboardStatus(
            @RequestParam("id") Long id,
            @RequestParam("status") Integer status) {
        dashboardService.updateDashboardStatus(id, status);
        return success(true);
    }
}
