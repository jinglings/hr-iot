package cn.iocoder.yudao.module.iot.dal.mysql.energy.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.report.vo.record.IotEnergyReportRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.report.IotEnergyReportRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 能源报表记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyReportRecordMapper extends BaseMapperX<IotEnergyReportRecordDO> {

    default PageResult<IotEnergyReportRecordDO> selectPage(IotEnergyReportRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyReportRecordDO>()
                .likeIfPresent(IotEnergyReportRecordDO::getReportName, reqVO.getReportName())
                .eqIfPresent(IotEnergyReportRecordDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(IotEnergyReportRecordDO::getReportType, reqVO.getReportType())
                .eqIfPresent(IotEnergyReportRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEnergyReportRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotEnergyReportRecordDO::getId));
    }

    default List<IotEnergyReportRecordDO> selectListByTemplateId(Long templateId) {
        return selectList(new LambdaQueryWrapperX<IotEnergyReportRecordDO>()
                .eq(IotEnergyReportRecordDO::getTemplateId, templateId)
                .orderByDesc(IotEnergyReportRecordDO::getCreateTime));
    }

    default int deleteByCreateTimeBefore(LocalDateTime time) {
        return delete(new LambdaQueryWrapperX<IotEnergyReportRecordDO>()
                .lt(IotEnergyReportRecordDO::getCreateTime, time));
    }

}
