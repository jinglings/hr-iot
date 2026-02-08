package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.energy.data.IotEnergyDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询电表读数 Tool (最新 + 历史)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnergyMeterQueryTool implements IotAiTool {

    private final IotEnergyDataService energyDataService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_meter_data";
    }

    @Override
    public String getDescription() {
        return "查询计量点/电表的数据。可查询最新读数，也可查询指定时间范围的历史数据（支持聚合）。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("meterId", Map.of("type", "integer", "description", "计量点ID"));
        props.put("queryType", Map.of("type", "string",
                "enum", new String[]{"latest", "history"},
                "description", "查询类型: latest=最新读数, history=历史数据"));
        props.put("startTime", Map.of("type", "integer", "description", "开始时间戳(毫秒), 仅history模式"));
        props.put("endTime", Map.of("type", "integer", "description", "结束时间戳(毫秒), 仅history模式"));
        props.put("interval", Map.of("type", "string",
                "enum", new String[]{"1m", "1h", "1d"},
                "description", "聚合间隔, 仅history模式, 1m=每分钟,1h=每小时,1d=每天"));
        schema.put("properties", props);
        schema.put("required", new String[]{"meterId", "queryType"});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            Long meterId = args.get("meterId").asLong();
            String queryType = args.has("queryType") ? args.get("queryType").asText() : "latest";

            if ("latest".equals(queryType)) {
                var data = energyDataService.getLatestDataByMeterId(meterId);
                if (data == null) {
                    return "{\"message\": \"该计量点暂无数据\"}";
                }
                return objectMapper.writeValueAsString(Map.of("data", data));
            } else {
                Long startTime = args.has("startTime") && !args.get("startTime").isNull()
                        ? args.get("startTime").asLong() : null;
                Long endTime = args.has("endTime") && !args.get("endTime").isNull()
                        ? args.get("endTime").asLong() : null;
                String interval = args.has("interval") && !args.get("interval").isNull()
                        ? args.get("interval").asText() : null;

                if (startTime == null || endTime == null) {
                    endTime = System.currentTimeMillis();
                    startTime = endTime - 24 * 60 * 60 * 1000L;
                }

                if (interval != null) {
                    List<Map<String, Object>> aggData = energyDataService.getAggregateDataByTimeRange(
                            meterId, startTime, endTime, interval);
                    return objectMapper.writeValueAsString(Map.of("data", aggData, "count", aggData.size()));
                } else {
                    var dataList = energyDataService.getDataListByMeterIdAndTimeRange(meterId, startTime, endTime);
                    return objectMapper.writeValueAsString(Map.of("data", dataList, "count", dataList.size()));
                }
            }
        } catch (Exception e) {
            log.error("[EnergyMeterQueryTool] 查询失败", e);
            return "{\"error\": \"查询电表数据失败: " + e.getMessage() + "\"}";
        }
    }
}
