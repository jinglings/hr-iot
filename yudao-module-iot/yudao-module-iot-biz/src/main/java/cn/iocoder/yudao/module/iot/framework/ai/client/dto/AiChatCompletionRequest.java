package cn.iocoder.yudao.module.iot.framework.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Chat Completion 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiChatCompletionRequest {

    private String model;

    private List<AiMessage> messages;

    private List<Tool> tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private Boolean stream;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {
        private String type;
        private Function function;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Function {
        private String name;
        private String description;
        private Object parameters;
    }
}
