package cn.iocoder.yudao.module.iot.service.energy.cleanup;

/**
 * IoT 能源数据清理 Service 接口
 *
 * @author 芋道源码
 */
public interface IotEnergyDataCleanupService {

    /**
     * 清理TDengine中的历史实时数据
     *
     * @param retentionDays 保留天数
     * @return 清理数量
     */
    int cleanupRealtimeData(int retentionDays);

    /**
     * 清理MySQL中的历史统计数据
     *
     * @param statPeriod 统计周期（minute/hour/day/month）
     * @param retentionDays 保留天数
     * @return 清理数量
     */
    int cleanupStatisticsData(String statPeriod, int retentionDays);

}
