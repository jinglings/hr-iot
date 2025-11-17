package cn.iocoder.yudao.module.iot.core.edge.producer;

import cn.iocoder.yudao.module.iot.core.edge.constant.EdgeMessageTopicConstants;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeModelDeployDTO;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeRuleDeployDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 边缘计算消息生产者实现
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class EdgeMessageProducerImpl implements EdgeMessageProducer {

    @Resource
    private IotMessageBus iotMessageBus;

    @Override
    public void sendRuleDeployMessage(EdgeRuleDeployDTO message) {
        log.info("[sendRuleDeployMessage][发送规则下发消息: gatewayKey={}, ruleId={}, action={}]",
                message.getGatewayKey(), message.getRuleId(), message.getAction());
        iotMessageBus.post(EdgeMessageTopicConstants.EDGE_RULE_DEPLOY, message);
    }

    @Override
    public void sendModelDeployMessage(EdgeModelDeployDTO message) {
        log.info("[sendModelDeployMessage][发送模型下发消息: gatewayKey={}, modelId={}, action={}]",
                message.getGatewayKey(), message.getModelId(), message.getAction());
        iotMessageBus.post(EdgeMessageTopicConstants.EDGE_MODEL_DEPLOY, message);
    }

}
