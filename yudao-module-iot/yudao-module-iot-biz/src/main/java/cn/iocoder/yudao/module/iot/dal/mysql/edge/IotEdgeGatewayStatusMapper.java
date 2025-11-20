package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.status.IotEdgeGatewayStatusPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayStatusDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 边缘网关状态 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeGatewayStatusMapper extends BaseMapperX<IotEdgeGatewayStatusDO> {

    default PageResult<IotEdgeGatewayStatusDO> selectPage(IotEdgeGatewayStatusPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeGatewayStatusDO>()
                .eqIfPresent(IotEdgeGatewayStatusDO::getGatewayId, reqVO.getGatewayId())
                .betweenIfPresent(IotEdgeGatewayStatusDO::getRecordTime, reqVO.getRecordTime())
                .orderByDesc(IotEdgeGatewayStatusDO::getRecordTime));
    }

    default IotEdgeGatewayStatusDO selectLatestByGatewayId(Long gatewayId) {
        return selectOne(new LambdaQueryWrapperX<IotEdgeGatewayStatusDO>()
                .eq(IotEdgeGatewayStatusDO::getGatewayId, gatewayId)
                .orderByDesc(IotEdgeGatewayStatusDO::getRecordTime)
                .last("LIMIT 1"));
    }

    default List<IotEdgeGatewayStatusDO> selectListByGatewayIdAndTime(Long gatewayId,
                                                                       LocalDateTime startTime,
                                                                       LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotEdgeGatewayStatusDO>()
                .eq(IotEdgeGatewayStatusDO::getGatewayId, gatewayId)
                .between(IotEdgeGatewayStatusDO::getRecordTime, startTime, endTime)
                .orderByAsc(IotEdgeGatewayStatusDO::getRecordTime));
    }

    default IotEdgeGatewayStatusDO selectByGatewayId(Long gatewayId){
        return selectOne(new LambdaQueryWrapperX<IotEdgeGatewayStatusDO>()
                .eq(IotEdgeGatewayStatusDO::getGatewayId, gatewayId));
    }
}
