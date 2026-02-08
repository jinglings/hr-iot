package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询设备最新属性值 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevicePropertyQueryTool implements IotAiTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IotDeviceService deviceService;
    private final IotDevicePropertyService devicePropertyService;
    private final IotThingModelService thingModelService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_device_latest_property";
    }

    @Override
    public String getDescription() {
        return "查询指定设备的所有最新属性值，包括能耗(energy)等。需要提供设备ID。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("deviceId", Map.of("type", "integer", "description", "设备ID（必填）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"deviceId"});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            Long deviceId = args.get("deviceId").asLong();

            // 1. 获取设备信息
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                return "{\"error\": \"设备不存在, deviceId=" + deviceId + "\"}";
            }

            // 2. 获取设备最新属性
            Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(deviceId);

            // 3. 获取物模型定义
            List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                    device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());

            // 4. 组装结果
            List<Map<String, Object>> propertyList = new ArrayList<>();
            for (IotThingModelDO thingModel : thingModels) {
                ThingModelProperty tmProp = thingModel.getProperty();
                if (tmProp == null) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", thingModel.getName());
                item.put("identifier", thingModel.getIdentifier());
                item.put("dataType", tmProp.getDataType());

                IotDevicePropertyDO propDO = properties.get(thingModel.getIdentifier());
                if (propDO != null) {
                    item.put("value", propDO.getValue());
                    if (propDO.getUpdateTime() != null) {
                        item.put("updateTime", propDO.getUpdateTime()
                                .atZone(ZoneId.systemDefault()).format(FORMATTER));
                    }
                } else {
                    item.put("value", null);
                    item.put("updateTime", null);
                }
                propertyList.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("deviceId", device.getId());
            result.put("deviceName", device.getDeviceName());
            result.put("nickname", device.getNickname());
            result.put("properties", propertyList);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("[DevicePropertyQueryTool] 查询设备属性失败", e);
            return "{\"error\": \"查询设备属性失败: " + e.getMessage() + "\"}";
        }
    }
}
