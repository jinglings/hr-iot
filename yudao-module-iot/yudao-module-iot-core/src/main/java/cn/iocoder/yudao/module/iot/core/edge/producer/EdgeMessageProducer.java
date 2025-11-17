package cn.iocoder.yudao.module.iot.core.edge.producer;

import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeModelDeployDTO;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeRuleDeployDTO;

/**
 * 边缘计算消息生产者接口
 *
 * @author AI Assistant
 */
public interface EdgeMessageProducer {

    /**
     * 发送规则下发消息
     *
     * @param message 规则下发消息
     */
    void sendRuleDeployMessage(EdgeRuleDeployDTO message);

    /**
     * 发送模型下发消息
     *
     * @param message 模型下发消息
     */
    void sendModelDeployMessage(EdgeModelDeployDTO message);

}
