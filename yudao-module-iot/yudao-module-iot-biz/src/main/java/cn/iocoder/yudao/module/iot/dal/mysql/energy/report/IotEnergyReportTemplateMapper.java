package cn.iocoder.yudao.module.iot.dal.mysql.energy.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.template.IotEnergyReportTemplatePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 能源报表模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyReportTemplateMapper extends BaseMapperX<IotEnergyReportTemplateDO> {

    default PageResult<IotEnergyReportTemplateDO> selectPage(IotEnergyReportTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyReportTemplateDO>()
                .likeIfPresent(IotEnergyReportTemplateDO::getName, reqVO.getName())
                .eqIfPresent(IotEnergyReportTemplateDO::getReportType, reqVO.getReportType())
                .eqIfPresent(IotEnergyReportTemplateDO::getStatus, reqVO.getStatus())
                .orderByDesc(IotEnergyReportTemplateDO::getId));
    }

    default List<IotEnergyReportTemplateDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotEnergyReportTemplateDO>()
                .eq(IotEnergyReportTemplateDO::getStatus, status));
    }

}
