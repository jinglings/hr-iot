package cn.iocoder.yudao.module.iot.service.energy.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * IoT 能源数据采集 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyDataCollectServiceImpl implements IotEnergyDataCollectService {

    @Resource
    private IotEnergyMeterService meterService;

    @Resource
    private IotEnergyDataService energyDataService;

    @Override
    @Async
    public void processDevicePropertyReport(IotDeviceDO device, IotDeviceMessage message) {
        // 1. 判断是否为属性上报消息
        if (!IotDeviceMessageMethodEnum.PROPERTY_REPORT.getMethod().equals(message.getMethod())) {
            return;
        }

        // 2. 查询该设备关联的所有计量点
        List<IotEnergyMeterDO> meters = meterService.getMeterListByDeviceId(device.getId());
        if (CollUtil.isEmpty(meters)) {
            return;
        }

        // 3. 获取设备上报的属性数据
        Object params = message.getParams();
        if (!(params instanceof Map)) {
            log.warn("[processDevicePropertyReport][设备({})上报的属性数据格式不正确]", device.getId());
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) params;

        // 4. 遍历计量点，提取对应的能源数据
        List<IotEnergyRealtimeDataDO> realtimeDataList = new ArrayList<>();
        for (IotEnergyMeterDO meter : meters) {
            try {
                // 4.1 获取计量点对应的设备属性值
                String propertyKey = meter.getDeviceProperty();
                if (propertyKey == null || !properties.containsKey(propertyKey)) {
                    continue;
                }

                Object propertyValue = properties.get(propertyKey);
                if (propertyValue == null) {
                    continue;
                }

                // 4.2 构建能源实时数据
                IotEnergyRealtimeDataDO realtimeData = buildRealtimeData(meter, propertyValue, message);
                if (realtimeData != null) {
                    realtimeDataList.add(realtimeData);
                }
            } catch (Exception e) {
                log.error("[processDevicePropertyReport][处理计量点({})数据失败]", meter.getId(), e);
            }
        }

        // 5. 批量保存能源数据
        if (CollUtil.isNotEmpty(realtimeDataList)) {
            try {
                if (realtimeDataList.size() == 1) {
                    energyDataService.saveRealtimeData(realtimeDataList.get(0));
                } else {
                    energyDataService.saveRealtimeDataBatch(realtimeDataList);
                }
                log.debug("[processDevicePropertyReport][成功保存设备({})的{}条能源数据]",
                        device.getId(), realtimeDataList.size());
            } catch (Exception e) {
                log.error("[processDevicePropertyReport][保存能源数据失败，设备({})]", device.getId(), e);
            }
        }
    }

    /**
     * 构建能源实时数据
     *
     * @param meter 计量点
     * @param propertyValue 属性值
     * @param message 设备消息
     * @return 能源实时数据
     */
    private IotEnergyRealtimeDataDO buildRealtimeData(IotEnergyMeterDO meter, Object propertyValue,
                                                       IotDeviceMessage message) {
        try {
            // 1. 解析数值
            Double value = parseNumericValue(propertyValue);
            if (value == null) {
                log.warn("[buildRealtimeData][计量点({})的属性值({})无法转换为数值]", meter.getId(), propertyValue);
                return null;
            }

            // 2. 应用计量倍率
            BigDecimal ratio = meter.getRatio();
            if (ratio != null && ratio.compareTo(BigDecimal.ONE) != 0) {
                value = value * ratio.doubleValue();
            }

            // 3. 构建实时数据对象
            return IotEnergyRealtimeDataDO.builder()
                    .ts(LocalDateTimeUtil.toEpochMilli(message.getReportTime()))
                    .meterId(meter.getId())
                    .instantPower(value)  // 瞬时功率/流量
                    .cumulativeValue(value)  // 累计值（根据实际业务调整）
                    .dataQuality(1)  // 默认数据质量为正常
                    // 标签字段
                    .energyTypeId(meter.getEnergyTypeId())
                    .buildingId(meter.getBuildingId())
                    .areaId(meter.getAreaId())
                    .floorId(meter.getFloorId())
                    .roomId(meter.getRoomId())
                    .tenantId(message.getTenantId())
                    .build();
        } catch (Exception e) {
            log.error("[buildRealtimeData][构建能源实时数据失败，计量点({})]", meter.getId(), e);
            return null;
        }
    }

    /**
     * 解析数值类型的属性值
     *
     * @param value 属性值
     * @return 数值，解析失败返回 null
     */
    private Double parseNumericValue(Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }

            if (value instanceof String) {
                String strValue = (String) value;
                if (NumberUtil.isNumber(strValue)) {
                    return Double.parseDouble(strValue);
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("[parseNumericValue][解析数值失败: {}]", value, e);
            return null;
        }
    }

}
