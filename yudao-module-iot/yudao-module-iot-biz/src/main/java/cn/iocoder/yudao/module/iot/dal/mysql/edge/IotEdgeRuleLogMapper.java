package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.log.IotEdgeRuleLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 边缘规则执行日志 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeRuleLogMapper extends BaseMapperX<IotEdgeRuleLogDO> {

    default PageResult<IotEdgeRuleLogDO> selectPage(IotEdgeRuleLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeRuleLogDO>()
                .eqIfPresent(IotEdgeRuleLogDO::getRuleId, reqVO.getRuleId())
                .eqIfPresent(IotEdgeRuleLogDO::getGatewayId, reqVO.getGatewayId())
                .eqIfPresent(IotEdgeRuleLogDO::getExecuteResult, reqVO.getExecuteResult())
                .betweenIfPresent(IotEdgeRuleLogDO::getExecuteTime, reqVO.getExecuteTime())
                .orderByDesc(IotEdgeRuleLogDO::getExecuteTime));
    }

    default List<IotEdgeRuleLogDO> selectListByRuleId(Long ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotEdgeRuleLogDO>()
                .eq(IotEdgeRuleLogDO::getRuleId, ruleId)
                .between(IotEdgeRuleLogDO::getExecuteTime, startTime, endTime)
                .orderByDesc(IotEdgeRuleLogDO::getExecuteTime));
    }

    default Long selectCountByRuleIdAndResult(Long ruleId, Integer executeResult) {
        return selectCount(new LambdaQueryWrapperX<IotEdgeRuleLogDO>()
                .eq(IotEdgeRuleLogDO::getRuleId, ruleId)
                .eq(IotEdgeRuleLogDO::getExecuteResult, executeResult));
    }

}
