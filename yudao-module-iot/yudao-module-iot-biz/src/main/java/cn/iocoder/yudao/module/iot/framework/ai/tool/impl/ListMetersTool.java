package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询计量点列表 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListMetersTool implements IotAiTool {

    private final IotEnergyMeterService meterService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "list_meters";
    }

    @Override
    public String getDescription() {
        return "查询计量点/电表列表，可按建筑ID筛选。当用户提到电表但没给ID时使用。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("buildingId", Map.of("type", "integer", "description", "建筑ID（可选, 按建筑筛选）"));
        schema.put("properties", props);
        schema.put("required", new String[]{});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments != null ? arguments : "{}");
            List<IotEnergyMeterDO> meterList;

            if (args.has("buildingId") && !args.get("buildingId").isNull()) {
                Long buildingId = args.get("buildingId").asLong();
                meterList = meterService.getMeterListByBuildingId(buildingId);
            } else {
                // 查询所有启用的计量点
                meterList = meterService.getMeterListByStatus(1);
            }

            List<Map<String, Object>> result = meterList.stream().map(m -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", m.getId());
                map.put("meterName", m.getMeterName());
                map.put("meterCode", m.getMeterCode());
                map.put("buildingId", m.getBuildingId());
                map.put("meterLevel", m.getMeterLevel());
                map.put("energyTypeId", m.getEnergyTypeId());
                return map;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("meters", result, "total", result.size()));
        } catch (Exception e) {
            log.error("[ListMetersTool] 查询计量点列表失败", e);
            return "{\"error\": \"查询计量点列表失败: " + e.getMessage() + "\"}";
        }
    }
}
