package cn.iocoder.yudao.module.iot.framework.ai.client;

import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionRequest;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionResponse;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiMessage;
import cn.iocoder.yudao.module.iot.framework.ai.config.IotAiProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 基于 OkHttp 的 LLM 客户端实现，调用 OpenAI 兼容 API
 */
@Slf4j
public class IotAiClientImpl implements IotAiClient {

    private final IotAiProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IotAiClientImpl(IotAiProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public AiChatCompletionResponse chatCompletion(AiChatCompletionRequest request) {
        request.setStream(false);
        fillDefaults(request);
        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            Request httpRequest = new Request.Builder()
                    .url(properties.getBaseUrl() + "/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "unknown";
                    throw new RuntimeException("LLM API 调用失败: " + response.code() + " - " + errorBody);
                }
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, AiChatCompletionResponse.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("LLM API 调用异常", e);
        }
    }

    @Override
    public void chatCompletionStream(AiChatCompletionRequest request,
                                     Consumer<AiChatCompletionResponse> onDelta,
                                     Consumer<AiChatCompletionResponse> onComplete,
                                     Consumer<Throwable> onError) {
        request.setStream(true);
        fillDefaults(request);
        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            Request httpRequest = new Request.Builder()
                    .url(properties.getBaseUrl() + "/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            EventSource.Factory factory = EventSources.createFactory(httpClient);
            final List<AiMessage.ToolCall> accumulatedToolCalls = new ArrayList<>();
            final StringBuilder[] toolCallArgsBuilders = new StringBuilder[20];

            factory.newEventSource(httpRequest, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equals(data)) {
                        AiChatCompletionResponse finalResp = new AiChatCompletionResponse();
                        if (!accumulatedToolCalls.isEmpty()) {
                            for (int i = 0; i < accumulatedToolCalls.size(); i++) {
                                if (toolCallArgsBuilders[i] != null) {
                                    accumulatedToolCalls.get(i).getFunction()
                                            .setArguments(toolCallArgsBuilders[i].toString());
                                }
                            }
                            AiChatCompletionResponse.Choice choice = new AiChatCompletionResponse.Choice();
                            AiMessage msg = AiMessage.assistantWithToolCalls(accumulatedToolCalls);
                            choice.setMessage(msg);
                            choice.setFinishReason("tool_calls");
                            finalResp.setChoices(List.of(choice));
                        }
                        onComplete.accept(finalResp);
                        return;
                    }
                    try {
                        AiChatCompletionResponse chunk = objectMapper.readValue(data, AiChatCompletionResponse.class);
                        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                            AiChatCompletionResponse.Delta delta = chunk.getChoices().get(0).getDelta();
                            if (delta != null && delta.getToolCalls() != null) {
                                for (AiMessage.ToolCall tc : delta.getToolCalls()) {
                                    if (tc.getId() != null) {
                                        accumulatedToolCalls.add(AiMessage.ToolCall.builder()
                                                .id(tc.getId())
                                                .type(tc.getType())
                                                .function(AiMessage.FunctionCall.builder()
                                                        .name(tc.getFunction() != null ? tc.getFunction().getName() : null)
                                                        .arguments("")
                                                        .build())
                                                .build());
                                        int idx = accumulatedToolCalls.size() - 1;
                                        toolCallArgsBuilders[idx] = new StringBuilder();
                                        if (tc.getFunction() != null && tc.getFunction().getArguments() != null) {
                                            toolCallArgsBuilders[idx].append(tc.getFunction().getArguments());
                                        }
                                    } else if (tc.getFunction() != null && tc.getFunction().getArguments() != null) {
                                        int idx = accumulatedToolCalls.size() - 1;
                                        if (idx >= 0 && toolCallArgsBuilders[idx] != null) {
                                            toolCallArgsBuilders[idx].append(tc.getFunction().getArguments());
                                        }
                                    }
                                }
                            }
                        }
                        onDelta.accept(chunk);
                    } catch (Exception e) {
                        log.warn("解析 SSE 数据失败: {}", data, e);
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    String errorMsg = "LLM SSE 连接失败";
                    if (response != null && response.body() != null) {
                        try {
                            errorMsg = response.body().string();
                        } catch (IOException ignored) {
                        }
                    }
                    log.error(errorMsg, t);
                    onError.accept(t != null ? t : new RuntimeException(errorMsg));
                }
            });
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private void fillDefaults(AiChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getModel());
        }
        if (request.getMaxTokens() == null) {
            request.setMaxTokens(properties.getMaxTokens());
        }
        if (request.getTemperature() == null) {
            request.setTemperature(properties.getTemperature());
        }
    }
}
