package cn.iocoder.yudao.module.iot.mq.consumer.edge;

import cn.iocoder.yudao.module.iot.core.edge.constant.EdgeMessageTopicConstants;
import cn.iocoder.yudao.module.iot.core.edge.dto.EdgeRuleExecuteResultDTO;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.dal.dataobject.edge.IotEdgeGatewayDO;
import cn.iocoder.yudao.module.iot.dal.mysql.edge.IotEdgeGatewayMapper;
import cn.iocoder.yudao.module.iot.service.edge.log.IotEdgeRuleLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 边缘规则执行结果消息订阅者
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class EdgeRuleExecuteResultSubscriber implements IotMessageSubscriber<EdgeRuleExecuteResultDTO> {

    @Resource
    private IotEdgeGatewayMapper edgeGatewayMapper;
    @Resource
    private IotEdgeRuleLogService ruleLogService;

    @Override
    public String getTopic() {
        return EdgeMessageTopicConstants.EDGE_RULE_EXECUTE_RESULT;
    }

    @Override
    public String getGroup() {
        return "edge-rule-execute-result-group";
    }

    @Override
    public void onMessage(EdgeRuleExecuteResultDTO message) {
        log.info("[onMessage][收到规则执行结果: gatewayKey={}, ruleId={}, status={}]",
                message.getGatewayKey(), message.getRuleId(), message.getExecuteStatus());

        try {
            // 1. 查找网关
            IotEdgeGatewayDO gateway = edgeGatewayMapper.selectByGatewayKey(message.getGatewayKey());
            if (gateway == null) {
                log.warn("[onMessage][网关不存在: gatewayKey={}]", message.getGatewayKey());
                return;
            }

            // 2. 创建规则执行日志
            ruleLogService.createRuleLog(
                    message.getRuleId(),
                    gateway.getId(),
                    message.getExecuteStatus(),
                    message.getExecuteResult()
            );

            log.info("[onMessage][规则执行结果处理成功: ruleId={}, gatewayId={}]",
                    message.getRuleId(), gateway.getId());

        } catch (Exception e) {
            log.error("[onMessage][规则执行结果处理失败: ruleId={}]", message.getRuleId(), e);
        }
    }

}
