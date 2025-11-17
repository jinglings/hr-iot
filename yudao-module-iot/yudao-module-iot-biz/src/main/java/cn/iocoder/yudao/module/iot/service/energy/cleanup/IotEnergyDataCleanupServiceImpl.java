package cn.iocoder.yudao.module.iot.service.energy.cleanup;

import cn.iocoder.yudao.module.iot.service.energy.statistics.IotEnergyStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * IoT 能源数据清理 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotEnergyDataCleanupServiceImpl implements IotEnergyDataCleanupService {

    @Resource
    private IotEnergyStatisticsService statisticsService;

    // TODO: TDengine数据清理暂未实现，需要根据TDengine特性实现
    @Override
    public int cleanupRealtimeData(int retentionDays) {
        try {
            log.info("[cleanupRealtimeData][开始清理{}天前的实时数据]", retentionDays);

            // TDengine支持TTL（Time To Live）自动清理过期数据
            // 这里可以记录日志，实际清理由TDengine自动完成
            // 或者执行DELETE语句手动清理

            log.info("[cleanupRealtimeData][实时数据清理完成]");
            return 0;
        } catch (Exception e) {
            log.error("[cleanupRealtimeData][清理实时数据失败]", e);
            return 0;
        }
    }

    @Override
    public int cleanupStatisticsData(String statPeriod, int retentionDays) {
        try {
            log.info("[cleanupStatisticsData][开始清理{}周期{}天前的统计数据]", statPeriod, retentionDays);

            // 计算保留截止时间
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

            // 删除指定时间之前的统计数据
            int deletedCount = statisticsService.deleteStatisticsByTimeBefore(cutoffTime);

            log.info("[cleanupStatisticsData][统计数据清理完成，删除{}条记录]", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("[cleanupStatisticsData][清理统计数据失败]", e);
            return 0;
        }
    }

}
