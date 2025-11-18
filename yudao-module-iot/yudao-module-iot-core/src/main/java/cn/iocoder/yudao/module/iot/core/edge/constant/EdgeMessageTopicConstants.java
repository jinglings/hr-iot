package cn.iocoder.yudao.module.iot.core.edge.constant;

/**
 * 边缘计算消息主题常量
 *
 * @author AI Assistant
 */
public interface EdgeMessageTopicConstants {

    // ==================== 上行消息（边缘网关 -> 云端） ====================

    /**
     * 网关心跳上报
     */
    String EDGE_GATEWAY_HEARTBEAT = "edge.gateway.heartbeat";

    /**
     * 规则执行结果上报
     */
    String EDGE_RULE_EXECUTE_RESULT = "edge.rule.execute.result";

    /**
     * AI推理结果上报
     */
    String EDGE_AI_INFERENCE_RESULT = "edge.ai.inference.result";

    // ==================== 下行消息（云端 -> 边缘网关） ====================

    /**
     * 规则下发
     */
    String EDGE_RULE_DEPLOY = "edge.rule.deploy";

    /**
     * AI模型下发
     */
    String EDGE_MODEL_DEPLOY = "edge.model.deploy";

    /**
     * 网关控制指令
     */
    String EDGE_GATEWAY_CONTROL = "edge.gateway.control";

}
