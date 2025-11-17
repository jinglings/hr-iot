package cn.iocoder.yudao.module.iot.job.energy;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.service.energy.cleanup.IotEnergyDataCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 能源数据清理定时任务
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class EnergyDataCleanupJob {

    @Resource
    private IotEnergyDataCleanupService cleanupService;

    /**
     * 清理TDengine中的历史实时数据
     * 每天凌晨3点执行
     * 保留最近30天的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @TenantJob
    public void cleanupRealtimeData() {
        try {
            log.info("[cleanupRealtimeData][开始清理历史实时数据]");

            int retentionDays = 30;  // 保留30天
            int deletedCount = cleanupService.cleanupRealtimeData(retentionDays);

            log.info("[cleanupRealtimeData][历史实时数据清理完成，删除{}条记录]", deletedCount);
        } catch (Exception e) {
            log.error("[cleanupRealtimeData][清理历史实时数据失败]", e);
        }
    }

    /**
     * 清理分钟级统计数据
     * 每天凌晨3点10分执行
     * 保留最近7天的数据
     */
    @Scheduled(cron = "0 10 3 * * ?")
    @TenantJob
    public void cleanupMinuteStatistics() {
        try {
            log.info("[cleanupMinuteStatistics][开始清理分钟级统计数据]");

            int retentionDays = 7;  // 保留7天
            int deletedCount = cleanupService.cleanupStatisticsData("minute", retentionDays);

            log.info("[cleanupMinuteStatistics][分钟级统计数据清理完成，删除{}条记录]", deletedCount);
        } catch (Exception e) {
            log.error("[cleanupMinuteStatistics][清理分钟级统计数据失败]", e);
        }
    }

    /**
     * 清理小时级统计数据
     * 每天凌晨3点20分执行
     * 保留最近90天的数据
     */
    @Scheduled(cron = "0 20 3 * * ?")
    @TenantJob
    public void cleanupHourStatistics() {
        try {
            log.info("[cleanupHourStatistics][开始清理小时级统计数据]");

            int retentionDays = 90;  // 保留90天
            int deletedCount = cleanupService.cleanupStatisticsData("hour", retentionDays);

            log.info("[cleanupHourStatistics][小时级统计数据清理完成，删除{}条记录]", deletedCount);
        } catch (Exception e) {
            log.error("[cleanupHourStatistics][清理小时级统计数据失败]", e);
        }
    }

    /**
     * 清理日级统计数据
     * 每月1号凌晨3点30分执行
     * 保留最近2年的数据
     */
    @Scheduled(cron = "0 30 3 1 * ?")
    @TenantJob
    public void cleanupDayStatistics() {
        try {
            log.info("[cleanupDayStatistics][开始清理日级统计数据]");

            int retentionDays = 730;  // 保留2年
            int deletedCount = cleanupService.cleanupStatisticsData("day", retentionDays);

            log.info("[cleanupDayStatistics][日级统计数据清理完成，删除{}条记录]", deletedCount);
        } catch (Exception e) {
            log.error("[cleanupDayStatistics][清理日级统计数据失败]", e);
        }
    }

}
