package cn.iocoder.yudao.module.iot.framework.ai.client;

import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionRequest;
import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionResponse;

import java.util.function.Consumer;

/**
 * LLM API 客户端接口
 */
public interface IotAiClient {

    /**
     * 同步调用 Chat Completion
     */
    AiChatCompletionResponse chatCompletion(AiChatCompletionRequest request);

    /**
     * 流式调用 Chat Completion
     *
     * @param request    请求
     * @param onDelta    每个流式片段的回调
     * @param onComplete 完成回调
     * @param onError    错误回调
     */
    void chatCompletionStream(AiChatCompletionRequest request,
                              Consumer<AiChatCompletionResponse> onDelta,
                              Consumer<AiChatCompletionResponse> onComplete,
                              Consumer<Throwable> onError);
}
