package cn.iocoder.yudao.module.iot.framework.ai.tool;

import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Tool 执行器
 */
@Slf4j
@RequiredArgsConstructor
public class IotAiToolExecutor {

    private final IotAiToolRegistry toolRegistry;

    /**
     * 执行多个 tool calls, 返回 tool role 消息列表
     */
    public List<AiMessage> executeToolCalls(List<AiMessage.ToolCall> toolCalls) {
        List<AiMessage> results = new ArrayList<>();
        for (AiMessage.ToolCall toolCall : toolCalls) {
            String toolName = toolCall.getFunction().getName();
            String arguments = toolCall.getFunction().getArguments();
            String toolCallId = toolCall.getId();

            log.info("[executeToolCalls] 执行 Tool: {}, 参数: {}", toolName, arguments);

            IotAiTool tool = toolRegistry.getTool(toolName);
            String result;
            if (tool == null) {
                result = "{\"error\": \"未找到工具: " + toolName + "\"}";
                log.warn("[executeToolCalls] 未找到 Tool: {}", toolName);
            } else {
                try {
                    result = tool.execute(arguments);
                    log.info("[executeToolCalls] Tool {} 执行成功, 结果长度: {}", toolName,
                            result != null ? result.length() : 0);
                } catch (Exception e) {
                    result = "{\"error\": \"工具执行失败: " + e.getMessage() + "\"}";
                    log.error("[executeToolCalls] Tool {} 执行失败", toolName, e);
                }
            }
            results.add(AiMessage.tool(toolCallId, toolName, result));
        }
        return results;
    }
}
