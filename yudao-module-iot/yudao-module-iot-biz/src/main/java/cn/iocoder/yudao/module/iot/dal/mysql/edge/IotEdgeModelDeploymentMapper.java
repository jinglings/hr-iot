package cn.iocoder.yudao.module.iot.dal.mysql.edge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment.IotEdgeModelDeploymentPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeModelDeploymentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 边缘模型部署记录 Mapper
 *
 * @author AI Assistant
 */
@Mapper
public interface IotEdgeModelDeploymentMapper extends BaseMapperX<IotEdgeModelDeploymentDO> {

    default PageResult<IotEdgeModelDeploymentDO> selectPage(IotEdgeModelDeploymentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotEdgeModelDeploymentDO>()
                .eqIfPresent(IotEdgeModelDeploymentDO::getModelId, reqVO.getModelId())
                .eqIfPresent(IotEdgeModelDeploymentDO::getGatewayId, reqVO.getGatewayId())
                .eqIfPresent(IotEdgeModelDeploymentDO::getDeployStatus, reqVO.getDeployStatus())
                .eqIfPresent(IotEdgeModelDeploymentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotEdgeModelDeploymentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotEdgeModelDeploymentDO::getId));
    }

    default IotEdgeModelDeploymentDO selectByModelIdAndGatewayId(Long modelId, Long gatewayId) {
        return selectOne(new LambdaQueryWrapperX<IotEdgeModelDeploymentDO>()
                .eq(IotEdgeModelDeploymentDO::getModelId, modelId)
                .eq(IotEdgeModelDeploymentDO::getGatewayId, gatewayId));
    }

    default List<IotEdgeModelDeploymentDO> selectListByGatewayId(Long gatewayId) {
        return selectList(IotEdgeModelDeploymentDO::getGatewayId, gatewayId);
    }

    default List<IotEdgeModelDeploymentDO> selectListByModelId(Long modelId) {
        return selectList(IotEdgeModelDeploymentDO::getModelId, modelId);
    }

    default Long selectCountByModelId(Long modelId) {
        return selectCount(IotEdgeModelDeploymentDO::getModelId, modelId);
    }

    default Long selectCountByGatewayIdAndStatus(Long gatewayId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<IotEdgeModelDeploymentDO>()
                .eq(IotEdgeModelDeploymentDO::getGatewayId, gatewayId)
                .eq(IotEdgeModelDeploymentDO::getStatus, status));
    }

}
