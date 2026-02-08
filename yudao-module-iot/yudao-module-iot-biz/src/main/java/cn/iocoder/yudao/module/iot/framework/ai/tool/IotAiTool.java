package cn.iocoder.yudao.module.iot.framework.ai.tool;

import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionRequest;

/**
 * IoT AI Tool 接口
 * 每个 Tool 对应 LLM Function Calling 中的一个 function
 */
public interface IotAiTool {

    /**
     * Tool 名称 (对应 function name)
     */
    String getName();

    /**
     * Tool 描述 (告诉 LLM 这个工具做什么)
     */
    String getDescription();

    /**
     * 参数的 JSON Schema
     */
    Object getParameterSchema();

    /**
     * 执行 Tool, 返回结果 JSON 字符串
     */
    String execute(String arguments);

    /**
     * 转换为 OpenAI Tool 定义
     */
    default AiChatCompletionRequest.Tool toToolDefinition() {
        return AiChatCompletionRequest.Tool.builder()
                .type("function")
                .function(AiChatCompletionRequest.Function.builder()
                        .name(getName())
                        .description(getDescription())
                        .parameters(getParameterSchema())
                        .build())
                .build();
    }
}
