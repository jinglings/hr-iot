package cn.iocoder.yudao.module.iot.framework.ai.tool.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.framework.ai.tool.IotAiTool;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询电费/能耗费用 Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEnergyCostTool implements IotAiTool {

    private static final String ENERGY_PROPERTY_IDENTIFIER = "energy";
    private static final BigDecimal ELECTRICITY_UNIT_PRICE = new BigDecimal("1.0616");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IotDeviceService deviceService;
    private final IotDevicePropertyService devicePropertyService;
    private final IotEnergyMeterService energyMeterService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_electricity_cost";
    }

    @Override
    public String getDescription() {
        return "计算指定时间段内设备的电费。根据起止时间的电表读数差值乘以倍率和单价（1.0616元/度）计算。"
                + "可按分组ID或设备名称筛选。返回每个设备的起止读数、消耗量和电费。";
    }

    @Override
    public Object getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("startTime", Map.of("type", "string", "description",
                "开始时间（必填），格式: yyyy-MM-dd HH:mm:ss"));
        props.put("endTime", Map.of("type", "string", "description",
                "结束时间（必填），格式: yyyy-MM-dd HH:mm:ss"));
        props.put("groupId", Map.of("type", "integer", "description",
                "设备分组ID（可选，如1=公共区域-动力,2=商铺,3=公区备用电表）"));
        props.put("deviceName", Map.of("type", "string", "description",
                "设备名称关键字（可选，模糊搜索）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"startTime", "endTime"});
        return schema;
    }

    @Override
    public String execute(String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            LocalDateTime startTime = LocalDateTime.parse(args.get("startTime").asText(), FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(args.get("endTime").asText(), FORMATTER);

            // 构造设备查询条件
            IotDevicePageReqVO pageReqVO = new IotDevicePageReqVO();
            pageReqVO.setPageNo(1);
            pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE); // 不分页
            if (args.has("groupId") && !args.get("groupId").isNull()) {
                pageReqVO.setGroupId(args.get("groupId").asLong());
            }
            if (args.has("deviceName") && !args.get("deviceName").isNull()) {
                pageReqVO.setDeviceName(args.get("deviceName").asText());
            }

            PageResult<IotDeviceDO> devicePage = deviceService.getDevicePage(pageReqVO);
            if (devicePage.getList().isEmpty()) {
                return objectMapper.writeValueAsString(Map.of(
                        "message", "未找到符合条件的设备",
                        "devices", List.of(),
                        "totalConsumption", 0,
                        "totalCost", 0
                ));
            }

            // 计算每个设备的电费
            List<Map<String, Object>> deviceCosts = new ArrayList<>();
            BigDecimal totalConsumption = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;

            for (IotDeviceDO device : devicePage.getList()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deviceId", device.getId());
                item.put("deviceName", device.getDeviceName());
                item.put("nickname", device.getNickname());

                // 查询起止时间的能耗读数
                IotDevicePropertyRespVO startProp = devicePropertyService
                        .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, startTime);
                IotDevicePropertyRespVO endProp = devicePropertyService
                        .getPropertyValueBeforeTime(device.getId(), ENERGY_PROPERTY_IDENTIFIER, endTime);

                BigDecimal startEnergy = null;
                BigDecimal endEnergy = null;
                if (startProp != null && startProp.getValue() != null) {
                    try {
                        startEnergy = new BigDecimal(String.valueOf(startProp.getValue()));
                    } catch (NumberFormatException ignored) {}
                }
                if (endProp != null && endProp.getValue() != null) {
                    try {
                        endEnergy = new BigDecimal(String.valueOf(endProp.getValue()));
                    } catch (NumberFormatException ignored) {}
                }

                item.put("startEnergy", startEnergy);
                item.put("endEnergy", endEnergy);

                if (startEnergy != null && endEnergy != null) {
                    BigDecimal rawConsumption = endEnergy.subtract(startEnergy);

                    // 获取倍率
                    BigDecimal ratio = BigDecimal.ONE;
                    List<IotEnergyMeterDO> meters = energyMeterService.getMeterListByDeviceId(device.getId());
                    if (meters != null && !meters.isEmpty()) {
                        IotEnergyMeterDO meter = meters.get(0);
                        if (meter.getRatio() != null && meter.getRatio().compareTo(BigDecimal.ZERO) > 0) {
                            ratio = meter.getRatio();
                        }
                    }
                    // 也尝试从设备名称解析倍率
                    if (ratio.compareTo(BigDecimal.ONE) == 0) {
                        ratio = parseRatioFromDeviceName(device.getDeviceName());
                    }

                    BigDecimal consumption = rawConsumption.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal cost = consumption.multiply(ELECTRICITY_UNIT_PRICE).setScale(2, RoundingMode.HALF_UP);

                    item.put("ratio", ratio);
                    item.put("consumption", consumption);
                    item.put("consumptionUnit", "kWh");
                    item.put("unitPrice", ELECTRICITY_UNIT_PRICE);
                    item.put("cost", cost);
                    item.put("costUnit", "元");

                    totalConsumption = totalConsumption.add(consumption);
                    totalCost = totalCost.add(cost);
                } else {
                    item.put("consumption", null);
                    item.put("cost", null);
                    item.put("message", "缺少起止读数，无法计算");
                }

                deviceCosts.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("timeRange", startTime.format(FORMATTER) + " ~ " + endTime.format(FORMATTER));
            result.put("unitPrice", ELECTRICITY_UNIT_PRICE + " 元/kWh");
            result.put("devices", deviceCosts);
            result.put("deviceCount", deviceCosts.size());
            result.put("totalConsumption", totalConsumption.setScale(2, RoundingMode.HALF_UP) + " kWh");
            result.put("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP) + " 元");
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("[DeviceEnergyCostTool] 计算电费失败", e);
            return "{\"error\": \"计算电费失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 从设备名称解析倍率
     * 格式: B1_01B_倍率30_922101010083 -> ratio=30
     */
    private BigDecimal parseRatioFromDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return BigDecimal.ONE;
        }
        int ratioIdx = deviceName.indexOf("倍率");
        if (ratioIdx < 0) {
            return BigDecimal.ONE;
        }
        String afterRatio = deviceName.substring(ratioIdx + 2);
        int sepIdx = afterRatio.indexOf("_");
        String ratioStr = sepIdx >= 0 ? afterRatio.substring(0, sepIdx) : afterRatio;
        try {
            return new BigDecimal(ratioStr);
        } catch (NumberFormatException e) {
            return BigDecimal.ONE;
        }
    }
}
