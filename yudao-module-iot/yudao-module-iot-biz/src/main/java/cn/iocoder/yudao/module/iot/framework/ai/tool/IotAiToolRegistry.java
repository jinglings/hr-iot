package cn.iocoder.yudao.module.iot.framework.ai.tool;

import cn.iocoder.yudao.module.iot.framework.ai.client.dto.AiChatCompletionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Tool 注册表
 */
@Slf4j
public class IotAiToolRegistry implements InitializingBean {

    private final Map<String, IotAiTool> tools = new LinkedHashMap<>();

    @Autowired(required = false)
    private List<IotAiTool> toolList;

    @Override
    public void afterPropertiesSet() {
        if (toolList != null) {
            for (IotAiTool tool : toolList) {
                tools.put(tool.getName(), tool);
                log.info("[IotAiToolRegistry] 注册 Tool: {}", tool.getName());
            }
        }
        log.info("[IotAiToolRegistry] 共注册 {} 个 Tool", tools.size());
    }

    public IotAiTool getTool(String name) {
        return tools.get(name);
    }

    public List<AiChatCompletionRequest.Tool> getToolDefinitions() {
        return tools.values().stream()
                .map(IotAiTool::toToolDefinition)
                .collect(Collectors.toList());
    }

    public Collection<IotAiTool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }
}
