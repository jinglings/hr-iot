package cn.iocoder.yudao.module.iot.service.edge.engine;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 规则条件评估器
 *
 * 支持简单的条件判断表达式
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class RuleConditionEvaluator {

    /**
     * 评估规则条件是否满足
     *
     * @param conditionJson 条件JSON字符串
     * @param deviceData 设备数据
     * @return true=条件满足, false=条件不满足
     */
    public boolean evaluate(String conditionJson, Map<String, Object> deviceData) {
        try {
            if (conditionJson == null || conditionJson.isEmpty()) {
                return true; // 无条件则默认满足
            }

            // 解析条件JSON
            Map<String, Object> condition = JSONUtil.toBean(conditionJson, Map.class);

            String operator = (String) condition.get("operator");
            if (operator == null) {
                return evaluateSingleCondition(condition, deviceData);
            }

            // 支持AND/OR逻辑运算
            if ("AND".equalsIgnoreCase(operator)) {
                return evaluateAndCondition(condition, deviceData);
            } else if ("OR".equalsIgnoreCase(operator)) {
                return evaluateOrCondition(condition, deviceData);
            }

            return evaluateSingleCondition(condition, deviceData);

        } catch (Exception e) {
            log.error("[evaluate][条件评估失败: condition={}, error={}]", conditionJson, e.getMessage());
            return false;
        }
    }

    /**
     * 评估单个条件
     */
    private boolean evaluateSingleCondition(Map<String, Object> condition, Map<String, Object> deviceData) {
        String property = (String) condition.get("property");
        String comparator = (String) condition.get("comparator");
        Object expectedValue = condition.get("value");

        Object actualValue = deviceData.get(property);
        if (actualValue == null) {
            return false;
        }

        return compare(actualValue, comparator, expectedValue);
    }

    /**
     * 评估AND条件（所有子条件都满足）
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateAndCondition(Map<String, Object> condition, Map<String, Object> deviceData) {
        Object conditionsObj = condition.get("conditions");
        if (!(conditionsObj instanceof Iterable)) {
            return false;
        }

        for (Object subConditionObj : (Iterable<?>) conditionsObj) {
            if (!(subConditionObj instanceof Map)) {
                continue;
            }
            Map<String, Object> subCondition = (Map<String, Object>) subConditionObj;
            if (!evaluateSingleCondition(subCondition, deviceData)) {
                return false; // 有一个不满足则返回false
            }
        }
        return true;
    }

    /**
     * 评估OR条件（任一子条件满足）
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateOrCondition(Map<String, Object> condition, Map<String, Object> deviceData) {
        Object conditionsObj = condition.get("conditions");
        if (!(conditionsObj instanceof Iterable)) {
            return false;
        }

        for (Object subConditionObj : (Iterable<?>) conditionsObj) {
            if (!(subConditionObj instanceof Map)) {
                continue;
            }
            Map<String, Object> subCondition = (Map<String, Object>) subConditionObj;
            if (evaluateSingleCondition(subCondition, deviceData)) {
                return true; // 有一个满足则返回true
            }
        }
        return false;
    }

    /**
     * 比较两个值
     */
    private boolean compare(Object actual, String comparator, Object expected) {
        try {
            switch (comparator.toUpperCase()) {
                case "EQ": // 等于
                case "==":
                    return actual.equals(expected);

                case "NE": // 不等于
                case "!=":
                    return !actual.equals(expected);

                case "GT": // 大于
                case ">":
                    return compareNumeric(actual, expected) > 0;

                case "GTE": // 大于等于
                case ">=":
                    return compareNumeric(actual, expected) >= 0;

                case "LT": // 小于
                case "<":
                    return compareNumeric(actual, expected) < 0;

                case "LTE": // 小于等于
                case "<=":
                    return compareNumeric(actual, expected) <= 0;

                case "CONTAINS": // 包含
                    return actual.toString().contains(expected.toString());

                default:
                    log.warn("[compare][不支持的比较运算符: {}]", comparator);
                    return false;
            }
        } catch (Exception e) {
            log.error("[compare][比较失败: actual={}, comparator={}, expected={}]",
                    actual, comparator, expected);
            return false;
        }
    }

    /**
     * 数值比较
     */
    private int compareNumeric(Object actual, Object expected) {
        double actualValue = Double.parseDouble(actual.toString());
        double expectedValue = Double.parseDouble(expected.toString());
        return Double.compare(actualValue, expectedValue);
    }

}
