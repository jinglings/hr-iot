package cn.iocoder.yudao.module.iot.service.edge.gateway;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.gateway.IotEdgeGatewayUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeGatewayConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeRuleMapper;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeGatewayStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 边缘网关 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
public class IotEdgeGatewayServiceImpl implements IotEdgeGatewayService {

    @Resource
    private IotEdgeGatewayMapper edgeGatewayMapper;
    @Resource
    private IotEdgeRuleMapper edgeRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGateway(IotEdgeGatewayCreateReqVO createReqVO) {
        // 1. 校验序列号唯一性
        validateSerialNumberUnique(null, createReqVO.getSerialNumber());

        // 2. 生成网关标识和密钥
        String gatewayKey = generateGatewayKey();
        String gatewaySecret = generateGatewaySecret();

        // 3. 插入数据库
        IotEdgeGatewayDO gateway = IotEdgeGatewayConvert.INSTANCE.convert(createReqVO);
        gateway.setGatewayKey(gatewayKey);
        gateway.setGatewaySecret(gatewaySecret);
        gateway.setStatus(IotEdgeGatewayStatusEnum.INACTIVE.getStatus());
        edgeGatewayMapper.insert(gateway);

        return gateway.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGateway(IotEdgeGatewayUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateGatewayExists(updateReqVO.getId());

        // 2. 更新
        IotEdgeGatewayDO updateObj = IotEdgeGatewayConvert.INSTANCE.convert(updateReqVO);
        edgeGatewayMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGateway(Long id) {
        // 1. 校验存在
        validateGatewayExists(id);

        // 2. 校验是否有规则
        Long ruleCount = edgeRuleMapper.selectCountByGatewayId(id);
        if (ruleCount > 0) {
            throw exception(EDGE_GATEWAY_DELETE_FAIL_HAS_RULE);
        }

        // 3. 删除
        edgeGatewayMapper.deleteById(id);
    }

    @Override
    public IotEdgeGatewayDO getGateway(Long id) {
        return edgeGatewayMapper.selectById(id);
    }

    @Override
    public PageResult<IotEdgeGatewayDO> getGatewayPage(IotEdgeGatewayPageReqVO pageReqVO) {
        return edgeGatewayMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGatewayStatus(Long id, Integer status) {
        // 1. 校验存在
        validateGatewayExists(id);

        // 2. 更新状态
        IotEdgeGatewayDO updateObj = new IotEdgeGatewayDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        edgeGatewayMapper.updateById(updateObj);
    }

    @Override
    public IotEdgeGatewayDO getGatewayByKey(String gatewayKey) {
        return edgeGatewayMapper.selectByGatewayKey(gatewayKey);
    }

    // ==================== 私有方法 ====================

    private void validateGatewayExists(Long id) {
        if (edgeGatewayMapper.selectById(id) == null) {
            throw exception(EDGE_GATEWAY_NOT_EXISTS);
        }
    }

    private void validateSerialNumberUnique(Long id, String serialNumber) {
        IotEdgeGatewayDO gateway = edgeGatewayMapper.selectBySerialNumber(serialNumber);
        if (gateway == null) {
            return;
        }
        if (id == null || !gateway.getId().equals(id)) {
            throw exception(EDGE_GATEWAY_SERIAL_NUMBER_EXISTS);
        }
    }

    private String generateGatewayKey() {
        return "EDG-" + IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase();
    }

    private String generateGatewaySecret() {
        return IdUtil.fastSimpleUUID();
    }

}
