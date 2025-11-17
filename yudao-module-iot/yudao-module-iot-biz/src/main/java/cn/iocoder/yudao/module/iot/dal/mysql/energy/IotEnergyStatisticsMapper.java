package cn.iocoder.yudao.module.iot.dal.mysql.energy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.energy.statistics.vo.IotEnergyStatisticsPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyStatisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 能源统计数据 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotEnergyStatisticsMapper extends BaseMapperX<IotEnergyStatisticsDO> {

    default PageResult<IotEnergyStatisticsDO> selectPage(IotEnergyStatisticsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .eqIfPresent(IotEnergyStatisticsDO::getMeterId, reqVO.getMeterId())
                .eqIfPresent(IotEnergyStatisticsDO::getEnergyTypeId, reqVO.getEnergyTypeId())
                .eqIfPresent(IotEnergyStatisticsDO::getBuildingId, reqVO.getBuildingId())
                .eqIfPresent(IotEnergyStatisticsDO::getStatPeriod, reqVO.getStatPeriod())
                .betweenIfPresent(IotEnergyStatisticsDO::getStatTime, reqVO.getStatTime())
                .orderByDesc(IotEnergyStatisticsDO::getStatTime));
    }

    default List<IotEnergyStatisticsDO> selectListByMeterIdAndPeriod(Long meterId, String statPeriod,
                                                                      LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .eq(IotEnergyStatisticsDO::getMeterId, meterId)
                .eq(IotEnergyStatisticsDO::getStatPeriod, statPeriod)
                .between(IotEnergyStatisticsDO::getStatTime, startTime, endTime)
                .orderByAsc(IotEnergyStatisticsDO::getStatTime));
    }

    default List<IotEnergyStatisticsDO> selectListByBuildingIdAndPeriod(Long buildingId, String statPeriod,
                                                                         LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .eq(IotEnergyStatisticsDO::getBuildingId, buildingId)
                .eq(IotEnergyStatisticsDO::getStatPeriod, statPeriod)
                .between(IotEnergyStatisticsDO::getStatTime, startTime, endTime)
                .orderByAsc(IotEnergyStatisticsDO::getStatTime));
    }

    default List<IotEnergyStatisticsDO> selectListByEnergyTypeIdAndPeriod(Long energyTypeId, String statPeriod,
                                                                           LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .eq(IotEnergyStatisticsDO::getEnergyTypeId, energyTypeId)
                .eq(IotEnergyStatisticsDO::getStatPeriod, statPeriod)
                .between(IotEnergyStatisticsDO::getStatTime, startTime, endTime)
                .orderByAsc(IotEnergyStatisticsDO::getStatTime));
    }

    default IotEnergyStatisticsDO selectOneByMeterIdAndTime(Long meterId, String statPeriod, LocalDateTime statTime) {
        return selectOne(new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .eq(IotEnergyStatisticsDO::getMeterId, meterId)
                .eq(IotEnergyStatisticsDO::getStatPeriod, statPeriod)
                .eq(IotEnergyStatisticsDO::getStatTime, statTime));
    }

    default int deleteByStatTimeBefore(LocalDateTime statTime) {
        return delete(new LambdaQueryWrapperX<IotEnergyStatisticsDO>()
                .lt(IotEnergyStatisticsDO::getStatTime, statTime));
    }

}
