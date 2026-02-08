package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyBuildingDO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.energy.building.IotEnergyBuildingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询建筑列表 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListBuildingsTool implements IotAiTool {

    private final IotEnergyBuildingService buildingService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "list_buildings";
    }

    @Override
    public String getDescription() {
        return "查询建筑列表，获取建筑ID和名称。当用户提到某个建筑但没给ID时使用此工具。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of());
        schema.put("required", new String[]{});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            // 查询启用状态的建筑
            List<IotEnergyBuildingDO> buildingList = buildingService.getBuildingListByStatus(1);
            List<Map<String, Object>> result = buildingList.stream().map(b -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", b.getId());
                map.put("name", b.getBuildingName());
                return map;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("buildings", result, "total", result.size()));
        } catch (Exception e) {
            log.error("[ListBuildingsTool] 查询建筑列表失败", e);
            return "{\"error\": \"查询建筑列表失败: " + e.getMessage() + "\"}";
        }
    }
}
