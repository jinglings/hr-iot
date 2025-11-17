package cn.iocoder.yudao.module.iot.service.energy.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportGenerateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportRecordDO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * IoT 能源报表 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyReportService {

    /**
     * 生成报表
     *
     * @param generateReqVO 生成请求
     * @return 报表记录ID
     */
    Long generateReport(@Valid IotEnergyReportGenerateReqVO generateReqVO);

    /**
     * 导出报表到Excel
     *
     * @param recordId 报表记录ID
     * @param response HTTP响应
     */
    void exportReportToExcel(Long recordId, HttpServletResponse response);

    /**
     * 导出报表到PDF
     *
     * @param recordId 报表记录ID
     * @param response HTTP响应
     */
    void exportReportToPdf(Long recordId, HttpServletResponse response);

    /**
     * 获得报表记录
     *
     * @param id 记录ID
     * @return 报表记录
     */
    IotEnergyReportRecordDO getReportRecord(Long id);

    /**
     * 获得报表记录分页
     *
     * @param pageReqVO 分页查询
     * @return 报表记录分页
     */
    PageResult<IotEnergyReportRecordDO> getReportRecordPage(IotEnergyReportRecordPageReqVO pageReqVO);

    /**
     * 删除报表记录
     *
     * @param id 记录ID
     */
    void deleteReportRecord(Long id);

}
