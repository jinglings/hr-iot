package cn.iocoder.yudao.module.iot.framework.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI 兼容格式消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiMessage {

    private String role;

    private String content;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    private String name;

    public static AiMessage system(String content) {
        return AiMessage.builder().role("system").content(content).build();
    }

    public static AiMessage user(String content) {
        return AiMessage.builder().role("user").content(content).build();
    }

    public static AiMessage assistant(String content) {
        return AiMessage.builder().role("assistant").content(content).build();
    }

    public static AiMessage assistantWithToolCalls(List<ToolCall> toolCalls) {
        return AiMessage.builder().role("assistant").toolCalls(toolCalls).build();
    }

    public static AiMessage tool(String toolCallId, String name, String content) {
        return AiMessage.builder().role("tool").toolCallId(toolCallId).name(name).content(content).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolCall {
        private String id;
        private String type;
        private FunctionCall function;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
}
