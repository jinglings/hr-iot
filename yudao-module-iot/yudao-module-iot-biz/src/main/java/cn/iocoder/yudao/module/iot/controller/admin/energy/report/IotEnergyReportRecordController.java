package cn.iocoder.yudao.module.iot.controller.admin.energy.report;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportGenerateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportRecordDO;
import cn.iocoder.yudao.module.iot.service.energy.report.IotEnergyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 能源报表记录
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 能源报表记录")
@RestController
@RequestMapping("/iot/energy/report/record")
@Validated
public class IotEnergyReportRecordController {

    @Resource
    private IotEnergyReportService reportService;

    @PostMapping("/generate")
    @Operation(summary = "生成报表")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:generate')")
    public CommonResult<Long> generateReport(@Valid @RequestBody IotEnergyReportGenerateReqVO generateReqVO) {
        return success(reportService.generateReport(generateReqVO));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "导出报表到Excel")
    @Parameter(name = "recordId", description = "报表记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy:report:export')")
    public void exportReportToExcel(@RequestParam("recordId") Long recordId, HttpServletResponse response) {
        reportService.exportReportToExcel(recordId, response);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "导出报表到PDF")
    @Parameter(name = "recordId", description = "报表记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy:report:export')")
    public void exportReportToPdf(@RequestParam("recordId") Long recordId, HttpServletResponse response) {
        reportService.exportReportToPdf(recordId, response);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报表记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:query')")
    public CommonResult<IotEnergyReportRecordRespVO> getReportRecord(@RequestParam("id") Long id) {
        IotEnergyReportRecordDO record = reportService.getReportRecord(id);
        return success(BeanUtils.toBean(record, IotEnergyReportRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报表记录分页")
    @PreAuthorize("@ss.hasPermission('iot:energy:report:query')")
    public CommonResult<PageResult<IotEnergyReportRecordRespVO>> getReportRecordPage(@Valid IotEnergyReportRecordPageReqVO pageReqVO) {
        PageResult<IotEnergyReportRecordDO> pageResult = reportService.getReportRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotEnergyReportRecordRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报表记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:energy:report:delete')")
    public CommonResult<Boolean> deleteReportRecord(@RequestParam("id") Long id) {
        reportService.deleteReportRecord(id);
        return success(true);
    }

}
