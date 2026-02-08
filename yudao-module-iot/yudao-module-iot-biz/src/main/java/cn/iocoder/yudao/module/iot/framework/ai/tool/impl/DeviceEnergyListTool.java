package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询设备列表及当前能耗读数 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEnergyListTool implements IotAiTool {

    private static final String ENERGY_PROPERTY_IDENTIFIER = "energy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IotDeviceService deviceService;
    private final IotDevicePropertyService devicePropertyService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "list_devices_with_energy";
    }

    @Override
    public String getDescription() {
        return "查询电表/设备列表及其当前能耗(电表)读数。可按分组ID或设备名称筛选。返回设备ID、名称、备注名、当前电表读数。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("groupId", Map.of("type", "integer", "description", "设备分组ID（可选，按分组筛选，如1=公共区域-动力,2=商铺,3=公区备用电表）"));
        props.put("deviceName", Map.of("type", "string", "description", "设备名称关键字（可选，模糊搜索）"));
        schema.put("properties", props);
        schema.put("required", new String[]{});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments != null ? arguments : "{}");

            IotDevicePageReqVO pageReqVO = new IotDevicePageReqVO();
            pageReqVO.setPageNo(1);
            pageReqVO.setPageSize(50);
            if (args.has("groupId") && !args.get("groupId").isNull()) {
                pageReqVO.setGroupId(args.get("groupId").asLong());
            }
            if (args.has("deviceName") && !args.get("deviceName").isNull()) {
                pageReqVO.setDeviceName(args.get("deviceName").asText());
            }

            PageResult<IotDeviceDO> devicePage = deviceService.getDevicePage(pageReqVO);
            List<Map<String, Object>> resultList = new ArrayList<>();

            for (IotDeviceDO device : devicePage.getList()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deviceId", device.getId());
                item.put("deviceName", device.getDeviceName());
                item.put("nickname", device.getNickname());
                item.put("state", device.getState());

                // 获取当前 energy 属性值
                Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(device.getId());
                IotDevicePropertyDO energyProp = properties.get(ENERGY_PROPERTY_IDENTIFIER);
                if (energyProp != null && energyProp.getValue() != null) {
                    try {
                        item.put("currentEnergy", new BigDecimal(String.valueOf(energyProp.getValue())));
                        item.put("energyUnit", "kWh");
                        if (energyProp.getUpdateTime() != null) {
                            item.put("energyUpdateTime", energyProp.getUpdateTime()
                                    .atZone(ZoneId.systemDefault()).format(FORMATTER));
                        }
                    } catch (NumberFormatException e) {
                        item.put("currentEnergy", null);
                    }
                } else {
                    item.put("currentEnergy", null);
                }
                resultList.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("devices", resultList);
            result.put("total", devicePage.getTotal());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("[DeviceEnergyListTool] 查询设备列表失败", e);
            return "{\"error\": \"查询设备列表失败: " + e.getMessage() + "\"}";
        }
    }
}
