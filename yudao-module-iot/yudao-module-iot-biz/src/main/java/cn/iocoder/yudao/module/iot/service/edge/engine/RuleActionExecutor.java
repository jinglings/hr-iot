package cn.iocoder.yudao.module.iot.service.edge.engine;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 规则动作执行器
 *
 * 执行规则触发后的动作
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class RuleActionExecutor {

    /**
     * 执行规则动作
     *
     * @param actionsJson 动作JSON字符串
     * @param deviceData 设备数据
     * @return 执行结果描述
     */
    public String executeActions(String actionsJson, Map<String, Object> deviceData) {
        try {
            if (actionsJson == null || actionsJson.isEmpty()) {
                return "无动作需要执行";
            }

            // 解析动作JSON
            List<Map<String, Object>> actions = JSONUtil.toBean(actionsJson, new TypeReference<List<Map<String, Object>>>() {}, false);

            StringBuilder result = new StringBuilder();
            for (Map<String, Object> action : actions) {
                String actionResult = executeAction(action, deviceData);
                result.append(actionResult).append("; ");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("[executeActions][动作执行失败: actions={}, error={}]",
                    actionsJson, e.getMessage());
            return "执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行单个动作
     */
    private String executeAction(Map<String, Object> action, Map<String, Object> deviceData) {
        String actionType = (String) action.get("type");

        switch (actionType.toUpperCase()) {
            case "ALERT":
                return executeAlert(action);

            case "DEVICE_CONTROL":
                return executeDeviceControl(action, deviceData);

            case "REPORT_TO_CLOUD":
                return executeReportToCloud(action, deviceData);

            case "LOG":
                return executeLog(action, deviceData);

            default:
                log.warn("[executeAction][不支持的动作类型: {}]", actionType);
                return "不支持的动作类型: " + actionType;
        }
    }

    /**
     * 执行告警动作
     */
    private String executeAlert(Map<String, Object> action) {
        String level = (String) action.get("level");
        String message = (String) action.get("message");

        log.warn("[ALERT][{}] {}", level, message);

        // TODO: 实际应该发送到告警系统
        return String.format("发送告警[%s]: %s", level, message);
    }

    /**
     * 执行设备控制动作
     */
    private String executeDeviceControl(Map<String, Object> action, Map<String, Object> deviceData) {
        String deviceId = (String) action.get("deviceId");
        String identifier = (String) action.get("identifier");
        Object value = action.get("value");

        log.info("[DEVICE_CONTROL][设备控制] deviceId={}, identifier={}, value={}",
                deviceId, identifier, value);

        // TODO: 实际应该发送控制指令到设备
        return String.format("控制设备[%s]属性[%s]=%s", deviceId, identifier, value);
    }

    /**
     * 执行数据上报云端动作
     */
    private String executeReportToCloud(Map<String, Object> action, Map<String, Object> deviceData) {
        Object dataPoints = action.get("dataPoints");

        log.info("[REPORT_TO_CLOUD][数据上报] dataPoints={}, data={}", dataPoints, deviceData);

        // TODO: 实际应该通过消息总线上报到云端
        return String.format("上报数据到云端: %s", dataPoints);
    }

    /**
     * 执行日志记录动作
     */
    private String executeLog(Map<String, Object> action, Map<String, Object> deviceData) {
        String message = (String) action.get("message");
        String level = (String) action.getOrDefault("level", "INFO");

        log.info("[LOG][{}] {}, data={}", level, message, deviceData);

        return String.format("记录日志[%s]: %s", level, message);
    }

}
