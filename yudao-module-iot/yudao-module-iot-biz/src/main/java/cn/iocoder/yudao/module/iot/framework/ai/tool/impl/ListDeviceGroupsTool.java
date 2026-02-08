package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceGroupService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询设备分组列表 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListDeviceGroupsTool implements IotAiTool {

    private final IotDeviceGroupService deviceGroupService;
    private final IotDeviceService deviceService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "list_device_groups";
    }

    @Override
    public String getDescription() {
        return "查询设备分组列表，获取分组ID和名称。当用户提到某个分组(如公共区域、商铺等)但没给ID时使用此工具。";
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
            List<IotDeviceGroupDO> groupList = deviceGroupService.getDeviceGroupListByStatus(0); // 0=启用
            List<Map<String, Object>> result = groupList.stream().map(g -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", g.getId());
                map.put("name", g.getName());
                map.put("deviceCount", deviceService.getDeviceCountByGroupId(g.getId()));
                return map;
            }).collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of("groups", result, "total", result.size()));
        } catch (Exception e) {
            log.error("[ListDeviceGroupsTool] 查询设备分组失败", e);
            return "{\"error\": \"查询设备分组失败: " + e.getMessage() + "\"}";
        }
    }
}
