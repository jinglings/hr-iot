package cn.iocoder.yudao.module.iot.service.energy.virtualMeter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyMeterDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.service.energy.data.IotEnergyDataService;
import cn.iocoder.yudao.module.iot.service.energy.meter.IotEnergyMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ENERGY_METER_NOT_EXISTS;

/**
 * IoT 能源虚拟表计算 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyVirtualMeterServiceImpl implements IotEnergyVirtualMeterService {

    @Resource
    private IotEnergyMeterService meterService;

    @Resource
    private IotEnergyDataService energyDataService;

    private static final Pattern METER_ID_PATTERN = Pattern.compile("M(\\d+)");

    @Override
    public Double calculateVirtualMeterValue(Long meterId) {
        // 1. 获取虚拟计量点信息
        IotEnergyMeterDO meter = meterService.getMeter(meterId);
        if (meter == null) {
            throw exception(ENERGY_METER_NOT_EXISTS);
        }

        // 2. 检查是否为虚拟表
        if (!Boolean.TRUE.equals(meter.getIsVirtual())) {
            log.warn("[calculateVirtualMeterValue][计量点({})不是虚拟表]", meterId);
            return null;
        }

        // 3. 获取计算公式
        String formula = meter.getCalcFormula();
        if (StrUtil.isBlank(formula)) {
            log.warn("[calculateVirtualMeterValue][虚拟表({})没有配置计算公式]", meterId);
            return null;
        }

        try {
            // 4. 解析公式中的计量点ID并获取最新值
            Map<String, Double> meterValues = new HashMap<>();
            Matcher matcher = METER_ID_PATTERN.matcher(formula);

            while (matcher.find()) {
                String meterVar = matcher.group(0);  // M001
                Long refMeterId = Long.parseLong(matcher.group(1));  // 001 -> 1

                // 获取被引用计量点的最新数据
                IotEnergyRealtimeDataDO refData = energyDataService.getLatestDataByMeterId(refMeterId);
                if (refData == null || refData.getCumulativeValue() == null) {
                    log.warn("[calculateVirtualMeterValue][计量点({})没有最新数据]", refMeterId);
                    return null;
                }

                meterValues.put(meterVar, refData.getCumulativeValue());
            }

            // 5. 替换公式中的变量为实际值
            String calculableFormula = formula;
            for (Map.Entry<String, Double> entry : meterValues.entrySet()) {
                calculableFormula = calculableFormula.replace(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 6. 使用JavaScript引擎计算表达式
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(calculableFormula);

            // 7. 转换结果为Double
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }

            log.warn("[calculateVirtualMeterValue][计算结果类型不是数值: {}]", result);
            return null;

        } catch (Exception e) {
            log.error("[calculateVirtualMeterValue][计算虚拟表({})失败，公式：{}]", meterId, formula, e);
            return null;
        }
    }

    @Override
    public void calculateAndSaveVirtualMeterData(Long meterId) {
        try {
            // 1. 计算虚拟表的值
            Double value = calculateVirtualMeterValue(meterId);
            if (value == null) {
                return;
            }

            // 2. 获取虚拟表信息
            IotEnergyMeterDO meter = meterService.getMeter(meterId);
            if (meter == null) {
                return;
            }

            // 3. 构建实时数据对象
            IotEnergyRealtimeDataDO realtimeData = IotEnergyRealtimeDataDO.builder()
                    .ts(System.currentTimeMillis())
                    .meterId(meterId)
                    .instantPower(value)
                    .cumulativeValue(value)
                    .dataQuality(1)
                    // 标签字段
                    .energyTypeId(meter.getEnergyTypeId())
                    .buildingId(meter.getBuildingId())
                    .areaId(meter.getAreaId())
                    .floorId(meter.getFloorId())
                    .roomId(meter.getRoomId())
                    .build();

            // 4. 保存实时数据
            energyDataService.saveRealtimeData(realtimeData);

            log.debug("[calculateAndSaveVirtualMeterData][成功计算并保存虚拟表({})数据，值：{}]", meterId, value);
        } catch (Exception e) {
            log.error("[calculateAndSaveVirtualMeterData][计算并保存虚拟表({})数据失败]", meterId, e);
        }
    }

}
