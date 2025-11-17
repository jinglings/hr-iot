package cn.iocoder.yudao.module.iot.service.energy.quality;

import cn.iocoder.yudao.module.iot.dal.dataobject.energy.IotEnergyRealtimeDataDO;
import cn.iocoder.yudao.module.iot.service.energy.data.IotEnergyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * IoT 能源数据质量监控 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyDataQualityServiceImpl implements IotEnergyDataQualityService {

    @Resource
    private IotEnergyDataService energyDataService;

    // 数据突变阈值（变化超过100%视为突变）
    private static final double MUTATION_THRESHOLD = 1.0;

    // 最大合理值（根据实际业务调整）
    private static final double MAX_REASONABLE_VALUE = 1000000.0;

    @Override
    public Integer detectDataQuality(IotEnergyRealtimeDataDO data) {
        try {
            // 1. 基本数据校验
            if (data == null || data.getMeterId() == null) {
                return 0;
            }

            Double currentValue = data.getCumulativeValue();
            if (currentValue == null) {
                return 0;
            }

            // 2. 检查数值是否为负数
            if (currentValue < 0) {
                log.warn("[detectDataQuality][计量点({})数据异常：负数值({})]", data.getMeterId(), currentValue);
                return 0;
            }

            // 3. 检查数值是否超过合理范围
            if (currentValue > MAX_REASONABLE_VALUE) {
                log.warn("[detectDataQuality][计量点({})数据异常：超过最大合理值({})]", data.getMeterId(), currentValue);
                return 0;
            }

            // 4. 检查数据是否突变
            if (isMutationValue(data.getMeterId(), currentValue)) {
                log.warn("[detectDataQuality][计量点({})数据突变：当前值({})]", data.getMeterId(), currentValue);
                return 0;
            }

            // 5. 其他质量检测
            if (isAbnormalValue(data.getMeterId(), currentValue)) {
                log.warn("[detectDataQuality][计量点({})数据异常：当前值({})]", data.getMeterId(), currentValue);
                return 0;
            }

            return 1;
        } catch (Exception e) {
            log.error("[detectDataQuality][数据质量检测失败]", e);
            return 0;
        }
    }

    @Override
    public boolean isAbnormalValue(Long meterId, Double currentValue) {
        try {
            // 获取最新数据
            IotEnergyRealtimeDataDO latestData = energyDataService.getLatestDataByMeterId(meterId);
            if (latestData == null || latestData.getCumulativeValue() == null) {
                return false;
            }

            Double lastValue = latestData.getCumulativeValue();

            // 检查累计值是否递减（能源累计值应该只增不减）
            if (currentValue < lastValue) {
                log.warn("[isAbnormalValue][计量点({})累计值递减：上次({})，当前({})]",
                        meterId, lastValue, currentValue);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[isAbnormalValue][检测数据异常失败]", e);
            return false;
        }
    }

    @Override
    public boolean isMutationValue(Long meterId, Double currentValue) {
        try {
            // 获取最新数据
            IotEnergyRealtimeDataDO latestData = energyDataService.getLatestDataByMeterId(meterId);
            if (latestData == null || latestData.getCumulativeValue() == null) {
                return false;
            }

            Double lastValue = latestData.getCumulativeValue();

            // 避免除零错误
            if (lastValue == 0) {
                return currentValue > 1000;  // 如果上次为0，当前值大于1000视为突变
            }

            // 计算变化率
            double changeRate = Math.abs((currentValue - lastValue) / lastValue);

            // 如果变化率超过阈值，视为突变
            if (changeRate > MUTATION_THRESHOLD) {
                log.warn("[isMutationValue][计量点({})数据突变：上次({})，当前({})，变化率({})]",
                        meterId, lastValue, currentValue, changeRate);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[isMutationValue][检测数据突变失败]", e);
            return false;
        }
    }

}
