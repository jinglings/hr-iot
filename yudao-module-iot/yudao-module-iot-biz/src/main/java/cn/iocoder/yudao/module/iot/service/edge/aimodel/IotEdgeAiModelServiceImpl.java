package cn.iocoder.yudao.module.iot.service.edge.aimodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.aimodel.IotEdgeAiModelUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeAiModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeAiModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeAiModelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeModelDeploymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 边缘AI模型 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
public class IotEdgeAiModelServiceImpl implements IotEdgeAiModelService {

    @Resource
    private IotEdgeAiModelMapper edgeAiModelMapper;
    @Resource
    private IotEdgeModelDeploymentMapper modelDeploymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createModel(IotEdgeAiModelCreateReqVO createReqVO) {
        // 1. 校验模型版本唯一性
        validateModelVersionUnique(null, createReqVO.getName(), createReqVO.getVersion());

        // 2. 插入数据库
        IotEdgeAiModelDO model = IotEdgeAiModelConvert.INSTANCE.convert(createReqVO);
        model.setStatus(1); // 默认启用
        edgeAiModelMapper.insert(model);

        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(IotEdgeAiModelUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateModelExists(updateReqVO.getId());

        // 2. 更新
        IotEdgeAiModelDO updateObj = IotEdgeAiModelConvert.INSTANCE.convert(updateReqVO);
        edgeAiModelMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        // 1. 校验存在
        validateModelExists(id);

        // 2. 校验是否有部署记录
        Long deploymentCount = modelDeploymentMapper.selectCountByModelId(id);
        if (deploymentCount > 0) {
            throw exception(EDGE_AI_MODEL_DELETE_FAIL_HAS_DEPLOYMENT);
        }

        // 3. 删除
        edgeAiModelMapper.deleteById(id);
    }

    @Override
    public IotEdgeAiModelDO getModel(Long id) {
        return edgeAiModelMapper.selectById(id);
    }

    @Override
    public PageResult<IotEdgeAiModelDO> getModelPage(IotEdgeAiModelPageReqVO pageReqVO) {
        return edgeAiModelMapper.selectPage(pageReqVO);
    }

    // ==================== 私有方法 ====================

    private void validateModelExists(Long id) {
        if (edgeAiModelMapper.selectById(id) == null) {
            throw exception(EDGE_AI_MODEL_NOT_EXISTS);
        }
    }

    private void validateModelVersionUnique(Long id, String name, String version) {
        IotEdgeAiModelDO model = edgeAiModelMapper.selectByNameAndVersion(name, version);
        if (model == null) {
            return;
        }
        if (id == null || !model.getId().equals(id)) {
            throw exception(EDGE_AI_MODEL_VERSION_EXISTS);
        }
    }

}
