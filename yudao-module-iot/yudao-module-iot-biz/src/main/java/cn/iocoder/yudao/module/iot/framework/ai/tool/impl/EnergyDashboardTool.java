package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.energy.dashboard.IotEnergyDashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 能耗总览/仪表板 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnergyDashboardTool implements IotAiTool {

    private final IotEnergyDashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_energy_overview";
    }

    @Override
    public String getDescription() {
        return "获取能耗总览数据，包括总能耗、能源占比、在线计量点数等概览信息。";
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
            var overview = dashboardService.getEnergyOverview();
            return objectMapper.writeValueAsString(Map.of("overview", overview));
        } catch (Exception e) {
            log.error("[EnergyDashboardTool] 查询总览失败", e);
            return "{\"error\": \"查询能耗总览失败: " + e.getMessage() + "\"}";
        }
    }
}
