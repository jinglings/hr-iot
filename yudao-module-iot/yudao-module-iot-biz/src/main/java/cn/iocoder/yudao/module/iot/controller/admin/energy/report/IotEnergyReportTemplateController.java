package cn.iocoder.yudao.module.iot.controller.admin.energy.report;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplateRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportTemplateDO;
import cn.iocoder.yudao.module.iot.service.energy.report.IotEnergyReportTemplateService;
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

/**
 * 管理后台 - IoT 能源报表模板
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 能源报表模板")
@RestController
@RequestMapping("/iot/energy/report/template")
@Validated
public class IotEnergyReportTemplateController {

    @Resource
    private IotEnergyReportTemplateService reportTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建报表模板")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:create')")
    public CommonResult<Long> createReportTemplate(@Valid @RequestBody IotEnergyReportTemplateSaveReqVO createReqVO) {
        return success(reportTemplateService.createReportTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报表模板")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:update')")
    public CommonResult<Boolean> updateReportTemplate(@Valid @RequestBody IotEnergyReportTemplateSaveReqVO updateReqVO) {
        reportTemplateService.updateReportTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报表模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:delete')")
    public CommonResult<Boolean> deleteReportTemplate(@RequestParam("id") Long id) {
        reportTemplateService.deleteReportTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报表模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:query')")
    public CommonResult<IotEnergyReportTemplateRespVO> getReportTemplate(@RequestParam("id") Long id) {
        IotEnergyReportTemplateDO template = reportTemplateService.getReportTemplate(id);
        return success(BeanUtils.toBean(template, IotEnergyReportTemplateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报表模板分页")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:query')")
    public CommonResult<PageResult<IotEnergyReportTemplateRespVO>> getReportTemplatePage(@Valid IotEnergyReportTemplatePageReqVO pageReqVO) {
        PageResult<IotEnergyReportTemplateDO> pageResult = reportTemplateService.getReportTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyReportTemplateRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用的报表模板列表")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:template:query')")
    public CommonResult<List<IotEnergyReportTemplateRespVO>> getEnabledReportTemplateList() {
        List<IotEnergyReportTemplateDO> list = reportTemplateService.getEnabledReportTemplateList();
        return success(BeanUtils.toBean(list, IotEnergyReportTemplateRespVO.class));
    }

}
