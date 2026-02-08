package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.energy.data.IotEnergyDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 能耗统计 Tool (按建筑/能源类型)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnergyStatisticsTool implements IotAiTool {

    private final IotEnergyDataService energyDataService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_energy_statistics";
    }

    @Override
    public String getDescription() {
        return "查询能耗统计数据，可按建筑或能源类型统计。需要指定时间范围和统计维度。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("dimension", Map.of("type", "string",
                "enum", new String[]{"building", "energyType"},
                "description", "统计维度: building=按建筑, energyType=按能源类型"));
        props.put("dimensionId", Map.of("type", "integer", "description", "维度ID (建筑ID或能源类型ID)"));
        props.put("startTime", Map.of("type", "integer", "description", "开始时间戳(毫秒)"));
        props.put("endTime", Map.of("type", "integer", "description", "结束时间戳(毫秒)"));
        schema.put("properties", props);
        schema.put("required", new String[]{"dimension", "dimensionId", "startTime", "endTime"});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            String dimension = args.get("dimension").asText();
            Long dimensionId = args.get("dimensionId").asLong();
            Long startTime = args.get("startTime").asLong();
            Long endTime = args.get("endTime").asLong();

            Object stats;
            if ("building".equals(dimension)) {
                stats = energyDataService.getEnergyStatsByBuilding(dimensionId, startTime, endTime);
            } else {
                stats = energyDataService.getEnergyStatsByType(dimensionId, startTime, endTime);
            }
            return objectMapper.writeValueAsString(Map.of("statistics", stats));
        } catch (Exception e) {
            log.error("[EnergyStatisticsTool] 查询统计失败", e);
            return "{\"error\": \"查询能耗统计失败: " + e.getMessage() + "\"}";
        }
    }
}
