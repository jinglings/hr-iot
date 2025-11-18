package cn.iocoder.yudao.module.iot.service.edge.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 边缘规则引擎
 *
 * 负责规则的条件评估和动作执行
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class EdgeRuleEngine {

    @Resource
    private RuleConditionEvaluator conditionEvaluator;
    @Resource
    private RuleActionExecutor actionExecutor;

    /**
     * 执行规则
     *
     * @param ruleConfig 规则配置JSON
     * @param deviceData 设备数据
     * @return 执行结果
     */
    public RuleExecuteResult executeRule(String ruleConfig, Map<String, Object> deviceData) {
        log.debug("[executeRule][规则执行开始: ruleConfig={}, deviceData={}]", ruleConfig, deviceData);

        try {
            // 1. 解析规则配置
            Map<String, Object> config = cn.hutool.json.JSONUtil.toBean(ruleConfig, Map.class);
            String conditionsJson = cn.hutool.json.JSONUtil.toJsonStr(config.get("conditions"));
            String actionsJson = cn.hutool.json.JSONUtil.toJsonStr(config.get("actions"));

            // 2. 评估条件
            boolean conditionMet = conditionEvaluator.evaluate(conditionsJson, deviceData);

            if (!conditionMet) {
                log.debug("[executeRule][条件不满足，跳过执行]");
                return RuleExecuteResult.builder()
                        .success(true)
                        .conditionMet(false)
                        .message("条件不满足")
                        .build();
            }

            // 3. 执行动作
            String actionResult = actionExecutor.executeActions(actionsJson, deviceData);

            log.info("[executeRule][规则执行成功: result={}]", actionResult);

            return RuleExecuteResult.builder()
                    .success(true)
                    .conditionMet(true)
                    .message(actionResult)
                    .build();

        } catch (Exception e) {
            log.error("[executeRule][规则执行失败]", e);
            return RuleExecuteResult.builder()
                    .success(false)
                    .conditionMet(false)
                    .message("执行失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 规则执行结果
     */
    @lombok.Data
    @lombok.Builder
    public static class RuleExecuteResult {
        /**
         * 是否执行成功
         */
        private boolean success;

        /**
         * 条件是否满足
         */
        private boolean conditionMet;

        /**
         * 执行结果消息
         */
        private String message;
    }

}
