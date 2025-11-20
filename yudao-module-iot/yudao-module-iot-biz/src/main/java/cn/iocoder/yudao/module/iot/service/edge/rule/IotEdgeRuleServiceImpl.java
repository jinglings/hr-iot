package cn.iocoder.yudao.module.iot.service.edge.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.edge.vo.rule.IotEdgeRuleUpdateReqVO;
import cn.iocoder.yudao.module.iot.convert.edge.IotEdgeRuleConvert;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeRuleDeployDTO;
import cn.iocoder.yudao.module.iot.core.edge.producer.EdgeMessageProducer;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeRuleMapper;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeGatewayStatusEnum;
import cn.iocoder.yudao.module.iot.enums.edge.IotEdgeRuleDeployStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 边缘规则 Service 实现类
 *
 * @author AI Assistant
 */
@Service
@Validated
@Slf4j
public class IotEdgeRuleServiceImpl implements IotEdgeRuleService {

    @Resource
    private IotEdgeRuleMapper edgeRuleMapper;
    @Resource
    private IotEdgeGatewayMapper edgeGatewayMapper;
    @Resource
    private EdgeMessageProducer edgeMessageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(IotEdgeRuleCreateReqVO createReqVO) {
        // 1. 校验网关存在
        validateGatewayExists(createReqVO.getGatewayId());

        // 2. 插入数据库
        IotEdgeRuleDO rule = IotEdgeRuleConvert.INSTANCE.convert(createReqVO);
        rule.setStatus(1); // 默认启用
        rule.setDeployStatus(IotEdgeRuleDeployStatusEnum.NOT_DEPLOYED.getStatus());
        if (rule.getExecuteLocal() == null) {
            rule.setExecuteLocal(1); // 默认本地执行
        }
        edgeRuleMapper.insert(rule);

        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(IotEdgeRuleUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateRuleExists(updateReqVO.getId());

        // 2. 更新
        IotEdgeRuleDO updateObj = IotEdgeRuleConvert.INSTANCE.convert(updateReqVO);
        edgeRuleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        // 1. 校验存在
        validateRuleExists(id);

        // 2. 删除
        edgeRuleMapper.deleteById(id);
    }

    @Override
    public IotEdgeRuleDO getRule(Long id) {
        return edgeRuleMapper.selectById(id);
    }

    @Override
    public PageResult<IotEdgeRuleDO> getRulePage(IotEdgeRulePageReqVO pageReqVO) {
        return edgeRuleMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deployRule(Long id) {
        // 1. 校验规则存在
        IotEdgeRuleDO rule = validateRuleExists(id);

        // 2. 校验网关在线
        IotEdgeGatewayDO gateway = edgeGatewayMapper.selectById(rule.getGatewayId());
        if (gateway == null || !IotEdgeGatewayStatusEnum.ONLINE.getStatus().equals(gateway.getStatus())) {
            throw exception(EDGE_GATEWAY_OFFLINE);
        }

        // 3. 更新部署状态
        IotEdgeRuleDO updateObj = new IotEdgeRuleDO();
        updateObj.setId(id);
        updateObj.setDeployStatus(IotEdgeRuleDeployStatusEnum.DEPLOYED.getStatus());
        updateObj.setDeployTime(LocalDateTime.now());
        edgeRuleMapper.updateById(updateObj);

        // 4. 发送规则到边缘网关
        EdgeRuleDeployDTO deployDTO = EdgeRuleDeployDTO.builder()
                .action("deploy")
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .gatewayKey(gateway.getGatewayKey())
                .ruleType(Integer.valueOf(rule.getRuleType()))
                .ruleConfig(rule.getRuleConfig())
                .status(rule.getStatus())
                .build();
        edgeMessageProducer.sendRuleDeployMessage(deployDTO);

        log.info("[deployRule][规则部署消息已发送: ruleId={}, gatewayId={}]", id, rule.getGatewayId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void undeployRule(Long id) {
        // 1. 校验规则存在
        IotEdgeRuleDO rule = validateRuleExists(id);

        // 2. 获取网关信息
        IotEdgeGatewayDO gateway = edgeGatewayMapper.selectById(rule.getGatewayId());
        if (gateway == null) {
            throw exception(EDGE_GATEWAY_NOT_EXISTS);
        }

        // 3. 更新部署状态
        IotEdgeRuleDO updateObj = new IotEdgeRuleDO();
        updateObj.setId(id);
        updateObj.setDeployStatus(IotEdgeRuleDeployStatusEnum.NOT_DEPLOYED.getStatus());
        edgeRuleMapper.updateById(updateObj);

        // 4. 发送取消部署消息到边缘网关
        EdgeRuleDeployDTO deployDTO = EdgeRuleDeployDTO.builder()
                .action("delete")
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .gatewayKey(gateway.getGatewayKey())
                .build();
        edgeMessageProducer.sendRuleDeployMessage(deployDTO);

        log.info("[undeployRule][规则取消部署消息已发送: ruleId={}, gatewayId={}]", id, rule.getGatewayId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRuleStatus(Long id, Integer status) {
        // 1. 校验存在
        validateRuleExists(id);

        // 2. 更新状态
        IotEdgeRuleDO updateObj = new IotEdgeRuleDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        edgeRuleMapper.updateById(updateObj);
    }

    // ==================== 私有方法 ====================

    private IotEdgeRuleDO validateRuleExists(Long id) {
        IotEdgeRuleDO rule = edgeRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(EDGE_RULE_NOT_EXISTS);
        }
        return rule;
    }

    private void validateGatewayExists(Long gatewayId) {
        if (edgeGatewayMapper.selectById(gatewayId) == null) {
            throw exception(EDGE_GATEWAY_NOT_EXISTS);
        }
    }

}
