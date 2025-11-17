package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 边缘规则 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeRuleMapper extends BaseMapperX<IotEdgeRuleDO> {

    default PageResult<IotEdgeRuleDO> selectPage(IotEdgeRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeRuleDO>()
                .likeIfPresent(IotEdgeRuleDO::getName, reqVO.getName())
                .eqIfPresent(IotEdgeRuleDO::getGatewayId, reqVO.getGatewayId())
                .eqIfPresent(IotEdgeRuleDO::getRuleType, reqVO.getRuleType())
                .eqIfPresent(IotEdgeRuleDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotEdgeRuleDO::getDeployStatus, reqVO.getDeployStatus())
                .betweenIfPresent(IotEdgeRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotEdgeRuleDO::getId));
    }

    default List<IotEdgeRuleDO> selectListByGatewayId(Long gatewayId) {
        return selectList(IotEdgeRuleDO::getGatewayId, gatewayId);
    }

    default Long selectCountByGatewayId(Long gatewayId) {
        return selectCount(IotEdgeRuleDO::getGatewayId, gatewayId);
    }

    default List<IotEdgeRuleDO> selectListByGatewayIdAndStatus(Long gatewayId, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotEdgeRuleDO>()
                .eq(IotEdgeRuleDO::getGatewayId, gatewayId)
                .eq(IotEdgeRuleDO::getStatus, status));
    }

    default Long selectCountByStatusAndDeployStatus(Integer status, Integer deployStatus) {
        return selectCount(new LambdaQueryWrapperX<IotEdgeRuleDO>()
                .eq(IotEdgeRuleDO::getStatus, status)
                .eq(IotEdgeRuleDO::getDeployStatus, deployStatus));
    }

}
