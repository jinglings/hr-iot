package cn.iocoder.yudao.module.iot.service.ai;

import cn.iocoder.yudao.module.iot.dal.dataobject.ai.IotAiMessageDO;
import cn.iocoder.yudao.module.iot.framework.ai.client.IotAiClient;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionRequest;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionResponse;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiMessage;
import cn.iocoder.yudao.module.iot.framework.ai.config.IotAiProperties;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiToolExecutor;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IotAiAssistantServiceImpl implements IotAiAssistantService {

    private final IotAiClient aiClient;
    private final IotAiProperties aiProperties;
    private final IotAiToolRegistry toolRegistry;
    private final IotAiToolExecutor toolExecutor;
    private final IotAiConversationService conversationService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT =
            "你是HR-IoT平台的智能能源管理助手。你的职责是帮助用户查询和分析IoT设备数据、能源消耗情况。\n\n"
            + "你可以做的事情：\n"
            + "1. 查询设备分组列表（list_device_groups）\n"
            + "2. 查询电表/设备列表及当前电表读数（list_devices_with_energy）\n"
            + "3. 查询某个设备的所有最新属性值（query_device_latest_property）\n"
            + "4. 查询设备属性的历史数据（query_device_property_history），属性标识符 energy 代表电表读数\n"
            + "5. 计算指定时间段的电费（query_electricity_cost），电价为 1.0616 元/度\n"
            + "6. 提供节能建议\n\n"
            + "重要规则：\n"
            + "- 当用户提到\"公共区域\"、\"商铺\"、\"某个分组\"时，先调用 list_device_groups 查询分组列表确认分组ID\n"
            + "- 当用户提到\"电表\"、\"设备\"时，先调用 list_devices_with_energy 查询设备列表\n"
            + "- 查询电费时，时间参数格式为 yyyy-MM-dd HH:mm:ss，如 2026-01-01 00:00:00\n"
            + "- 当用户说\"某月电费\"时，startTime 为该月1号 00:00:00，endTime 为下月1号 00:00:00\n"
            + "- 数据展示时注明单位（kWh、元等）\n"
            + "- 对于异常数据（如负数消耗），主动提示可能原因\n"
            + "- 回答使用中文，简洁明了";

    private static final int MAX_TOOL_ITERATIONS = 5;
    private static final int MAX_HISTORY_MESSAGES = 20;

    @Override
    @Async
    public void chat(Long userId, Long conversationId, String content, SseEmitter emitter) {
        try {
            // 1. 创建或获取对话
            if (conversationId == null) {
                String title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                conversationId = conversationService.createConversation(userId, title, aiProperties.getModel());
                sendSseEvent(emitter, Map.of("type", "conversation", "conversationId", conversationId));
            }

            // 2. 保存用户消息
            IotAiMessageDO userMsg = IotAiMessageDO.builder()
                    .conversationId(conversationId)
                    .role("user")
                    .content(content)
                    .tokens(0)
                    .build();
            conversationService.saveMessage(userMsg);

            // 3. 构建消息列表 (system + history + current)
            List<AiMessage> messages = buildMessages(conversationId);

            // 4. LLM 调用 + Tool Calling 循环
            StringBuilder fullResponse = new StringBuilder();
            int iteration = 0;

            while (iteration < MAX_TOOL_ITERATIONS) {
                iteration++;
                AiChatCompletionRequest request = AiChatCompletionRequest.builder()
                        .messages(new ArrayList<>(messages))
                        .tools(toolRegistry.getToolDefinitions())
                        .toolChoice("auto")
                        .build();

                final StringBuilder chunkContent = new StringBuilder();
                final List<AiMessage.ToolCall>[] pendingToolCalls = new List[]{null};
                final boolean[] hasError = {false};
                final CountDownLatch latch = new CountDownLatch(1);

                aiClient.chatCompletionStream(request,
                        // onDelta: 流式文本推送
                        (chunk) -> {
                            if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                                AiChatCompletionResponse.Delta delta = chunk.getChoices().get(0).getDelta();
                                if (delta != null && delta.getContent() != null) {
                                    chunkContent.append(delta.getContent());
                                    try {
                                        sendSseEvent(emitter, Map.of(
                                                "type", "content",
                                                "content", delta.getContent()
                                        ));
                                    } catch (Exception e) {
                                        log.warn("SSE 推送失败", e);
                                    }
                                }
                            }
                        },
                        // onComplete
                        (finalResp) -> {
                            if (finalResp != null && finalResp.getChoices() != null && !finalResp.getChoices().isEmpty()) {
                                AiChatCompletionResponse.Choice choice = finalResp.getChoices().get(0);
                                if (choice.getMessage() != null && choice.getMessage().getToolCalls() != null
                                        && !choice.getMessage().getToolCalls().isEmpty()) {
                                    pendingToolCalls[0] = choice.getMessage().getToolCalls();
                                }
                            }
                            latch.countDown();
                        },
                        // onError
                        (error) -> {
                            hasError[0] = true;
                            log.error("[chat] LLM 调用失败", error);
                            try {
                                sendSseEvent(emitter, Map.of("type", "error",
                                        "message", "AI 服务调用失败: " + error.getMessage()));
                            } catch (Exception e) {
                                log.warn("SSE error 推送失败", e);
                            }
                            latch.countDown();
                        }
                );

                // 等待流式完成
                boolean completed = latch.await(120, TimeUnit.SECONDS);
                if (!completed) {
                    log.warn("[chat] LLM 调用超时");
                    sendSseEvent(emitter, Map.of("type", "error", "message", "AI 响应超时"));
                    break;
                }

                if (hasError[0]) {
                    break;
                }

                // 如果有 Tool 调用，执行它们
                if (pendingToolCalls[0] != null && !pendingToolCalls[0].isEmpty()) {
                    List<String> toolNames = pendingToolCalls[0].stream()
                            .map(tc -> tc.getFunction().getName())
                            .collect(Collectors.toList());
                    sendSseEvent(emitter, Map.of("type", "tool_call", "status", "executing", "tools", toolNames));

                    // 添加 assistant 消息（包含 tool calls）
                    messages.add(AiMessage.assistantWithToolCalls(pendingToolCalls[0]));

                    // 执行 tools
                    List<AiMessage> toolResults = toolExecutor.executeToolCalls(pendingToolCalls[0]);
                    messages.addAll(toolResults);

                    // 继续循环 - LLM 将处理 tool 结果
                    continue;
                }

                // 纯文本响应 - 完成
                fullResponse.append(chunkContent);
                break;
            }

            // 5. 保存 assistant 消息
            if (fullResponse.length() > 0) {
                IotAiMessageDO assistantMsg = IotAiMessageDO.builder()
                        .conversationId(conversationId)
                        .role("assistant")
                        .content(fullResponse.toString())
                        .tokens(0)
                        .build();
                conversationService.saveMessage(assistantMsg);
                conversationService.incrementMessageCount(conversationId, 2);
            }

            // 6. 发送完成事件
            sendSseEvent(emitter, Map.of("type", "done", "conversationId", conversationId));
            emitter.complete();

        } catch (Exception e) {
            log.error("[chat] 处理失败", e);
            try {
                sendSseEvent(emitter, Map.of("type", "error", "message", "处理失败: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private List<AiMessage> buildMessages(Long conversationId) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(SYSTEM_PROMPT));

        // 加载历史消息 (只保留 user + assistant)
        List<IotAiMessageDO> history = conversationService.getMessageList(conversationId);
        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (int i = start; i < history.size(); i++) {
            IotAiMessageDO msg = history.get(i);
            if ("user".equals(msg.getRole())) {
                messages.add(AiMessage.user(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(AiMessage.assistant(msg.getContent()));
            }
        }
        return messages;
    }

    private void sendSseEvent(SseEmitter emitter, Object data) throws IOException {
        emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(data)));
    }

}
