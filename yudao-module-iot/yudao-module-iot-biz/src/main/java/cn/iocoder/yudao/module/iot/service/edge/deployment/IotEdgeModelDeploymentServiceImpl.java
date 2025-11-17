package cn.iocoder.yudao.module.iot.service.edge.deployment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.deployment.IotEdgeModelDeploymentPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeModelDeploymentDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeAiModelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeModelDeploymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 边缘模型部署 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
public class IotEdgeModelDeploymentServiceImpl implements IotEdgeModelDeploymentService {

    @Resource
    private IotEdgeModelDeploymentMapper modelDeploymentMapper;
    @Resource
    private IotEdgeAiModelMapper aiModelMapper;
    @Resource
    private IotEdgeGatewayMapper edgeGatewayMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deployModel(Long modelId, Long gatewayId) {
        // 1. 校验模型存在且启用
        IotEdgeAiModelDO model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw exception(EDGE_AI_MODEL_NOT_EXISTS);
        }
        if (model.getStatus() == null || model.getStatus() != 1) {
            throw exception(EDGE_AI_MODEL_NOT_ENABLED);
        }

        // 2. 校验网关存在且在线
        IotEdgeGatewayDO gateway = edgeGatewayMapper.selectById(gatewayId);
        if (gateway == null) {
            throw exception(EDGE_GATEWAY_NOT_EXISTS);
        }
        if (gateway.getStatus() == null || gateway.getStatus() == 0) {
            throw exception(EDGE_GATEWAY_NOT_ONLINE);
        }

        // 3. 校验是否已部署
        IotEdgeModelDeploymentDO existDeployment = modelDeploymentMapper.selectByModelIdAndGatewayId(modelId, gatewayId);
        if (existDeployment != null) {
            throw exception(EDGE_MODEL_DEPLOYMENT_ALREADY_EXISTS);
        }

        // 4. 创建部署记录
        IotEdgeModelDeploymentDO deployment = IotEdgeModelDeploymentDO.builder()
                .modelId(modelId)
                .gatewayId(gatewayId)
                .deployStatus(1) // 部署中
                .status(0) // 停止
                .build();
        modelDeploymentMapper.insert(deployment);

        // TODO: 发送部署指令到边缘网关 (MQTT消息)

        return deployment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void undeployModel(Long id) {
        // 1. 校验存在
        validateDeploymentExists(id);

        // TODO: 发送取消部署指令到边缘网关 (MQTT消息)

        // 2. 删除部署记录
        modelDeploymentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeploymentStatus(Long id, Integer status) {
        // 1. 校验存在
        validateDeploymentExists(id);

        // TODO: 发送启动/停止指令到边缘网关 (MQTT消息)

        // 2. 更新状态
        IotEdgeModelDeploymentDO updateObj = IotEdgeModelDeploymentDO.builder()
                .id(id)
                .status(status)
                .build();
        modelDeploymentMapper.updateById(updateObj);
    }

    @Override
    public IotEdgeModelDeploymentDO getDeployment(Long id) {
        return modelDeploymentMapper.selectById(id);
    }

    @Override
    public PageResult<IotEdgeModelDeploymentDO> getDeploymentPage(IotEdgeModelDeploymentPageReqVO pageReqVO) {
        return modelDeploymentMapper.selectPage(pageReqVO);
    }

    // ==================== 私有方法 ====================

    private void validateDeploymentExists(Long id) {
        if (modelDeploymentMapper.selectById(id) == null) {
            throw exception(EDGE_MODEL_DEPLOYMENT_NOT_EXISTS);
        }
    }

}
