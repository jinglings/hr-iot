package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询设备属性历史数据 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevicePropertyHistoryTool implements IotAiTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IotDevicePropertyService devicePropertyService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_device_property_history";
    }

    @Override
    public String getDescription() {
        return "查询设备属性的历史数据列表。需要提供设备ID和属性标识符（如energy表示电表读数），以及时间范围。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("deviceId", Map.of("type", "integer", "description", "设备ID（必填）"));
        props.put("identifier", Map.of("type", "string", "description",
                "属性标识符（必填），如 energy 表示电表读数/能耗"));
        props.put("startTime", Map.of("type", "string", "description",
                "开始时间（可选），格式: yyyy-MM-dd HH:mm:ss，默认最近24小时"));
        props.put("endTime", Map.of("type", "string", "description",
                "结束时间（可选），格式: yyyy-MM-dd HH:mm:ss，默认当前时间"));
        schema.put("properties", props);
        schema.put("required", new String[]{"deviceId", "identifier"});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            Long deviceId = args.get("deviceId").asLong();
            String identifier = args.get("identifier").asText();

            // 解析时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(1);
            if (args.has("startTime") && !args.get("startTime").isNull()) {
                startTime = LocalDateTime.parse(args.get("startTime").asText(), FORMATTER);
            }
            if (args.has("endTime") && !args.get("endTime").isNull()) {
                endTime = LocalDateTime.parse(args.get("endTime").asText(), FORMATTER);
            }

            IotDevicePropertyHistoryListReqVO reqVO = new IotDevicePropertyHistoryListReqVO();
            reqVO.setDeviceId(deviceId);
            reqVO.setIdentifier(identifier);
            reqVO.setTimes(new LocalDateTime[]{startTime, endTime});

            List<IotDevicePropertyRespVO> historyList = devicePropertyService.getHistoryDevicePropertyList(reqVO);

            // 组装结果（限制最多返回200条避免过长）
            int limit = Math.min(historyList.size(), 200);
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                IotDevicePropertyRespVO item = historyList.get(i);
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("value", item.getValue());
                if (item.getUpdateTime() != null) {
                    map.put("updateTime", Instant.ofEpochMilli(item.getUpdateTime())
                            .atZone(ZoneId.systemDefault()).format(FORMATTER));
                }
                dataList.add(map);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("deviceId", deviceId);
            result.put("identifier", identifier);
            result.put("timeRange", startTime.format(FORMATTER) + " ~ " + endTime.format(FORMATTER));
            result.put("data", dataList);
            result.put("count", dataList.size());
            result.put("totalCount", historyList.size());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("[DevicePropertyHistoryTool] 查询历史数据失败", e);
            return "{\"error\": \"查询历史数据失败: " + e.getMessage() + "\"}";
        }
    }
}
