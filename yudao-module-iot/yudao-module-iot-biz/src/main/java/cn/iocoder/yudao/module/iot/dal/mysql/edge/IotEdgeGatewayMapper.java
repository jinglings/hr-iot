package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 边缘网关 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeGatewayMapper extends BaseMapperX<IotEdgeGatewayDO> {

    default PageResult<IotEdgeGatewayDO> selectPage(IotEdgeGatewayPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeGatewayDO>()
                .likeIfPresent(IotEdgeGatewayDO::getName, reqVO.getName())
                .eqIfPresent(IotEdgeGatewayDO::getSerialNumber, reqVO.getSerialNumber())
                .eqIfPresent(IotEdgeGatewayDO::getGatewayKey, reqVO.getGatewayKey())
                .eqIfPresent(IotEdgeGatewayDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEdgeGatewayDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotEdgeGatewayDO::getId));
    }

    default IotEdgeGatewayDO selectBySerialNumber(String serialNumber) {
        return selectOne(IotEdgeGatewayDO::getSerialNumber, serialNumber);
    }

    default IotEdgeGatewayDO selectByGatewayKey(String gatewayKey) {
        return selectOne(IotEdgeGatewayDO::getGatewayKey, gatewayKey);
    }

    default Long selectCountByStatus(Integer status) {
        return selectCount(IotEdgeGatewayDO::getStatus, status);
    }

}
