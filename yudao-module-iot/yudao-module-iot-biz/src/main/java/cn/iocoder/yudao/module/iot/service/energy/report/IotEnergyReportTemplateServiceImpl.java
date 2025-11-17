package cn.iocoder.yudao.module.iot.service.energy.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplatePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplateSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportTemplateDO;
import cn.iocoder.yudao.module.iot.dal.mysql.energy.report.IotEnergyReportTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ENERGY_REPORT_TEMPLATE_NOT_EXISTS;

/**
 * IoT 能源报表模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyReportTemplateServiceImpl implements IotEnergyReportTemplateService {

    @Resource
    private IotEnergyReportTemplateMapper reportTemplateMapper;

    @Override
    public Long createReportTemplate(IotEnergyReportTemplateSaveReqVO createReqVO) {
        // 插入
        IotEnergyReportTemplateDO template = BeanUtils.toBean(createReqVO, IotEnergyReportTemplateDO.class);
        reportTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateReportTemplate(IotEnergyReportTemplateSaveReqVO updateReqVO) {
        // 校验存在
        validateReportTemplateExists(updateReqVO.getId());
        // 更新
        IotEnergyReportTemplateDO updateObj = BeanUtils.toBean(updateReqVO, IotEnergyReportTemplateDO.class);
        reportTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteReportTemplate(Long id) {
        // 校验存在
        validateReportTemplateExists(id);
        // 删除
        reportTemplateMapper.deleteById(id);
    }

    private void validateReportTemplateExists(Long id) {
        if (reportTemplateMapper.selectById(id) == null) {
            throw exception(ENERGY_REPORT_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public IotEnergyReportTemplateDO getReportTemplate(Long id) {
        return reportTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<IotEnergyReportTemplateDO> getReportTemplatePage(IotEnergyReportTemplatePageReqVO pageReqVO) {
        return reportTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotEnergyReportTemplateDO> getEnabledReportTemplateList() {
        return reportTemplateMapper.selectListByStatus(1);
    }

}
