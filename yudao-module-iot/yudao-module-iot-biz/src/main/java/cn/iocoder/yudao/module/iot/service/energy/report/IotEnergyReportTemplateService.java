package cn.iocoder.yudao.module.iot.service.energy.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportTemplateDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IoT 能源报表模板 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyReportTemplateService {

    /**
     * 创建报表模板
     */
    Long createReportTemplate(@Valid IotEnergyReportTemplateSaveReqVO createReqVO);

    /**
     * 更新报表模板
     */
    void updateReportTemplate(@Valid IotEnergyReportTemplateSaveReqVO updateReqVO);

    /**
     * 删除报表模板
     */
    void deleteReportTemplate(Long id);

    /**
     * 获得报表模板
     */
    IotEnergyReportTemplateDO getReportTemplate(Long id);

    /**
     * 获得报表模板分页
     */
    PageResult<IotEnergyReportTemplateDO> getReportTemplatePage(IotEnergyReportTemplatePageReqVO pageReqVO);

    /**
     * 获得启用的报表模板列表
     */
    List<IotEnergyReportTemplateDO> getEnabledReportTemplateList();

}
